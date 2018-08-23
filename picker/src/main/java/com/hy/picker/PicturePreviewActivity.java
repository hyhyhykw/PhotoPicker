package com.hy.picker;


import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.Logger;
import com.hy.picker.view.HackyViewPager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import me.kareluo.imaging.IMGEditActivity;


/**
 * Created time : 2018/8/2 8:23.
 *
 * @author HY
 */
public class PicturePreviewActivity extends AppCompatActivity {
    public static final int RESULT_SEND = 1;
    private TextView mIndexTotal;
    private View mWholeView;
    private View mToolbarTop;
    private View mToolbarBottom;
    private ImageButton mBtnBack;
    private TextView mBtnSend;
    //    private AppCompatRadioButton mUseOrigin;
    private AppCompatCheckBox mSelectBox;
    private HackyViewPager mViewPager;
    private ArrayList<PictureSelectorActivity.PicItem> mItemList;
    private ArrayList<PictureSelectorActivity.PicItem> mItemSelectedList;
    private int mCurrentIndex;
    private boolean mFullScreen;

    private int max;
    private TextView mTvEdit;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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

//        mUseOrigin.setChecked(intent.getBooleanExtra("sendOrigin", false));
        mCurrentIndex = intent.getIntExtra("index", 0);
        if (mItemList == null) {
            mItemList = PictureSelectorActivity.PicItemHolder.itemList;
            mItemSelectedList = PictureSelectorActivity.PicItemHolder.itemSelectedList;
        }

        mIndexTotal.setText(String.format(Locale.getDefault(), "%d/%d", mCurrentIndex + 1, mItemList.size()));

        mWholeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        result = getSmartBarHeight(this);
//        if (result > 0) {
//            LayoutParams lp = (LayoutParams) mToolbarBottom.getLayoutParams();
//            lp.setMargins(0, 0, 0, result);
//            mToolbarBottom.setLayoutParams(lp);
//        }

//        int  result = 0;
//        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            result = getResources().getDimensionPixelSize(resourceId);
//        }

//        LayoutParams lp = new LayoutParams(mToolbarTop.getLayoutParams());
//        lp.setMargins(0, result, 0, 0);
//        mToolbarTop.setLayoutParams(lp);

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

//        mUseOrigin.setText(R.string.rc_picprev_origin);
//        mUseOrigin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked && getTotalSelectedNum() == 0) {
//                    mItemList.get(mCurrentIndex).selected = mSelectBox.isChecked();
//                    updateToolbar();
//                }
//            }
//        });

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
        mTvEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureSelectorActivity.PicItem picItem = mItemList.get(mViewPager.getCurrentItem());
                toEdit(Uri.fromFile(new File(picItem.uri)));
            }
        });
        updateToolbar();
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
        if (!mEditFile.exists()) {
            try {
                boolean newFile = mEditFile.createNewFile();
                Logger.d("文件：" + mEditFile + "创建" + (newFile ? "成功" : "失败"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                    MediaScannerConnection.scanFile(this, new String[]{uriPath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(final String path, Uri uri) {
                            Logger.d("crop path===" + path);
                        }
                    });

                    startActivityForResult(new Intent(this, PictureEditPreviewActivity.class)
                            .putExtra("picItem", item), REQUEST_EDIT_PREVIEW);
                } else {
                    Toast.makeText(this, R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if (requestCode == REQUEST_EDIT_PREVIEW) {
                PictureSelectorActivity.PicItem item = new PictureSelectorActivity.PicItem();
                item.uri = mEditFile.getAbsolutePath();
                item.selected = true;
                mItemList.add(item);
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

//    private String getTotalSelectedSize() {
//        float size = 0.0F;
//
//        int i;
//        File file;
//        for (i = 0; i < mItemList.size(); ++i) {
//            if (mItemList.get(i).selected) {
//                file = new File(mItemList.get(i).uri);
//                size += (float) (file.length() / 1024L);
//            }
//        }
//
//        if (mItemSelectedList != null) {
//            for (i = 0; i < mItemSelectedList.size(); ++i) {
//                if (mItemSelectedList.get(i).selected) {
//                    file = new File(mItemSelectedList.get(i).uri);
//                    size += (float) (file.length() / 1024L);
//                }
//            }
//        }
//
//        String totalSize;
//        if (size < 1024.0F) {
//            totalSize = String.format(Locale.getDefault(), "%.0fK", size);
//        } else {
//            totalSize = String.format(Locale.getDefault(), "%.1fM", size / 1024.0F);
//        }
//
//        return totalSize;
//    }

    private void updateToolbar() {
        int selNum = getTotalSelectedNum();
        if (mItemList.size() == 1 && selNum == 0) {
            mBtnSend.setText(R.string.picker_picsel_toolbar_send);
//            mUseOrigin.setText(R.string.rc_picprev_origin);
            mBtnSend.setEnabled(false);
        } else {
            if (selNum == 0) {
                mBtnSend.setText(R.string.picker_picsel_toolbar_send);
//                mUseOrigin.setText(R.string.rc_picprev_origin);
//                mUseOrigin.setChecked(false);
                mBtnSend.setEnabled(false);
            } else if (selNum <= 9) {
                mBtnSend.setEnabled(true);
                mBtnSend.setText(getResources().getString(R.string.picker_picsel_toolbar_send_num, selNum, max));
//                mUseOrigin.setText(String.format(getResources().getString(R.string.rc_picprev_origin_size), getTotalSelectedSize()));
            }

        }
    }

//    public static int getSmartBarHeight(Context context) {
//        try {
//            @SuppressLint("PrivateApi")
//            Class c = Class.forName("com.android.internal.R$dimen");
//            Object obj = c.newInstance();
//            Field field = c.getField("mz_action_button_min_height");
//            int height = Integer.parseInt(field.get(obj).toString());
//            return context.getResources().getDimensionPixelSize(height);
//        } catch (Exception var5) {
//            var5.printStackTrace();
//            return 0;
//        }
//    }


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
            final ImageView imageView;
            if (picItem.isGif()) {
                imageView = new ImageView(container.getContext());
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFullScreen = !mFullScreen;
                        View decorView;
                        byte uiOptions;
                        if (mFullScreen) {
                            if (VERSION.SDK_INT < 16) {
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            } else {
                                decorView = getWindow().getDecorView();
                                uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                                decorView.setSystemUiVisibility(uiOptions);
                            }

                            mToolbarTop.setVisibility(View.INVISIBLE);
                            mToolbarBottom.setVisibility(View.INVISIBLE);
                        } else {
                            if (VERSION.SDK_INT < 16) {
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            } else {
                                decorView = getWindow().getDecorView();
                                uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                                decorView.setSystemUiVisibility(uiOptions);
                            }

                            mToolbarTop.setVisibility(View.VISIBLE);
                            mToolbarBottom.setVisibility(View.VISIBLE);
                        }
                    }
                });


            } else {
                imageView = new PhotoView(container.getContext());

                ((PhotoView) imageView).setOnViewTapListener(new OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float x, float y) {
                        mFullScreen = !mFullScreen;
                        View decorView;
                        byte uiOptions;
                        if (mFullScreen) {
                            if (VERSION.SDK_INT < 16) {
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            } else {
                                decorView = getWindow().getDecorView();
                                uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                                decorView.setSystemUiVisibility(uiOptions);
                            }

                            mToolbarTop.setVisibility(View.INVISIBLE);
                            mToolbarBottom.setVisibility(View.INVISIBLE);
                        } else {
                            if (VERSION.SDK_INT < 16) {
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            } else {
                                decorView = getWindow().getDecorView();
                                uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                                decorView.setSystemUiVisibility(uiOptions);
                            }

                            mToolbarTop.setVisibility(View.VISIBLE);
                            mToolbarBottom.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }

            String uri = picItem.getUri();
            Glide.with(container.getContext())
                    .load(new File(uri))
                    .apply(new RequestOptions()
                            .error(R.drawable.picker_grid_image_default)
                            .placeholder(R.drawable.picker_grid_image_default))
                    .into(imageView);
            container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return imageView;
        }

        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
