package com.hy.picker.view


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import com.hy.picker.R
import com.hy.picker.adapter.CateDlgAdapter
import com.hy.picker.utils.MediaListHolder
import kotlinx.android.synthetic.main.picker_folder_window.*

/**
 * Created by leandom on 2017/9/2.
 * 自定义PopupWindow动画。
 */

class FolderPopupWindow(context: Context,
                        viewWidth: Int,
                        defaultDrawable: Drawable,
                        listener: (Int,String?, Boolean) -> Unit,
                        private val viewHeight: Int)
    : AnimatorPopupWindow(context) {

    private val duration = 300L

    private val adapter = CateDlgAdapter(defaultDrawable)

    init {
        val view = View.inflate(context, R.layout.picker_folder_window, null)
        contentView = view
        width = viewWidth
        height = viewHeight
        adapter.setOnItemClickListener(listener)
        rcyFolder.adapter = adapter
        adapter.reset(MediaListHolder.allDirectories)
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(0x00000000))
    }


    override fun animateIn() {

        rcyFolder.translationY = viewHeight.toFloat()
        rcyFolder.animate()
                .translationY(0f)
                .setDuration(duration)
                .setListener(null)
                .start()

    }


    override fun animateOut() {

        rcyFolder.animate()
                .translationY(viewHeight.toFloat())
                .setListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        onAnimationEnd()
                        rcyFolder.animate().setListener(null)
                    }
                }).setDuration(duration)
                .start()

    }

}
