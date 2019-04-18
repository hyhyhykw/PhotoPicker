package com.hy.picker.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hy.picker.PhotoContext;
import com.hy.picker.PictureSelectorActivity;
import com.hy.picker.R;
import com.hy.picker.core.util.SizeUtils;
import com.hy.picker.utils.OnItemClickListener;
import com.hy.picker.model.PhotoDirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created time : 2019/4/18 12:31 PM.
 *
 * @author HY
 */
public class CateDlgAdapter extends BaseAdapter {
    private final List<PhotoDirectory> mDirectories = new ArrayList<>();

    private final Drawable mDefaultDrawable;

    public CateDlgAdapter(Drawable defaultDrawable) {
        mDefaultDrawable = defaultDrawable;
    }

    public void reset(List<PhotoDirectory> directories) {
        mDirectories.clear();
        mDirectories.addAll(directories);
        notifyDataSetChanged();
    }

    public void add(int index, PhotoDirectory directory) {
        mDirectories.add(index, directory);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDirectories.size();
    }

    @Override
    public PhotoDirectory getItem(int position) {
        return mDirectories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private int selectCateIndex = 0;

    private void selected(int index) {
        selectCateIndex = index;
        notifyDataSetChanged();
    }
    private int dp75;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        if (dp75==0){
            dp75= SizeUtils.px2dp(context,75);
        }

        View view = convertView;
        ViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.picker_item_lst_catalog, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bind(position);

        return view;
    }


    private class ViewHolder {
        SimpleDraweeView image;
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

        void bind(int position) {

            boolean showSelected = selectCateIndex == position;

            PhotoDirectory item = getItem(position);

            GenericDraweeHierarchy hierarchy = image.getHierarchy();
            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            image.setController(PictureSelectorActivity.
                    getDraweeController(image,
                            Uri.fromFile(new File(item.getCoverPath())),
                            dp75, dp75));
            tvNumber.setText(String.format(
                    PhotoContext.getContext().
                            getResources()
                            .getString(R.string.picker_picsel_catalog_number),
                    item.getPhotos().size()));

            tvName.setText(item.getName());
            selected.setVisibility(showSelected ? View.VISIBLE : View.INVISIBLE);

            itemView.setOnClickListener(v -> {
                if (position == selectCateIndex) {
//                    hideCatalog();
                    if (null!=mOnItemClickListener){
                        mOnItemClickListener.onClick(position,false);
                    }
                } else {
//                    hideCatalog();
                    if (null!=mOnItemClickListener){
                        mOnItemClickListener.onClick(position,true);
                    }
//                    selectCateIndex = position;
//                    mPicType.setText(tvName.getText().toString());
//                    MediaListHolder.currentPhotos.clear();
//                    MediaListHolder.currentPhotos.addAll(MediaListHolder.allDirectories.get(position).getPhotos());
//
//                    mCatalogAdapter.notifyDataSetChanged();
//                    mGridViewAdapter.notifyDataSetChanged();
//                    GridLayoutManager layoutManager = (GridLayoutManager) mGridView.getLayoutManager();
//                    if (layoutManager.findFirstVisibleItemPosition() != 0) {
//                        Fresco.getImagePipeline().pause();
//                        CommonUtils.postDelay(() -> mGridView.smoothScrollToPosition(0), 350);
//                    }
                    selected(position);
                }
            });
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
}
