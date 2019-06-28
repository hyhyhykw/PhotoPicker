package com.hy.picker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager.LayoutParams
import android.widget.RadioGroup
import com.hy.picker.core.IMGText
import kotlinx.android.synthetic.main.picker_color_layout.*
import kotlinx.android.synthetic.main.picker_text_dialog.*

/**
 * Created by felix on 2017/12/1 上午11:21.
 */

class IMGTextEditDialog(context: Context, private val callback: Callback?) : Dialog(context, R.style.PickerTextDialog), RadioGroup.OnCheckedChangeListener {


    private var defaultText: IMGText? = null


    init {
        setContentView(R.layout.picker_text_dialog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        pickerCgColors.setOnCheckedChangeListener(this)
        pickerDlgDone.setOnClickListener{
            onDone()
        }
        pickerDlgCancel.setOnClickListener{
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        if (defaultText != null) {
            pickerEtText.setText(defaultText?.text)
            pickerEtText.setTextColor(defaultText!!.color)
            if (!defaultText!!.isEmpty) {
                pickerEtText.setSelection(pickerEtText.length())
            }
            defaultText = null
        } else
            pickerEtText?.setText("")
        pickerCgColors.checkColor = pickerEtText.currentTextColor
    }

    fun setText(text: IMGText?) {
        defaultText = text
    }

    fun reset() {
        setText(IMGText(null, Color.WHITE))
    }

    private fun onDone() {
        val text = pickerEtText.text.toString()
        if (!TextUtils.isEmpty(text)) {
            callback?.onText(IMGText(text, pickerEtText.currentTextColor))
        }
        dismiss()
    }

//    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
//        pickerEtText.setTextColor(pickerCgColors.checkColor)
//    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        pickerEtText.setTextColor(pickerCgColors.checkColor)
    }

    interface Callback {

        fun onText(text: IMGText)
    }
}
