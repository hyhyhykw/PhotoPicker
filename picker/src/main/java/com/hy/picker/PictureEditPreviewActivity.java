package com.hy.picker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.DisplayOptimizeListener;
import com.hy.picker.view.ImageSource;
import com.hy.picker.view.PickerScaleImageView;
import com.picker2.model.Photo;
import com.picker2.utils.AndroidLifecycleUtils;

import java.io.File;
import java.lang.ref.WeakReference;

import androidx.core.content.ContextCompat;
import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created time : 2018/8/2 8:23.
 *
 * @author HY
 */
public class PictureEditPreviewActivity extends BaseActivity {

    private RelativeLayout mWholeView;
    private View mToolbarTop;
    private ImageView mBtnBack;
    private TextView mBtnSend;
    //    private AppCompatRadioButton mUseOrigin;
//    private PhotoView mPhotoView;
//    private BigImageView mLongIv;
    private boolean mFullScreen;

    private Photo mPicItem;
    private Drawable mDefaultDrawable;
    private View mView;

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


        if (mPicItem.isLong()) {
            mView = new PickerScaleImageView(this);
            mView.setOnClickListener(v -> {
                mFullScreen = !mFullScreen;
                if (mFullScreen) {
                    mToolbarTop.setVisibility(View.INVISIBLE);
                } else {

                    mToolbarTop.setVisibility(View.VISIBLE);
                }
            });
        } else {
            mView = new PhotoDraweeView(this);
            GenericDraweeHierarchy hierarchy = ((PhotoDraweeView) mView).getHierarchy();
            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
            ((PhotoDraweeView) mView).setOnViewTapListener((view, x, y) -> {
                mFullScreen = !mFullScreen;
                if (mFullScreen) {
                    mToolbarTop.setVisibility(View.INVISIBLE);
                } else {

                    mToolbarTop.setVisibility(View.VISIBLE);
                }
            });
        }
        mWholeView.addView(mView, 0, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Looper.myQueue().addIdleHandler(new MyIdleHandler(this));

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

    private static final class MyIdleHandler implements MessageQueue.IdleHandler {
        private WeakReference<PictureEditPreviewActivity> mReference;

        MyIdleHandler(PictureEditPreviewActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public boolean queueIdle() {
            if (null == mReference) return false;

            PictureEditPreviewActivity activity = mReference.get();
            if (AndroidLifecycleUtils.canLoadImage(activity)) {
                activity.init();
            }
            return false;
        }
    }

    private void init() {
        if (mView instanceof PickerScaleImageView) {
            PickerScaleImageView imageView = (PickerScaleImageView) mView;
            imageView.setMinimumTileDpi(160);

            imageView.setOnImageEventListener(new DisplayOptimizeListener(imageView));
            imageView.setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_INSIDE);
            imageView.setImage(ImageSource.uri(Uri.fromFile(new File(mPicItem.getUri()))));
        } else if (mView instanceof PhotoDraweeView) {
            PhotoDraweeView imageView = (PhotoDraweeView) mView;

            imageView.setPhotoUri(Uri.fromFile(new File(mPicItem.getUri())));
        }
    }

    private void initView() {
        mToolbarTop = findViewById(R.id.picker_preview_toolbar);
        mBtnBack = findViewById(R.id.picker_back);
        mBtnSend = findViewById(R.id.picker_sure);
        mWholeView = findViewById(R.id.picker_whole_layout);
//        mPhotoView = findViewById(R.id.picker_photo_preview);
//        mLongIv = findViewById(R.id.picker_long_photo);
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
