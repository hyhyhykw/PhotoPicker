package com.hy.picker;

import android.content.Intent;

import java.util.ArrayList;

/**
 * Created time : 2018/8/20 8:17.
 *
 * @author HY
 */
public class PhotoPicker {

    static PhotoListener sPhotoListener;

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

}
