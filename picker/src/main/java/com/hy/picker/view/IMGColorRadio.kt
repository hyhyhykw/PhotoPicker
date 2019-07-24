package com.hy.picker.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatRadioButton
import com.hy.picker.R
import kotlin.math.min

/**
 * Created by felix on 2017/12/1 下午2:50.
 */

class IMGColorRadio @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : AppCompatRadioButton(context, attrs, defStyleAttr), ValueAnimator.AnimatorUpdateListener {


    private val strokeColor: Int

    private var radiusRatio = 0f


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val animator = ValueAnimator.ofFloat(0f, 1f)


    var color = Color.WHITE
        set(value) {
            field = value
            paint.color = value
        }

    init {
        animator.addUpdateListener(this)
        animator.duration = 200
        animator.interpolator = AccelerateDecelerateInterpolator()

        val a = context.obtainStyledAttributes(attrs, R.styleable.IMGColorRadio)

        color = a.getColor(R.styleable.IMGColorRadio_image_color, Color.WHITE)
        strokeColor = a.getColor(R.styleable.IMGColorRadio_image_stroke_color, Color.WHITE)

        a.recycle()
        buttonDrawable = null

        paint.strokeWidth = 5f
        setOnClickListener { toggle() }
    }


    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        val hw = width / 2f
        val hh = height / 2f
        val radius = min(hw, hh)

        canvas?.save()
        paint.color = color
        paint.style = Paint.Style.FILL
        Log.e("TAG", "hw====$hw")
        Log.e("TAG", "hh====$hh")
        Log.e("TAG", "radiusRatio====$radiusRatio")
        canvas?.drawCircle(hw, hh, getBallRadius(radius), paint)

        paint.color = strokeColor
        paint.style = Paint.Style.STROKE
        canvas?.drawCircle(hw, hh, getRingRadius(radius), paint)
        canvas?.restore()
    }

    private fun getBallRadius(radius: Float): Float {
        return radius * ((RADIUS_BALL - RADIUS_BASE) * radiusRatio + RADIUS_BASE)
    }

    private fun getRingRadius(radius: Float): Float {
        return radius * ((RADIUS_RING - RADIUS_BASE) * radiusRatio + RADIUS_BASE)
    }

    override fun setChecked(checked: Boolean) {
        val isChanged = checked != isChecked

        super.setChecked(checked)

        Log.e("TAG", "111111111111====$radiusRatio")

        if (isChanged) {
            if (checked) {
                Log.e("TAG", "111111111111====$radiusRatio")
                animator.start()
            } else {
                Log.e("TAG", "22222222222====$radiusRatio")
                animator.reverse()
            }
        }
    }


    override fun onAnimationUpdate(animation: ValueAnimator?) {
        radiusRatio = animation?.animatedValue as Float? ?: 0f
        Log.e("TAG", "radiusRatio====$radiusRatio")
        invalidate()
    }

    override fun performClick(): Boolean {
        Log.e("TAG", "radiusRatio====$radiusRatio")
        return super.performClick()
    }

    companion object {

        private const val RADIUS_BASE = 0.6f

        private const val RADIUS_RING = 0.9f

        private const val RADIUS_BALL = 0.72f
    }
}
