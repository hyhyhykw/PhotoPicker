package com.hy.picker;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.davemorrissey.labs.subscaleview.PickerScaleImageView;
import com.github.chrisbanes.photoview.PhotoView;
import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.PickerScaleViewTarget;
import com.hy.picker.view.HackyViewPager;
import com.picker2.model.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * Created time : 2018/12/28 16:38.
 *
 * @author HY
 */
public class SelectedPicturePreviewActivity extends BaseActivity {
    private TextView mIndexTotal;
    private View mToolbarTop;
    private ImageView mBtnBack;
    private HackyViewPager mViewPager;

    private int mCurrentIndex;
    private ArrayList<Photo> mItemList;

    private Drawable mDefaultDrawable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_selected_preview);
        initView();
        mDefaultDrawable = AttrsUtils.getTypeValueDrawable(this, R.attr.picker_image_default);
        if (null == mDefaultDrawable) {
            mDefaultDrawable = ContextCompat.getDrawable(this, R.drawable.picker_grid_image_default);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mToolbarTop.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        }
        Intent intent = getIntent();
        mCurrentIndex = intent.getIntExtra("index", 0);
        mItemList = intent.getParcelableArrayListExtra("items");

        mIndexTotal.setText(String.format(Locale.getDefault(), "%d/%d", mCurrentIndex + 1, mItemList.size()));

        mBtnBack.setOnClickListener(v -> onBackPressed());

        mViewPager.setAdapter(new PreviewAdapter());
        mViewPager.setCurrentItem(mCurrentIndex);
        mViewPager.setOffscreenPageLimit(1);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                mCurrentIndex = position;
                mIndexTotal.setText(String.format(Locale.getDefault(), "%d/%d", position + 1, mItemList.size()));

            }

            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private class PreviewAdapter extends PagerAdapter {
        private PreviewAdapter() {
        }

        public int getCount() {
            return mItemList.size();
        }

        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Photo photo = mItemList.get(position);
            if (photo.isGif()) {
                final PhotoView imageView = new PhotoView(container.getContext());
                imageView.setOnClickListener(v -> finish());

                String uri = photo.getUri();
                Glide.with(container.getContext())
                        .load(new File(uri))
                        .apply(new RequestOptions()
                                .error(mDefaultDrawable)
                                .override(480, 800)
                                .priority(Priority.HIGH)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .placeholder(mDefaultDrawable))
                        .into(imageView);
                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return imageView;

            } else {

                String uri = photo.getUri();

                if (photo.isLong()) {
                    PickerScaleImageView scaleImageView = new PickerScaleImageView(container.getContext());
                    Glide.with(container)
                            .asBitmap()
                            .load(uri)
                            .apply(new RequestOptions()
                                    .error(mDefaultDrawable)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(mDefaultDrawable))
                            .into(new PickerScaleViewTarget(scaleImageView));

                    container.addView(scaleImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    return scaleImageView;
                } else {
                    PhotoView scaleImageView = new PhotoView(container.getContext());
                    Glide.with(container)
                            .asBitmap()
                            .load(uri)
                            .apply(new RequestOptions()
                                    .error(mDefaultDrawable)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(mDefaultDrawable))
                            .into(scaleImageView);

                    container.addView(scaleImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    return scaleImageView;
                }
            }
        }

        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private void initView() {
        mToolbarTop = findViewById(R.id.picker_preview_toolbar);
        mIndexTotal = findViewById(R.id.picker_index_total);
        mBtnBack = findViewById(R.id.picker_back);
        mViewPager = findViewById(R.id.picker_vpg_preview);
    }
}
