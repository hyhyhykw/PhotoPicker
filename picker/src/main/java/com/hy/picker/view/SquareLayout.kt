package com.hy.picker.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Created time : 2018/5/30 14:54.
 *
 * @author HY
 */
class SquareLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec))
        val i = measuredWidth
//        measuredHeight

        val j = MeasureSpec.makeMeasureSpec(i, MeasureSpec.EXACTLY)
        super.onMeasure(j, j)
    }
}
