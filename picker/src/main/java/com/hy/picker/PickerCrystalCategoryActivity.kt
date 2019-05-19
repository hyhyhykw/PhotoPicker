package com.hy.picker

import android.content.Intent
import android.graphics.Color
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.hy.picker.adapter.CrystalCategoryAdapter
import com.hy.picker.core.CrystalCategory
import com.hy.picker.utils.DefaultItemDecoration
import com.hy.picker.utils.NetworkUtils
import kotlinx.android.synthetic.main.picker_activity_crystal.*

/**
 * Created time : 2018/8/27 16:42.
 *
 * @author HY
 */
class PickerCrystalCategoryActivity : BaseListActivity() {

    private lateinit var mCrystalCategoryAdapter: CrystalCategoryAdapter

    private var isOther: Boolean = false

    override fun initView() {
        picker_rcy_crystal.addItemDecoration(DefaultItemDecoration(Color.parseColor("#f5f5f5")))


        val mDefaultDrawable = ContextCompat.getDrawable(this, PhotoPicker.mDefaultDrawable)!!
        mCrystalCategoryAdapter = CrystalCategoryAdapter(mDefaultDrawable)
        mCrystalCategoryAdapter.setOnItemClickListener { item ->
            if (item.id == 11) {
                startActivityForResult(Intent(this, PickerCrystalCategoryActivity::class.java)
                        .putExtra(EXTRA_OTHER, true), 666)
            } else {
                startActivityForResult(Intent(this, PickerCrystalActivity::class.java)
                        .putExtra(EXTRA_ID, item.id), 666)
            }
        }

        picker_rcy_crystal.adapter = mCrystalCategoryAdapter
        picker_rcy_crystal.layoutManager = LinearLayoutManager(this)

        isOther = intent.getBooleanExtra(EXTRA_OTHER, false)
        Looper.myQueue().addIdleHandler {
            initData()
            false
        }
    }

    override fun initData() {
        val url = if (isOther) {
            getString(CHILD_CATEGORY)
        } else {
            getString(CATEGORY)
        }
        NetworkUtils.getInstance()
                .url(url)
                .start(object : NetworkUtils.TaskListener {
                    override fun onSuccess(json: String) {
                        loadSuccess()
                        val category = Gson().fromJson(json, CrystalCategory::class.java)
                        mCrystalCategoryAdapter.reset(category.category)
                    }

                    override fun onFailed() {
                        loadFailed()
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            setResult(RESULT_OK, data)
            finish()
        }
    }

}
