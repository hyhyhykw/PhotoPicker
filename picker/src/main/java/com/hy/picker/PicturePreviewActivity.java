package com.hy.picker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.picker2.utils.MediaListHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;


/**
 * Created time : 2018/8/2 8:23.
 *
 * @author HY
 */
public class PicturePreviewActivity extends BaseActivity {
    //    public static final int RESULT_SEND = 1;
    private TextView mIndexTotal;
    private View mWholeView;
    private View mToolbarTop;
    private View mToolbarBottom;
    private ImageView mBtnBack;
    private TextView mBtnSend;
    //    private AppCompatRadioButton mUseOrigin;
    private AppCompatCheckBox mSelectBox;
    private HackyViewPager mViewPager;

    private ArrayList<Photo> mItemList;

    private int mCurrentIndex;
    private boolean mFullScreen;

    private int max;
    private TextView mTvEdit;
    //    private int mStartIndex;
    private Drawable mDefaultDrawable;

    private PreviewReceiver mPreviewReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_preview);
        mPreviewReceiver = new PreviewReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PICKER_ACTION_MEDIA_ADD);
        registerReceiver(mPreviewReceiver, intentFilter);

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
        max = intent.getIntExtra(EXTRA_MAX, 9);
        boolean isGif = intent.getBooleanExtra(EXTRA_IS_GIF, false);
        mTvEdit.setVisibility(isGif ? View.GONE : View.VISIBLE);
        boolean isPreview = intent.getBooleanExtra(EXTRA_IS_PREVIEW, false);
//        mUseOrigin.setChecked(intent.getBooleanExtra("sendOrigin", false));
        mCurrentIndex = intent.getIntExtra(EXTRA_INDEX, 0);

        mItemList = isPreview ? MediaListHolder.selectPhotos : MediaListHolder.currentPhotos;

        mIndexTotal.setText(String.format(Locale.getDefault(), "%d/%d", mCurrentIndex + 1, mItemList.size()));

        mWholeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mBtnBack.setOnClickListener(v -> onBackPressed());

        mBtnSend.setOnClickListener(v -> {
            sendBroadcast(new Intent(PICKER_ACTION_MEDIA_SEND));
//                setResult(RESULT_SEND);
            finish();
        });


        mSelectBox.setText(R.string.picker_picprev_select);

        mSelectBox.setChecked(MediaListHolder.selectPhotos.contains(mItemList.get(mCurrentIndex)));
        mSelectBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (isChecked && MediaListHolder.selectPhotos.size() == max) {
                    mSelectBox.setChecked(false);


                    Toast.makeText(PicturePreviewActivity.this,
                            getResources().getQuantityString(R.plurals.picker_picsel_selected_max, 1, max)
                            , Toast.LENGTH_SHORT).show();
                } else {

                    if (mItemList.isEmpty()) return;
                    Photo photo = mItemList.get(mCurrentIndex);
                    if (isChecked) {
                        MediaListHolder.selectPhotos.add(photo);
                    } else {
                        MediaListHolder.selectPhotos.remove(photo);
                    }

                    sendBroadcast(new Intent(PICKER_ACTION_MEDIA_SELECT)
                            .putExtra(PICKER_EXTRA_PHOTO, photo));
                    updateToolbar();
                }
            }
        });

        mViewPager.setAdapter(new PreviewAdapter());
        mViewPager.setCurrentItem(mCurrentIndex);
        mViewPager.setOffscreenPageLimit(1);

        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                mCurrentIndex = position;
                mIndexTotal.setText(String.format(Locale.getDefault(), "%d/%d", position + 1, mItemList.size()));

                Photo photo = mItemList.get(position);
                mSelectBox.setChecked(MediaListHolder.selectPhotos.contains(photo));
                mTvEdit.setVisibility(photo.isGif() ? View.GONE : View.VISIBLE);
            }

            public void onPageScrollStateChanged(int state) {
            }
        });

        mTvEdit.setOnClickListener(v -> {
            Photo photo = mItemList.get(mViewPager.getCurrentItem());
            toEdit(Uri.fromFile(new File(photo.getUri())));
        });
        updateToolbar();
    }


    private File mEditFile;

    private void toEdit(Uri uri) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            boolean mkdirs = path.mkdirs();
            if (BuildConfig.DEBUG) {
                Log.d("TAG", "文件夹：" + path + "创建" + (mkdirs ? "成功" : "失败"));
            }

        }

        String name = "IMG-EDIT-" + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + ".jpg";
        mEditFile = new File(path, name);

        startActivity(new Intent(this, IMGEditActivity.class)
                .putExtra(EXTRA_IMAGE_URI, uri)
                .putExtra(EXTRA_IMAGE_SAVE_PATH, mEditFile.getAbsolutePath())
                .putExtra(EXTRA_MAX, max));
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mPreviewReceiver);
        super.onDestroy();
    }

    private void initView() {
        mTvEdit = findViewById(R.id.picker_tv_edit);
        mToolbarTop = findViewById(R.id.picker_preview_toolbar);
        mIndexTotal = findViewById(R.id.picker_index_total);
        mBtnBack = findViewById(R.id.picker_back);
        mBtnSend = findViewById(R.id.picker_send);
        mWholeView = findViewById(R.id.picker_whole_layout);
        mViewPager = findViewById(R.id.picker_vpg_preview);
        mToolbarBottom = findViewById(R.id.picker_bottom_bar);
//        mUseOrigin = findViewById(R.id.origin_check);
        mSelectBox = findViewById(R.id.picker_select_check);
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

    protected void onResume() {
        super.onResume();
    }

    private void updateToolbar() {
        int selNum = MediaListHolder.selectPhotos.size();

        if (mItemList.size() == 1 && selNum == 0) {
            mBtnSend.setText(R.string.picker_picsel_toolbar_send);
//            mUseOrigin.setText(R.string.rc_picprev_origin);
            mBtnSend.setEnabled(false);
        } else {
            if (selNum == 0) {
                mBtnSend.setText(R.string.picker_picsel_toolbar_send);
                mBtnSend.setEnabled(false);
            } else if (selNum <= 9) {
                mBtnSend.setEnabled(true);
                mBtnSend.setText(getResources().getString(R.string.picker_picsel_toolbar_send_num, selNum, max));
            }

        }
    }

    private class PreviewAdapter extends PagerAdapter {
        private PreviewAdapter() {
        }


        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
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
                PhotoView imageView = new PhotoView(container.getContext());
                imageView.setOnPhotoTapListener((v, x, y) -> {
                    mFullScreen = !mFullScreen;
//                        View decorView;
//                        byte uiOptions;
                    if (mFullScreen) {

                        mToolbarTop.setVisibility(View.INVISIBLE);
                        mToolbarBottom.setVisibility(View.INVISIBLE);
                    } else {

                        mToolbarTop.setVisibility(View.VISIBLE);
                        mToolbarBottom.setVisibility(View.VISIBLE);
//                            AppTool.processMIUI(PicturePreviewActivity.this, mIsStatusBlack);
                    }
                });

                String uri = photo.getUri();
                Glide.with(container.getContext())
                        .load(uri)
                        .apply(new RequestOptions()
                                .override(480, 800)
                                .priority(Priority.HIGH)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .error(mDefaultDrawable)
                                .placeholder(mDefaultDrawable))
                        .into(imageView);
                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return imageView;

            } else {

                if (photo.isLong()) {
                    PickerScaleImageView scaleImageView = new PickerScaleImageView(container.getContext());
                    String uri = photo.getUri();
                    Glide.with(PicturePreviewActivity.this)
                            .asBitmap()
                            .load(uri)
                            .apply(new RequestOptions()
                                    .error(mDefaultDrawable)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(mDefaultDrawable))
                            .into(new PickerScaleViewTarget(scaleImageView));

                    scaleImageView.setOnClickListener(v -> {
                        mFullScreen = !mFullScreen;
                        if (mFullScreen) {
                            mToolbarTop.setVisibility(View.INVISIBLE);
                            mToolbarBottom.setVisibility(View.INVISIBLE);
                        } else {
                            mToolbarTop.setVisibility(View.VISIBLE);
                            mToolbarBottom.setVisibility(View.VISIBLE);
                        }
                    });
                    container.addView(scaleImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    return scaleImageView;
                } else {
                    PhotoView imageView = new PhotoView(container.getContext());
                    imageView.setOnPhotoTapListener((v, x, y) -> {
                        mFullScreen = !mFullScreen;
                        if (mFullScreen) {

                            mToolbarTop.setVisibility(View.INVISIBLE);
                            mToolbarBottom.setVisibility(View.INVISIBLE);
                        } else {

                            mToolbarTop.setVisibility(View.VISIBLE);
                            mToolbarBottom.setVisibility(View.VISIBLE);
                        }
                    });

                    String uri = photo.getUri();
                    Glide.with(container.getContext())
                            .asBitmap()
                            .load(uri)
                            .apply(new RequestOptions()
                                    .error(mDefaultDrawable)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(mDefaultDrawable))
                            .into(imageView);
                    container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    return imageView;
                }

            }


        }

        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }


    public class PreviewReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {

                case PICKER_ACTION_MEDIA_ADD: {
                    runOnUiThread(() -> {
                        mViewPager.getAdapter().notifyDataSetChanged();
                        updateToolbar();
                    });

                }
                break;
            }


        }
    }
}
