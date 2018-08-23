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
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.Logger;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created time : 2018/8/23 10:56.
 *
 * @author HY
 */
public class OpenCameraResultActivity extends AppCompatActivity {
    public static final int REQUEST_CAMERA = 0x357;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCamera();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CAMERA) {
            if (mTakePictureUri != null) {
                String path = mTakePictureUri.getEncodedPath();// getPathFromUri(this, mTakePhotoUri);

                if (mTakePictureUri.toString().startsWith("content")) {
                    path = path.replaceAll("/external_storage_root", "");

                    path = Environment.getExternalStorageDirectory() + path;
                }

                if (new File(path).exists()) {
                    MediaScannerConnection.scanFile(this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Logger.d("path===" + path);
                            if (null != PhotoPicker.sTakePhotoListener) {
                                PictureSelectorActivity.PicItem item = new PictureSelectorActivity.PicItem();
                                item.uri = path;
                                item.selected = true;
                                PhotoPicker.sTakePhotoListener.onTake(item);
                                finish();
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                finish();
            }
        }

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
            path.mkdirs();
        }

        String name = "IMG-" + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + ".jpg";
        File file = new File(path, name);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resInfoList.size() <= 0) {
            Toast.makeText(this, getResources().getString(R.string.picker_voip_cpu_error), Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mTakePictureUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".file_provider", file);
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
