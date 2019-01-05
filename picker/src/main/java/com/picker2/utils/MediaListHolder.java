package com.picker2.utils;

import com.hy.picker.utils.SetList;
import com.picker2.model.Photo;
import com.picker2.model.PhotoDirectory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created time : 2018/12/27 15:05.
 *
 * @author HY
 */
public class MediaListHolder {

    public static final SetList<Photo> selectPhotos = new SetList<>();
    public static final ArrayList<Photo> currentPhotos = new ArrayList<>();
    public static final List<PhotoDirectory> allDirectories = new ArrayList<>();
}
