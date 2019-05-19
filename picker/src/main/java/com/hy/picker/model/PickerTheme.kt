package com.hy.picker.model

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils


/**
 * Created time : 2019-05-19 08:32.
 *
 * @author HY
 */
abstract class PickerTheme {

    companion object {
        /**
         * 判断颜色是不是亮色
         *
         * @param color
         * @return
         * @from https://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android
         */
        fun isLightColor(@ColorInt color: Int): Boolean {
            return ColorUtils.calculateLuminance(color) >= 0.5
        }
    }


    abstract val titleBgColor: Int

    abstract val titleTvColor: Int

    abstract val sendBgColor: Int

    abstract val backIvColor: Int

    abstract val sendTvColorDisable: Int

    abstract val sendTvColorEnable: Int

    abstract val previewTvColorDisable: Int

    abstract val previewTvColorEnable: Int

    abstract val windowBgColor: Int

}
