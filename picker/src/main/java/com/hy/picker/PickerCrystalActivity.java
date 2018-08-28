package com.hy.picker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.hy.picker.adapter.CrystalAdapter;
import com.hy.picker.utils.DefaultItemDecoration;

import java.io.IOException;
import java.io.InputStream;

import me.kareluo.imaging.core.CrystalResult;
import me.kareluo.imaging.core.ExistBean;

/**
 * Created time : 2018/8/27 16:42.
 *
 * @author HY
 */
public class PickerCrystalActivity extends BaseActivity implements CrystalAdapter.OnItemClickListener {


    private CrystalAdapter mCrystalAdapter;
    private String cate;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_crystal);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        ImageView ibBack = findViewById(R.id.picker_back);
        RecyclerView rvCrystal = findViewById(R.id.picker_rcy_crystal);
        rvCrystal.addItemDecoration(new DefaultItemDecoration(Color.parseColor("#f5f5f5")));
        int id = getIntent().getIntExtra("id", 1);
        cate = getCateFromId(id);
        mCrystalAdapter = new CrystalAdapter(cate);
        mCrystalAdapter.setOnItemClickListener(this);
        rvCrystal.setAdapter(mCrystalAdapter);
        rvCrystal.setLayoutManager(new GridLayoutManager(this,3));
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                initData();
                return false;
            }
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

    private void initData() {
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream open = getAssets().open(cate + ".json");
                    byte[] bytes = new byte[open.available()];
                    //noinspection ResultOfMethodCallIgnored
                    open.read(bytes);
                    String json = new String(bytes);
                    final CrystalResult result = new Gson().fromJson(json, CrystalResult.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCrystalAdapter.reset(result.getData());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onClick(ExistBean exist) {
        Intent intent = new Intent();
        intent.putExtra("path", exist.getFile().getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }
}
