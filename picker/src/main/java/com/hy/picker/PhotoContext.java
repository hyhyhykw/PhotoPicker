package com.hy.picker;

import android.content.Context;
import android.util.DisplayMetrics;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.fresco.FrescoImageLoader;

/**
 * Created time : 2018/8/13 16:37.
 *
 * @author HY
 */
public class PhotoContext {

    private static PhotoModule sPhotoModule;
    private static int screenWidth;
    private static int screenHeight;

    static void setPhotoModule(PhotoModule photoModule) {
        sPhotoModule = photoModule;
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        BigImageViewer.initialize(FrescoImageLoader.with(photoModule.getContext()));
//        ViewTarget.setTagId(R.id.picker_tag_glide);
    }

    public static int getScreenWidth() {
        return screenWidth;
    }


    public static int getScreenHeight() {
        return screenHeight;
    }


    public static Context getContext() {
        return sPhotoModule.getContext();
    }

    public static String getPkgName() {
        return sPhotoModule.getContext().getPackageName();
    }
}
