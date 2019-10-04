package com.hy.picker

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.picker_activity_list.*

/**
 * Created time : 2018/8/28 9:58.
 *
 * @author HY
 */
abstract class PickerBaseListActivity : PickerBaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picker_activity_list)
        val layoutParams = pickerTitleBg.layoutParams
        layoutParams.height=dp(48f)+getStatusBarHeight()
        pickerTitleBg.layoutParams=layoutParams
        pickerBackIv.setOnClickListener { onBackPressed() }

        pickerLoadError.setOnClickListener { reload() }
        initView()
    }

    protected abstract fun initView()

    private fun reload() {
        pickerPhotoLoad.visibility = View.VISIBLE
        pickerLoadError.visibility = View.GONE
        initData()
    }

    protected abstract fun initData()

    fun loadSuccess() {
        if (!canLoadImage()) return
        pickerPhotoLoad.visibility = View.GONE
        pickerLoadError.visibility = View.GONE
    }

    fun loadFailed() {
        if (!canLoadImage()) return
        pickerPhotoLoad.visibility = View.GONE
        pickerLoadError.visibility = View.VISIBLE
    }
}
