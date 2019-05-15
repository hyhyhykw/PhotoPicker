package com.hy.picker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hy.picker.adapter.PreviewAdapter;
import com.hy.picker.model.Photo;
import com.hy.picker.utils.AndroidLifecycleUtils;
import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.MediaListHolder;
import com.hy.picker.utils.OnItemClickListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;


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
    private ViewPager2 mViewPager;

    private ArrayList<Photo> mItemList;

    private int mCurrentIndex;
    private boolean mFullScreen;

    private int max;
    private TextView mTvEdit;

    private PreviewReceiver mPreviewReceiver;
    private boolean mIsPreview;

    private PreviewAdapter mPreviewAdapter;


    private static final class WeakListener implements OnItemClickListener {

        private WeakReference<PicturePreviewActivity> mReference;

        WeakListener(PicturePreviewActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void onClick(int position, boolean isChange) {
            if (null == mReference) return;
            PicturePreviewActivity activity = mReference.get();
            if (!AndroidLifecycleUtils.canLoadImage(activity)) return;
            activity.mFullScreen = !activity.mFullScreen;
            if (activity.mFullScreen) {

                activity.mToolbarTop.setVisibility(View.INVISIBLE);
                activity.mToolbarBottom.setVisibility(View.INVISIBLE);
            } else {

                activity.mToolbarTop.setVisibility(View.VISIBLE);
                activity.mToolbarBottom.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_preview);
        mPreviewReceiver = new PreviewReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PICKER_ACTION_MEDIA_ADD);
        registerReceiver(mPreviewReceiver, intentFilter);

        initView();
        //    private int mStartIndex;
        Drawable defaultDrawable = AttrsUtils.getTypeValueDrawable(this, R.attr.picker_image_default);
        if (null == defaultDrawable) {
            defaultDrawable = ContextCompat.getDrawable(this, R.drawable.picker_grid_image_default);
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mToolbarTop.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);

        Intent intent = getIntent();
        max = intent.getIntExtra(EXTRA_MAX, 9);
        boolean isGif = intent.getBooleanExtra(EXTRA_IS_GIF, false);
        mTvEdit.setVisibility(isGif ? View.GONE : View.VISIBLE);
        mIsPreview = intent.getBooleanExtra(EXTRA_IS_PREVIEW, false);
        mCurrentIndex = intent.getIntExtra(EXTRA_INDEX, 0);

        mItemList = mIsPreview ? MediaListHolder.selectPhotos : MediaListHolder.currentPhotos;

        mIndexTotal.setText(String.format(Locale.getDefault(), "%d/%d", mCurrentIndex + 1, mItemList.size()));

        mWholeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mBtnBack.setOnClickListener(v -> onBackPressed());

        mBtnSend.setOnClickListener(v -> {
            sendBroadcast(new Intent(PICKER_ACTION_MEDIA_SEND));
            finish();
        });


        mSelectBox.setText(R.string.picker_picprev_select);

        mSelectBox.setChecked(mItemList.get(mCurrentIndex).isSelected());
        mSelectBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (isChecked && getSelNum() == max) {
                    mSelectBox.setChecked(false);


                    Toast.makeText(PicturePreviewActivity.this,
                            getResources().getQuantityString(R.plurals.picker_picsel_selected_max, 1, max)
                            , Toast.LENGTH_SHORT).show();
                } else {

                    if (mItemList.isEmpty()) return;

                    Photo photo = mItemList.get(mCurrentIndex);
                    sendBroadcast(new Intent(PICKER_ACTION_MEDIA_SELECT)
                            .putExtra(PICKER_EXTRA_PHOTO, photo));

                    if (isChecked) {
                        photo.setSelected(true);
                        MediaListHolder.selectPhotos.add(photo);
                    } else {
                        photo.setSelected(false);
                        if (!mIsPreview) {
                            MediaListHolder.selectPhotos.remove(photo);
                        }
                    }
                    updateToolbar();
                }
            }
        });
        mPreviewAdapter = new PreviewAdapter(defaultDrawable);
        mPreviewAdapter.setOnItemClickListener(new WeakListener(this));

        mViewPager.setAdapter(mPreviewAdapter);

//        mViewPager.setOffscreenPageLimit(1);

        mPreviewAdapter.reset(mItemList);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mCurrentIndex = position;
                mIndexTotal.setText(String.format(Locale.getDefault(), "%d/%d", position + 1, mItemList.size()));

                Photo photo = mItemList.get(position);
                mSelectBox.setChecked(photo.isSelected());
                mTvEdit.setVisibility(photo.isGif() ? View.GONE : View.VISIBLE);
            }
        });
//        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            }
//
//            public void onPageSelected(int position) {
//
//            }
//
//            public void onPageScrollStateChanged(int state) {
//            }
//        });
        mViewPager.setCurrentItem(mCurrentIndex,false);


        mTvEdit.setOnClickListener(v -> {
            Photo photo = mItemList.get(mViewPager.getCurrentItem());
            toEdit(Uri.fromFile(new File(photo.getUri())));
        });
        updateToolbar();
    }


    private int getSelNum() {
        int num = 0;
        for (Photo photo : MediaListHolder.selectPhotos) {
            if (photo.isSelected()) {
                num += 1;
            }
        }
        return num;
    }

    private void toEdit(Uri uri) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            boolean mkdirs = path.mkdirs();
            if (BuildConfig.DEBUG) {
                Log.d("TAG", "文件夹：" + path + "创建" + (mkdirs ? "成功" : "失败"));
            }

        }

        String name = "IMG-EDIT-" + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + ".jpg";
        File editFile = new File(path, name);

        startActivity(new Intent(this, IMGEditActivity.class)
                .putExtra(EXTRA_IMAGE_URI, uri)
                .putExtra(EXTRA_IMAGE_SAVE_PATH, editFile.getAbsolutePath())
                .putExtra(EXTRA_MAX, max));
    }

    @Override
    public void onBackPressed() {
        if (mIsPreview) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();

    }

    @Override
    public void finish() {
        if (mIsPreview) {
            setResult(RESULT_OK);
        }
        super.finish();

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

        int selNum = getSelNum();

//        int selNum = MediaListHolder.selectPhotos.size();

        mIndexTotal.setText(String.format(Locale.getDefault(), "%d/%d", mCurrentIndex + 1, mItemList.size()));

        mSelectBox.setChecked(mItemList.get(mCurrentIndex).isSelected());
        if (mItemList.size() == 1 && selNum == 0) {
            mBtnSend.setText(R.string.picker_picsel_toolbar_send);
//            mUseOrigin.setText(R.string.rc_picprev_origin);
            mBtnSend.setEnabled(false);
        } else {
            if (selNum == 0) {
                mBtnSend.setText(R.string.picker_picsel_toolbar_send);
                mBtnSend.setEnabled(false);
            } else if (selNum <= max) {
                mBtnSend.setEnabled(true);
                mBtnSend.setText(getResources().getString(R.string.picker_picsel_toolbar_send_num, selNum, max));
            }
        }
    }


//    private class PreviewAdapter extends PagerAdapter {
//
//        @Override
//        public int getItemPosition(@NonNull Object object) {
//            return POSITION_NONE;
//        }
//
//        public int getCount() {
//            return mItemList.size();
//        }
//
//        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
//            return view == object;
//        }
//
//        @NonNull
//        public Object instantiateItem(@NonNull ViewGroup container, int position) {
//
//            Photo photo = mItemList.get(position);
//            String uri = photo.getUri();
//            if (photo.isGif()) {
//                SimpleDraweeView imageView = new SimpleDraweeView(container.getContext());
//                GenericDraweeHierarchy hierarchy = imageView.getHierarchy();
//                hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
//                hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
//                imageView.setOnClickListener(v -> {
//                    mFullScreen = !mFullScreen;
//                    if (mFullScreen) {
//
//                        mToolbarTop.setVisibility(View.INVISIBLE);
//                        mToolbarBottom.setVisibility(View.INVISIBLE);
//                    } else {
//
//                        mToolbarTop.setVisibility(View.VISIBLE);
//                        mToolbarBottom.setVisibility(View.VISIBLE);
//                    }
//                });
//                DraweeController controller = Fresco.newDraweeControllerBuilder()
//                        .setUri(Uri.fromFile(new File(photo.getUri())))
//                        .setAutoPlayAnimations(true)
//                        .build();
//                imageView.setController(controller);
//                hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
//                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                return imageView;
//            } else {
//                if (photo.isLong()) {
//
//                    PickerScaleImageView imageView = new PickerScaleImageView(container.getContext());
//                    imageView.setOnClickListener(v -> {
//                        mFullScreen = !mFullScreen;
//                        if (mFullScreen) {
//
//                            mToolbarTop.setVisibility(View.INVISIBLE);
//                            mToolbarBottom.setVisibility(View.INVISIBLE);
//                        } else {
//
//                            mToolbarTop.setVisibility(View.VISIBLE);
//                            mToolbarBottom.setVisibility(View.VISIBLE);
//                        }
//                    });
//                    imageView.setMinimumTileDpi(160);
//
//                    imageView.setOnImageEventListener(new DisplayOptimizeListener(imageView));
//                    imageView.setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_INSIDE);
//                    imageView.setImage(ImageSource.uri(Uri.fromFile(new File(photo.getUri()))));
//                    container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                    return imageView;
//                } else {
//                    PhotoDraweeView imageView = new PhotoDraweeView(container.getContext());
//                    imageView.setOnViewTapListener((view, x, y) -> {
//                        mFullScreen = !mFullScreen;
//                        if (mFullScreen) {
//
//                            mToolbarTop.setVisibility(View.INVISIBLE);
//                            mToolbarBottom.setVisibility(View.INVISIBLE);
//                        } else {
//
//                            mToolbarTop.setVisibility(View.VISIBLE);
//                            mToolbarBottom.setVisibility(View.VISIBLE);
//                        }
//                    });
//                    imageView.setPhotoUri(Uri.fromFile(new File(uri)));
//
//                    container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                    return imageView;
//                }
//            }
//
//
//        }
//
//
//        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//            container.removeView((View) object);
//        }
//    }


    public class PreviewReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            String action = intent.getAction();
            if (action == null) return;

            Photo photo = intent.getParcelableExtra(PICKER_EXTRA_PHOTO);
            switch (action) {

                case PICKER_ACTION_MEDIA_ADD: {
                    runOnUiThread(() -> {
                        mPreviewAdapter.add(0, photo);

                        updateToolbar();
                    });

                }
                break;
            }


        }
    }
}
