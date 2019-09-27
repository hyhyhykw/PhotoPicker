package com.hy.picker

/**
 * Created time : 2018/8/13 16:37.
 *
 * @author HY
 */
object PhotoContext {

    private lateinit var sPhotoModule: PhotoModule

    val context by lazy { sPhotoModule.context }

    val imageItemSize: Int by lazy {
        (context.screenWidth() - context.dp(4f) * 3) / 4
    }

    @JvmStatic
    internal fun setPhotoModule(photoModule: PhotoModule) {
        sPhotoModule = photoModule
    }
}
