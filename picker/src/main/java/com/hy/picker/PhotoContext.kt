package com.hy.picker

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable

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

    val imageItemSize: Int by lazy {
        (screenWidth - context.dp(4f) * 3) / 4
    }

    private val colors = intArrayOf( 0x30000000,0x10FFFFFF)
    val normalDrawable: Drawable by lazy {
        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors)
        gradientDrawable.setBounds(0, 0, imageItemSize, imageItemSize)
        gradientDrawable

    }

    val selectedDrawable: Drawable by lazy {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.color = ColorStateList.valueOf(Color.parseColor("#80000000"))
        gradientDrawable.setBounds(0, 0, imageItemSize, imageItemSize)
        gradientDrawable
    }
    val dp32: Int by lazy {
        context.dp(32f)
    }
    val dp30: Int by lazy {
        context.dp(30f)
    }
    val dp28: Int by lazy {
        context.dp(28f)
    }
    val dp20: Int by lazy {
        context.dp(20f)
    }
    val dp15: Int by lazy {
        context.dp(15f)
    }
    val dp10: Int by lazy {
        context.dp(10f)
    }
    val dp5: Int by lazy {
        context.dp(5f)
    }

//    val bottomDrawable: Drawable by lazy {
//        val gradientDrawable = GradientDrawable()
//        gradientDrawable.color = ColorStateList.valueOf(0x40000000)
//        gradientDrawable.setBounds(0, 0, imageItemSize, dp30)
//        gradientDrawable
//    }

    internal fun setPhotoModule(photoModule: PhotoModule) {
        sPhotoModule = photoModule
        val displayMetrics = context.resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
    }
}
