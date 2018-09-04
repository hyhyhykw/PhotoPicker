package com.hy.picker;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.hy.picker.utils.Logger;
import com.hy.picker.utils.PermissionUtils;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Created time : 2018/8/20 8:17.
 *
 * @author HY
 */
public class PhotoPicker {

    static PhotoListener sPhotoListener;
    static TakePhotoListener sTakePhotoListener;
    static boolean isEdit;

    public static void destroy() {
        sTakePhotoListener = null;
        sPhotoListener = null;
        isEdit = false;
    }

    public PhotoPicker() {
        isEdit = false;
    }

    public static void init(PhotoModule photoModule) {
        PhotoContext.setPhotoModule(photoModule);
    }

    private int max;

    public PhotoPicker max(int max) {
        this.max = max;
        return this;
    }

    public PhotoPicker edit(boolean edit) {
        isEdit = edit;
        return this;
    }

    private ArrayList<PictureSelectorActivity.PicItem> mPicItems;

    public PhotoPicker select(ArrayList<PictureSelectorActivity.PicItem> picItems) {
        mPicItems = picItems;
        return this;
    }

    private boolean gif = true;

    public PhotoPicker gif(boolean gif) {
        this.gif = gif;
        return this;
    }

    public void start(PhotoListener photoListener) {
        sPhotoListener = photoListener;
        Intent intent = new Intent(PhotoContext.getContext(), PictureSelectorActivity.class);
        intent.putExtra("max", max);
        intent.putExtra("gif", gif);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (null != mPicItems) {
            intent.putParcelableArrayListExtra("items", mPicItems);
        }
        PhotoContext.getContext().startActivity(intent);
    }

    public void openCamera(final Context context, TakePhotoListener takePhotoListener) {
        sTakePhotoListener = takePhotoListener;
        new PermissionUtils(context)
                .setPermissionListener(new PermissionUtils.PermissionListener() {
                    @Override
                    public void onResult() {
                        context.startActivity(new Intent(context, OpenCameraResultActivity.class)
                                .putExtra("edit", isEdit)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                })
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

            File[] files = cache.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !pathname.isDirectory() && pathname.getAbsolutePath().startsWith("IMG-EDIT");
                }
            });
            for (File file : files) {
                delete(file);
            }
        } else {
            boolean delete = cache.delete();
            Logger.d("缓存文件：" + cache + "删除" + (delete ? "成功" : "失败"));
        }
    }
}
