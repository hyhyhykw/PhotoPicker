package com.hy.picker.view

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import com.hy.picker.R
import com.hy.picker.postDelay
import com.hy.picker.utils.OnDismissAnimatorListener
import kotlinx.android.extensions.LayoutContainer

/**
 * Created time : 2019-07-24 16:27.
 *
 * @author HY
 */
abstract class AnimatorPopupWindow(protected val context: Context) : PopupWindow(context), LayoutContainer {

    init {
        animationStyle = R.style.pickerCustomAnimPop
    }

    private fun postAnimateIn() {
        postDelay({ animateIn() }, 5)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
        postAnimateIn()
    }


    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        postAnimateIn()
    }

    /**
     * 直接关闭PopupWindow，没有动画效果
     */
    fun superDismiss() {
        super.dismiss()
        onDismissAnimatorListener?.onDismiss()
    }

    override fun dismiss() {
        animateOut()
        onDismissAnimatorListener?.onStartDismiss()
    }

    private var onDismissAnimatorListener: OnDismissAnimatorListener? = null

    /**
     * PopupWindow进入屏幕动画
     */
    protected abstract fun animateIn()

    fun onAnimationEnd() {
        superDismiss()
    }


    fun setOnDismissAnimatorListener(onDismissAnimatorListener: OnDismissAnimatorListener) {
        this.onDismissAnimatorListener = onDismissAnimatorListener
    }


    fun setOnDismissAnimatorListener(
            onStartDismiss: BlockCallback = {},
            onDismiss: BlockCallback = {}
    ) {
        setOnDismissAnimatorListener(object : OnDismissAnimatorListener {
            override fun onStartDismiss() {
                onStartDismiss()
            }

            override fun onDismiss() {
                onDismiss()
            }
        })
    }


    /**
     * PopupWindow从屏幕消失动画。在动画执行结束时请调用 [AnimatorPopupWindow.onAnimationEnd]
     */
    protected abstract fun animateOut()


    override val containerView: View?
        get() = contentView


}
typealias BlockCallback = () -> Unit
