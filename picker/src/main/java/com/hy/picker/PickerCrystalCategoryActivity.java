package com.hy.picker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Looper;

import com.google.gson.Gson;
import com.hy.picker.adapter.CrystalCategoryAdapter;
import com.hy.picker.core.CrystalCategory;
import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.DefaultItemDecoration;
import com.hy.picker.utils.NetworkUtils;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * Created time : 2018/8/27 16:42.
 *
 * @author HY
 */
public class PickerCrystalCategoryActivity extends BaseListActivity implements CrystalCategoryAdapter.OnItemClickListener, Constants, PickerConstants {

    private CrystalCategoryAdapter mCrystalCategoryAdapter;

    private boolean isOther;

    @Override
    protected void initView() {
        rvCrystal.addItemDecoration(new DefaultItemDecoration(Color.parseColor("#f5f5f5")));


        Drawable mDefaultDrawable = AttrsUtils.getTypeValueDrawable(this, R.attr.picker_image_default);
        if (null == mDefaultDrawable) {
            mDefaultDrawable = ContextCompat.getDrawable(this, R.drawable.picker_grid_image_default);
        }
        mCrystalCategoryAdapter = new CrystalCategoryAdapter(mDefaultDrawable);
        mCrystalCategoryAdapter.setOnItemClickListener(this);

        rvCrystal.setAdapter(mCrystalCategoryAdapter);
        rvCrystal.setLayoutManager(new LinearLayoutManager(this));

        isOther = getIntent().getBooleanExtra(EXTRA_OTHER, false);
        Looper.myQueue().addIdleHandler(() -> {
            initData();
            return false;
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void initData() {
        String url;
        if (isOther) {
            url = getString(CHILD_CATEGORY);
        } else {
            url = getString(CATEGORY);
        }
        NetworkUtils.getInstance()
                .url(url)
                .start(new NetworkUtils.TaskListener() {
                    @Override
                    public void onSuccess(String json) {
                        loadSuccess();
                        CrystalCategory category = new Gson().fromJson(json, CrystalCategory.class);
                        mCrystalCategoryAdapter.reset(category.getCategory());
                    }

                    @Override
                    public void onFailed() {
                        loadFailed();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onClick(CrystalCategory.Category item) {
        if (item.getId() == 11) {
            startActivityForResult(new Intent(this, PickerCrystalCategoryActivity.class)
                    .putExtra(EXTRA_OTHER, true), 666);
        } else {
            startActivityForResult(new Intent(this, PickerCrystalActivity.class)
                    .putExtra(EXTRA_ID, item.getId()), 666);
        }
    }
}
