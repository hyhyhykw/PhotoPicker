package com.hy.picker;

import android.content.Context;
import android.content.Intent;

import com.hy.picker.utils.PermissionUtils;
import com.yanzhenjie.permission.Permission;

import java.util.ArrayList;

/**
 * Created time : 2018/8/20 8:17.
 *
 * @author HY
 */
public class PhotoPicker {

    static PhotoListener sPhotoListener;
    static TakePhotoListener sTakePhotoListener;

    public static void init(PhotoModule photoModule) {
        PhotoContext.setPhotoModule(photoModule);
    }

    private int max;

    public PhotoPicker max(int max) {
        this.max = max;
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
                        context.startActivity(new Intent(context, OpenCameraResultActivity.class));
                    }
                })
                .requestPermission(Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE);
    }
}
