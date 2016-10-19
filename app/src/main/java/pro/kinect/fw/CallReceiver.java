package pro.kinect.fw;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

/**
 * Created by http://kinect.pro on 19.10.16.
 * Developer Andrew.Gahov@gmail.com
 */

public class CallReceiver extends AbstractCallReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        Log.d("Custom", "onIncomingCallReceived");
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Log.d("Custom", "onIncomingCallAnswered");
        ctx.startService(new Intent(ctx, FloatingSnitch.class));
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d("Custom", "onIncomingCallEnded");
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.d("Custom", "onOutgoingCallStarted");
        ctx.startService(new Intent(ctx, FloatingSnitch.class));
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d("Custom", "onOutgoingCallEnded");
        ctx.stopService(new Intent(ctx, FloatingSnitch.class));
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Log.d("Custom", "onMissedCall");
    }
}
