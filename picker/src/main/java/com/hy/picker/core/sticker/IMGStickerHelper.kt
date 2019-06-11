package com.hy.picker.core.sticker

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.View

import com.hy.picker.view.IMGStickerImageView

/**
 * Created by felix on 2017/11/16 下午5:52.
 */

class IMGStickerHelper<StickerView>(private val outerView: StickerView) :
        IMGStickerPortrait,
        IMGStickerPortrait.Callback
        where StickerView : View, StickerView : IMGSticker {

    private var frame: RectF? = null

    private var callback: IMGStickerPortrait.Callback? = null

    private var isShowing = false

    override fun show(): Boolean {
        if (!isShowing()) {
            isShowing = true
            onShowing(outerView)
            return true
        }
        return false
    }

    override fun remove(): Boolean {
        if (outerView is IMGStickerImageView) {
            (outerView as IMGStickerImageView).destroy()
        }
        return onRemove(outerView)
    }

    override fun dismiss(): Boolean {
        if (isShowing()) {
            isShowing = false
            onDismiss(outerView)
            return true
        }
        return false
    }

    override fun isShowing(): Boolean {
        return isShowing
    }

    override fun getFrame(): RectF {
        if (frame == null) {
            frame = RectF(0f, 0f, outerView.getWidth().toFloat(), outerView.getHeight().toFloat())
            val pivotX = outerView.getX() + outerView.getPivotX()
            val pivotY = outerView.getY() + outerView.getPivotY()

            val matrix = Matrix()
            matrix.setTranslate(outerView.getX(), outerView.getY())
            matrix.postScale(outerView.getScaleX(), outerView.getScaleY(), pivotX, pivotY)
            matrix.mapRect(frame)
        }
        return frame!!
    }

    override fun onSticker(canvas: Canvas) {
        // empty
    }

    override fun registerCallback(callback: IMGStickerPortrait.Callback) {
        this.callback = callback
    }

    override fun unregisterCallback(callback: IMGStickerPortrait.Callback) {
        this.callback = null
    }

    override fun <V> onRemove(stickerView: V): Boolean where V : View, V : IMGSticker {
        return callback?.onRemove(stickerView) ?: false
    }

    override fun <V> onDismiss(stickerView: V) where V : View, V : IMGSticker {
        frame = null
        stickerView.invalidate()
        callback?.onDismiss(stickerView)
    }

    override fun <V> onShowing(stickerView: V) where V : View, V : IMGSticker {
        stickerView.invalidate()
        callback?.onShowing(stickerView)
    }
}
