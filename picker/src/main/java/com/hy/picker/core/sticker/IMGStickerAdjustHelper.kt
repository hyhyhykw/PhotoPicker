package com.hy.picker.core.sticker

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.hy.picker.BuildConfig
import com.hy.picker.view.IMGStickerView

/**
 * Created by felix on 2017/11/15 下午5:44.
 */

class IMGStickerAdjustHelper(private val container: IMGStickerView, 
                             private val outerView: View)
    : View.OnTouchListener {

    private var centerX = 0f
    private var centerY = 0f

    private var radius = .0
    private var degree = .0

    private val matrix = Matrix()

    init {
        outerView.setOnTouchListener(this)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {

                val x = event.x

                val y = event.y

                centerY = 0f
                centerX = centerY

                val pointX = outerView.x + x - container.pivotX

                val pointY = outerView.y + y - container.pivotY

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("X=%f,Y=%f", pointX, pointY))
                }

                radius = toLength(0f, 0f, pointX, pointY)

                degree = toDegrees(pointY, pointX)

                matrix.setTranslate(pointX - x, pointY - y)
                if (BuildConfig.DEBUG)
                    Log.d(TAG, String.format("degrees=%f", toDegrees(pointY, pointX)))

                matrix.postRotate((-toDegrees(pointY, pointX)).toFloat(), centerX, centerY)

                return true
            }

            MotionEvent.ACTION_MOVE -> {

                val xy = floatArrayOf(event.x, event.y)

                val pointX = outerView.x + xy[0] - container.pivotX

                val pointY = outerView.y + xy[1] - container.pivotY
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("X=%f,Y=%f", pointX, pointY))
                }

                val radius = toLength(0f, 0f, pointX, pointY)

                val degrees = toDegrees(pointY, pointX)

                val scale = (radius / radius).toFloat()


                container.addScale(scale)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "    D   = " + (degrees - degree))
                }

                container.rotation = (container.rotation + degrees - degree).toFloat()

                this.radius = radius

                return true
            }
        }
        return false
    }

    companion object {

        private const val TAG = "IMGStickerAdjustHelper"

        private fun toDegrees(v: Float, v1: Float): Double {
            return Math.toDegrees(Math.atan2(v.toDouble(), v1.toDouble()))
        }

        private fun toLength(x1: Float, y1: Float, x2: Float, y2: Float): Double {
            return Math.sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)).toDouble())
        }
    }
}
