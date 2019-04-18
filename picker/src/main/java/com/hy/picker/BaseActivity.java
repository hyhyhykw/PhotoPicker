package com.hy.picker;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.CommonUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created time : 2018/8/28 8:40.
 *
 * @author HY
 */
public class BaseActivity extends AppCompatActivity implements PickerConstants {

    protected boolean mIsStatusBlack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        super.onCreate(savedInstanceState);
        mIsStatusBlack = AttrsUtils.getTypeValueBoolean(this, R.attr.picker_status_black);
        CommonUtils.processMIUI(this, mIsStatusBlack);
    }
}
