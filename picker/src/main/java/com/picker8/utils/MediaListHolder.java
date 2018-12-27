package com.picker8.utils;

import com.hy.picker.utils.SetList;
import com.picker8.model.Photo;
import com.picker8.model.PhotoDirectory;

import java.util.ArrayList;

/**
 * Created time : 2018/12/27 15:05.
 *
 * @author HY
 */
public class MediaListHolder {

    public static final SetList<Photo> selectPhotos = new SetList<>();
    public static final SetList<Photo> currentPhotos = new SetList<>();
    public static final ArrayList<PhotoDirectory> allDirectories = new ArrayList<>();

}
