package com.hy.picker;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.Logger;
import com.hy.picker.utils.PickerProgressScaleViewTarget;
import com.hy.picker.view.HackyViewPager;
import com.hy.picker.view.ProgressScaleImageView;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_preview);

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
        max = intent.getIntExtra("max", 9);
        boolean isGif = intent.getBooleanExtra("isGif", false);
        mTvEdit.setVisibility(isGif ? View.GONE : View.VISIBLE);
        boolean isPreview = intent.getBooleanExtra("isPreview", false);
//        mUseOrigin.setChecked(intent.getBooleanExtra("sendOrigin", false));
        mCurrentIndex = intent.getIntExtra("index", 0);

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
                            getResources().getQuantityString(R.plurals.picker_picsel_selected_max,1,max)
                            , Toast.LENGTH_SHORT).show();
                } else {

                    if (isChecked) {
                        MediaListHolder.selectPhotos.add(mItemList.get(mCurrentIndex));
                    } else {
                        MediaListHolder.selectPhotos.remove(mItemList.get(mCurrentIndex));
                    }
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

    public static final int REQUEST_EDIT = 0x987;
    private File mEditFile;

    private void toEdit(Uri uri) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            boolean mkdirs = path.mkdirs();
            Logger.d("文件夹：" + path + "创建" + (mkdirs ? "成功" : "失败"));
        }

        String name = "IMG-EDIT-" + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + ".jpg";
        mEditFile = new File(path, name);

        startActivityForResult(new Intent(this, IMGEditActivity.class)
                .putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri)
                .putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, mEditFile.getAbsolutePath()), REQUEST_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_EDIT) {
                if (mEditFile != null) {
                    mViewPager.getAdapter().notifyDataSetChanged();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

        }
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
                imageView.setOnClickListener(v -> {
                    mFullScreen = !mFullScreen;
//                        View decorView;
//                        byte uiOptions;
                    if (mFullScreen) {

                        mToolbarTop.setVisibility(View.INVISIBLE);
                        mToolbarBottom.setVisibility(View.INVISIBLE);
                    } else {

                        mToolbarTop.setVisibility(View.VISIBLE);
                        mToolbarBottom.setVisibility(View.VISIBLE);
//                            CommonUtils.processMIUI(PicturePreviewActivity.this, mIsStatusBlack);
                    }
                });

                String uri = photo.getUri();
                Glide.with(container.getContext())
                        .load(new File(uri))
                        .apply(new RequestOptions()
                                .error(mDefaultDrawable)
                                .placeholder(mDefaultDrawable))
                        .into(imageView);
                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return imageView;

            } else {

                ProgressScaleImageView imageView = new ProgressScaleImageView(container.getContext());
//                PickerScaleImageView imageView = new PickerScaleImageView(container.getContext());
                imageView.setOnClickListener(v -> {
                    mFullScreen = !mFullScreen;
//                        View decorView;
//                        byte uiOptions;
                    if (mFullScreen) {
//                            if (VERSION.SDK_INT < 16) {
//                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                            } else {
//                                decorView = getWindow().getDecorView();
//                                uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//                                decorView.setSystemUiVisibility(uiOptions);
//                            }

                        mToolbarTop.setVisibility(View.INVISIBLE);
                        mToolbarBottom.setVisibility(View.INVISIBLE);
                    } else {
                        mToolbarTop.setVisibility(View.VISIBLE);
                        mToolbarBottom.setVisibility(View.VISIBLE);
                    }
                });


                String uri = photo.getUri();

                Glide.with(container.getContext())
                        .asFile()
                        .load(new File(uri))
                        .apply(new RequestOptions()
                                .error(mDefaultDrawable)
                                .placeholder(mDefaultDrawable))
                        .into(new PickerProgressScaleViewTarget(imageView));
//                imageView.setImage(ImageSource.uri(Uri.fromFile(new File(uri))));

                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return imageView;
            }


        }

        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
