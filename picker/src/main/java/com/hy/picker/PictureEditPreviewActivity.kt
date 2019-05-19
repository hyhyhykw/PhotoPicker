package com.hy.picker

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
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
class PictureEditPreviewActivity : BaseActivity() {

    private var mFullScreen = false

    private val constraintSet1 = ConstraintSet()
    private val constraintSet2 = ConstraintSet()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picker_activity_edit_preview)

        val defaultDrawable = ContextCompat.getDrawable(this, PhotoPicker.mDefaultDrawable)!!

        initView()

        val intent = intent
        val mPicItem: Photo? = intent.getParcelableExtra(EXTRA_ITEM)

        if (mPicItem == null) {
            Toast.makeText(this, R.string.picker_file_error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        val mView: View
        if (mPicItem.isLong) {
            mView = PickerScaleImageView(this)
            mView.setOnClickListener {
                mFullScreen = !mFullScreen

                val autoTransition = AutoTransition()
                autoTransition.duration = 200
                TransitionManager.beginDelayedTransition(picker_whole_layout, autoTransition)
                if (mFullScreen) {
                    constraintSet2.applyTo(picker_whole_layout)
                } else {
                    constraintSet1.applyTo(picker_whole_layout)
                }
//                if (mFullScreen) {
//                    picker_preview_toolbar.visibility = View.INVISIBLE
//                } else {
//
//                    picker_preview_toolbar.visibility = View.VISIBLE
//                }
            }
        } else {
            mView = PhotoDraweeView(this)
            val hierarchy = mView.hierarchy
            hierarchy.setFailureImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.setPlaceholderImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.actualImageScaleType = ScalingUtils.ScaleType.FIT_CENTER
            mView.setOnViewTapListener { _, _, _ ->
                mFullScreen = !mFullScreen
//                if (mFullScreen) {
//                    picker_preview_toolbar.visibility = View.INVISIBLE
//                } else {
//
//                    picker_preview_toolbar.visibility = View.VISIBLE
//                }
                val autoTransition = AutoTransition()
                autoTransition.duration = 200
                TransitionManager.beginDelayedTransition(picker_whole_layout, autoTransition)
                if (mFullScreen) {
                    constraintSet2.applyTo(picker_whole_layout)
                } else {
                    constraintSet1.applyTo(picker_whole_layout)
                }
            }
        }
        mView.id = R.id.picker_photo_image


        picker_whole_layout.addView(mView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(picker_whole_layout)
        constraintSet.constrainHeight(R.id.picker_photo_image, 0)
        constraintSet.constrainWidth(R.id.picker_photo_image, 0)
        constraintSet.connect(R.id.picker_photo_image, ConstraintSet.TOP, R.id.picker_back, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.picker_photo_image, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.picker_photo_image, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(R.id.picker_photo_image, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraintSet.applyTo(picker_whole_layout)

        constraintSet1.clone(constraintSet)

        constraintSet2.clone(constraintSet)
        constraintSet2.clear(R.id.picker_title_bg)
        constraintSet2.connect(R.id.picker_title_bg, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet2.setMargin(R.id.picker_title_bg, ConstraintSet.BOTTOM, 2)

        Looper.myQueue().addIdleHandler {
            if (canLoadImage()) {
                if (mView is PickerScaleImageView) {
                    mView.setMinimumTileDpi(160)

                    mView.setOnImageEventListener(DisplayOptimizeListener(mView))
                    mView.setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_INSIDE)
                    mView.setImage(ImageSource.uri(Uri.fromFile(File(mPicItem.uri))))
                } else if (mView is PhotoDraweeView) {

                    mView.setPhotoUri(Uri.fromFile(File(mPicItem.uri)))
                }
            }
            return@addIdleHandler false
        }


        picker_back.setOnClickListener { onBackPressed() }

        picker_sure.setOnClickListener {
            val broadcast = Intent()
            broadcast.action = PICKER_ACTION_MEDIA_SURE
            broadcast.putExtra(PICKER_EXTRA_PHOTO, mPicItem)
            sendBroadcast(broadcast)
            onBackPressed()
        }
    }


    private fun initView() {
        val layoutParams = picker_title_bg.layoutParams

        layoutParams.height = getStatusBarHeight() + dp(48f)
        picker_title_bg.layoutParams = layoutParams

        val theme = PhotoPicker.theme

        picker_title_bg.setBackgroundColor(theme.titleBgColor)
        picker_back.setColorFilter(theme.backIvColor)
        picker_index_total.setTextColor(theme.titleTvColor)

        val drawable = GradientDrawable()
        drawable.setColor(theme.sendBgColor)
        drawable.cornerRadius = dp(5f).toFloat()
        picker_sure.background = drawable

        val sendColors = intArrayOf(theme.sendTvColorDisable, theme.sendTvColorEnable)
        val sendStates = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))

        val sendColorStateList = ColorStateList(sendStates, sendColors)
        picker_sure.setTextColor(sendColorStateList)

    }

}
