package com.hy.picker;


import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.davemorrissey.labs.subscaleview.PickerScaleImageView;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.Logger;
import com.hy.picker.utils.PickerScaleViewTarget;
import com.hy.picker.view.HackyViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created time : 2018/8/2 8:23.
 *
 * @author HY
 */
public class PicturePreviewActivity extends BaseActivity {
    public static final int RESULT_SEND = 1;
    private TextView mIndexTotal;
    private View mWholeView;
    private View mToolbarTop;
    private View mToolbarBottom;
    private ImageView mBtnBack;
    private TextView mBtnSend;
    //    private AppCompatRadioButton mUseOrigin;
    private AppCompatCheckBox mSelectBox;
    private HackyViewPager mViewPager;
    private ArrayList<PictureSelectorActivity.PicItem> mItemList;
    private ArrayList<PictureSelectorActivity.PicItem> mItemSelectedList;
    private int mCurrentIndex;
    private boolean mFullScreen;
    private boolean isPreview;

    private int max;
    private TextView mTvEdit;
    private int mStartIndex;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_preview);

        initView();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mToolbarTop.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        }

        Intent intent = getIntent();
        max = intent.getIntExtra("max", 9);
        boolean isGif = intent.getBooleanExtra("isGif", false);
        mTvEdit.setVisibility(isGif ? View.GONE : View.VISIBLE);
        isPreview = intent.getBooleanExtra("isPreview", false);
//        mUseOrigin.setChecked(intent.getBooleanExtra("sendOrigin", false));
        mCurrentIndex = intent.getIntExtra("index", 0);
        mStartIndex = mCurrentIndex;
        if (mItemList == null) {
            mItemList = PictureSelectorActivity.PicItemHolder.itemList;
            mItemSelectedList = PictureSelectorActivity.PicItemHolder.itemSelectedList;
        }


        mIndexTotal.setText(String.format(Locale.getDefault(), "%d/%d", mCurrentIndex + 1, mItemList.size()));

        mWholeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mBtnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
//                intent.putExtra("sendOrigin", mUseOrigin.isChecked());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mBtnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<PictureSelectorActivity.PicItem> picItems = new ArrayList<>();
                if (mItemSelectedList != null) {
                    for (PictureSelectorActivity.PicItem picItem : mItemSelectedList) {
                        if (picItem.selected) {
                            picItems.add(picItem);
                        }
                    }

                }
                for (PictureSelectorActivity.PicItem picItem : mItemList) {
                    if (picItem.selected) {
                        picItems.add(picItem);
                    }
                }
                PhotoPicker.sPhotoListener.onPicked(picItems);
                setResult(RESULT_SEND);
                finish();
            }
        });


        mSelectBox.setText(R.string.picker_picprev_select);
        mSelectBox.setChecked(mItemList.get(mCurrentIndex).selected);
        mSelectBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    if (isChecked && getTotalSelectedNum() == max) {
                        mSelectBox.setChecked(false);
                        Toast.makeText(PicturePreviewActivity.this, getString(R.string.picker_picsel_selected_max, max), Toast.LENGTH_SHORT).show();
                    } else {
                        mItemList.get(mCurrentIndex).selected = mSelectBox.isChecked();
                        updateToolbar();
                    }
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
                PictureSelectorActivity.PicItem item = mItemList.get(position);
                mSelectBox.setChecked(item.selected);
                mTvEdit.setVisibility(item.isGif() ? View.GONE : View.VISIBLE);
            }

            public void onPageScrollStateChanged(int state) {
            }
        });

        supportPostponeEnterTransition();//延缓执行 然后在fragment里面的控件加载完成后start
        if (Build.VERSION.SDK_INT >= 22) {

            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

                    PictureSelectorActivity.PicItem item = mItemList.get(mCurrentIndex);
//                    ViewCompat.setTransitionName(mViewPager, item.getUri());
                    if (mStartIndex != mCurrentIndex) {
                        names.clear();
                        names.add(item.getUri());
                        String url = item.getUri();
                        sharedElements.clear();
                        sharedElements.put(url, mViewPager);
                    }
                }
            });

            postponeEnterTransition();
            PictureSelectorActivity.PicItem item = mItemList.get(mCurrentIndex);
            ViewCompat.setTransitionName(mViewPager, item.getUri());
            startPostponedEnterTransition();


        }
        mTvEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureSelectorActivity.PicItem picItem = mItemList.get(mViewPager.getCurrentItem());
                toEdit(Uri.fromFile(new File(picItem.uri)));
            }
        });
        updateToolbar();
    }

    @TargetApi(22)
    @Override
    public void supportFinishAfterTransition() {
        Intent data = new Intent()
                .putExtra("index", mCurrentIndex)
                .putExtra("isPreview", isPreview)
                .putExtra("startIndex", mStartIndex);
        setResult(RESULT_OK, data);
        super.supportFinishAfterTransition();
    }


    @Override
    public void onBackPressed() {
        Intent data = new Intent()
                .putExtra("index", mCurrentIndex)
                .putExtra("isPreview", isPreview)
                .putExtra("startIndex", mStartIndex);
        setResult(RESULT_OK, data);
        super.supportFinishAfterTransition();
    }


    public static final int REQUEST_EDIT = 0x987;
    public static final int REQUEST_EDIT_PREVIEW = 0x876;
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
                    PictureSelectorActivity.PicItem item = new PictureSelectorActivity.PicItem();
                    String uriPath = mEditFile.getAbsolutePath();
                    item.uri = uriPath;
                    item.selected = true;
                    mItemList.add(item);
                    MediaScannerConnection.scanFile(this, new String[]{uriPath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(final String path, Uri uri) {
                            Intent intent = new Intent(PictureSelectorActivity.ACTION_UPDATE);
                            intent.putExtra(PictureSelectorActivity.ACTION_UPDATE_PATH, path);
                            sendBroadcast(intent);
                        }
                    });
                    mViewPager.getAdapter().notifyDataSetChanged();
                    startActivityForResult(new Intent(this, PictureEditPreviewActivity.class)
                            .putExtra("picItem", item), REQUEST_EDIT_PREVIEW);
                } else {
                    Toast.makeText(this, R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if (requestCode == REQUEST_EDIT_PREVIEW) {
                setResult(RESULT_OK);
                finish();
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
//            intent.putExtra("sendOrigin", mUseOrigin.isChecked());
            setResult(RESULT_OK, intent);
        }

        return super.onKeyDown(keyCode, event);
    }

    private int getTotalSelectedNum() {
        int sum = 0;

        for (int i = 0; i < mItemList.size(); ++i) {
            if (mItemList.get(i).selected) {
                ++sum;
            }
        }

        if (mItemSelectedList != null) {
            sum += mItemSelectedList.size();
        }

        return sum;
    }


    private void updateToolbar() {
        int selNum = getTotalSelectedNum();
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
            PictureSelectorActivity.PicItem picItem = mItemList.get(position);
            if (picItem.isGif()) {
                final ImageView imageView = new ImageView(container.getContext());
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

//                            if (VERSION.SDK_INT < 16) {
//                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                            } else {
//                                decorView = getWindow().getDecorView();
//                                uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
//                                decorView.setSystemUiVisibility(uiOptions);
//                            }

                            mToolbarTop.setVisibility(View.VISIBLE);
                            mToolbarBottom.setVisibility(View.VISIBLE);
//                            CommonUtils.processMIUI(PicturePreviewActivity.this, mIsStatusBlack);
                        }
                    }
                });

                String uri = picItem.getUri();
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
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
//                            if (VERSION.SDK_INT < 16) {
//                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                            } else {
//                                decorView = getWindow().getDecorView();
//                                uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
//                                decorView.setSystemUiVisibility(uiOptions);
//                            }
//                            CommonUtils.processMIUI(PicturePreviewActivity.this, mIsStatusBlack);
                            mToolbarTop.setVisibility(View.VISIBLE);
                            mToolbarBottom.setVisibility(View.VISIBLE);
                        }
                    }
                });


                String uri = picItem.getUri();
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
}
