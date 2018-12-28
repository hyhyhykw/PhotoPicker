package com.picker2.utils;

import com.hy.picker.utils.SetList;
import com.picker2.model.Photo;
import com.picker2.model.PhotoDirectory;

/**
 * Created time : 2018/12/27 15:05.
 *
 * @author HY
 */
public class MediaListHolder {

    public static final SetList<Photo> selectPhotos = new SetList<>();
    public static final SetList<Photo> currentPhotos = new SetList<>();
    public static final SetList<PhotoDirectory> allDirectories = new SetList<>();
}
