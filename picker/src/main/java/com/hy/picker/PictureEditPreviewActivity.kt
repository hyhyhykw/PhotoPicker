package com.hy.picker

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.facebook.drawee.drawable.ScalingUtils
import com.hy.picker.model.Photo
import com.hy.picker.utils.DisplayOptimizeListener
import com.hy.picker.view.ImageSource
import com.hy.picker.view.PickerScaleImageView
import kotlinx.android.synthetic.main.picker_activity_edit_preview.*
import me.relex.photodraweeview.PhotoDraweeView
import java.io.File

/**
 * Created time : 2018/8/2 8:23.
 *
 * @author HY
 */
class PictureEditPreviewActivity : PickerBaseActivity() {

    private var fullScreen = false

    private val constraintSet1 = ConstraintSet()
    private val constraintSet2 = ConstraintSet()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picker_activity_edit_preview)

        val defaultDrawable = ContextCompat.getDrawable(this, PhotoPicker.defaultDrawable)!!

        initView()

        val intent = intent
        val picItem: Photo? = intent.getParcelableExtra(EXTRA_ITEM)

        if (picItem == null) {
            Toast.makeText(this, R.string.picker_file_error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        val imageView: View
        if (picItem.isLong) {
            imageView = PickerScaleImageView(this)
            imageView.setOnClickListener {
                fullScreen = !fullScreen

                val autoTransition = AutoTransition()
                autoTransition.duration = 200
                TransitionManager.beginDelayedTransition(picker_whole_layout, autoTransition)
                if (fullScreen) {
                    constraintSet2.applyTo(picker_whole_layout)
                } else {
                    constraintSet1.applyTo(picker_whole_layout)
                }
            }
        } else {
            imageView = PhotoDraweeView(this)
            val hierarchy = imageView.hierarchy
            hierarchy.setFailureImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.setPlaceholderImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.actualImageScaleType = ScalingUtils.ScaleType.FIT_CENTER
            imageView.setOnViewTapListener { _, _, _ ->
                fullScreen = !fullScreen
                val autoTransition = AutoTransition()
                autoTransition.duration = 200
                TransitionManager.beginDelayedTransition(picker_whole_layout, autoTransition)
                if (fullScreen) {
                    constraintSet2.applyTo(picker_whole_layout)
                } else {
                    constraintSet1.applyTo(picker_whole_layout)
                }
            }
        }
        imageView.id = R.id.pickerPhotoImage


        picker_whole_layout.addView(imageView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(picker_whole_layout)
        constraintSet.constrainHeight(R.id.pickerPhotoImage, 0)
        constraintSet.constrainWidth(R.id.pickerPhotoImage, 0)
        constraintSet.connect(R.id.pickerPhotoImage, ConstraintSet.TOP, R.id.pickerBackIv, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.pickerPhotoImage, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.pickerPhotoImage, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(R.id.pickerPhotoImage, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraintSet.applyTo(picker_whole_layout)

        constraintSet1.clone(constraintSet)

        constraintSet2.clone(constraintSet)
        constraintSet2.clear(R.id.pickerTitleBg)
        constraintSet2.connect(R.id.pickerTitleBg, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet2.setMargin(R.id.pickerTitleBg, ConstraintSet.BOTTOM, 2)

        Looper.myQueue().addIdleHandler {
            if (canLoadImage()) {
                if (imageView is PickerScaleImageView) {
                    imageView.setMinimumTileDpi(160)

                    imageView.setOnImageEventListener(DisplayOptimizeListener(imageView))
                    imageView.setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_INSIDE)
                    imageView.setImage(ImageSource.uri(Uri.fromFile(File(picItem.uri))))
                } else if (imageView is PhotoDraweeView) {

                    imageView.setPhotoUri(Uri.fromFile(File(picItem.uri)))
                }
            }
            return@addIdleHandler false
        }


        pickerBackIv.setOnClickListener { onBackPressed() }

        pickerSure.setOnClickListener {
            val broadcast = Intent()
            broadcast.action = PICKER_ACTION_MEDIA_SURE
            broadcast.putExtra(PICKER_EXTRA_PHOTO, picItem)
            sendBroadcast(broadcast)
            onBackPressed()
        }
    }


    private fun initView() {
        val layoutParams = pickerTitleBg.layoutParams

        layoutParams.height = getStatusBarHeight() + dp(48f)
        pickerTitleBg.layoutParams = layoutParams

        val theme = PhotoPicker.theme

        pickerTitleBg.setBackgroundColor(theme.titleBgColor)
        pickerBackIv.setColorFilter(theme.backIvColor)
        pickerIndexTotalTv.setTextColor(theme.titleTvColor)

        val drawable = GradientDrawable()
        drawable.setColor(theme.sendBgColor)
        drawable.cornerRadius = dp(5f).toFloat()
//        pickerSure.background = drawable
        val sendStates = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))

        val colorDrawable = GradientDrawable()
        colorDrawable.setColor(0x4f000000)
        colorDrawable.cornerRadius= dp(5f).toFloat()

        val layerDrawable = LayerDrawable(arrayOf(drawable, colorDrawable))

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(-android.R.attr.state_enabled),layerDrawable)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_enabled),drawable)
        pickerSure.background = stateListDrawable

        val sendColors = intArrayOf(theme.sendTvColorDisable, theme.sendTvColorEnable)

        val sendColorStateList = ColorStateList(sendStates, sendColors)
        pickerSure.setTextColor(sendColorStateList)

    }

}
