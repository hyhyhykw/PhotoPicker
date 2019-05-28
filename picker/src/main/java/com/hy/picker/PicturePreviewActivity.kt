package com.hy.picker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.hy.picker.adapter.PreviewAdapter
import com.hy.picker.model.Photo
import com.hy.picker.utils.CommonUtils
import com.hy.picker.utils.MediaListHolder
import kotlinx.android.synthetic.main.picker_activity_preview.*
import java.io.File
import java.util.*


/**
 * Created time : 2018/8/2 8:23.
 *
 * @author HY
 */
class PicturePreviewActivity : BaseActivity() {
    private lateinit var mItemList: ArrayList<Photo>

    private var mCurrentIndex = 0
    private var mFullScreen = false

    private var max = 9

    private val mPreviewReceiver = PreviewReceiver()
    private var mIsPreview = false

    private lateinit var mPreviewAdapter: PreviewAdapter


    private val mConstraintSet1 = ConstraintSet()
    private val mConstraintSet2 = ConstraintSet()


    private val selNum: Int
        get() {
            var num = 0
            for (photo in MediaListHolder.selectPhotos) {
                if (photo.isSelected) {
                    num += 1
                }
            }
            return num
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picker_activity_preview)
        val intentFilter = IntentFilter()
        intentFilter.addAction(PICKER_ACTION_MEDIA_ADD)
        registerReceiver(mPreviewReceiver, intentFilter)

        initView()
        //    private int mStartIndex;
        val defaultDrawable = ContextCompat.getDrawable(this, PhotoPicker.mDefaultDrawable)!!

        val statusBarHeight = getStatusBarHeight()
        val height = statusBarHeight + dp(48f)

        mConstraintSet1.clone(picker_whole_layout)
        mConstraintSet1.constrainHeight(R.id.pickerTitleBg, height)
        mConstraintSet1.applyTo(picker_whole_layout)

        mConstraintSet2.clone(picker_whole_layout)
        mConstraintSet2.constrainHeight(R.id.pickerTitleBg, height)
        mConstraintSet2.clear(R.id.pickerTitleBg, ConstraintSet.TOP)
        mConstraintSet2.connect(R.id.pickerTitleBg, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        mConstraintSet2.clear(R.id.pickerBottomBg, ConstraintSet.BOTTOM)
        mConstraintSet2.connect(R.id.pickerBottomBg, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        mConstraintSet2.setMargin(R.id.pickerTitleBg, ConstraintSet.BOTTOM, 2)

        val intent = intent
        max = intent.getIntExtra(EXTRA_MAX, 9)
        val isGif = intent.getBooleanExtra(EXTRA_IS_GIF, false)
        pickerEditTv.visibility = if (isGif) View.GONE else View.VISIBLE
        mIsPreview = intent.getBooleanExtra(EXTRA_IS_PREVIEW, false)
        mCurrentIndex = intent.getIntExtra(EXTRA_INDEX, 0)

        mItemList = if (mIsPreview) MediaListHolder.selectPhotos else MediaListHolder.currentPhotos

        pickerIndexTotalTv.text = String.format(Locale.getDefault(), "%d/%d", mCurrentIndex + 1, mItemList.size)

//        picker_whole_layout.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        pickerBackIv.setOnClickListener { onBackPressed() }

        pickerSend.setOnClickListener {
            sendBroadcast(Intent(PICKER_ACTION_MEDIA_SEND))
            finish()
        }


        pickerSelectorCheck.setText(R.string.picker_picprev_select)

        pickerSelectorCheck.isChecked = mItemList[mCurrentIndex].isSelected
        pickerSelectorCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked && selNum == max) {
                    pickerSelectorCheck.isChecked = false


                    Toast.makeText(this@PicturePreviewActivity,
                            resources.getQuantityString(R.plurals.picker_picsel_selected_max, 1, max), Toast.LENGTH_SHORT).show()
                } else {

                    if (mItemList.isEmpty()) return@setOnCheckedChangeListener

                    val photo = mItemList[mCurrentIndex]
                    sendBroadcast(Intent(PICKER_ACTION_MEDIA_SELECT)
                            .putExtra(PICKER_EXTRA_PHOTO, photo))

                    if (isChecked) {
                        photo.isSelected = true
                        MediaListHolder.selectPhotos.add(photo)
                    } else {
                        photo.isSelected = false
                        if (!mIsPreview) {
                            MediaListHolder.selectPhotos.remove(photo)
                        }
                    }
                    updateToolbar()
                }
            }
        }
        mPreviewAdapter = PreviewAdapter(defaultDrawable)
        mPreviewAdapter.setOnItemClickListener {
            if (!canLoadImage()) return@setOnItemClickListener

            mFullScreen = !mFullScreen
            val autoTransition = AutoTransition()
            autoTransition.duration = 200
            TransitionManager.beginDelayedTransition(picker_whole_layout, autoTransition)
            if (mFullScreen) {
                mConstraintSet2.applyTo(picker_whole_layout)
            } else {
                mConstraintSet1.applyTo(picker_whole_layout)
            }
        }

        pickerVpgPreview.adapter = mPreviewAdapter

        //        mViewPager.setOffscreenPageLimit(1);

        mPreviewAdapter.reset(mItemList)
        pickerVpgPreview.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mCurrentIndex = position
                pickerIndexTotalTv.text = String.format(Locale.getDefault(), "%d/%d", position + 1, mItemList.size)

                val photo = mItemList[position]
                pickerSelectorCheck.isChecked = photo.isSelected
                pickerEditTv.visibility = if (photo.isGif) View.GONE else View.VISIBLE
            }
        })
        pickerVpgPreview.setCurrentItem(mCurrentIndex, false)


        pickerEditTv.setOnClickListener {
            val photo = mItemList[pickerVpgPreview.currentItem]
            toEdit(Uri.fromFile(File(photo.uri)))
        }
        updateToolbar()
    }

    private fun toEdit(uri: Uri) {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!path.exists()) {
            val mkdirs = path.mkdirs()
            if (BuildConfig.DEBUG) {
                Log.d("TAG", "文件夹：" + path + "创建" + if (mkdirs) "成功" else "失败")
            }

        }

        val name = "IMG-EDIT-" + CommonUtils.format(Date(), "yyyy-MM-dd-HHmmss") + ".jpg"
        val editFile = File(path, name)

        startActivity(Intent(this, IMGEditActivity::class.java)
                .putExtra(EXTRA_IMAGE_URI, uri)
                .putExtra(EXTRA_IMAGE_SAVE_PATH, editFile.absolutePath)
                .putExtra(EXTRA_MAX, max))
    }

    override fun onBackPressed() {
        if (mIsPreview) {
            setResult(RESULT_OK)
        }
        super.onBackPressed()

    }

    override fun finish() {
        if (mIsPreview) {
            setResult(RESULT_OK)
        }
        super.finish()

    }

    override fun onDestroy() {
        unregisterReceiver(mPreviewReceiver)
        super.onDestroy()
    }

    private fun initView() {
        val theme = PhotoPicker.theme

        pickerTitleBg.setBackgroundColor(theme.titleBgColor)
        pickerBackIv.setColorFilter(theme.backIvColor)
        pickerIndexTotalTv.setTextColor(theme.titleTvColor)

        val drawable = GradientDrawable()
        drawable.setColor(theme.sendBgColor)
        drawable.cornerRadius = dp(5f).toFloat()
        val sendStates = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))

        val colorDrawable = GradientDrawable()
        colorDrawable.setColor(0x4f000000)
        colorDrawable.cornerRadius= dp(5f).toFloat()

        val layerDrawable = LayerDrawable(arrayOf(drawable, colorDrawable))

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(-android.R.attr.state_enabled),layerDrawable)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_enabled),drawable)
        pickerSend.background = stateListDrawable


        val sendColors = intArrayOf(theme.sendTvColorDisable, theme.sendTvColorEnable)

        val sendColorStateList = ColorStateList(sendStates, sendColors)
        pickerSend.setTextColor(sendColorStateList)


        pickerBottomBg.setBackgroundColor(theme.titleBgColor)

        val previewColors = intArrayOf(theme.previewTvColorDisable, theme.previewTvColorEnable)
        val previewStates = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))

        val previewColorStateList = ColorStateList(previewStates, previewColors)


        pickerEditTv.setTextColor(previewColorStateList)


        pickerSelectorCheck.setTextColor(theme.titleTvColor)

        val states = arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))
        val colors = intArrayOf(PhotoPicker.theme.sendBgColor, PhotoPicker.theme.sendBgColor)
        pickerSelectorCheck.supportButtonTintList = ColorStateList(states, colors)
    }

    private fun updateToolbar() {

        val selNum = selNum

        pickerIndexTotalTv.text = String.format(Locale.getDefault(), "%d/%d", mCurrentIndex + 1, mItemList.size)

        pickerSelectorCheck.isChecked = mItemList[mCurrentIndex].isSelected
        if (mItemList.size == 1 && selNum == 0) {
            pickerSend.setText(R.string.picker_picsel_toolbar_send)
            pickerSend.isEnabled = false
        } else {
            if (selNum == 0) {
                pickerSend.setText(R.string.picker_picsel_toolbar_send)
                pickerSend.isEnabled = false
            } else if (selNum <= max) {
                pickerSend.isEnabled = true
                pickerSend.text = resources.getString(R.string.picker_picsel_toolbar_send_num, selNum, max)
            }
        }
    }


    inner class PreviewReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action ?: return

            val photo = intent.getParcelableExtra<Photo>(PICKER_EXTRA_PHOTO)
            when (action) {
                PICKER_ACTION_MEDIA_ADD -> {
                    runOnUiThread {
                        mPreviewAdapter.add(0, photo)

                        updateToolbar()
                    }

                }
            }
        }
    }
}
