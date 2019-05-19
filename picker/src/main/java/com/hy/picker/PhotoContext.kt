package com.hy.picker

import android.content.Context

/**
 * Created time : 2018/8/13 16:37.
 *
 * @author HY
 */
object PhotoContext {

    private lateinit var sPhotoModule: PhotoModule
    var screenWidth = 0
        private set
    var screenHeight = 0
        private set



    val context: Context
        get() = sPhotoModule.context

    val pkgName: String
        get() = sPhotoModule.context.packageName

    internal fun setPhotoModule(photoModule: PhotoModule) {
        sPhotoModule = photoModule
        val displayMetrics = context.resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
    }
}
