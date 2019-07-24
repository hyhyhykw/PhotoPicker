package com.hy.picker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.hy.picker.R
import kotlin.math.ceil

/**
 * Created time : 2018/8/27 14:24.
 *
 * @author HY
 */
class CompletedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {
    // 画实心圆的画笔
    private val circlePaint = Paint()
    // 画圆环的画笔
    private val ringPaint = Paint()
    // 画圆环的画笔背景色
    private val ringPaintBg = Paint()
    // 画字体的画笔
    private val textPaint = Paint()
    // 圆形颜色
    private val circleColor: Int
    // 圆环颜色
    private val ringColor: Int
    // 圆环背景颜色
    private val ringBgColor: Int
    // 半径
    private val radius: Float
    // 圆环半径
    private val ringRadius: Float
    // 圆环宽度
    private val strokeWidth: Float
    // 字的高度
    private var textHeight = 0f

    // 当前进度
    var progress = 0
        set(value) {
            field = value
            postInvalidate()//重绘
        }


    private val oval1 = RectF()
    private val oval = RectF()

    init {
        val typeArray = context.theme.obtainStyledAttributes(attrs,
                R.styleable.PickerCompletedView, 0, 0)
        radius = typeArray.getDimension(R.styleable.PickerCompletedView_picker_complete_radius, 80f)
        strokeWidth = typeArray.getDimension(R.styleable.PickerCompletedView_picker_complete_strokeWidth, 10f)
        circleColor = typeArray.getColor(R.styleable.PickerCompletedView_picker_complete_circleColor, -0x1)
        ringColor = typeArray.getColor(R.styleable.PickerCompletedView_picker_complete_ringColor, -0x1)
        ringBgColor = typeArray.getColor(R.styleable.PickerCompletedView_picker_complete_ringBgColor, -0x1)
        typeArray.recycle()
        ringRadius = radius + strokeWidth / 2
        initVariable()
    }


    //初始化画笔
    private fun initVariable() {
        //内圆
        circlePaint.isAntiAlias = true
        circlePaint.color = circleColor
        circlePaint.style = Paint.Style.FILL

        //外圆弧背景
        ringPaintBg.isAntiAlias = true
        ringPaintBg.color = ringBgColor
        ringPaintBg.style = Paint.Style.STROKE
        ringPaintBg.strokeWidth = strokeWidth


        //外圆弧
        ringPaint.isAntiAlias = true
        ringPaint.color = ringColor
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = strokeWidth
        //ringPaint.setStrokeCap(Paint.Cap.ROUND);//设置线冒样式，有圆 有方

        //中间字
        textPaint.isAntiAlias = true
        textPaint.style = Paint.Style.FILL
        textPaint.color = ringColor
        textPaint.textSize = radius / 2

        val fm = textPaint.fontMetrics
        textHeight = ceil((fm.descent - fm.ascent).toDouble()).toInt().toFloat()
    }

    //画图
    override fun onDraw(canvas: Canvas) {
        // 圆心x坐标
        val xCenter = width / 2
        // 圆心y坐标
        val yCenter = height / 2

        //内圆
        canvas.drawCircle(xCenter.toFloat(), yCenter.toFloat(), radius, circlePaint)

        //外圆弧背景
        oval1.left = xCenter - ringRadius
        oval1.top = yCenter - ringRadius
        oval1.right = ringRadius * 2 + (xCenter - ringRadius)
        oval1.bottom = ringRadius * 2 + (yCenter - ringRadius)
        canvas.drawArc(oval1, 0f, 360f, false, ringPaintBg) //圆弧所在的椭圆对象、圆弧的起始角度、圆弧的角度、是否显示半径连线

        //外圆弧
        if (progress > 0) {

            oval.left = xCenter - ringRadius
            oval.top = yCenter - ringRadius
            oval.right = ringRadius * 2 + (xCenter - ringRadius)
            oval.bottom = ringRadius * 2 + (yCenter - ringRadius)
            // 总进度
            val totalProgress = 100
            canvas.drawArc(oval, -90f, progress.toFloat() / totalProgress * 360, false, ringPaint) //

            //字体
            val txt = progress.toString() + ""
            // 字的长度
            val textWidth = textPaint.measureText(txt, 0, txt.length)
            canvas.drawText(txt, xCenter - textWidth / 2, yCenter + textHeight / 4, textPaint)
        }
    }
}
