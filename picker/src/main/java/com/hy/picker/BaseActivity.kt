package com.hy.picker

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hy.picker.model.PickerTheme

/**
 * Created time : 2018/8/28 8:40.
 *
 * @author HY
 */
open class BaseActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//竖屏
        super.onCreate(savedInstanceState)

        if (PickerTheme.isLightColor(PhotoPicker.theme.titleBgColor)) {
            setStatusTransparentLight()
        } else {
            setStatusTransparent()
        }
    }
}
