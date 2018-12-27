package com.picker8.utils;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import static android.provider.MediaStore.MediaColumns.MIME_TYPE;

/**
 * Created by 黄东鲁 on 15/6/28.
 */
public class PhotoDirectoryLoader extends CursorLoader {

    public static final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
    };

    public static final String[] VIDEO_PROJECTION = {
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
    };



    public PhotoDirectoryLoader(Context context, boolean showGif, boolean gifOnly, boolean video) {
        super(context);
        String selections;
        String[] selectionArgs;
        if (video) {
            setProjection(VIDEO_PROJECTION);
            setUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            setSortOrder(MediaStore.Video.Media.DATE_ADDED + " DESC");

            selections = null;
            selectionArgs = null;
        } else {
            setProjection(IMAGE_PROJECTION);
            setUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            setSortOrder(MediaStore.Images.Media.DATE_ADDED + " DESC");

            if (gifOnly) {
                selections = MIME_TYPE + "=?";
                selectionArgs = new String[]{"image/gif"};
            } else {
                if (showGif) {
                    selections = MIME_TYPE + "=? or " + MIME_TYPE + "=? or " + MIME_TYPE + "=? " + "or " + MIME_TYPE + "=?";
                    selectionArgs = new String[]{"image/jpeg", "image/png", "image/bmp", "image/gif"};
                } else {
                    selections = MIME_TYPE + "=? or " + MIME_TYPE + "=? or " + MIME_TYPE + "=?";
                    selectionArgs = new String[]{"image/jpeg", "image/png", "image/jpg"};
                }
            }

        }
        setSelection(selections);
        setSelectionArgs(selectionArgs);
    }


    private PhotoDirectoryLoader(Context context, Uri uri, String[] projection, String selection,
                                 String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }


}
