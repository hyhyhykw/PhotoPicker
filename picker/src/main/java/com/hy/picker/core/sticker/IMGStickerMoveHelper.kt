package com.hy.picker.core.sticker

import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View

/**
 * Created by felix on 2017/11/17 下午6:08.
 */

class IMGStickerMoveHelper {

    private var lastX = 0f
    private var lastY = 0f

    fun onTouch(v: View, event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                MATRIX.reset()
                MATRIX.setRotate(v.rotation)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dxy = floatArrayOf(event.x - lastX, event.y - lastY)
                MATRIX.mapPoints(dxy)
                v.translationX = v.translationX + dxy[0]
                v.translationY = v.translationY + dxy[1]
                return true
            }
        }
        return false
    }

    companion object {

        private val MATRIX = Matrix()
    }
}
