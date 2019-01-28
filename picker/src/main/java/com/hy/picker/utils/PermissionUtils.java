package com.hy.picker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.widget.Toast;

import com.hy.picker.R;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;

import java.lang.reflect.Method;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.AppOpsManagerCompat;

/**
 * Created time : 2018/4/19 11:43.
 *
 * @author HY
 */
public class PermissionUtils {
    private final Context mActivity;
    private PermissionListener mPermissionListener;

    private Rationale<List<String>> mRationale;
    private PermissionSetting mSetting;

    public PermissionUtils setPermissionListener(PermissionListener permissionListener) {
        mPermissionListener = permissionListener;
        return this;
    }

    public PermissionUtils(Context activity) {
        mActivity = activity;
        mRationale = new DefaultRationale();
        mSetting = new PermissionSetting(activity);
    }


    public void requestPermission(String... permissions) {
        AndPermission.with(mActivity)
                .runtime()
                .permission(permissions)
                .rationale(mRationale)
                .onGranted(permissions12 -> {
                    Logger.d("permission request success");
                    if (null != mPermissionListener) mPermissionListener.onResult();
                })
                .onDenied(permissions1 -> {
                    Toast.makeText(mActivity, R.string.picker_failure, Toast.LENGTH_SHORT).show();
                    String[] strings = permissions1.toArray(new String[]{});

                    if (!checkPermissions(mActivity, strings)) {
                        mSetting.showSetting(permissions1);
                    }
                })
                .start();
    }

    public static boolean checkPermissions(Context context, @NonNull String[] permissions) {
        if (permissions.length != 0) {
            for (String permission : permissions) {
                if ((isFlyme() || Build.VERSION.SDK_INT < 23) && permission.equals("android.permission.RECORD_AUDIO")) {
                    if (!hasRecordPermission()) {
                        return false;
                    }
                } else if (!hasPermission(context, permission)) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    private static boolean hasPermission(Context context, String permission) {
        String opStr = AppOpsManagerCompat.permissionToOp(permission);
        return opStr == null || context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean hasRecordPermission() {
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);

        try {
            audioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (audioRecord.getRecordingState() == 3) {
            audioRecord.stop();
            return true;
        } else {
            return false;
        }
    }

    private static boolean isFlyme() {
        String osString;

        try {
            @SuppressLint("PrivateApi")
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            osString = (String) get.invoke(clz, "ro.build.display.id", "");
        } catch (Exception var3) {
            return false;
        }

        return osString.toLowerCase().contains("flyme");
    }


    public interface PermissionListener {
        void onResult();
    }
}
