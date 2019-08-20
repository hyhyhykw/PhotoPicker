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

    val selectPhotos = SetList<Photo>()
    val currentPhotos = ArrayList<Photo>()
    val allDirectories = ArrayList<PhotoDirectory>()
}
