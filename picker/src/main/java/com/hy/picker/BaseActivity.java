package com.hy.picker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.CommonUtils;

/**
 * Created time : 2018/8/28 8:40.
 *
 * @author HY
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isStatusBlack = AttrsUtils.getTypeValueBoolean(this, R.attr.picker_status_black);
        CommonUtils.processMIUI(this, isStatusBlack);
    }
}