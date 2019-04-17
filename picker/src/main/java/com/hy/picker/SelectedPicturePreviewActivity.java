package com.hy.picker;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.hy.picker.view.ImageSource;
import com.hy.picker.view.PickerScaleImageView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hy.picker.utils.DisplayOptimizeListener;
import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.CommonUtils;
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
import me.relex.photodraweeview.PhotoDraweeView;

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
        mCurrentIndex = intent.getIntExtra(EXTRA_INDEX, 0);
        mItemList = intent.getParcelableArrayListExtra(EXTRA_ITEMS);

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
            String uri = photo.getUri();
            if (photo.isGif()) {
                SimpleDraweeView imageView = new SimpleDraweeView(container.getContext());
                GenericDraweeHierarchy hierarchy = imageView.getHierarchy();
                hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
                hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
                imageView.setOnClickListener(v -> finish());
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(Uri.fromFile(new File(photo.getUri())))
                        .setAutoPlayAnimations(true)
                        .build();
                imageView.setController(controller);
                hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return imageView;
            } else {
                if (photo.isLong()) {

                    PickerScaleImageView imageView = new PickerScaleImageView(container.getContext());
                    imageView.setOnClickListener(v -> finish());
                    imageView.setMinimumTileDpi(160);

                    imageView.setOnImageEventListener(new DisplayOptimizeListener(imageView));
                    imageView.setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                    imageView.setImage(ImageSource.uri(Uri.fromFile(new File(photo.getUri()))));
                    container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    return imageView;
                } else {
                    PhotoDraweeView imageView = new PhotoDraweeView(container.getContext());
                    imageView.setOnViewTapListener((view, x, y) -> finish());
                    imageView.setPhotoUri(Uri.fromFile(new File(uri)));

                    container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    return imageView;
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
