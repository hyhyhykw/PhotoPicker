package com.hy.picker.utils

import com.hy.picker.model.Photo
import com.hy.picker.model.PhotoDirectory
import java.util.*

/**
 * Created time : 2018/12/27 15:05.
 *
 * @author HY
 */
object MediaListHolder {

    @JvmStatic
    val selectPhotos = SetList<Photo>()
    @JvmStatic
    val currentPhotos = ArrayList<Photo>()
    @JvmStatic
    val allDirectories = ArrayList<PhotoDirectory>()
}
