package com.hy.picker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.davemorrissey.labs.subscaleview.PickerScaleImageView;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.PickerScaleViewTarget;
import com.hy.picker.view.HackyViewPager;
import com.picker2.model.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_selected_preview);
        initView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mToolbarTop.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        }
        Intent intent = getIntent();
        mCurrentIndex = intent.getIntExtra("index", 0);
        mItemList = intent.getParcelableArrayListExtra("items");

        mIndexTotal.setText(String.format(Locale.getDefault(), "%d/%d", mCurrentIndex + 1, mItemList.size()));

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

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
                final ImageView imageView = new ImageView(container.getContext());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                String uri = photo.getUri();
                Glide.with(container.getContext())
                        .load(new File(uri))
                        .apply(new RequestOptions()
                                .error(R.drawable.picker_grid_image_default)
                                .placeholder(R.drawable.picker_grid_image_default))
                        .into(imageView);
                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return imageView;

            } else {

                PickerScaleImageView imageView = new PickerScaleImageView(container.getContext());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });


                String uri = photo.getUri();
                imageView.setOrientation(PickerScaleImageView.ORIENTATION_USE_EXIF);

                Glide.with(container.getContext())
                        .asFile()
                        .load(new File(uri))
                        .apply(new RequestOptions()
                                .error(R.drawable.picker_grid_image_default)
                                .placeholder(R.drawable.picker_grid_image_default))
                        .into(new PickerScaleViewTarget(imageView));
//                imageView.setImage(ImageSource.uri(Uri.fromFile(new File(uri))));

                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return imageView;
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
