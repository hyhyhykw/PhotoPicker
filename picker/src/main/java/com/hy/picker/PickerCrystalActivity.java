package com.hy.picker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Looper;

import com.google.gson.Gson;
import com.hy.picker.adapter.CrystalAdapter;
import com.hy.picker.core.CrystalResult;
import com.hy.picker.core.ExistBean;
import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.DefaultItemDecoration;
import com.hy.picker.utils.NetworkUtils;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

/**
 * Created time : 2018/8/27 16:42.
 *
 * @author HY
 */
public class PickerCrystalActivity extends BaseListActivity implements CrystalAdapter.OnItemClickListener {


    private CrystalAdapter mCrystalAdapter;
    private String cate;

    @Override
    protected void initView() {
        rvCrystal.addItemDecoration(new DefaultItemDecoration(Color.parseColor("#f5f5f5")));
        int id = getIntent().getIntExtra(EXTRA_ID, 1);
        cate = getCateFromId(id);

        Drawable defaultDrawable = AttrsUtils.getTypeValueDrawable(this, R.attr.picker_image_default);
        if (null == defaultDrawable) {
            defaultDrawable = ContextCompat.getDrawable(this, R.drawable.picker_grid_image_default);
        }

        mCrystalAdapter = new CrystalAdapter(cate, defaultDrawable);
        mCrystalAdapter.setOnItemClickListener(this);
        rvCrystal.setAdapter(mCrystalAdapter);
        rvCrystal.setLayoutManager(new GridLayoutManager(this, 3));

        Looper.myQueue().addIdleHandler(() -> {
            initData();
            return false;
        });
    }

    private String getCateFromId(int id) {
        switch (id) {
            case 1:
            default:
                return "ali";
            case 2:
                return "ice";
            case 3:
                return "dadatu";
            case 4:
                return "jiafa";
            case 5:
                return "meimao";
            case 6:
                return "mocmoc";
            case 7:
                return "qingrenjie";
            case 8:
                return "wenshen";
            case 9:
                return "yanjing";
            case 10:
                return "yifu";
            case 12:
                return "shipin-egao";
            case 13:
                return "shipin-feizhuliu";
            case 14:
                return "shipin-jieri";
            case 15:
                return "shipin-katong";
            case 16:
                return "shipin-qipao";
            case 17:
                return "shipin-xin";
            case 18:
                return "shipin-zhedang";
        }
    }

    protected void initData() {
        NetworkUtils.getInstance()
                .url(JSON_BASE + cate + ".json")
                .start(new NetworkUtils.TaskListener() {
                    @Override
                    public void onSuccess(String json) {
                        loadSuccess();
                        final CrystalResult result = new Gson().fromJson(json, CrystalResult.class);
                        mCrystalAdapter.reset(result.getData());
                    }

                    @Override
                    public void onFailed() {
                        loadFailed();
                    }
                });
    }

    @Override
    public void onClick(ExistBean exist) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PATH, exist.getFile().getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }
}
