package com.hy.picker.model

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Created time : 2019-05-19 08:44.
 *
 * @author HY
 */
open class PickerWhiteTheme : PickerTheme() {

    companion object {
        @ColorInt
        val color1 = Color.parseColor("#ffffff")
        @ColorInt
        val color2 = Color.parseColor("#FF6E40")
        @ColorInt
        val color3 = Color.parseColor("#333333")
        @ColorInt
        val color4 = Color.parseColor("#AAAAAA")
        @ColorInt
        val color5 = Color.parseColor("#303135")
    }

    override val titleTvColor: Int
        @ColorInt get() = color3
    override val titleBgColor: Int
        @ColorInt get() = color1
    override val sendBgColor: Int
        @ColorInt get() = color2
    override val backIvColor: Int
        @ColorInt get() = color3
    override val sendTvColorDisable: Int
        @ColorInt get() = color4
    override val sendTvColorEnable: Int
        @ColorInt get() = color5
    override val previewTvColorDisable: Int
        @ColorInt get() = color4
    override val previewTvColorEnable: Int
        @ColorInt get() = color3
    override val windowBgColor: Int
        @ColorInt get() = color1

}