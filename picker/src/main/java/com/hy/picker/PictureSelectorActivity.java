package com.hy.picker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.MessageQueue;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hy.picker.core.util.SizeUtils;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.MyFileProvider;
import com.hy.picker.utils.MyGridItemDecoration;
import com.hy.picker.utils.PermissionUtils;
import com.picker2.model.Photo;
import com.picker2.model.PhotoDirectory;
import com.picker2.utils.MediaListHolder;
import com.picker2.utils.MediaStoreHelper;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PictureSelectorActivity extends BaseActivity {
    public static final int REQUEST_CAMERA = 1;
    private RecyclerView mGridView;
    private ImageView mBtnBack;
    private TextView mBtnSend;
    private TextView mTvTitle;
    private PicTypeBtn mPicType;
    private PreviewBtn mPreviewBtn;

    private RelativeLayout mCatalogWindow;
    private ListView mCatalogListView;

    private Uri mTakePictureUri;
    //    private boolean mSendOrigin = false;
    private int max;
    private ArrayList<Photo> mSelectItems;

    private LinearLayout mLytLoad;
    private boolean gif;
    private boolean preview;
    private boolean video;
    private boolean gifOnly;
    private GridViewAdapter mGridViewAdapter;
    private CatalogAdapter mCatalogAdapter;

    private View mToolbarMask;
    private View mBottomBarMask;
    private View mCatalogMask;
    private int catalogHeight;
    private boolean isShowCamera;

    //    private UpdateReceiver mUpdateReceiver;
    private SelectReceiver mSelectReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_selector);

        mSelectReceiver = new SelectReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PICKER_ACTION_MEDIA_ADD);
        intentFilter.addAction(PICKER_ACTION_MEDIA_SELECT);
        intentFilter.addAction(PICKER_ACTION_MEDIA_SEND);
        registerReceiver(mSelectReceiver, intentFilter);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        catalogHeight = PhotoContext.getScreenHeight() - SizeUtils.dp2px(this, 145) - CommonUtils.getStatusBarHeight(this);

        Intent intent = getIntent();
        max = intent.getIntExtra("max", 9);
        gif = intent.getBooleanExtra("gif", true);
        preview = intent.getBooleanExtra("preview", true);
        gifOnly = intent.getBooleanExtra("gifOnly", false);
        video = intent.getBooleanExtra("video", false);
        isShowCamera = intent.getBooleanExtra("showCamera", false);

        mSelectItems = intent.getParcelableArrayListExtra("items");

        mLytLoad = findViewById(R.id.picker_photo_load);
        mGridView = findViewById(R.id.picker_photo_grd);
        mBtnBack = findViewById(R.id.picker_back);
        mToolbarMask = findViewById(R.id.picker_toolbar_mask);
        mBottomBarMask = findViewById(R.id.picker_bottom_mask);
        mCatalogMask = findViewById(R.id.picker_catalog_mask);


        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mBtnSend = findViewById(R.id.picker_send);
        mTvTitle = findViewById(R.id.picker_title);
        mPicType = findViewById(R.id.picker_pic_type);
        mPicType.init(this);
        mPicType.setEnabled(false);
        mPicType.setText(video ? R.string.picker_all_video : R.string.picker_all_image);

        mPreviewBtn = findViewById(R.id.picker_preview);
        mPreviewBtn.init(this);
        mPreviewBtn.setEnabled(null != mSelectItems && !mSelectItems.isEmpty());

        mTvTitle.setText(video ? R.string.picker_picsel_videotype : R.string.picker_picsel_pictype);
        if (video) {
            mPreviewBtn.setVisibility(View.GONE);
            mBtnSend.setVisibility(View.GONE);
        }
        if (null != mSelectItems) {
            int size = mSelectItems.size();
            if (size == 0) {
                mBtnSend.setEnabled(false);
                mBtnSend.setText(R.string.picker_picsel_toolbar_send);
                mPreviewBtn.setEnabled(false);
                mPreviewBtn.setText(R.string.picker_picsel_toolbar_preview);
            } else if (size <= max) {
                mBtnSend.setEnabled(true);
                mBtnSend.setText(getResources().getString(R.string.picker_picsel_toolbar_send_num, size, max));
                mPreviewBtn.setEnabled(true);
                mPreviewBtn.setText(String.format(getResources().getString(R.string.picker_picsel_toolbar_preview_num), size));
            }
        }
        mCatalogListView = findViewById(R.id.picker_catalog_lst);
        mCatalogWindow = findViewById(R.id.picker_catalog_window);
        mCatalogWindow.setVisibility(View.GONE);

//        if (Build.VERSION.SDK_INT >= 22) {
//            setExitSharedElementCallback(new SharedElementCallback() {
//                @Override
//                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
//                    if (mData!=null){
//                        index = mData.getIntExtra("index", 0);
//                        isPreview = mData.getBooleanExtra("isPreview", false);
//
//                        int i = index;
//                        sharedElements.clear();
//                        names.clear();
//                        String url;
//
//                        if (isPreview) {
//                            PicItem item = PicItemHolder.itemSelectedList.get(i);
//                            url = item.uri;
//                            i = mAllItemList.indexOf(item);
//                        } else {
//                            url = mAllItemList.get(i).uri;
//                        }
//                        names.add(url);
//                        View itemView = mLayoutManager.findViewByPosition(i + 1);
//
////                        View itemView = lm.findViewByPosition(i);
////                    ImageView imageView = itemView.findViewById(R.id.picker_photo_image);
//                        //注意这里第二个参数，如果放置的是条目的item则动画不自然。放置对应的imageView则完美
//                        sharedElements.put(url, itemView);
//
//                        mData=null;
//                    } else{
//                        View navigationBar = findViewById(android.R.id.navigationBarBackground);
//                        View statusBar = findViewById(android.R.id.statusBarBackground);
//                        if (navigationBar != null) {
//                            names.add(navigationBar.getTransitionName());
//                            sharedElements.put(navigationBar.getTransitionName(), navigationBar);
//                        }
//                        if (statusBar != null) {
//                            names.add(statusBar.getTransitionName());
//                            sharedElements.put(statusBar.getTransitionName(), statusBar);
//                        }
//                    }
//                }
//            });
//        }

        mGridViewAdapter = new GridViewAdapter();
        mGridView.setAdapter(mGridViewAdapter);
        mGridView.setLayoutManager(new GridLayoutManager(this, 3));
        mGridView.addItemDecoration(new MyGridItemDecoration(this));

        mCatalogAdapter = new CatalogAdapter();
        mCatalogListView.setAdapter(mCatalogAdapter);
        mCatalogListView.setTranslationY(catalogHeight);
        mCatalogListView.setVisibility(View.VISIBLE);

        mLytLoad.setVisibility(View.VISIBLE);


        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                new PermissionUtils(PictureSelectorActivity.this)
                        .setPermissionListener(new PermissionUtils.PermissionListener() {
                            @Override
                            public void onResult() {
                                initView();
                            }
                        })
                        .requestPermission(Permission.WRITE_EXTERNAL_STORAGE);
                return false;
            }
        });


    }

//    private int index;
//
//    private Intent mData;
//
//    @Override
//    public void onActivityReenter(int resultCode, Intent data) {
//        super.onActivityReenter(resultCode, data);
//        mData = data;
//    }


    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(PhotoPicker.EXTRA_SHOW_GIF, gif);
        bundle.putBoolean(PhotoPicker.EXTRA_ONLY_GIF, gifOnly);
        bundle.putBoolean(PhotoPicker.EXTRA_PICK_VIDEO, video);
        bundle.putParcelableArrayList(PhotoPicker.EXTRA_ITEMS, mSelectItems);
        if (null == mSelectItems || mSelectItems.isEmpty()) {
            MediaListHolder.selectPhotos.clear();
        } else {
            MediaListHolder.selectPhotos.clear();
            MediaListHolder.selectPhotos.addAll(mSelectItems);
        }

        MediaStoreHelper.getPhotoDirs(this, bundle, new MediaStoreHelper.PhotosResultCallback() {
            @Override
            public void onResultCallback() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MediaStoreHelper.destroyLoader(PictureSelectorActivity.this, 1);
                        mGridViewAdapter.notifyDataSetChanged();
                        mCatalogAdapter.notifyDataSetChanged();
                        updateToolbar();
                        if (mLytLoad.getVisibility() == View.VISIBLE) {
                            mLytLoad.setVisibility(View.GONE);
                        }
                    }
                });
            }


        });


        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                SetList<PicItem> picItems = new SetList<>();
//
//                for (String key : mItemMap.keySet()) {
//                    for (PicItem item : mItemMap.get(key)) {
//                        if (item.selected) {
//                            picItems.add(item);
//                        }
//                    }
//                }
                PhotoPicker.sPhotoListener.onPicked(new ArrayList<>(MediaListHolder.selectPhotos));
                MediaListHolder.selectPhotos.clear();
                finish();
            }
        });

        mPicType.setText(video ? R.string.picker_all_video : R.string.picker_all_image);

        mPicType.setEnabled(true);
        mPicType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCatalog();
            }
        });

        if (preview) {
            mPreviewBtn.setVisibility(View.VISIBLE);
        } else {
            mPreviewBtn.setVisibility(View.GONE);
        }

        mPreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PicItemHolder.itemList = new SetList<>();
//
//                for (String key : mItemMap.keySet()) {
//                    for (PicItem item : mItemMap.get(key)) {
//                        if (item.selected) {
//                            PicItemHolder.itemList.add(item);
//                        }
//                    }
//                }
//
//                PicItem item = PicItemHolder.itemList.get(0);
//
//                int index = mAllItemList.indexOf(item);
//
//                PicItemHolder.itemSelectedList = null;

                Photo item = MediaListHolder.selectPhotos.get(0);
                Intent intent = new Intent(PictureSelectorActivity.this, PicturePreviewActivity.class);
//                intent.putExtra("sendOrigin", mSendOrigin);
                intent.putExtra("isGif", item.isGif());
                intent.putExtra("max", max);
                intent.putExtra("isPreview", true);

//                if (Build.VERSION.SDK_INT >= 22) {
//                    View view = mLayoutManager.findViewByPosition(index + 1);
//                    ImageView iv = view.findViewById(R.id.picker_photo_image);
//                    String uri = item.getUri();
//
////                    Pair pair = new Pair<>(iv, uri);
////                    ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
////                            PictureSelectorActivity.this, pair);
//
//                    ActivityOptionsCompat options = ActivityOptionsCompat
//                            .makeSceneTransitionAnimation(PictureSelectorActivity.this, iv, uri);// mAdapter.get(position).getUrl()
////                        startActivity(intent, options.toBundle());
//                    ActivityCompat.startActivityForResult(PictureSelectorActivity.this, intent, REQUEST_PREVIEW, options.toBundle());
////                        startActivityForResult(intent, REQUEST_PREVIEW, activityOptions.toBundle());
//
////                    ActivityOptionsCompat options = ActivityOptionsCompat
////                            .makeSceneTransitionAnimation(PictureSelectorActivity.this, iv, uri);// mAdapter.get(position).getUrl()
////                    startActivityForResult(intent, REQUEST_PREVIEW, options.toBundle());
//                } else {
//                }
                startActivity(intent);
            }
        });


        mToolbarMask.setOnClickListener(new MaskClickListener());
        mBottomBarMask.setOnClickListener(new MaskClickListener());
        mCatalogMask.setOnClickListener(new MaskClickListener());

        mCatalogListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    View first_view = mCatalogListView.getChildAt(0);
                    canDown = first_view != null && first_view.getTop() == 0;
                } else {
                    canDown = false;
                }
            }
        });
        mCatalogListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        listLastY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {//判断是否在滑动
                            if (canDown && event.getY() - listLastY >= 20) {//判断到达顶部后是否又向下滑动了20像素 可以修改
                                hideCatalog();
                                return true;
                            }
                        }
                        break;
                }
                return false;
            }
        });
    }

    private float listLastY;
    private int mScrollState;
    private boolean canDown;

    private class MaskClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            hideCatalog();
        }
    }

    private void showCatalog() {
        if (isAnimating) return;
        mCatalogMask.setVisibility(View.VISIBLE);
        mCatalogWindow.setVisibility(View.VISIBLE);
        final ObjectAnimator translationY = ObjectAnimator.ofFloat(mCatalogListView, "translationY", catalogHeight, 0);
        translationY.setDuration(300);
        translationY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimating = false;
                translationY.removeAllListeners();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mToolbarMask.setVisibility(View.VISIBLE);
                mBottomBarMask.setVisibility(View.VISIBLE);
                isAnimating = true;
                isShowing = true;
            }
        });
        translationY.start();
    }

    private boolean isAnimating = false;
    private boolean isShowing = false;

    private void hideCatalog() {
        if (isAnimating) return;
        final ObjectAnimator translationY = ObjectAnimator.ofFloat(mCatalogListView, "translationY", 0, catalogHeight);
        translationY.setDuration(300);
        translationY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mToolbarMask.setVisibility(View.GONE);
                mBottomBarMask.setVisibility(View.GONE);
                mCatalogMask.setVisibility(View.GONE);
                mCatalogWindow.setVisibility(View.GONE);
                isAnimating = false;
                isShowing = false;
                translationY.removeAllListeners();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isAnimating = true;
            }
        });
        translationY.start();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                if (mTakePictureUri != null) {
                    String path = mTakePictureUri.getEncodedPath();// getPathFromUri(this, mTakePhotoUri);

                    if (path == null) {
                        Toast.makeText(this, video ?
                                        R.string.picker_video_failure :
                                        R.string.picker_photo_failure,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mTakePictureUri.toString().startsWith("content")) {
                        path = path.replaceAll("/external_storage_root", "");

                        path = Environment.getExternalStorageDirectory() + path;
                    }

                    final File file = new File(path);

                    if (file.exists()) {
                        MediaScannerConnection.scanFile(this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(final String path, Uri uri) {
                                getPhoto(path);
                            }
                        });

                    } else {
                        Toast.makeText(this, video ?
                                        R.string.picker_video_failure :
                                        R.string.picker_photo_failure,
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    }

    private void getPhoto(final String path) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString("path", path);
                bundle.putBoolean("video", video);

                MediaStoreHelper.getPhoto(PictureSelectorActivity.this, bundle, new MediaStoreHelper.PhotoSingleCallback() {
                    @Override
                    public void onResultCallback(@Nullable Photo photo, int updateIndex) {
                        if (photo == null) {
                            Toast.makeText(PictureSelectorActivity.this, video ?
                                            R.string.picker_video_failure : R.string.picker_photo_failure,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (selectCateIndex == 0) {
                            if (MediaListHolder.currentPhotos.isEmpty()) {
                                MediaListHolder.currentPhotos.add(photo);
                            } else {
                                MediaListHolder.currentPhotos.add(0, photo);
                            }
                        } else {
                            if (selectCateIndex == updateIndex) {
                                if (MediaListHolder.currentPhotos.isEmpty()) {
                                    MediaListHolder.currentPhotos.add(photo);
                                } else {
                                    MediaListHolder.currentPhotos.add(0, photo);
                                }
                            }
                        }

                        mGridViewAdapter.notifyDataSetChanged();
                        mCatalogAdapter.notifyDataSetChanged();
                        updateToolbar();
                        MediaStoreHelper.destroyLoader(PictureSelectorActivity.this, 0);
                    }
                });
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (isShowing) {
            hideCatalog();
            return;
        }
        super.onBackPressed();
    }

    protected void requestCamera() {
        if (!CommonUtils.existSDCard()) {
            Toast.makeText(this, R.string.picker_empty_sdcard, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = video ? new Intent(MediaStore.ACTION_VIDEO_CAPTURE) : new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            path.mkdirs();
        }

        String name = "IMG-" + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + (video ? ".mp4" : ".jpg");
        File file = new File(path, name);
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resInfoList.size() <= 0) {
            Toast.makeText(this, getResources().getString(R.string.picker_voip_cpu_error), Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mTakePictureUri = MyFileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".demo.file_provider", file);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                mTakePictureUri = Uri.fromFile(file);
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePictureUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }


    public class SelectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case PICKER_ACTION_MEDIA_SELECT: {
                    Photo photo = intent.getParcelableExtra(PICKER_EXTRA_PHOTO);
                    if (mGridViewAdapter != null) {
                        int index;

                        index = MediaListHolder.currentPhotos.indexOf(photo);

                        mGridViewAdapter.notifyItemChanged(index);
                        updateToolbar();
                    }
                }
                break;
                case PICKER_ACTION_MEDIA_ADD: {
                    Photo photo = intent.getParcelableExtra(PICKER_EXTRA_PHOTO);
//                    MediaListHolder.selectPhotos.add(photo);
                    int updateIndex = intent.getIntExtra(PICKER_EXTRA_UPDATE_INDEX, selectCateIndex);
                    if (selectCateIndex == 0) {
                        if (MediaListHolder.currentPhotos.isEmpty()) {
                            MediaListHolder.currentPhotos.add(photo);
                        } else {
                            MediaListHolder.currentPhotos.add(0, photo);
                        }
                    } else {
                        if (selectCateIndex == updateIndex) {
                            if (MediaListHolder.currentPhotos.isEmpty()) {
                                MediaListHolder.currentPhotos.add(photo);
                            } else {
                                MediaListHolder.currentPhotos.add(0, photo);
                            }
                        }
                    }


                    mGridViewAdapter.notifyDataSetChanged();
                    mCatalogAdapter.notifyDataSetChanged();
                    updateToolbar();
                }
                break;
                case PICKER_ACTION_MEDIA_SEND: {
                    PhotoPicker.sPhotoListener.onPicked(new ArrayList<>(MediaListHolder.selectPhotos));
                    MediaListHolder.selectPhotos.clear();
                    finish();
                }
                break;
            }


        }
    }


    private int getTotalSelectedNum() {
        return MediaListHolder.selectPhotos.size();
    }

    private void updateToolbar() {
        int sum = getTotalSelectedNum();
        if (sum == 0) {
            mBtnSend.setEnabled(false);
            mBtnSend.setText(R.string.picker_picsel_toolbar_send);
            mPreviewBtn.setEnabled(false);
            mPreviewBtn.setText(R.string.picker_picsel_toolbar_preview);
        } else if (sum <= max) {
            mBtnSend.setEnabled(true);
            mBtnSend.setText(getResources().getString(R.string.picker_picsel_toolbar_send_num, sum, max));
            mPreviewBtn.setEnabled(true);
            mPreviewBtn.setText(String.format(getResources().getString(R.string.picker_picsel_toolbar_preview_num), sum));
        }

    }


    private Context getContext() {
        return this;
    }

    protected void onDestroy() {
        super.onDestroy();
        PhotoPicker.destroy();
        unregisterReceiver(mSelectReceiver);
    }


    public static class PreviewBtn extends LinearLayout {
        private TextView mText;

        public PreviewBtn(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void init(Activity root) {
            mText = root.findViewById(R.id.picker_preview_text);
        }

        public void setText(int id) {
            mText.setText(id);
        }

        public void setText(String text) {
            mText.setText(text);
        }

        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            mText.setEnabled(enabled);
        }

        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouchEvent(MotionEvent event) {
            if (isEnabled()) {
                switch (event.getAction()) {
                    case 0:
                        mText.setVisibility(INVISIBLE);
                        break;
                    case 1:
                        mText.setVisibility(VISIBLE);
                }
            }

            return super.onTouchEvent(event);
        }
    }

    public static class PicTypeBtn extends LinearLayout {
        TextView mText;

        public PicTypeBtn(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void init(Activity root) {
            mText = root.findViewById(R.id.picker_type_text);
        }

        public void setText(String text) {
            mText.setText(text);
        }

        public void setText(@StringRes int text) {
            mText.setText(text);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            mText.setEnabled(enabled);
        }

        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouchEvent(MotionEvent event) {
            if (isEnabled()) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mText.setVisibility(INVISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        mText.setVisibility(VISIBLE);
                }
            }

            return super.onTouchEvent(event);
        }
    }


    private int selectCateIndex = 0;

    private class CatalogAdapter extends BaseAdapter {
        private LayoutInflater mInflater = getLayoutInflater();

        public int getCount() {

            return MediaListHolder.allDirectories.size();
        }

        public PhotoDirectory getItem(int position) {
            return MediaListHolder.allDirectories.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.picker_item_lst_catalog, parent, false);
                holder = new ViewHolder(view);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.bind(position);

            return view;
        }

        private class ViewHolder {
            ImageView image;
            TextView tvName;
            TextView tvNumber;
            ImageView selected;
            View itemView;

            private ViewHolder(View itemView) {
                this.itemView = itemView;
                image = itemView.findViewById(R.id.picker_catalog_image);
                tvName = itemView.findViewById(R.id.picker_catalog_name);
                tvNumber = itemView.findViewById(R.id.picker_catalog_photo_number);
                selected = itemView.findViewById(R.id.picker_catalog_selected);
            }

            void bind(final int position) {

                boolean showSelected = selectCateIndex == position;

                PhotoDirectory item = getItem(position);

                Glide.with(getContext())
                        .asBitmap()
                        .load(item.getCoverPath())
                        .thumbnail(0.2f)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.picker_grid_image_default)
                                .error(R.drawable.picker_grid_image_default))
                        .into(image);

                if (position == 0) {
                    tvNumber.setVisibility(View.GONE);
                } else {
                    tvNumber.setVisibility(View.VISIBLE);
                    tvNumber.setText(String.format(getResources().getString(R.string.picker_picsel_catalog_number), item.getPhotos().size()));
                }

                tvName.setText(item.getName());
                selected.setVisibility(showSelected ? View.VISIBLE : View.INVISIBLE);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position == selectCateIndex) {
                            hideCatalog();
                        } else {
                            selectCateIndex = position;
                            mPicType.setText(tvName.getText().toString());

                            MediaListHolder.currentPhotos.clear();
                            MediaListHolder.currentPhotos.addAll(MediaListHolder.allDirectories.get(position).getPhotos());

                            mCatalogAdapter.notifyDataSetChanged();
                            mGridViewAdapter.notifyDataSetChanged();
                            hideCatalog();
                        }
                    }
                });
            }
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        ImageView image;
        View mask;
        AppCompatCheckBox checkBox;
        View itemView;
        ImageView ivGif;
        RelativeLayout lytVideo;
        TextView tvTime;

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            image = itemView.findViewById(R.id.picker_photo_image);
            mask = itemView.findViewById(R.id.picker_item_mask);
            checkBox = itemView.findViewById(R.id.picker_item_checkbox);
            ivGif = itemView.findViewById(R.id.picker_iv_gif);
            lytVideo = itemView.findViewById(R.id.picker_lyt_video);
            tvTime = itemView.findViewById(R.id.picker_video_time);
            if (video) {
                checkBox.setVisibility(View.GONE);
                lytVideo.setVisibility(View.VISIBLE);
            }
        }

        void bind() {
            int adapterPosition = getAdapterPosition();
            final int position;
            if (isShowCamera) {
                position = adapterPosition - 1;
            } else {
                position = adapterPosition;
            }

            final Photo item = MediaListHolder.currentPhotos.get(position);

            if (item.isGif()) {
                ivGif.setVisibility(View.VISIBLE);
            } else {
                ivGif.setVisibility(View.GONE);
            }

            String uri = item.getUri();

            Glide.with(getContext())
                    .asBitmap()
                    .load(new File(uri))
                    .thumbnail(0.5f)
                    .apply(new RequestOptions()
                            .error(R.drawable.picker_grid_image_default)
                            .placeholder(R.drawable.picker_grid_image_default))
                    .into(image);


            checkBox.setChecked(MediaListHolder.selectPhotos.contains(item));

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isPressed()) {
                        if (getTotalSelectedNum() == max && isChecked) {
                            Toast.makeText(getApplicationContext(), getString(R.string.picker_picsel_selected_max, max), Toast.LENGTH_SHORT).show();
                            buttonView.setChecked(false);
                        } else {
//                            item.setSelected(isChecked);
                            if (isChecked) {
                                MediaListHolder.selectPhotos.add(item);
                            } else {
                                MediaListHolder.selectPhotos.remove(item);
                            }
                        }
                        if (MediaListHolder.selectPhotos.contains(item)) {
                            mask.setBackgroundColor(getResources().getColor(R.color.picker_picsel_grid_mask_pressed));
                        } else {
                            mask.setBackgroundResource(R.drawable.picker_sp_grid_mask);
                        }

                        updateToolbar();
                    }

                }
            });


            if (MediaListHolder.selectPhotos.contains(item)) {
                mask.setBackgroundColor(getResources().getColor(R.color.picker_picsel_grid_mask_pressed));
            } else {
                mask.setBackgroundResource(R.drawable.picker_sp_grid_mask);
            }

            mask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (preview) {
                        Intent intent = new Intent(PictureSelectorActivity.this, PicturePreviewActivity.class);
                        intent.putExtra("index", position);
                        intent.putExtra("isGif", item.isGif());
                        intent.putExtra("max", max);

//                    if (Build.VERSION.SDK_INT >= 22) {
////                        View view = mLayoutManager.findViewByPosition(position);
////                        ImageView iv = view.findViewById(R.id.picker_photo_image);
//                        String uri = PicItemHolder.itemList.get(position - 1).getUri();
//
////                        Pair pair = new Pair<>(iv, uri);
////                        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
////                                PictureSelectorActivity.this, pair);
//
//                        ActivityOptionsCompat options = ActivityOptionsCompat
//                                .makeSceneTransitionAnimation(PictureSelectorActivity.this, itemView, uri);// mAdapter.get(position).getUrl()
//                        ActivityCompat.startActivityForResult(PictureSelectorActivity.this, intent, REQUEST_PREVIEW, options.toBundle());
//                    } else {
//                    }
                        startActivity(intent);
                    } else {
                        if (max == 1) {
                            ArrayList<Photo> list = new ArrayList<>();
                            list.add(item);
                            PhotoPicker.sPhotoListener.onPicked(list);
                            finish();
                        } else {
                            checkBox.toggle();
                            boolean isChecked = checkBox.isChecked();
                            if (getTotalSelectedNum() == max && isChecked) {
                                Toast.makeText(getApplicationContext(), getString(R.string.picker_picsel_selected_max, max), Toast.LENGTH_SHORT).show();
                                checkBox.setChecked(false);
                            } else {
//                            item.setSelected(isChecked);
                                if (isChecked) {
                                    MediaListHolder.selectPhotos.add(item);
                                } else {
                                    MediaListHolder.selectPhotos.remove(item);
                                }
                            }
                            if (MediaListHolder.selectPhotos.contains(item)) {
                                mask.setBackgroundColor(getResources().getColor(R.color.picker_picsel_grid_mask_pressed));
                            } else {
                                mask.setBackgroundResource(R.drawable.picker_sp_grid_mask);
                            }

                            updateToolbar();
                        }
                    }


                }
            });


            if (video) {
                tvTime.setText(CommonUtils.format(item.getDuration()));
                mask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaListHolder.selectPhotos.add(item);
                        PhotoPicker.sPhotoListener.onPicked(new ArrayList<>(MediaListHolder.selectPhotos));
                        MediaListHolder.selectPhotos.clear();
                        finish();
                    }
                });
            }
        }
    }


    class CameraHolder extends RecyclerView.ViewHolder {

        private final ImageButton mMask;
        private final TextView mTvTitle;

        CameraHolder(@NonNull View itemView) {
            super(itemView);
            mMask = itemView.findViewById(R.id.picker_camera_mask);
            mTvTitle = itemView.findViewById(R.id.picker_take_picture);

        }

        void bind() {
            mTvTitle.setText(video ?
                    R.string.picker_picsel_record_video :
                    R.string.picker_picsel_take_picture);
            mMask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new PermissionUtils(PictureSelectorActivity.this)
                            .setPermissionListener(new PermissionUtils.PermissionListener() {
                                @Override
                                public void onResult() {
                                    requestCamera();
                                }
                            })
                            .requestPermission(Permission.CAMERA);
                }
            });

        }
    }

    private class GridViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater mInflater = getLayoutInflater();

        @Override
        public int getItemViewType(int position) {
            if (!isShowCamera) return 1;
            else {
                if (position == 0) {
                    return 0;
                } else
                    return 1;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final RecyclerView.ViewHolder holder;

            if (viewType == 0) {
                View cameraView = mInflater.inflate(R.layout.picker_grid_camera, parent, false);

                holder = new CameraHolder(cameraView);
            } else {
                View convertView = mInflater.inflate(R.layout.picker_grid_item, parent, false);
                holder = new ItemHolder(convertView);
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof PictureSelectorActivity.ItemHolder) {
                ((ItemHolder) viewHolder).bind();
            } else if (viewHolder instanceof CameraHolder) {
                ((CameraHolder) viewHolder).bind();
            }
        }

        @Override
        public int getItemCount() {
            return MediaListHolder.currentPhotos.size() + (isShowCamera ? 1 : 0);
        }

    }
}
