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
import kotlinx.android.synthetic.main.picker_activity_list.*

/**
 * Created time : 2018/8/27 16:42.
 *
 * @author HY
 */
class PickerCrystalCategoryActivity : BaseListActivity() {

    private val crystalCategoryAdapter by lazy {
        val defaultDrawable = ContextCompat.getDrawable(this, PhotoPicker.defaultDrawable)!!
        CrystalCategoryAdapter(defaultDrawable)
    }

    private val isOther by lazy {
        intent.getBooleanExtra(EXTRA_OTHER, false)
    }

    override fun initView() {
        pickerRcy.addItemDecoration(DefaultItemDecoration(Color.parseColor("#f5f5f5")))


//        val defaultDrawable = ContextCompat.getDrawable(this, PhotoPicker.defaultDrawable)!!
//        crystalCategoryAdapter = CrystalCategoryAdapter(defaultDrawable)
        crystalCategoryAdapter.setOnItemClickListener { item ->
            if (item.id == 11) {
                startActivityForResult(Intent(this, PickerCrystalCategoryActivity::class.java)
                        .putExtra(EXTRA_OTHER, true), 666)
            } else {
                startActivityForResult(Intent(this, PickerCrystalActivity::class.java)
                        .putExtra(EXTRA_ID, item.id), 666)
            }
        }

        pickerRcy.adapter = crystalCategoryAdapter
        pickerRcy.layoutManager = LinearLayoutManager(this)

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
        NetworkUtils.instance
                .url(url)
                .start(object : NetworkUtils.TaskListener {
                    override fun onSuccess(json: String) {
                        loadSuccess()
                        val category = Gson().fromJson(json, CrystalCategory::class.java)
                        crystalCategoryAdapter.reset(category.category)
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
