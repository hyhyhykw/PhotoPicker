package com.hy.picker.core.util

import android.graphics.Matrix
import android.graphics.RectF

import com.hy.picker.core.homing.IMGHoming
import kotlin.math.max
import kotlin.math.min

/**
 * Created by felix on 2017/12/5 下午2:20.
 */

object IMGUtils {

    private val MATRIX = Matrix()

    fun center(win: RectF, frame: RectF) {
        frame.offset(win.centerX() - frame.centerX(), win.centerY() - frame.centerY())
    }

    @JvmOverloads
    fun fitCenter(win: RectF, frame: RectF, padding: Float = 0f) {
        fitCenter(win, frame, padding, padding, padding, padding)
    }

    private fun fitCenter(win: RectF, frame: RectF, paddingLeft: Float, paddingTop: Float, paddingRight: Float, paddingBottom: Float) {

        var paddingL = paddingLeft
        var paddingT = paddingTop
        var paddingR = paddingRight
        var paddingB = paddingBottom

        if (win.isEmpty || frame.isEmpty) {
            return
        }

        if (win.width() < paddingL + paddingR) {
            paddingR = 0f
            paddingL = paddingR
            // 忽略Padding 值
        }

        if (win.height() < paddingT + paddingB) {
            paddingB = 0f
            paddingT = paddingB
            // 忽略Padding 值
        }

        val w = win.width() - paddingL - paddingR
        val h = win.height() - paddingT - paddingB

        val scale = min(w / frame.width(), h / frame.height())

        // 缩放FIT
        frame.set(0f, 0f, frame.width() * scale, frame.height() * scale)

        // 中心对齐
        frame.offset(
                win.centerX() + (paddingL - paddingR) / 2 - frame.centerX(),
                win.centerY() + (paddingT - paddingB) / 2 - frame.centerY()
        )
    }

    fun fitHoming(win: RectF, frame: RectF): IMGHoming {
        val dHoming = IMGHoming(0f, 0f, 1f, 0f)

        if (frame.contains(win)) {
            // 不需要Fit
            return dHoming
        }

        // 宽高都小于Win，才有必要放大
        if (frame.width() < win.width() && frame.height() < win.height()) {
            dHoming.scale = min(win.width() / frame.width(), win.height() / frame.height())
        }

        val rect = RectF()
        MATRIX.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY())
        MATRIX.mapRect(rect, frame)

        if (rect.width() < win.width()) {
            dHoming.x += win.centerX() - rect.centerX()
        } else {
            if (rect.left > win.left) {
                dHoming.x += win.left - rect.left
            } else if (rect.right < win.right) {
                dHoming.x += win.right - rect.right
            }
        }

        if (rect.height() < win.height()) {
            dHoming.y += win.centerY() - rect.centerY()
        } else {
            if (rect.top > win.top) {
                dHoming.y += win.top - rect.top
            } else if (rect.bottom < win.bottom) {
                dHoming.y += win.bottom - rect.bottom
            }
        }

        return dHoming
    }

    fun fitHoming(win: RectF, frame: RectF, centerX: Float, centerY: Float): IMGHoming {
        val dHoming = IMGHoming(0f, 0f, 1f, 0f)

        if (frame.contains(win)) {
            // 不需要Fit
            return dHoming
        }

        // 宽高都小于Win，才有必要放大
        if (frame.width() < win.width() && frame.height() < win.height()) {
            dHoming.scale = min(win.width() / frame.width(), win.height() / frame.height())
        }

        val rect = RectF()
        MATRIX.setScale(dHoming.scale, dHoming.scale, centerX, centerY)
        MATRIX.mapRect(rect, frame)

        if (rect.width() < win.width()) {
            dHoming.x += win.centerX() - rect.centerX()
        } else {
            if (rect.left > win.left) {
                dHoming.x += win.left - rect.left
            } else if (rect.right < win.right) {
                dHoming.x += win.right - rect.right
            }
        }

        if (rect.height() < win.height()) {
            dHoming.y += win.centerY() - rect.centerY()
        } else {
            if (rect.top > win.top) {
                dHoming.y += win.top - rect.top
            } else if (rect.bottom < win.bottom) {
                dHoming.y += win.bottom - rect.bottom
            }
        }

        return dHoming
    }


    fun fitHoming(win: RectF, frame: RectF, isJustInner: Boolean): IMGHoming {
        val dHoming = IMGHoming(0f, 0f, 1f, 0f)

        if (frame.contains(win) && !isJustInner) {
            // 不需要Fit
            return dHoming
        }

        // 宽高都小于Win，才有必要放大
        if (isJustInner || frame.width() < win.width() && frame.height() < win.height()) {
            dHoming.scale = min(win.width() / frame.width(), win.height() / frame.height())
        }

        val rect = RectF()
        MATRIX.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY())
        MATRIX.mapRect(rect, frame)

        if (rect.width() < win.width()) {
            dHoming.x += win.centerX() - rect.centerX()
        } else {
            if (rect.left > win.left) {
                dHoming.x += win.left - rect.left
            } else if (rect.right < win.right) {
                dHoming.x += win.right - rect.right
            }
        }

        if (rect.height() < win.height()) {
            dHoming.y += win.centerY() - rect.centerY()
        } else {
            if (rect.top > win.top) {
                dHoming.y += win.top - rect.top
            } else if (rect.bottom < win.bottom) {
                dHoming.y += win.bottom - rect.bottom
            }
        }

        return dHoming
    }

    fun fillHoming(win: RectF, frame: RectF): IMGHoming {
        val dHoming = IMGHoming(0f, 0f, 1f, 0f)
        if (frame.contains(win)) {
            // 不需要Fill
            return dHoming
        }

        if (frame.width() < win.width() || frame.height() < win.height()) {
            dHoming.scale = max(win.width() / frame.width(), win.height() / frame.height())
        }

        val rect = RectF()
        MATRIX.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY())
        MATRIX.mapRect(rect, frame)

        if (rect.left > win.left) {
            dHoming.x += win.left - rect.left
        } else if (rect.right < win.right) {
            dHoming.x += win.right - rect.right
        }

        if (rect.top > win.top) {
            dHoming.y += win.top - rect.top
        } else if (rect.bottom < win.bottom) {
            dHoming.y += win.bottom - rect.bottom
        }

        return dHoming
    }

    fun fillHoming(win: RectF, frame: RectF, pivotX: Float, pivotY: Float): IMGHoming {
        val dHoming = IMGHoming(0f, 0f, 1f, 0f)
        if (frame.contains(win)) {
            // 不需要Fill
            return dHoming
        }

        if (frame.width() < win.width() || frame.height() < win.height()) {
            dHoming.scale = max(win.width() / frame.width(), win.height() / frame.height())
        }

        val rect = RectF()
        MATRIX.setScale(dHoming.scale, dHoming.scale, pivotX, pivotY)
        MATRIX.mapRect(rect, frame)

        if (rect.left > win.left) {
            dHoming.x += win.left - rect.left
        } else if (rect.right < win.right) {
            dHoming.x += win.right - rect.right
        }

        if (rect.top > win.top) {
            dHoming.y += win.top - rect.top
        } else if (rect.bottom < win.bottom) {
            dHoming.y += win.bottom - rect.bottom
        }

        return dHoming
    }

    fun fill(win: RectF, frame: RectF): IMGHoming {
        val dHoming = IMGHoming(0f, 0f, 1f, 0f)

        if (win == frame) {
            return dHoming
        }

        // 第一次时缩放到裁剪区域内
        dHoming.scale = max(win.width() / frame.width(), win.height() / frame.height())

        val rect = RectF()
        MATRIX.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY())
        MATRIX.mapRect(rect, frame)

        dHoming.x += win.centerX() - rect.centerX()
        dHoming.y = dHoming.y + win.centerY() - rect.centerY()

        return dHoming
    }

    fun inSampleSize(rawSampleSize: Int): Int {
        var raw = rawSampleSize
        var ans = 1
        while (raw > 1) {
            ans = ans shl 1
            raw = raw shr 1
        }

        if (ans != rawSampleSize) {
            ans = ans shl 1
        }

        return ans
    }

    fun rectFill(win: RectF, frame: RectF) {
        if (win == frame) {
            return
        }

        val scale = max(win.width() / frame.width(), win.height() / frame.height())

        MATRIX.setScale(scale, scale, frame.centerX(), frame.centerY())
        MATRIX.mapRect(frame)

        if (frame.left > win.left) {
            frame.left = win.left
        } else if (frame.right < win.right) {
            frame.right = win.right
        }

        if (frame.top > win.top) {
            frame.top = win.top
        } else if (frame.bottom < win.bottom) {
            frame.bottom = win.bottom
        }
    }
}
