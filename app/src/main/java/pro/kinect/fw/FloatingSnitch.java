package pro.kinect.fw;

/**
 * Created by http://kinect.pro on 19.10.16.
 * Developer Andrew.Gahov@gmail.com
 */

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FloatingSnitch extends Service implements View.OnClickListener{

    // this is thw single most important variable..the window manager
    // this variable gives handle to the overlay above all android UI
    // basically intented for showing the alerts, dialogs by the system
    private WindowManager mWindowManager;

    // timestamp for triggered tap events correctly...
    private long mTouchTriggeredTime;

    // the layout to be shown over all the screens
    private LinearLayout floatingWidget;
    private ImageView ivTextNote;
    private ImageView ivMic;
    private ImageView ivVideoCam;
    private ImageView ivClose;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //get the windows manager service
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //get the view ready here
        floatingWidget = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.floating_snitch, null);

        ivTextNote = (ImageView) floatingWidget.findViewById(R.id.ivTextNote);
        ivTextNote.setOnClickListener(this);

        ivMic = (ImageView) floatingWidget.findViewById(R.id.ivMic);
        ivMic.setOnClickListener(this);

        ivVideoCam = (ImageView) floatingWidget.findViewById(R.id.ivVideoCam);
        ivVideoCam.setOnClickListener(this);

        ivClose = (ImageView) floatingWidget.findViewById(R.id.ivClose);
        ivClose.setOnClickListener(this);

        // here we are going to adjust the window manager layout params,
        // the most important being the last three, here i am using the a non focussable view
        // phone type which takes high priority over the windows manager children.
        // and the last being teh flag to tell the system to choose a alpha based color depth
        //for all the children of the location market float
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //initial setup...can be anything
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 500;

        //add the view to the location manager instance....magic happens here.
        mWindowManager.addView(floatingWidget, params);

        try {
            // I would suggest take this outside via a touch listner class and set the touch listener here
            // avoid inlining....this is just for example.

            floatingWidget.setOnTouchListener(new View.OnTouchListener() {
                private WindowManager.LayoutParams paramsF = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                // here just do what you want to do when user interacts with the widget
                // i am just changing position a detecting a tap.
                @Override public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            // Get current time in nano seconds.

                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            mTouchTriggeredTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_UP:
                            exposeLocationHover();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            mWindowManager.updateViewLayout(floatingWidget, paramsF);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            // TODO: handle exception
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //good practice, remove teh view from the window manager resdponsibly.
        // does haywire stuff when not removed and view is added somewhere else (AGAIN)
        // after manual checks ( for eg after seeing preferences, bundles etc).
        if (floatingWidget != null) mWindowManager.removeView(floatingWidget);
    }

    //function to expose the location htext hover here
    // pradeep...we might need to initiate a reverse geocode here (OSM of GP Services)
    private void exposeLocationHover(){
        if(floatingWidget != null && isMarkerTappped()){
//            TextView tvLocationHover = (TextView) mLocationMarker.findViewById(R.id.main_TvCurrentLoc);
//            tvLocationHover.setVisibility(tvLocationHover.getVisibility()==View.INVISIBLE?View.VISIBLE:View.INVISIBLE);
        }
    }

    // function to check if the marker has been tapped
    private boolean isMarkerTappped(){
        return (System.currentTimeMillis() - mTouchTriggeredTime < 300? true:false);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivTextNote :
                Toast.makeText(this, "TextNote", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ivMic :
                Toast.makeText(this, "Microphone", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ivVideoCam :
                Toast.makeText(this, "VideoCamera", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ivClose :
                stopService(new Intent(this, FloatingSnitch.class));
                break;
            default: break;
        }
    }



}