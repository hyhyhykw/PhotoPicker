package com.hy.picker.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.RadioGroup

/**
 * Created by felix on 2017/12/1 下午3:07.
 */

class IMGColorGroup @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : RadioGroup(context, attrs) {

    var checkColor: Int
        get() {
            val checkedId = checkedRadioButtonId
            val radio = findViewById<IMGColorRadio>(checkedId)
            return radio?.color ?: Color.WHITE
        }
        set(color) {
            val count = childCount
            for (i in 0 until count) {
                val radio = getChildAt(i) as IMGColorRadio
                if (radio.color == color) {
                    radio.isChecked = true
                    break
                }
            }
        }

//    constructor(context: Context) : super(context)

//    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
}
