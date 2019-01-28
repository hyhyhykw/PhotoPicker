package com.hy.picker;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.hy.picker.utils.Logger;
import com.hy.picker.utils.PermissionUtils;
import com.picker2.PickerConstants;
import com.picker2.model.Photo;
import com.picker2.utils.MediaListHolder;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.util.ArrayList;

/**
 * Created time : 2018/8/20 8:17.
 *
 * @author HY
 */
public class PhotoPicker implements PickerConstants {


    static PhotoListener sPhotoListener;
    static TakePhotoListener sTakePhotoListener;
    static boolean isEdit;

    public static void destroy() {
        sTakePhotoListener = null;
        sPhotoListener = null;
        isEdit = false;

        MediaListHolder.currentPhotos.clear();
        MediaListHolder.selectPhotos.clear();
        MediaListHolder.allDirectories.clear();
    }

    public PhotoPicker() {
        isEdit = false;
    }

    private boolean isVideo;

    public PhotoPicker video() {
        isVideo = true;
        return this;
    }

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

    public void start(PhotoListener photoListener) {
        sPhotoListener = photoListener;
        Intent intent = new Intent(PhotoContext.getContext(), PictureSelectorActivity.class);
        intent.putExtra("max", max);
        intent.putExtra("gif", gif);
        intent.putExtra("gifOnly", gifOnly);
        intent.putExtra("video", isVideo);
        intent.putExtra("showCamera", isShowCamera);
        intent.putExtra("preview", preview);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (null != mPicItems) {
            intent.putParcelableArrayListExtra("items", mPicItems);
        }
        PhotoContext.getContext().startActivity(intent);
    }

    public void openCamera(final Context context, TakePhotoListener takePhotoListener) {
        sTakePhotoListener = takePhotoListener;
        new PermissionUtils(context)
                .setPermissionListener(() -> context.startActivity(new Intent(context, OpenCameraResultActivity.class)
                        .putExtra("edit", isEdit)
                        .putExtra("video", isVideo)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                .requestPermission(Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * 删除编辑缓存
     */
    public static void deleteEditCache() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            boolean mkdirs = path.mkdirs();
            Logger.d("文件夹：" + path + "创建" + (mkdirs ? "成功" : "失败"));
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
            Logger.d("缓存文件：" + cache + "删除" + (delete ? "成功" : "失败"));
        }
    }


    public static void preview(int index, ArrayList<Photo> items) {
        Intent intent = new Intent(PhotoContext.getContext(), SelectedPicturePreviewActivity.class)
                .putExtra("index", index)
                .putExtra("items", items);
        PhotoContext.getContext().startActivity(intent);
    }
}
