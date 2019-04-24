package com.hy.picker;

import android.Manifest;
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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.hy.picker.adapter.CateDlgAdapter;
import com.hy.picker.adapter.PictureAdapter;
import com.hy.picker.core.util.SizeUtils;
import com.hy.picker.model.Photo;
import com.hy.picker.model.PhotoDirectory;
import com.hy.picker.utils.AndroidLifecycleUtils;
import com.hy.picker.utils.AttrsUtils;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.ImgScanListener;
import com.hy.picker.utils.MediaListHolder;
import com.hy.picker.utils.MediaScannerUtils;
import com.hy.picker.utils.MyFileProvider;
import com.hy.picker.utils.MyGridItemDecoration;
import com.hy.picker.utils.Permission;
import com.hy.picker.utils.SingleMediaScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

@SuppressWarnings({"ResultOfMethodCallIgnored", "FieldCanBeLocal"})
public class PictureSelectorActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {
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
    private PictureAdapter mGridViewAdapter;
//    private CatalogAdapter mCatalogAdapter;

    private View mToolbarMask;
    private View mBottomBarMask;
    private View mCatalogMask;
    private int catalogHeight;
    private boolean isShowCamera;

    //    private UpdateReceiver mUpdateReceiver;
    private SelectReceiver mSelectReceiver;
    private Drawable mDefaultDrawable;
    private ImageView mIvType;
//    private int size;
//    private int dp75;

    private CateDlgAdapter mCateDlgAdapter;
    private int mSize;
    private int mCount;

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
//        int dp4 = SizeUtils.dp2px(this, 4);
//        size = (PhotoContext.getScreenWidth() - dp4 * 3) / 4;

//        dp75 = SizeUtils.px2dp(this, 75);


        mDefaultDrawable = AttrsUtils.getTypeValueDrawable(this, R.attr.picker_image_default);
        Drawable typeDrawable = AttrsUtils.getTypeValueDrawable(this, R.attr.picker_preview_type);
        if (null == mDefaultDrawable) {
            mDefaultDrawable = ContextCompat.getDrawable(this, R.drawable.picker_grid_image_default);
        }
        int disableColor = AttrsUtils.getTypeValueColor(this, R.attr.picker_preview_color_disable);
        int enableColor = AttrsUtils.getTypeValueColor(this, R.attr.picker_preview_color_enable);

        int[] colors = new int[]{
                disableColor, enableColor
        };

        int[][] states = new int[][]{
                new int[]{
                        -android.R.attr.state_enabled
                },
                new int[]{
                        android.R.attr.state_enabled
                }
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);
        if (null == typeDrawable) {
            typeDrawable = ContextCompat.getDrawable(this, R.drawable.picker_type_selector_white);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        int screenHeight = PhotoContext.getScreenHeight();
        int statusBarHeight = CommonUtils.getStatusBarHeight(this);
        int gridHeight = screenHeight - statusBarHeight - SizeUtils.dp2px(this, 96);
        catalogHeight = screenHeight - SizeUtils.dp2px(this, 145) - statusBarHeight;

        mCount = (int) Math.ceil(gridHeight * 1.0 / mSize);

        Intent intent = getIntent();
        max = intent.getIntExtra(EXTRA_MAX, 9);
        gif = intent.getBooleanExtra(EXTRA_SHOW_GIF, true);
        preview = intent.getBooleanExtra(EXTRA_PREVIEW, true);
        gifOnly = intent.getBooleanExtra(EXTRA_ONLY_GIF, false);
        video = intent.getBooleanExtra(EXTRA_PICK_VIDEO, false);
        isShowCamera = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, false);

        mSelectItems = intent.getParcelableArrayListExtra(EXTRA_ITEMS);

        mLytLoad = findViewById(R.id.picker_photo_load);
        mGridView = findViewById(R.id.picker_photo_grd);
        mBtnBack = findViewById(R.id.picker_back);
        mToolbarMask = findViewById(R.id.picker_toolbar_mask);
        mIvType = findViewById(R.id.picker_preview_type);
        mBottomBarMask = findViewById(R.id.picker_bottom_mask);
        mCatalogMask = findViewById(R.id.picker_catalog_mask);


        mBtnBack.setOnClickListener(v -> onBackPressed());
        mBtnSend = findViewById(R.id.picker_send);
        mTvTitle = findViewById(R.id.picker_title);
        mPicType = findViewById(R.id.picker_pic_type);
        int sendEnableColor = AttrsUtils.getTypeValueColor(this, R.attr.picker_send_color_enable);
        int sendDisableColor = AttrsUtils.getTypeValueColor(this, R.attr.picker_send_color_disable);


        int[] sendColors = {
                sendDisableColor,
                sendEnableColor
        };
        int[][] sendStates = new int[][]{
                new int[]{
                        -android.R.attr.state_enabled
                },
                new int[]{
                        android.R.attr.state_enabled
                }
        };

        ColorStateList sendColorStateList = new ColorStateList(sendStates, sendColors);

        mPicType.init(this, sendColorStateList);
        mPicType.setEnabled(false);
        mPicType.setText(video ? R.string.picker_all_video : R.string.picker_all_image);

        mPreviewBtn = findViewById(R.id.picker_preview);
        mPreviewBtn.init(this, colorStateList);
        mPreviewBtn.setEnabled(null != mSelectItems && !mSelectItems.isEmpty());

        mTvTitle.setText(video ? R.string.picker_picsel_videotype : R.string.picker_picsel_pictype);

        mIvType.setImageDrawable(typeDrawable);
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

        int spanCount = AttrsUtils.getTypeValueInt(this, R.attr.picker_grid_span);

        mSize = (PhotoContext.getScreenWidth() - SizeUtils.dp2px(this, 4) * 3) / 4;
        mGridViewAdapter = new PictureAdapter(max, preview, isShowCamera, video, mDefaultDrawable, mSize);
        mGridViewAdapter.setOnItemListener(new PictureAdapter.OnItemListener() {
            @Override
            public void onItemClick(Photo photo) {
                if (video) {
                    setResult(RESULT_OK, new Intent()
                            .putParcelableArrayListExtra(EXTRA_ITEMS, new ArrayList<>(MediaListHolder.selectPhotos)));
                    finish();
                    return;
                }
                if (!preview && max == 1) {
                    ArrayList<Photo> list = new ArrayList<>();
                    list.add(photo);
                    setResult(RESULT_OK, new Intent()
                            .putParcelableArrayListExtra(EXTRA_ITEMS, list));
                    finish();
                }
            }

            @Override
            public void onItemChecked() {
                updateToolbar();
            }

            @Override
            public void onCameraClick() {
                String[] perms = {Manifest.permission.CAMERA};
                if (EasyPermissions.hasPermissions(PictureSelectorActivity.this, perms)) {
                    requestCamera();
                } else {
                    List<String> permissionNames = Permission.transformText(PictureSelectorActivity.this, perms);
                    String message = getString(R.string.picker_message_permission_rationale, TextUtils.join("\n", permissionNames));
                    EasyPermissions.requestPermissions(
                            PictureSelectorActivity.this,
                            message,
                            RC_CAMERA, Manifest.permission.CAMERA);
                }
            }
        });
        mGridView.setAdapter(mGridViewAdapter);
        mGridView.setLayoutManager(new GridLayoutManager(this, spanCount));
        mGridView.addItemDecoration(new MyGridItemDecoration(this));
        mGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!AndroidLifecycleUtils.canLoadImage(PictureSelectorActivity.this)) return;
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Fresco.getImagePipeline().resume();
                } else {
                    Fresco.getImagePipeline().pause();

                }
            }
        });
//        mCatalogAdapter = new CatalogAdapter();
        mCateDlgAdapter = new CateDlgAdapter(mDefaultDrawable);

        mCateDlgAdapter.setOnItemClickListener((position, isChange) -> {
            hideCatalog();
            selectCateIndex = position;
            if (isChange) {
                PhotoDirectory item = mCateDlgAdapter.getItem(position);
                mPicType.setText(item.getName());
                MediaListHolder.currentPhotos.clear();
                MediaListHolder.currentPhotos.addAll(MediaListHolder.allDirectories.get(position).getPhotos());

                mGridViewAdapter.reset(MediaListHolder.currentPhotos);
                GridLayoutManager layoutManager = (GridLayoutManager) mGridView.getLayoutManager();
                if (layoutManager != null && layoutManager.findFirstVisibleItemPosition() != 0) {
                    Fresco.getImagePipeline().pause();
                    CommonUtils.postDelay(() -> mGridView.smoothScrollToPosition(0), 350);
                }
            }
        });
        mCatalogListView.setAdapter(mCateDlgAdapter);
        mCatalogListView.setTranslationY(catalogHeight);
        mCatalogListView.setVisibility(View.VISIBLE);

        mLytLoad.setVisibility(View.VISIBLE);


        Looper.myQueue().addIdleHandler(() -> {
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            List<String> permissionNames = Permission.transformText(this, perms);
            String message = getString(R.string.picker_message_permission_rationale, TextUtils.join("\n", permissionNames));
            EasyPermissions.requestPermissions(
                    this,
                    message,
                    RC_WRITE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return false;
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        MediaListHolder.selectPhotos.clear();
        if (null != mSelectItems && !mSelectItems.isEmpty()) {
            for (Photo selectItem : mSelectItems) {
                selectItem.setSelected(true);
            }
            MediaListHolder.selectPhotos.addAll(mSelectItems);
        }


        new MediaScannerUtils.Builder(this)
                .gif(gif)
                .gifOnly(gifOnly)
                .video(video)
                .build()
                .scanner(success -> {
                    if (success)
                        runOnUiThread(() -> {
                            mCateDlgAdapter.reset(MediaListHolder.allDirectories);
                            mGridViewAdapter.reset(MediaListHolder.currentPhotos);
                            updateToolbar();
                            if (mLytLoad.getVisibility() == View.VISIBLE) {
                                mLytLoad.setVisibility(View.GONE);
                            }
                        });

                });


        mBtnSend.setOnClickListener(v -> {

            setResult(RESULT_OK, new Intent()
                    .putParcelableArrayListExtra(EXTRA_ITEMS, new ArrayList<>(MediaListHolder.selectPhotos)));
            finish();
        });

        mPicType.setText(video ? R.string.picker_all_video : R.string.picker_all_image);

        mPicType.setEnabled(true);
        mPicType.setOnClickListener(v -> showCatalog());

        if (preview && !video) {
            mPreviewBtn.setVisibility(View.VISIBLE);
        } else {
            mPreviewBtn.setVisibility(View.GONE);
        }

        mPreviewBtn.setOnClickListener(v -> {

            Photo item = MediaListHolder.selectPhotos.get(0);
            Intent intent = new Intent(PictureSelectorActivity.this, PicturePreviewActivity.class);
            intent.putExtra(EXTRA_IS_GIF, item.isGif());
            intent.putExtra(EXTRA_MAX, max);
            intent.putExtra(EXTRA_IS_PREVIEW, true);

            startActivityForResult(intent, PICKER_REQUEST_PREVIEW);
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
        mCatalogListView.setOnTouchListener((v, event) -> {
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
        });
    }


    private float listLastY;
    private int mScrollState;
    private boolean canDown;

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == RC_WRITE_STORAGE) {
            initView();
        } else if (requestCode == RC_CAMERA) {
            requestCamera();
        }
    }


    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (requestCode == RC_WRITE_STORAGE) {
                List<String> permissionNames = Permission.transformText(this, perms.toArray(new String[]{}));
                String message = getString(R.string.picker_message_permission_always_failed, TextUtils.join("\n", permissionNames));

                new AppSettingsDialog.Builder(this)
                        .setRationale(message)
                        .setRequestCode(requestCode)
                        .build()
                        .show();
            }
        } else {
            if (requestCode == RC_WRITE_STORAGE) {
                finish();
            }
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        if (requestCode == RC_WRITE_STORAGE) {
            finish();
        }
    }

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
        if (requestCode == RC_WRITE_STORAGE) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                initView();
            } else {
                Toast.makeText(this, R.string.picker_str_permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }
        if (requestCode == RC_CAMERA) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
                requestCamera();
            }
            return;
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == PICKER_REQUEST_PREVIEW) {
                boolean hasChange = false;
                for (int i = MediaListHolder.selectPhotos.size() - 1; i >= 0; i--) {
                    Photo photo = MediaListHolder.selectPhotos.get(i);
                    if (!photo.isSelected()) {
                        if (!hasChange) {
                            hasChange = true;
                        }
                        MediaListHolder.selectPhotos.remove(i);
                    }
                }

                if (hasChange) {
                    mGridViewAdapter.notifyDataSetChanged();
                    updateToolbar();
                }

            } else if (requestCode == REQUEST_CAMERA) {
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
//                        MediaScannerConnection.scanFile(this, new String[]{path}, null, (path1, uri) -> getPhoto(path1));
                        new SingleMediaScanner(PhotoContext.getContext(), path, new ImgScanListener<PictureSelectorActivity>(this) {
                            @Override
                            protected void onScanFinish(@NonNull PictureSelectorActivity activity, String path) {
                                activity.getPhoto(path);
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
        new MediaScannerUtils.Builder(PictureSelectorActivity.this)
                .path(path)
                .video(video)
                .max(max)
                .build()
                .scanner((photo, updateIndex) -> {
                    if (photo == null) {
                        Toast.makeText(PictureSelectorActivity.this, video ?
                                        R.string.picker_video_failure : R.string.picker_photo_failure,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectCateIndex == 0) {
                        if (MediaListHolder.currentPhotos.isEmpty()) {
                            MediaListHolder.currentPhotos.add(photo);
                            mGridViewAdapter.add(photo);
                        } else {
                            MediaListHolder.currentPhotos.add(0, photo);
                            mGridViewAdapter.add(0, photo);
                        }
                    } else {
                        if (selectCateIndex == updateIndex) {
                            if (MediaListHolder.currentPhotos.isEmpty()) {
                                MediaListHolder.currentPhotos.add(photo);
                                mGridViewAdapter.add(photo);
                            } else {
                                MediaListHolder.currentPhotos.add(0, photo);
                                mGridViewAdapter.add(0, photo);
                            }
                        }
                    }

                    Fresco.getImagePipeline().pause();
                    CommonUtils.postDelay(() -> {
                        mGridView.smoothScrollToPosition(0);
                    }, 50);
                    mCateDlgAdapter.reset(MediaListHolder.allDirectories);
                    updateToolbar();
                });

    }

    @Override
    public void onBackPressed() {
        if (isShowing) {
            hideCatalog();
            return;
        }
        if (mSelectItems != null && !mSelectItems.isEmpty()) {
            setResult(RESULT_OK, new Intent()
                    .putParcelableArrayListExtra(EXTRA_ITEMS, mSelectItems));
        } else {
            setResult(RESULT_CANCELED);
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

        String name = (video ? "VIDEO-" : "IMG-") + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + (video ? ".mp4" : ".jpg");
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
                        int index = MediaListHolder.currentPhotos.indexOf(photo);

                        mGridViewAdapter.notifyItemChanged(isShowCamera ? index + 1 : index);

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
                            mGridViewAdapter.add(photo);
                        } else {
                            MediaListHolder.currentPhotos.add(0, photo);
                            mGridViewAdapter.add(0, photo);
                        }
                    } else {
                        if (selectCateIndex == updateIndex) {
                            if (MediaListHolder.currentPhotos.isEmpty()) {
                                MediaListHolder.currentPhotos.add(photo);
                                mGridViewAdapter.add(photo);
                            } else {
                                MediaListHolder.currentPhotos.add(0, photo);
                                mGridViewAdapter.add(0, photo);
                            }
                        }
                    }

                    if (mGridViewAdapter.getItemCount() > mCount) {
                        Fresco.getImagePipeline().pause();
                    }
                    CommonUtils.postDelay(() -> mGridView.smoothScrollToPosition(0), 50);

                    mCateDlgAdapter.reset(MediaListHolder.allDirectories);
                    updateToolbar();

                }
                break;
                case PICKER_ACTION_MEDIA_SEND: {
                    PictureSelectorActivity.this.setResult(RESULT_OK, new Intent()
                            .putParcelableArrayListExtra(EXTRA_ITEMS, new ArrayList<>(MediaListHolder.selectPhotos)));
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
        unregisterReceiver(mSelectReceiver);
    }


    public static class PreviewBtn extends LinearLayout {
        private TextView mText;

        public PreviewBtn(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void init(Activity root, ColorStateList colorStateList) {
            mText = root.findViewById(R.id.picker_preview_text);
            mText.setTextColor(colorStateList);
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

        public void init(Activity root, ColorStateList colorStateList) {

            mText = root.findViewById(R.id.picker_type_text);

            mText.setTextColor(colorStateList);
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

    public static DraweeController getDraweeController(DraweeView targetView, Uri uri,
                                                       int width, int height) {

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                //根据View的尺寸放缩图片
                .setResizeOptions(new ResizeOptions(width, height))
                .build();

        return Fresco.newDraweeControllerBuilder()
                .setOldController(targetView.getController())
                .setImageRequest(request)
                .setCallerContext(uri)
                .build();
    }
}
