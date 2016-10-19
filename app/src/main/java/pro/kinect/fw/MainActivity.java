package pro.kinect.fw;

import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

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
            }
        }
    }
}
