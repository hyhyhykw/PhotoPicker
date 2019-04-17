package com.hy.picker.adapter;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hy.picker.PictureSelectorActivity;
import com.hy.picker.R;
import com.hy.picker.core.CrystalCategory;
import com.hy.picker.core.util.SizeUtils;

import androidx.annotation.NonNull;

/**
 * Created time : 2018/8/27 15:42.
 *
 * @author HY
 */
public class CrystalCategoryAdapter extends BaseRecyclerAdapter<CrystalCategory.Category, CrystalCategoryAdapter.ViewHolder> {

    private final Drawable mDefaultDrawable;

    public CrystalCategoryAdapter(Drawable defaultDrawable) {
        mDefaultDrawable = defaultDrawable;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view);
    }

    @Override
    protected int layout() {
        return R.layout.picker_item_category;
    }

    private int imageSize;

    public class ViewHolder extends BaseRecyclerAdapter.BaseViewHolder {
        private final SimpleDraweeView mIvCrystal;
        private final TextView mTvName;

        ViewHolder(View itemView) {
            super(itemView);
            mIvCrystal = itemView.findViewById(R.id.picker_iv_crystal);
            mTvName = itemView.findViewById(R.id.picker_tv_name);
        }

        @Override
        public void bind() {
            final CrystalCategory.Category item = getItem(getAdapterPosition());
//            Glide.with(mContext)
//                    .load(item.getImage())
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(mIvCrystal);

            if (imageSize == 0) {
                imageSize =  SizeUtils.dp2px(mContext, 50);
            }

            ViewGroup.LayoutParams layoutParams = mIvCrystal.getLayoutParams();
            if (layoutParams.height != imageSize || layoutParams.width != imageSize) {
                layoutParams.width = imageSize;
                layoutParams.height = imageSize;
            }

            GenericDraweeHierarchy hierarchy = mIvCrystal.getHierarchy();
            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);

            mIvCrystal.setController(PictureSelectorActivity
                    .getDraweeController(mIvCrystal, Uri.parse(item.getImage()), imageSize, imageSize));


            mTvName.setText(item.getName());
            itemView.setOnClickListener(v -> {
                if (null != mOnItemClickListener) mOnItemClickListener.onClick(item);
            });
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(CrystalCategory.Category item);
    }
}
