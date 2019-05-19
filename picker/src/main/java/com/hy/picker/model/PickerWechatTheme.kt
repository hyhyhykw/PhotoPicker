package com.hy.picker.model

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Created time : 2019-05-19 08:44.
 *
 * @author HY
 */
open class PickerWechatTheme : PickerTheme() {

    companion object {
        @ColorInt
        val color1 = Color.parseColor("#303135")
        @ColorInt
        val color2 = Color.parseColor("#ffffff")
        @ColorInt
        val color3 = Color.parseColor("#50BC55")
        @ColorInt
        val color4 = Color.parseColor("#000000")
    }

    override val titleBgColor: Int
        @ColorInt get() = color1

    override val titleTvColor: Int
        @ColorInt get() = color2

    override val sendBgColor: Int
        @ColorInt get() = color3
    override val backIvColor: Int
        @ColorInt get() = color2
    override val sendTvColorDisable: Int
        @ColorInt get() = PickerWhiteTheme.color4
    override val sendTvColorEnable: Int
        @ColorInt get() = color2
    override val previewTvColorDisable: Int
        @ColorInt get() = PickerWhiteTheme.color4
    override val previewTvColorEnable: Int
        @ColorInt get() = color2
    override val windowBgColor: Int
        @ColorInt get() = color4

}