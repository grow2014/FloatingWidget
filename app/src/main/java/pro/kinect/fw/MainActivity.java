package pro.kinect.fw;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    private PermissionUtil.PermissionRequestObject permissionRequest;
    private Button launchFloatingWidget;
    private Button stopFloatingWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.getString("LAUNCH").equals("YES")) {
            startService(new Intent(MainActivity.this, FloatingSnitch.class));
        }

        //provide buttons to launch/stop the service module
        stopFloatingWidget = (Button)findViewById(R.id.button2);
        launchFloatingWidget = (Button)findViewById(R.id.button1);

        launchFloatingWidget.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, FloatingSnitch.class));
            }
        });


        stopFloatingWidget.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, FloatingSnitch.class));
            }
        });

    }

    @Override
    protected void onResume() {
        Bundle bundle = getIntent().getExtras();

        if(bundle != null && bundle.getString("LAUNCH").equals("YES")) {
            startService(new Intent(MainActivity.this, FloatingSnitch.class));
        }
        super.onResume();

        //check permissons
        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            } else {
                permissionRequest = PermissionUtil.with(this)
                        .request(
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.PROCESS_OUTGOING_CALLS
                        )
                        .onAnyDenied(new PermissionUtil.OnPermission() {
                            @Override
                            public void call() {
                                Log.e("Custom", "no permissions");
                                Intent intent = new Intent()
                                        .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.fromParts("package", getApplicationContext().getPackageName(), null));
                                startActivity(intent);
                            }
                        }).onAllGranted(new PermissionUtil.OnPermission() {
                            @Override
                            public void call() {
                                Log.d("Custom", "We have all permissions");                            }
                        }).ask(REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
