package com.hy.picker;

import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.davemorrissey.labs.subscaleview.PickerScaleImageView;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.PickerScaleViewTarget;
import com.picker2.model.Photo;
import com.picker2.utils.AndroidLifecycleUtils;

import java.io.File;
/**
 * Created time : 2018/8/2 8:23.
 *
 * @author HY
 */
public class PictureEditPreviewActivity extends BaseActivity {

    private View mWholeView;
    private View mToolbarTop;
    private ImageView mBtnBack;
    private TextView mBtnSend;
    //    private AppCompatRadioButton mUseOrigin;
    private PickerScaleImageView mPhotoView;
    private boolean mFullScreen;

    private Photo mPicItem;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_edit_preview);

        initView();

        if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mToolbarTop.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        }

        Intent intent = getIntent();
        mPicItem = intent.getParcelableExtra("picItem");

        if (mPicItem == null) {
            Toast.makeText(this, R.string.picker_file_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                if (AndroidLifecycleUtils.canLoadImage(PictureEditPreviewActivity.this)) {
                    String uri = mPicItem.getUri();
                    Glide.with(PictureEditPreviewActivity.this)
                            .asFile()
                            .load(new File(uri))
                            .apply(new RequestOptions()
                                    .error(R.drawable.picker_grid_image_default)
                                    .placeholder(R.drawable.picker_grid_image_default))
                            .into(new PickerScaleViewTarget(mPhotoView));

                }
                return false;
            }
        });


        mPhotoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFullScreen = !mFullScreen;
//                View decorView;
//                byte uiOptions;
                if (mFullScreen) {
//                    decorView = getWindow().getDecorView();
//                    uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//                    decorView.setSystemUiVisibility(uiOptions);
                    mToolbarTop.setVisibility(View.INVISIBLE);
                } else {
//                    CommonUtils.processMIUI(PictureEditPreviewActivity.this, mIsStatusBlack);
//                    decorView = getWindow().getDecorView();
//                    uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
//                    decorView.setSystemUiVisibility(uiOptions);

                    mToolbarTop.setVisibility(View.VISIBLE);
                }
            }
        });


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

    }


    private void initView() {
        mToolbarTop = findViewById(R.id.picker_preview_toolbar);
        mBtnBack = findViewById(R.id.picker_back);
        mBtnSend = findViewById(R.id.picker_sure);
        mWholeView = findViewById(R.id.picker_whole_layout);
        mPhotoView = findViewById(R.id.picker_photo_preview);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
//            intent.putExtra("sendOrigin", mUseOrigin.isChecked());
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

}
