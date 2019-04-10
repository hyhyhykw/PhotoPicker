package com.hy.picker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.davemorrissey.labs.subscaleview.PickerScaleImageView;
import com.github.chrisbanes.photoview.PhotoView;
import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.PickerScaleViewTarget;
import com.picker2.model.Photo;
import com.picker2.utils.AndroidLifecycleUtils;

import androidx.core.content.ContextCompat;

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
    private PhotoView mPhotoView;
    private PickerScaleImageView mLongIv;
    private boolean mFullScreen;

    private Photo mPicItem;
    private Drawable mDefaultDrawable;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_edit_preview);

        mDefaultDrawable = AttrsUtils.getTypeValueDrawable(this, R.attr.picker_image_default);
        if (null == mDefaultDrawable) {
            mDefaultDrawable = ContextCompat.getDrawable(this, R.drawable.picker_grid_image_default);
        }

        initView();

        if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mToolbarTop.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        }

        Intent intent = getIntent();
        mPicItem = intent.getParcelableExtra(EXTRA_ITEM);

        if (mPicItem == null) {
            Toast.makeText(this, R.string.picker_file_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        Looper.myQueue().addIdleHandler(() -> {
            if (AndroidLifecycleUtils.canLoadImage(PictureEditPreviewActivity.this)) {
                String uri = mPicItem.getUri();
                if (mPicItem.isGif()) {
                    mPhotoView.setVisibility(View.VISIBLE);
                    mLongIv.setVisibility(View.GONE);
                    Glide.with(PictureEditPreviewActivity.this)
                            .asGif()
                            .load(uri)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .apply(new RequestOptions()
                                    .override(480, 800)
                                    .priority(Priority.HIGH)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .error(mDefaultDrawable)
                                    .placeholder(mDefaultDrawable))
                            .into(mPhotoView);
                } else {
                    if (mPicItem.isLong()) {
                        mPhotoView.setVisibility(View.GONE);
                        mLongIv.setVisibility(View.VISIBLE);
                        Glide.with(PictureEditPreviewActivity.this)
                                .asBitmap()
                                .load(uri)
                                .transition(BitmapTransitionOptions.withCrossFade())
                                .apply(new RequestOptions()
                                        .error(mDefaultDrawable)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .placeholder(mDefaultDrawable))
                                .into(new PickerScaleViewTarget(mLongIv));
                    } else {
                        mPhotoView.setVisibility(View.VISIBLE);
                        mLongIv.setVisibility(View.GONE);
                        Glide.with(PictureEditPreviewActivity.this)
                                .asBitmap()
                                .transition(BitmapTransitionOptions.withCrossFade())
                                .load(uri)
                                .apply(new RequestOptions()
                                        .error(mDefaultDrawable)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .placeholder(mDefaultDrawable))
                                .into(mPhotoView);
                    }
                }


            }
            return false;
        });


        mPhotoView.setOnClickListener(v -> {
            mFullScreen = !mFullScreen;
            if (mFullScreen) {
                mToolbarTop.setVisibility(View.INVISIBLE);
            } else {

                mToolbarTop.setVisibility(View.VISIBLE);
            }
        });


        mWholeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mBtnBack.setOnClickListener(v -> onBackPressed());

        mBtnSend.setOnClickListener(v -> {
            Intent broadcast = new Intent();
            broadcast.setAction(PICKER_ACTION_MEDIA_SURE);
            broadcast.putExtra(PICKER_EXTRA_PHOTO, mPicItem);
            sendBroadcast(broadcast);
            onBackPressed();
        });

    }


    private void initView() {
        mToolbarTop = findViewById(R.id.picker_preview_toolbar);
        mBtnBack = findViewById(R.id.picker_back);
        mBtnSend = findViewById(R.id.picker_sure);
        mWholeView = findViewById(R.id.picker_whole_layout);
        mPhotoView = findViewById(R.id.picker_photo_preview);
        mLongIv = findViewById(R.id.picker_long_photo);
        int enableColor = AttrsUtils.getTypeValueColor(this, R.attr.picker_send_color_enable);
        int disableColor = AttrsUtils.getTypeValueColor(this, R.attr.picker_send_color_disable);


        int[] colors = {
                disableColor,
                enableColor
        };
        int states[][] = new int[][]{
                new int[]{
                        -android.R.attr.state_enabled
                },
                new int[]{
                        android.R.attr.state_enabled
                }
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);

        mBtnSend.setTextColor(colorStateList);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
