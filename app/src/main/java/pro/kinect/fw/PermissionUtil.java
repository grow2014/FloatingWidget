package pro.kinect.fw;

/**
 * Created by http://kinect.pro on 19.10.16.
 * Developer Andrew.Gahov@gmail.com
 */

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

/**
 * Special class for get permissions to Android 6 and high.
 *
 */
public class PermissionUtil {
    static private AppCompatActivity sAppCompatActivity;


    /**
     * A method of initialization.
     *
     * @param activity where we will get permissions
     * @return an object for work
     */
    public static PermissionObject with(AppCompatActivity activity) {
        sAppCompatActivity = activity;
        return new PermissionObject();
    }

    public static class SinglePermission {

        private String mPermissionName;
        private boolean mRationalNeeded = false;
        private String mReason;

        public SinglePermission(String permissionName) {
            mPermissionName = permissionName;
        }

        public SinglePermission(String permissionName, String reason) {
            mPermissionName = permissionName;
            mReason = reason;
        }

        public boolean isRationalNeeded() {
            return mRationalNeeded;
        }

        public void setRationalNeeded(boolean rationalNeeded) {
            mRationalNeeded = rationalNeeded;
        }

        public String getReason() {
            return mReason == null ? "" : mReason;
        }

        public void setReason(String reason) {
            mReason = reason;
        }

        public String getPermissionName() {
            return mPermissionName;
        }

        public void setPermissionName(String permissionName) {
            mPermissionName = permissionName;
        }
    }

    public static class PermissionObject {

        public PermissionRequestObject request(String permissionName) {
            return new PermissionRequestObject(new String[]{permissionName});
        }

        public PermissionRequestObject request(String... permissionNames) {
            return new PermissionRequestObject(permissionNames);
        }
    }

    public interface OnPermission {
        void call();
    }

    public interface OnResult {
        void call(int requestCode, String permissions[], int[] grantResults);
    }

    abstract public class OnRational {
        protected abstract void call(String permissionName);
    }

    static public class PermissionRequestObject {

        private static final String TAG = PermissionObject.class.getSimpleName();

        private ArrayList<SinglePermission> permissions;
        private int requestCode;
        private OnPermission grantAllPermission;
        private OnPermission denyPermission;
        private OnResult onResult;
        private OnRational onRational;
        private String[] permissionNames;

        public PermissionRequestObject(String[] permissionNames) {
            this.permissionNames = permissionNames;
        }

        /**
         * Execute the permission request with the given Request Code
         *
         * @param reqCode a unique request code in your activity
         */
        public PermissionRequestObject ask(int reqCode) {
            requestCode = reqCode;
            int length = permissionNames.length;
            permissions = new ArrayList<>(length);
            for (String mPermissionName : permissionNames) {
                permissions.add(new SinglePermission(mPermissionName));
            }

            if (needToAsk()) {
                Log.i(TAG, "Asking for permission");
                ActivityCompat.requestPermissions(sAppCompatActivity, permissionNames, reqCode);
            } else {
                Log.i(TAG, "No need to ask for permission");
                if (grantAllPermission != null) grantAllPermission.call();
            }
            return this;
        }

        private boolean needToAsk() {
            ArrayList<SinglePermission> neededPermissions = new ArrayList<>(permissions);
            for (int i = 0; i < permissions.size(); i++) {
                SinglePermission perm = permissions.get(i);
                int checkRes = ContextCompat.checkSelfPermission(sAppCompatActivity, perm.getPermissionName());
                if (checkRes == PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.remove(perm);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(sAppCompatActivity, perm.getPermissionName())) {
                        perm.setRationalNeeded(true);
                    }
                }
            }
            permissions = neededPermissions;
            permissionNames = new String[permissions.size()];
            for (int i = 0; i < permissions.size(); i++) {
                permissionNames[i] = permissions.get(i).getPermissionName();
            }
            return permissions.size() != 0;
        }

        /**
         * Called for the first denied permission if there is need to show the rational
         */
        public PermissionRequestObject onRational(OnRational rationalFunc) {
            onRational = rationalFunc;
            return this;
        }

        /**
         * Called if all the permissions were granted
         */
        public PermissionRequestObject onAllGranted(OnPermission grantOnPermission) {
            this.grantAllPermission = grantOnPermission;
            return this;
        }

        /**
         * Called if there is at least one denied permission
         */
        public PermissionRequestObject onAnyDenied(OnPermission denyOnPermission) {
            this.denyPermission = denyOnPermission;
            return this;
        }

        /**
         * Called with the original operands from {@link AppCompatActivity#onRequestPermissionsResult(int, String[], int[])
         * onRequestPermissionsResult} for any result
         */
        public PermissionRequestObject onResult(OnResult resultFunc) {
            onResult = resultFunc;
            return this;
        }

        /**
         * This Method should be called from {@link AppCompatActivity#onRequestPermissionsResult(int, String[], int[])
         * onRequestPermissionsResult} with all the same incoming operands
         * <pre>
         * {@code
         *
         * public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
         *      if (mStoragePermissionRequest != null)
         *          mStoragePermissionRequest.onRequestPermissionsResult(requestCode, permissions,grantResults);
         * }
         * }
         * </pre>
         */
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            Log.i(TAG, String.format("ReqCode: %d, ResCode: %d, PermissionName: %s", requestCode, grantResults[0], permissions[0]));

            if (this.requestCode == requestCode) {
                if (onResult != null) {
                    Log.i(TAG, "Calling Results function");
                    onResult.call(requestCode, permissions, grantResults);
                    return;
                }

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if (this.permissions.get(i).isRationalNeeded()) {
                            if (onRational != null) {
                                Log.i(TAG, "Calling Rational function");
                                onRational.call(this.permissions.get(i).getPermissionName());
                            }
                        }
                        if (denyPermission != null) {
                            Log.i(TAG, "Calling Deny function");
                            denyPermission.call();
                        } else Log.e(TAG, "NUll DENY FUNCTIONS");

                        // terminate if there is at least one deny
                        return;
                    }
                }

                // there has not been any deny
                if (grantAllPermission != null) {
                    Log.i(TAG, "Calling Grant onDenied");
                    grantAllPermission.call();
                } else Log.e(TAG, "NUll GRANT FUNCTIONS");
            }
        }
    }
}

//for example
//do not delete this!

/*
public class SampleStartScreen extends AppCompatActivity {

    private static final int REQUEST_CODE = 12;

    private PermissionUtil.PermissionRequestObject permissionRequest;

    @Override
    protected void onResume() {
        super.onResume();

        permissionRequest = PermissionUtil.with(this)
                .request(
                        Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BROADCAST_STICKY,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.READ_PHONE_STATE)
                .onAnyDenied(new PermissionUtil.OnPermission() {
                    @Override
                    public void call() {
                        Intent intent = new Intent()
                                .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.fromParts("package", getApplicationContext().getPackageName(), null));
                        startActivity(intent);
                    }
                }).onAllGranted(new PermissionUtil.OnPermission() {
            @Override
            public void call() {

                try {
                    LvgStream.run(SampleStartScreen.this);
                } catch (LvgConfException ex) {
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    ErrorHandler.getInstance().setError(ex, ex.getMessage());
                }
            }
        }).ask(REQUEST_CODE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
 */