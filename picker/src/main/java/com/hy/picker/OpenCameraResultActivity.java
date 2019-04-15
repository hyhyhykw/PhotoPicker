package com.hy.picker;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.ImgScanListener;
import com.hy.picker.utils.MyFileProvider;
import com.hy.picker.utils.Permission;
import com.hy.picker.utils.SingleMediaScanner;
import com.picker2.PickerConstants;
import com.picker2.model.Photo;
import com.picker2.utils.MediaScannerUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created time : 2018/8/23 10:56.
 *
 * @author HY
 */
public class OpenCameraResultActivity extends Activity implements PickerConstants, EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {
    public static final int REQUEST_CAMERA = 0x357;
//    public static final int REQUEST_EDIT = 0x753;
    private boolean video;

    private SureReceiver mSureReceiver;
    private boolean isEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSureReceiver = new SureReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PICKER_ACTION_MEDIA_SURE);
        registerReceiver(mSureReceiver, intentFilter);
        video = getIntent().getBooleanExtra(EXTRA_PICK_VIDEO, false);
        isEdit = getIntent().getBooleanExtra(EXTRA_EDIT, false);

        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            requestCamera();
        } else {
            List<String> permissionNames = Permission.transformText(this, perms);
            String message = getString(R.string.picker_message_permission_rationale, TextUtils.join("\n", permissionNames));
            EasyPermissions.requestPermissions(
                    this,
                    message,
                    RC_CAMERA_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
        }

    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mSureReceiver);
        super.onDestroy();
    }

    private void toEdit(Uri uri) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            boolean mkdirs = path.mkdirs();
            if (BuildConfig.DEBUG) {
                Log.d("TAG", "文件夹：" + path + "创建" + (mkdirs ? "成功" : "失败"));
            }

        }

        String name = "IMG-EDIT-" + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + ".jpg";
        File editFile = new File(path, name);

        startActivity(new Intent(this, IMGEditActivity.class)
                .putExtra(EXTRA_IMAGE_URI, uri)
                .putExtra(EXTRA_IMAGE_SAVE_PATH, editFile.getAbsolutePath()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CAMERA_STORAGE) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestCamera();
            } else {
                if (isFirst) {
                    List<String> permissionNames = Permission.transformText(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    String message = getString(R.string.picker_message_permission_always_failed, TextUtils.join("\n", permissionNames));

                    new AppSettingsDialog.Builder(this)
                            .setRationale(message)
                            .setRequestCode(requestCode)
                            .build()
                            .show();
                    isFirst = false;
                } else {
                    finish();
                }

            }
            return;
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                if (mTakePictureUri != null) {
                    String path = mTakePictureUri.getEncodedPath();// getPathFromUri(this, mTakePhotoUri);

                    if (path == null) {
                        Toast.makeText(this, video ? R.string.picker_video_failure : R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    if (mTakePictureUri.toString().startsWith("content")) {
                        path = path.replaceAll("/external_storage_root", "");

                        path = Environment.getExternalStorageDirectory() + path;
                    }

                    final File file = new File(path);

                    if (file.exists()) {
                        new SingleMediaScanner(PhotoContext.getContext(), path, new ImgScanListener<OpenCameraResultActivity>(this) {
                            @Override
                            protected void onScanFinish(@NonNull OpenCameraResultActivity openCameraResultActivity, String path) {
                                openCameraResultActivity.getPhoto(path, file);
                            }
                        });
                    } else {
                        Toast.makeText(this, video ? R.string.picker_video_failure : R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, video ? R.string.picker_video_failure : R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        requestCamera();
    }

    private boolean isFirst = true;

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (isFirst) {
                List<String> permissionNames = Permission.transformText(this, perms.toArray(new String[]{}));
                String message = getString(R.string.picker_message_permission_always_failed, TextUtils.join("\n", permissionNames));

                new AppSettingsDialog.Builder(this)
                        .setRationale(message)
                        .setRequestCode(requestCode)
                        .build()
                        .show();
            } else {
                finish();
            }

        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        isFirst = false;
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        isFirst = false;
        finish();
    }

    private class SureReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            if (!PICKER_ACTION_MEDIA_SURE.equals(intent.getAction())) return;
            runOnUiThread(() -> {
                Photo photo = intent.getParcelableExtra(PICKER_EXTRA_PHOTO);
                OpenCameraResultActivity.this.
                        setResult(RESULT_OK, new Intent()
                                .putExtra(EXTRA_ITEM, photo));
                finish();
            });

        }
    }


    private void getPhoto(final String path, final File file) {
        new MediaScannerUtils.Builder(OpenCameraResultActivity.this)
                .path(path)
                .video(video)
                .build()
                .scanner((photo, updateIndex) -> {
                    if (photo == null) {
                        Toast.makeText(OpenCameraResultActivity.this, video ? R.string.picker_video_failure : R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (video) {
//                        if (null!=PhotoPicker.sTakePhotoListener)
//                        PhotoPicker.sTakePhotoListener.onTake(photo);
                        setResult(RESULT_OK, new Intent()
                                .putExtra(EXTRA_ITEM, photo));
                        finish();
                    } else {
                        if (isEdit) {
                            toEdit(Uri.fromFile(file));
                        } else {
//                            if (null!=PhotoPicker.sTakePhotoListener)
//                            PhotoPicker.sTakePhotoListener.onTake(photo);
                            setResult(RESULT_OK, new Intent()
                                    .putExtra(EXTRA_ITEM, photo));

                            finish();
                        }
                    }
                });

    }

    private Uri mTakePictureUri;

    protected void requestCamera() {
        if (!CommonUtils.existSDCard()) {
            Toast.makeText(this, R.string.picker_empty_sdcard, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            boolean mkdirs = path.mkdirs();
            if (BuildConfig.DEBUG) {
                Log.d("TAG", "文件夹：" + path + "创建" + (mkdirs ? "成功" : "失败"));
            }

        }


        String name = (video ? "VIDEO-" : "IMG-") + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + (video ? ".mp4" : ".jpg");
        File file = new File(path, name);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resInfoList.size() <= 0) {
            Toast.makeText(this, getResources().getString(R.string.picker_voip_cpu_error), Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mTakePictureUri = MyFileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".demo.file_provider", file);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                mTakePictureUri = Uri.fromFile(file);
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePictureUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

}
