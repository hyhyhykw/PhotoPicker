package com.hy.picker;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created time : 2018/8/28 9:58.
 *
 * @author HY
 */
public abstract class BaseListActivity extends BaseActivity implements Constants {

    protected ImageView ibBack;
    protected RecyclerView rvCrystal;
    protected LinearLayout mLytLoad;
    protected LinearLayout mLytLoadError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_crystal);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        ibBack = findViewById(R.id.picker_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        rvCrystal = findViewById(R.id.picker_rcy_crystal);
        mLytLoad = findViewById(R.id.picker_photo_load);
        mLytLoadError = findViewById(R.id.picker_load_error);
        mLytLoadError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });
        initView();
    }

    protected abstract void initView();

    protected void reload() {
        mLytLoad.setVisibility(View.VISIBLE);
        mLytLoadError.setVisibility(View.GONE);
        initData();
    }

    protected abstract void initData();

    public void loadSuccess() {
        mLytLoad.setVisibility(View.GONE);
        mLytLoadError.setVisibility(View.GONE);
    }

    public void loadFailed() {
        mLytLoad.setVisibility(View.GONE);
        mLytLoadError.setVisibility(View.VISIBLE);
    }
}
