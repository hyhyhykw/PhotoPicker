package com.hy.picker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.hy.picker.adapter.CrystalCategoryAdapter;
import com.hy.picker.utils.DefaultItemDecoration;

import java.io.IOException;
import java.io.InputStream;

import me.kareluo.imaging.core.CrystalCategory;

/**
 * Created time : 2018/8/27 16:42.
 *
 * @author HY
 */
public class PickerCrystalCategoryActivity extends AppCompatActivity implements CrystalCategoryAdapter.OnItemClickListener {

    private CrystalCategoryAdapter mCrystalCategoryAdapter = new CrystalCategoryAdapter();

    private boolean isOther;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_crystal);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        ImageButton ibBack = findViewById(R.id.picker_back);
        RecyclerView rvCrystal = findViewById(R.id.picker_rcy_crystal);
        rvCrystal.addItemDecoration(new DefaultItemDecoration(Color.parseColor("#f5f5f5")));
        mCrystalCategoryAdapter.setOnItemClickListener(this);
        rvCrystal.setAdapter(mCrystalCategoryAdapter);
        rvCrystal.setLayoutManager(new LinearLayoutManager(this));
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        isOther = getIntent().getBooleanExtra("other", false);
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                initData();
                return false;
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void initData() {
        String file;
        if (isOther) {
            file = "child-category.json";
        } else {
            file = "Category.json";
        }
        try {
            InputStream open = getAssets().open(file);
            byte[] bytes = new byte[open.available()];
            open.read(bytes);
            String json = new String(bytes);
            CrystalCategory category = new Gson().fromJson(json, CrystalCategory.class);
            mCrystalCategoryAdapter.reset(category.getCategory());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onClick(CrystalCategory.Category item) {
        if (item.getId() == 11) {
            startActivityForResult(new Intent(this, PickerCrystalCategoryActivity.class)
                    .putExtra("other", true), 666);
        } else {
            startActivityForResult(new Intent(this, PickerCrystalActivity.class)
                    .putExtra("id", item.getId()), 666);
        }
    }
}
