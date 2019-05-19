package com.hy.picker

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.picker_activity_crystal.*

/**
 * Created time : 2018/8/28 9:58.
 *
 * @author HY
 */
abstract class BaseListActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picker_activity_crystal)
        picker_back.setOnClickListener { onBackPressed() }

        picker_load_error.setOnClickListener { reload() }
        initView()
    }

    protected abstract fun initView()

    private fun reload() {
        picker_photo_load.visibility = View.VISIBLE
        picker_load_error.visibility = View.GONE
        initData()
    }

    protected abstract fun initData()

    fun loadSuccess() {
        if (!canLoadImage()) return
        picker_photo_load.visibility = View.GONE
        picker_load_error.visibility = View.GONE
    }

    fun loadFailed() {
        if (!canLoadImage()) return
        picker_photo_load.visibility = View.GONE
        picker_load_error.visibility = View.VISIBLE
    }
}
