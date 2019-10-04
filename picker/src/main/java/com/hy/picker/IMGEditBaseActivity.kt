package com.hy.picker

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.hy.picker.core.IMGMode
import kotlinx.android.synthetic.main.picker_edit_activity.*
import kotlinx.android.synthetic.main.picker_edit_opt_layout.*
import java.io.IOException

/**
 * Created by felix on 2017/12/5 下午3:08.
 */

abstract class IMGEditBaseActivity : PickerBaseActivity(), View.OnClickListener, IMGTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener, DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    private var textDialog: IMGTextEditDialog? = null

    abstract fun getBitmap(): Bitmap?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bitmap = getBitmap()
        if (bitmap != null) {
            setContentView(R.layout.picker_edit_activity)
            val mTv = findViewById<TextView>(R.id.picker_tv_clip_reset)
            pickerCgColors.setOnCheckedChangeListener(this)
            val colors = intArrayOf(-0xcccccd, ContextCompat.getColor(this, R.color.picker_color_accent), ContextCompat.getColor(this, R.color.picker_color_white))
            val states = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_pressed), intArrayOf())
            val colorStateList = ColorStateList(states, colors)
            mTv.setTextColor(colorStateList)
            picker_image_canvas.setImageBitmap(bitmap)

            onCreated()

        } else
            finish()
    }


    open fun onCreated() {

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.picker_rb_doodle -> onModeClick(IMGMode.DOODLE)
            R.id.picker_btn_text -> onTextModeClick()
            R.id.picker_rb_mosaic -> onModeClick(IMGMode.MOSAIC)
            R.id.picker_btn_clip -> onModeClick(IMGMode.CLIP)
            R.id.picker_btn_undo -> onUndoClick()
            R.id.pickerDlgDone -> onDoneClick()
            R.id.pickerDlgCancel -> onCancelClick()
            R.id.picker_ib_clip_cancel -> onCancelClipClick()
            R.id.picker_ib_clip_done -> onDoneClipClick()
            R.id.picker_tv_clip_reset -> onResetClipClick()
            R.id.picker_ib_clip_rotate -> onRotateClipClick()
            R.id.picker_btn_image -> onImageModeClick()
        }
    }

    protected abstract fun onImageModeClick()

    fun updateModeUI() {
        when (picker_image_canvas.mode) {
            IMGMode.DOODLE -> {
                picker_rg_modes.check(R.id.picker_rb_doodle)
                setOpSubDisplay(OP_SUB_DOODLE)
            }
            IMGMode.MOSAIC -> {
                picker_rg_modes.check(R.id.picker_rb_mosaic)
                setOpSubDisplay(OP_SUB_MOSAIC)
            }
            IMGMode.NONE -> {
                picker_rg_modes.clearCheck()
                setOpSubDisplay(OP_HIDE)
            }
            else -> {

            }
        }
    }

    private fun onTextModeClick() {
        if (textDialog == null) {
            textDialog = IMGTextEditDialog(this, this)
            textDialog?.setOnShowListener(this)
            textDialog?.setOnDismissListener(this)
        }
        textDialog?.show()
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        val checkedColor = pickerCgColors.checkColor
        onColorChanged(checkedColor)
    }

    fun setOpDisplay(op: Int) {
        if (op >= 0) {
            picker_vs_op.displayedChild = op
        }
    }

    private fun setOpSubDisplay(opSub: Int) {
        if (opSub < 0) {
            picker_layout_op_sub.visibility = View.GONE
        } else {
            picker_vs_op_sub.displayedChild = opSub
            picker_layout_op_sub.visibility = View.VISIBLE
        }
    }

    override fun onShow(dialog: DialogInterface) {
        picker_vs_op.visibility = View.GONE
    }

    override fun onDismiss(dialog: DialogInterface) {
        picker_vs_op.visibility = View.VISIBLE
    }

    abstract fun onModeClick(mode: IMGMode)

    abstract fun onUndoClick()

    abstract fun onCancelClick()

    abstract fun onDoneClick()

    abstract fun onCancelClipClick()

    abstract fun onDoneClipClick()

    abstract fun onResetClipClick()

    abstract fun onRotateClipClick()

    abstract fun onColorChanged(checkedColor: Int)

//    abstract override fun onText(text: IMGText)

    companion object {

        const val OP_HIDE = -1

        const val OP_NORMAL = 0

        const val OP_CLIP = 1

        const val OP_SUB_DOODLE = 0

        const val OP_SUB_MOSAIC = 1

        /**
         * 读取照片旋转角度
         *
         * @param path 照片路径
         * @return 角度
         */
        fun readPictureDegree(path: String): Int {
            var degree = 0
            try {
                val exifInterface = ExifInterface(path)
                when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            } catch (ignore: IOException) {
            }

            return degree
        }
    }
}
