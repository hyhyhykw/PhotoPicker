package com.hy.picker;


import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.hy.picker.utils.CommonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;


/**
 * Created time : 2018/8/2 8:23.
 *
 * @author HY
 */
public class PictureEditPreviewActivity extends AppCompatActivity {
    private View mWholeView;
    private View mToolbarTop;
    private View mToolbarBottom;
    private ImageButton mBtnBack;
    private TextView mBtnSend;
    //    private AppCompatRadioButton mUseOrigin;
    private AppCompatCheckBox mSelectBox;
    private PhotoView mPhotoView;
    private boolean mFullScreen;

    private PictureSelectorActivity.PicItem mPicItem;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.picker_activity_preview);
        Intent intent = getIntent();
        mPicItem = intent.getParcelableExtra("picItem");
        if (mPicItem == null) {
            Toast.makeText(this, R.string.picker_file_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initView();

        if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mToolbarTop.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        }

        mPhotoView.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                mFullScreen = !mFullScreen;
                View decorView;
                byte uiOptions;
                if (mFullScreen) {
                    if (VERSION.SDK_INT < 16) {
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    } else {
                        decorView = getWindow().getDecorView();
                        uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                        decorView.setSystemUiVisibility(uiOptions);
                    }

                    mToolbarTop.setVisibility(View.INVISIBLE);
                    mToolbarBottom.setVisibility(View.INVISIBLE);
                } else {
                    if (VERSION.SDK_INT < 16) {
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    } else {
                        decorView = getWindow().getDecorView();
                        uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                        decorView.setSystemUiVisibility(uiOptions);
                    }

                    mToolbarTop.setVisibility(View.VISIBLE);
                    mToolbarBottom.setVisibility(View.VISIBLE);
                }
            }
        });


        String uri = mPicItem.getUri();
        Glide.with(this)
                .load(new File(uri))
                .apply(new RequestOptions()
                        .error(R.drawable.picker_grid_image_default)
                        .placeholder(R.drawable.picker_grid_image_default))
                .into(mPhotoView);

        mWholeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mBtnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mBtnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                setResult(RESULT_OK);
                finish();
            }
        });


        mSelectBox.setText(R.string.picker_picprev_select);
        mSelectBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPicItem.setSelected(isChecked);
                if (buttonView.isPressed()) {
                    updateToolbar();
                }
            }
        });

        updateToolbar();
    }


    private void initView() {
        mToolbarTop = findViewById(R.id.picker_preview_toolbar);
        mBtnBack = findViewById(R.id.picker_back);
        mBtnSend = findViewById(R.id.picker_sure);
        mWholeView = findViewById(R.id.picker_whole_layout);
        mToolbarBottom = findViewById(R.id.picker_bottom_bar);
//        mUseOrigin = findViewById(R.id.origin_check);
        mSelectBox = findViewById(R.id.picker_select_check);
        mPhotoView = findViewById(R.id.picker_photo_preview);
    }

    protected void onResume() {
        super.onResume();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
//            intent.putExtra("sendOrigin", mUseOrigin.isChecked());
            setResult(RESULT_OK, intent);
        }

        return super.onKeyDown(keyCode, event);
    }

    private void updateToolbar() {
        mBtnSend.setEnabled(mPicItem.isSelected());
    }

}
