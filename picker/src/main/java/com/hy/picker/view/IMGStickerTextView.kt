package com.hy.picker.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView

import com.hy.picker.IMGTextEditDialog
import com.hy.picker.core.IMGText

/**
 * Created by felix on 2017/11/14 下午7:27.
 */
class IMGStickerTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : IMGStickerView(context, attrs, defStyleAttr), IMGTextEditDialog.Callback {

    private lateinit var textView: TextView


    var text: IMGText = IMGText("", Color.WHITE)
        set(value) {
            field = value
            textView.text = value.text
            textView.setTextColor(value.color)
        }

    private val dialog: IMGTextEditDialog
            by lazy {
                IMGTextEditDialog(context, this)
            }

    override fun onInitialize(context: Context) {
        if (baseTextSize <= 0) {
            baseTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    TEXT_SIZE_SP, context.resources.displayMetrics)
        }
        super.onInitialize(context)
    }

    override fun onCreateContentView(context: Context): View {
        textView = TextView(context)
        textView.textSize = baseTextSize
        textView.setPadding(PADDING, PADDING, PADDING, PADDING)
        textView.setTextColor(Color.WHITE)

        return textView
    }

    override fun onContentTap() {
        val dialog = dialog
        dialog.setText(text)
        dialog.show()
    }

    override fun onText(text: IMGText) {
        this.text = text
    }

    companion object {

        private var baseTextSize = -1f

        private const val PADDING = 26

        private const val TEXT_SIZE_SP = 24f
    }
}