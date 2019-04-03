package com.hy.picker;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.hy.picker.utils.PermissionUtils;
import com.picker2.PickerConstants;
import com.picker2.model.Photo;
import com.picker2.utils.MediaListHolder;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.Nullable;

/**
 * Created time : 2018/8/20 8:17.
 *
 * @author HY
 */
public class PhotoPicker implements PickerConstants {


    //    static PhotoListener sPhotoListener;
    //    static TakePhotoListener sTakePhotoListener;
    private boolean isEdit;


    public PhotoPicker() {
        isEdit = false;
//        sTakePhotoListener = null;
//        sPhotoListener = null;
        MediaListHolder.currentPhotos.clear();
        MediaListHolder.selectPhotos.clear();
        MediaListHolder.allDirectories.clear();
    }


    private boolean isVideo;

    public PhotoPicker video() {
        isVideo = true;
        return this;
    }

//    public static void destroy() {
//        isEdit = false;
////        sTakePhotoListener = null;
////        sPhotoListener = null;
//        MediaListHolder.currentPhotos.clear();
//        MediaListHolder.selectPhotos.clear();
//        MediaListHolder.allDirectories.clear();
//    }

    public static void init(PhotoModule photoModule) {
        PhotoContext.setPhotoModule(photoModule);
    }

    private int max = 1;

    public PhotoPicker max(int max) {
        this.max = max;
        return this;
    }

    public PhotoPicker edit(boolean edit) {
        isEdit = edit;
        return this;
    }

    private ArrayList<Photo> mPicItems;

    public PhotoPicker select(ArrayList<Photo> picItems) {
        mPicItems = picItems;
        return this;
    }

    private boolean gif = true;

    public PhotoPicker gif(boolean gif) {
        this.gif = gif;
        return this;
    }

    private boolean gifOnly = false;

    public PhotoPicker gifOnly(boolean gifOnly) {
        this.gifOnly = gifOnly;
        this.isShowCamera = false;
        return this;
    }

    private boolean preview = true;

    public PhotoPicker preview(boolean preview) {
        this.preview = preview;
        return this;
    }

    private boolean isShowCamera = true;

    public PhotoPicker showCamera(boolean camera) {
        isShowCamera = camera;
        return this;
    }

    public void start(Activity activity) {
        Intent intent = new Intent(activity, PictureSelectorActivity.class);
        intent.putExtra(EXTRA_MAX, max);
        intent.putExtra(EXTRA_SHOW_GIF, gif);
        intent.putExtra(EXTRA_ONLY_GIF, gifOnly);
        intent.putExtra(EXTRA_PICK_VIDEO, isVideo);
        intent.putExtra(EXTRA_SHOW_CAMERA, isShowCamera);
        intent.putExtra(EXTRA_PREVIEW, preview);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (null != mPicItems) {
            intent.putParcelableArrayListExtra(EXTRA_ITEMS, mPicItems);
        }
        activity.startActivityForResult(intent, isVideo ? PICKER_REQUEST_MULTI_VIDEO : PICKER_REQUEST_MULTI_PICK);
    }

    public static boolean isSingle(int requestCode) {
        return PICKER_REQUEST_TAKE_PHOTO == requestCode || requestCode == PICKER_REQUEST_TAKE_VIDEO;
    }

    public static boolean isVideo(int requestCode) {
        return requestCode == PICKER_REQUEST_MULTI_VIDEO || requestCode == PICKER_REQUEST_TAKE_VIDEO;
    }

    public void openCamera(final Activity context) {
//        sTakePhotoListener = takePhotoListener;
        new PermissionUtils(context)
                .setPermissionListener(() -> context.startActivityForResult(new Intent(context, OpenCameraResultActivity.class)
                        .putExtra(EXTRA_EDIT, isEdit)
                        .putExtra(EXTRA_PICK_VIDEO, isVideo)/*
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)*/, isVideo ? PICKER_REQUEST_TAKE_VIDEO : PICKER_REQUEST_TAKE_PHOTO))
                .requestPermission(PERMISSION_REQUEST_EXTERNAL_CAMERA, Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE);
    }

    @Nullable
    public static Photo obtainTakeResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK || (requestCode != PICKER_REQUEST_TAKE_PHOTO && requestCode != PICKER_REQUEST_TAKE_VIDEO) || intent == null) {
            return null;
        }
        return intent.getParcelableExtra(EXTRA_ITEM);
    }

    public static ArrayList<Photo> obtainMultiResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK || (requestCode != PICKER_REQUEST_MULTI_PICK && requestCode != PICKER_REQUEST_MULTI_VIDEO) || intent == null) {
            return new ArrayList<>();
        }
        ArrayList<Photo> extra = intent.getParcelableArrayListExtra(EXTRA_ITEMS);

        if (null == extra) {
            return new ArrayList<>();
        }
        return extra;
    }


    /**
     * 删除编辑缓存
     */
    public static void deleteEditCache() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            return;
        }
        delete(path);
    }

    private static void delete(File cache) {
        if (cache.isDirectory()) {

            File[] files = cache.listFiles(pathname -> !pathname.isDirectory() && pathname.getAbsolutePath().startsWith("IMG-EDIT"));
            for (File file : files) {
                delete(file);
            }
        } else {
            boolean delete = cache.delete();
            if (BuildConfig.DEBUG){
                Log.d("TAG","缓存文件：" + cache + "删除" + (delete ? "成功" : "失败"));
            }
        }
    }


    public static void preview(int index, ArrayList<Photo> items) {
        Intent intent = new Intent(PhotoContext.getContext(), SelectedPicturePreviewActivity.class)
                .putExtra(EXTRA_INDEX, index)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(EXTRA_ITEMS, items);
        PhotoContext.getContext().startActivity(intent);
    }
}
