package com.hy.picker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.Logger;
import com.hy.picker.utils.MyFileProvider;
import com.picker2.model.Photo;
import com.picker2.utils.MediaStoreHelper;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created time : 2018/8/23 10:56.
 *
 * @author HY
 */
public class OpenCameraResultActivity extends BaseActivity {
    public static final int REQUEST_CAMERA = 0x357;
    public static final int REQUEST_EDIT = 0x753;
    private File mEditFile;
    private boolean video;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        video = getIntent().getBooleanExtra("video", false);
        requestCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PhotoPicker.destroy();
    }

    private void toEdit(Uri uri) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            boolean mkdirs = path.mkdirs();
            Logger.d("文件夹：" + path + "创建" + (mkdirs ? "成功" : "失败"));
        }

        String name = "IMG-EDIT-" + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + ".jpg";
        mEditFile = new File(path, name);

        startActivityForResult(new Intent(this, IMGEditActivity.class)
                .putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri)
                .putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, mEditFile.getAbsolutePath()), REQUEST_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                if (mTakePictureUri != null) {
                    String path = mTakePictureUri.getEncodedPath();// getPathFromUri(this, mTakePhotoUri);

                    if (path==null){
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
                        MediaScannerConnection.scanFile(this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(final String path, Uri uri) {
                                getPhoto(path,file);
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
            } else if (requestCode == REQUEST_EDIT) {

                Photo photo = data.getParcelableExtra("photo");
                PhotoPicker.sTakePhotoListener.onTake(photo);

                finish();
            } else {
                finish();
            }
        } else {
            finish();
        }

    }

    private void getPhoto(final String path,final File file) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString("path", path);
                bundle.putBoolean("video", video);
                bundle.putBoolean(PICKER_EXTRA_ADD, false);
                MediaStoreHelper.getPhoto(OpenCameraResultActivity.this, bundle, new MediaStoreHelper.PhotoSingleCallback() {
                    @Override
                    public void onResultCallback(@Nullable Photo photo) {
                        if (photo == null) {
                            Toast.makeText(OpenCameraResultActivity.this, video ? R.string.picker_video_failure : R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (video) {
                            PhotoPicker.sTakePhotoListener.onTake(photo);
                        } else {
                            if (PhotoPicker.isEdit) {
                                toEdit(Uri.fromFile(file));
                            } else {
                                PhotoPicker.sTakePhotoListener.onTake(photo);
                            }
                        }
                        MediaStoreHelper.destroyLoader(OpenCameraResultActivity.this, 0);
                    }
                });
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
            Logger.d("文件夹：" + path + "创建" + (mkdirs ? "成功" : "失败"));
        }


        String name = "IMG-" + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + (video ? ".mp4" : ".jpg");
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
