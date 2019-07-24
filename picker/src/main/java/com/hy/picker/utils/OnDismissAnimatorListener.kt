package com.hy.picker.utils

/**
 * Created time : 2019-07-24 17:28.
 *
 * @author HY
 */
interface OnDismissAnimatorListener {
    /**
     * 开始消失，这个时候PopupWindow还在，只是在执行消失动画
     */
    fun onStartDismiss()

    /**
     * 完全消失
     */
    fun onDismiss()
}