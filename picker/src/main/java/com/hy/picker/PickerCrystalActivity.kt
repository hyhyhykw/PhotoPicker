package com.hy.picker

import android.content.Intent
import android.graphics.Color
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.hy.picker.adapter.CrystalAdapter
import com.hy.picker.core.CrystalResult
import com.hy.picker.utils.DefaultItemDecoration
import com.hy.picker.utils.NetworkUtils
import kotlinx.android.synthetic.main.picker_activity_crystal.*

/**
 * Created time : 2018/8/27 16:42.
 *
 * @author HY
 */
class PickerCrystalActivity : BaseListActivity() {


    private lateinit var mCrystalAdapter: CrystalAdapter
    private var cate = "ali"

    override fun initView() {
        picker_rcy_crystal.addItemDecoration(DefaultItemDecoration(Color.parseColor("#f5f5f5")))
        val id = intent.getIntExtra(EXTRA_ID, 1)
        cate = getCateFromId(id)

        val defaultDrawable = ContextCompat.getDrawable(this,PhotoPicker.mDefaultDrawable)!!

        mCrystalAdapter = CrystalAdapter(cate, defaultDrawable)
        mCrystalAdapter.setOnItemClickListener { exist->
            val intent = Intent()
            intent.putExtra(EXTRA_PATH, exist.file.absolutePath)
            setResult(RESULT_OK, intent)
            finish()
        }

        picker_rcy_crystal.adapter = mCrystalAdapter
        picker_rcy_crystal.layoutManager = GridLayoutManager(this, 3)

        Looper.myQueue().addIdleHandler {
            initData()
            false
        }
    }

    private fun getCateFromId(id: Int): String {
        return when (id) {
            1 -> "ali"
            2 -> "ice"
            3 -> "dadatu"
            4 -> "jiafa"
            5 -> "meimao"
            6 -> "mocmoc"
            7 -> "qingrenjie"
            8 -> "wenshen"
            9 -> "yanjing"
            10 -> "yifu"
            12 -> "shipin-egao"
            13 -> "shipin-feizhuliu"
            14 -> "shipin-jieri"
            15 -> "shipin-katong"
            16 -> "shipin-qipao"
            17 -> "shipin-xin"
            18 -> "shipin-zhedang"
            else -> "ali"
        }
    }

    override fun initData() {
        NetworkUtils.getInstance()
                .url("$JSON_BASE$cate.json")
                .start(object : NetworkUtils.TaskListener {
                    override fun onSuccess(json: String) {
                        loadSuccess()
                        val result = Gson().fromJson(json, CrystalResult::class.java)
                        mCrystalAdapter.reset(result.data)
                    }

                    override fun onFailed() {
                        loadFailed()
                    }
                })
    }

}
