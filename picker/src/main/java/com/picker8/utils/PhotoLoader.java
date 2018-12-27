package com.picker8.utils;

import android.content.Context;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;

import static android.provider.MediaStore.MediaColumns.DATA;

/**
 * Created time : 2018/12/27 16:38.
 *
 * @author HY
 */
public class PhotoLoader extends CursorLoader {


    public PhotoLoader(@NonNull Context context, String path, boolean video) {
        super(context);
        String selections= DATA + "=?";
        String[] selectionArgs= new String[]{path};

        if (video) {
            setProjection(PhotoDirectoryLoader.VIDEO_PROJECTION);
            setUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            setSortOrder(MediaStore.Video.Media.DATE_ADDED + " DESC");
        } else {
            setProjection(PhotoDirectoryLoader.IMAGE_PROJECTION);
            setUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            setSortOrder(MediaStore.Images.Media.DATE_ADDED + " DESC");
        }
        setSelection(selections);
        setSelectionArgs(selectionArgs);
    }


}
