package com.hy.picker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.RadioGroup
import com.hy.picker.core.IMGText
import kotlinx.android.synthetic.main.picker_color_layout.*
import kotlinx.android.synthetic.main.picker_text_dialog.*

/**
 * Created by felix on 2017/12/1 上午11:21.
 */

class IMGTextEditDialog(context: Context, private val mCallback: Callback?) : Dialog(context, R.style.PickerTextDialog), View.OnClickListener, RadioGroup.OnCheckedChangeListener {


    private var mDefaultText: IMGText? = null


    init {
        setContentView(R.layout.picker_text_dialog)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        picker_cg_colors.setOnCheckedChangeListener(this)
        picker_tv_cancel.setOnClickListener(this)
        picker_tv_done.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (mDefaultText != null) {
            picker_et_text.setText(mDefaultText!!.text)
            picker_et_text.setTextColor(mDefaultText!!.color)
            if (!mDefaultText!!.isEmpty) {
                picker_et_text.setSelection(picker_et_text.length())
            }
            mDefaultText = null
        } else
            picker_et_text!!.setText("")
        picker_cg_colors.checkColor = picker_et_text.currentTextColor
    }

    fun setText(text: IMGText) {
        mDefaultText = text
    }

    fun reset() {
        setText(IMGText(null, Color.WHITE))
    }

    override fun onClick(v: View?) {
        val vid = v?.id
        if (vid == R.id.picker_tv_done) {
            onDone()
        } else if (vid == R.id.picker_tv_cancel) {
            dismiss()
        }
    }

    private fun onDone() {
        val text = picker_et_text.text.toString()
        if (!TextUtils.isEmpty(text) && mCallback != null) {
            mCallback.onText(IMGText(text, picker_et_text.currentTextColor))
        }
        dismiss()
    }

//    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
//        picker_et_text.setTextColor(picker_cg_colors.checkColor)
//    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        picker_et_text.setTextColor(picker_cg_colors.checkColor)
    }

    interface Callback {

        fun onText(text: IMGText)
    }
}
