package com.hy.picker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.content.FileProvider;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
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
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.Logger;
import com.hy.picker.utils.ObjectsUtils;
import com.hy.picker.utils.PermissionUtils;
import com.hy.picker.utils.SetList;
import com.hy.picker.utils.SizeUtils;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class PictureSelectorActivity extends AppCompatActivity {
    public static final int REQUEST_PREVIEW = 0;
    public static final int REQUEST_CAMERA = 1;
    private GridView mGridView;
    private ImageButton mBtnBack;
    private TextView mBtnSend;
    private PicTypeBtn mPicType;
    private PreviewBtn mPreviewBtn;
    private ListView mCatalogListView;
    private SetList<PicItem> mAllItemList;
    private Map<String, List<PicItem>> mItemMap;
    private SetList<String> mCatalogList;
    private String mCurrentCatalog = "";
    private Uri mTakePictureUri;
    //    private boolean mSendOrigin = false;
    private int max;
    private ArrayList<PicItem> mSelectItems;

    private LinearLayout mLytLoad;
    private boolean gif;
    private GridViewAdapter mGridViewAdapter;
    private CatalogAdapter mCatalogAdapter;

    private View mToolbarMask;
    private View mBottomBarMask;
    private View mCatalogMask;
    private int catalogHeight;

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_selector);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        catalogHeight = PhotoContext.getScreenHeight() - SizeUtils.dp2px(this, 145) - CommonUtils.getStatusBarHeight(this);

        Intent intent = getIntent();
        max = intent.getIntExtra("max", 9);
        gif = intent.getBooleanExtra("gif", true);
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
                finish();
            }
        });
        mBtnSend = findViewById(R.id.picker_send);
        mPicType = findViewById(R.id.picker_pic_type);
        mPicType.init(this);
        mPicType.setEnabled(false);
        mPreviewBtn = findViewById(R.id.picker_preview);
        mPreviewBtn.init(this);
        mPreviewBtn.setEnabled(null != mSelectItems && !mSelectItems.isEmpty());

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
                        .requestPermission(Permission.READ_EXTERNAL_STORAGE);
                return false;
            }
        });


    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        updatePictureItems();
        if (mLytLoad.getVisibility() == View.VISIBLE) {
            mLytLoad.setVisibility(View.GONE);
        }
        mGridViewAdapter = new GridViewAdapter();
        mGridView.setAdapter(mGridViewAdapter);

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetList<PicItem> picItems = new SetList<>();

                for (String key : mItemMap.keySet()) {
                    for (PicItem item : mItemMap.get(key)) {
                        if (item.selected) {
                            picItems.add(item);
                        }
                    }
                }
                PhotoPicker.sPhotoListener.onPicked(picItems);
                finish();
            }
        });

        mPicType.setEnabled(true);
        mPicType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCatalog();
            }
        });

        mPreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PicItemHolder.itemList = new SetList<>();

                for (String key : mItemMap.keySet()) {
                    for (PicItem item : mItemMap.get(key)) {
                        if (item.selected) {
                            PicItemHolder.itemList.add(item);
                        }
                    }
                }


                PicItemHolder.itemSelectedList = null;
                Intent intent = new Intent(PictureSelectorActivity.this, PicturePreviewActivity.class);
//                intent.putExtra("sendOrigin", mSendOrigin);
                intent.putExtra("isGif", PicItemHolder.itemList.get(0).isGif());
                intent.putExtra("max", max);
                startActivityForResult(intent, REQUEST_PREVIEW);
            }
        });


        mCatalogAdapter = new CatalogAdapter();
        mCatalogListView.setAdapter(mCatalogAdapter);
        mCatalogListView.setTranslationY(catalogHeight);
        mCatalogListView.setVisibility(View.VISIBLE);
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
                mCatalogMask.setVisibility(View.VISIBLE);
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
        if (resultCode == PicturePreviewActivity.RESULT_SEND) {
            finish();
        } else if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PREVIEW:
//                        mSendOrigin = data.getBooleanExtra("sendOrigin", false);
                    SetList<PicItem> list = PicItemHolder.itemList;
                    if (list == null) {
                        return;
                    }

                    for (PicItem it : list) {
                        PicItem item = findByUri(it.uri);
                        if (item != null) {
                            item.selected = it.selected;
                        }
                    }

                    mGridViewAdapter.notifyDataSetChanged();
                    mCatalogAdapter.notifyDataSetChanged();

                    updateToolbar();
                    break;
                case REQUEST_CAMERA:
                    if (mTakePictureUri != null) {
                        String path = mTakePictureUri.getEncodedPath();// getPathFromUri(this, mTakePhotoUri);

                        if (mTakePictureUri.toString().startsWith("content")) {
                            path = path.replaceAll("/external_storage_root", "");

                            path = Environment.getExternalStorageDirectory() + path;
                        }
                        Logger.e("path==" + path);
                        if (new File(path).exists()) {
                            PicItemHolder.itemList = new SetList<>();
                            PicItem item = new PicItem();
                            item.uri = path;
                            PicItemHolder.itemList.add(item);
                            PicItemHolder.itemSelectedList = null;
                            Intent intent = new Intent(this, PicturePreviewActivity.class);
                            intent.putExtra("max", max);
                            startActivityForResult(intent, REQUEST_PREVIEW);

                            MediaScannerConnection.scanFile(this, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    updatePictureItems();
                                }
                            });
                        } else {
                            Toast.makeText(this, R.string.picker_photo_failure, Toast.LENGTH_SHORT).show();
                        }

                    }
            }
        }
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

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!path.exists()) {
            path.mkdirs();
        }

        String name = "IMG-" + CommonUtils.format(new Date(), "yyyy-MM-dd-HHmmss") + ".jpg";
        File file = new File(path, name);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resInfoList.size() <= 0) {
            Toast.makeText(this, getResources().getString(R.string.picker_voip_cpu_error), Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mTakePictureUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".file_provider", file);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                mTakePictureUri = Uri.fromFile(file);
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePictureUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private void updatePictureItems() {
        String[] projection = new String[]{"_data", "date_added"};
        String orderBy = "datetaken DESC";
        Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection, null, null, orderBy);
        mAllItemList = new SetList<>();
        mCatalogList = new SetList<>();
        mItemMap = new ArrayMap<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    PicItem item = new PicItem();
                    item.uri = cursor.getString(0);
                    if (item.uri == null) {
                        continue;
                    }

                    if (!gif && item.isGif()) {
                        continue;
                    }

                    File file = new File(item.uri);

                    if (!file.exists() || file.length() == 0L) {
                        continue;
                    }

                    if (null != mSelectItems && !mSelectItems.isEmpty()) {
                        boolean remove = mSelectItems.remove(item);
                        item.setSelected(remove);
                    }
                    mAllItemList.add(item);
                    int last = item.uri.lastIndexOf("/");
                    if (last != -1) {
                        String catalog;
                        if (last == 0) {
                            catalog = "/";
                        } else {
                            int secondLast = item.uri.lastIndexOf("/", last - 1);
                            catalog = item.uri.substring(secondLast + 1, last);
                        }

                        if (mItemMap.containsKey(catalog)) {
                            mItemMap.get(catalog).add(item);
                        } else {
                            SetList<PicItem> itemList = new SetList<>();
                            itemList.add(item);
                            mItemMap.put(catalog, itemList);
                            mCatalogList.add(catalog);
                        }
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

    }

    private int getTotalSelectedNum() {
        int sum = 0;

        for (String key : mItemMap.keySet()) {
            for (PicItem item : mItemMap.get(key)) {
                if (item.selected) {
                    ++sum;
                }
            }
        }

        return sum;
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

//    private PicItem getItemAt(int index) {
//
//        for (String key : mItemMap.keySet()) {
//            List<PicItem> picItems = mItemMap.get(key);
//            for (int i = 0; i < picItems.size(); i++) {
//                if (i == index) {
//                    return picItems.get(i);
//                }
//            }
//        }
//
//        return null;
//    }

    private PicItem getItemAt(String catalog, int index) {
        if (!mItemMap.containsKey(catalog)) {
            return null;
        } else {
            List<PicItem> picItems = mItemMap.get(catalog);
            if (index >= picItems.size()) {
                return null;
            } else {
                return picItems.get(index);
            }
        }
    }

    private PicItem findByUri(String uri) {
        for (String key : mItemMap.keySet()) {
            for (PicItem item : mItemMap.get(key)) {
                if (item.uri.equals(uri)) {
                    return item;
                }
            }
        }
        return null;
    }

    private Context getContext() {
        return this;
    }

    protected void onDestroy() {
        PicItemHolder.itemList = null;
        PicItemHolder.itemSelectedList = null;
        super.onDestroy();
    }

    public static class PicItemHolder {
        public static SetList<PicItem> itemList;
        public static SetList<PicItem> itemSelectedList;

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

    public static class PicItem implements Parcelable {
        String uri;
        boolean selected;
        public static final Creator<PicItem> CREATOR = new Creator<PicItem>() {
            public PicItem createFromParcel(Parcel source) {
                return new PicItem(source);
            }

            public PicItem[] newArray(int size) {
                return new PicItem[size];
            }
        };

        public boolean isGif() {
            return uri.toLowerCase().endsWith(".gif");
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public int describeContents() {
            return 0;
        }

        public PicItem() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PicItem picItem = (PicItem) o;
            return ObjectsUtils.equals(uri, picItem.uri);
        }

        @Override
        public int hashCode() {
            return ObjectsUtils.hash(uri);
        }

        public PicItem(Parcel in) {
            uri = in.readString();
            selected = in.readInt() == 1;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(uri);
            dest.writeInt(selected ? 1 : 0);
        }
    }

    private class CatalogAdapter extends BaseAdapter {
        private LayoutInflater mInflater = getLayoutInflater();

        public int getCount() {
            return mItemMap.size() + 1;
        }

        public Object getItem(int position) {
            return null;
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
                String path;
                int num = 0;
                boolean showSelected;
                String name;
                if (position == 0) {
                    if (mItemMap.size() == 0) {
                        image.setImageResource(R.drawable.picker_picsel_empty_pic);
                    } else {
                        path = ((PicItem) ((List) mItemMap.get(mCatalogList.get(0))).get(0)).uri;

                        Glide.with(getContext())
                                .asBitmap()
                                .load(path)
                                .thumbnail(0.5f)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.picker_grid_image_default)
                                        .error(R.drawable.picker_grid_image_default))
                                .into(image);
                    }

                    name = getResources().getString(R.string.picker_picsel_catalog_allpic);
                    tvNumber.setVisibility(View.GONE);
                    showSelected = mCurrentCatalog.isEmpty();
                } else {
                    path = ((PicItem) ((List) mItemMap.get(mCatalogList.get(position - 1))).get(0)).uri;
                    name = mCatalogList.get(position - 1);
                    num = mItemMap.get(mCatalogList.get(position - 1)).size();
                    tvNumber.setVisibility(View.VISIBLE);
                    showSelected = name.equals(mCurrentCatalog);

                    Glide.with(getContext())
                            .asBitmap()
                            .load(path)
                            .thumbnail(0.5f)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.picker_grid_image_default)
                                    .error(R.drawable.picker_grid_image_default))
                            .into(image);
                }

                tvName.setText(name);
                tvNumber.setText(String.format(getResources().getString(R.string.picker_picsel_catalog_number), num));
                selected.setVisibility(showSelected ? View.VISIBLE : View.INVISIBLE);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String catalog;
                        if (position == 0) {
                            catalog = "";
                        } else {
                            catalog = mCatalogList.get(position - 1);
                        }

                        if (catalog.equals(mCurrentCatalog)) {
                            hideCatalog();
                        } else {
                            mCurrentCatalog = catalog;

                            mPicType.setText(tvName.getText().toString());
                            hideCatalog();
                            ((CatalogAdapter) mCatalogListView.getAdapter()).notifyDataSetChanged();
                            ((GridViewAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    }

    private class GridViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater = getLayoutInflater();

        public int getCount() {
            int sum = 1;
            String key;
            if (mCurrentCatalog.isEmpty()) {
                for (Iterator var2 = mItemMap.keySet().iterator(); var2.hasNext(); sum += mItemMap.get(key).size()) {
                    key = (String) var2.next();
                }
            } else {
                sum += mItemMap.get(mCurrentCatalog).size();

            }

            return sum;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (position == 0) {
                View cameraView = mInflater.inflate(R.layout.picker_grid_camera, parent, false);
                ImageButton mask = cameraView.findViewById(R.id.picker_camera_mask);

                mask.setOnClickListener(new View.OnClickListener() {
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

                return cameraView;
            } else {
                final ViewHolder holder;
                if (convertView != null && convertView.getTag() != null) {
                    holder = (ViewHolder) convertView.getTag();
                } else {
                    convertView = mInflater.inflate(R.layout.picker_grid_item, parent, false);
                    holder = new ViewHolder(convertView);
                    convertView.setTag(holder);
                }

                holder.bind(position);
                return convertView;
            }
        }

        private class ViewHolder {
            ImageView image;
            View mask;
            AppCompatCheckBox checkBox;
            View itemView;
            ImageView ivGif;

            private ViewHolder(View itemView) {
                this.itemView = itemView;
                image = itemView.findViewById(R.id.picker_photo_image);
                mask = itemView.findViewById(R.id.mask);
                checkBox = itemView.findViewById(R.id.checkbox);
                ivGif = itemView.findViewById(R.id.iv_gif);
            }


            void bind(final int position) {
                final PicItem item;
                if (mCurrentCatalog.isEmpty()) {
                    item = mAllItemList.get(position - 1);
                } else {
                    item = getItemAt(mCurrentCatalog, position - 1);
                }
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

                checkBox.setChecked(item.selected);

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (buttonView.isPressed()) {
                            if (getTotalSelectedNum() == max && isChecked) {
                                Toast.makeText(getApplicationContext(), getString(R.string.picker_picsel_selected_max, max), Toast.LENGTH_SHORT).show();
                                buttonView.setChecked(false);
                            } else {
                                item.selected = isChecked;
                            }
                            if (item.selected) {
                                mask.setBackgroundColor(getResources().getColor(R.color.picker_picsel_grid_mask_pressed));
                            } else {
                                mask.setBackgroundResource(R.drawable.picker_sp_grid_mask);
                            }

                            updateToolbar();
                        }

                    }
                });

                if (item.selected) {
                    mask.setBackgroundColor(getResources().getColor(R.color.picker_picsel_grid_mask_pressed));
                } else {
                    mask.setBackgroundResource(R.drawable.picker_sp_grid_mask);
                }
                mask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PicItemHolder.itemList = new SetList<>();
                        if (mCurrentCatalog.isEmpty()) {
                            PicItemHolder.itemList.addAll(mAllItemList);
                            PicItemHolder.itemSelectedList = null;
                        } else {
                            PicItemHolder.itemList.addAll(mItemMap.get(mCurrentCatalog));
                            PicItemHolder.itemSelectedList = new SetList<>();

                            for (PicItem item : mItemMap.get(mCurrentCatalog)) {
                                if (item.selected) {
                                    PicItemHolder.itemSelectedList.add(item);
                                }
                            }
                        }

                        Intent intent = new Intent(PictureSelectorActivity.this, PicturePreviewActivity.class);
                        intent.putExtra("index", position - 1);
                        intent.putExtra("isGif", item.isGif());
                        intent.putExtra("max", max);
                        startActivityForResult(intent, REQUEST_PREVIEW);
                    }
                });
            }
        }
    }
}
