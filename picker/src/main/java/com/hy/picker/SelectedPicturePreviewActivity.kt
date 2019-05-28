package com.hy.picker

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.hy.picker.adapter.PreviewAdapter
import com.hy.picker.model.Photo
import kotlinx.android.synthetic.main.picker_activity_selected_preview.*
import java.util.*

/**
 * Created time : 2018/12/28 16:38.
 *
 * @author HY
 */
class SelectedPicturePreviewActivity : BaseActivity() {

    private var mCurrentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picker_activity_selected_preview)
        val layoutParams = pickerBackIv.layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.topMargin = getStatusBarHeight()
        pickerBackIv.layoutParams = layoutParams

        val theme = PhotoPicker.theme

        pickerTitleBg.setBackgroundColor(theme.titleBgColor)
        pickerBackIv.setColorFilter(theme.backIvColor)
        pickerIndexTotalTv.setTextColor(theme.titleTvColor)

        val intent = intent
        mCurrentIndex = intent.getIntExtra(EXTRA_INDEX, 0)
        val mItemList: ArrayList<Photo> = intent.getParcelableArrayListExtra(EXTRA_ITEMS)

        pickerIndexTotalTv.text = String.format(Locale.getDefault(), "%d/%d", mCurrentIndex + 1, mItemList.size)

        pickerBackIv.setOnClickListener { onBackPressed() }

        val defaultDrawable = ContextCompat.getDrawable(this, PhotoPicker.mDefaultDrawable)!!
        val adapter2 = PreviewAdapter(defaultDrawable)
        pickerVpgPreview.adapter = adapter2

        adapter2.setOnItemClickListener { finish() }
        adapter2.reset(mItemList)

        pickerVpgPreview.setCurrentItem(mCurrentIndex, false)
        pickerVpgPreview.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mCurrentIndex = position
                pickerIndexTotalTv.text = String.format(Locale.getDefault(), "%d/%d", position + 1, mItemList.size)

            }
        })
    }


}
