package com.hy.picker.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hy.picker.R;

import com.hy.picker.core.CrystalCategory;

/**
 * Created time : 2018/8/27 15:42.
 *
 * @author HY
 */
public class CrystalCategoryAdapter extends BaseRecyclerAdapter<CrystalCategory.Category, CrystalCategoryAdapter.ViewHolder> {


    @NonNull
    @Override
    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view);
    }

    @Override
    protected int layout() {
        return R.layout.picker_item_category;
    }

    public class ViewHolder extends BaseRecyclerAdapter.BaseViewHolder {
        private ImageView mIvCrystal;
        private TextView mTvName;

        ViewHolder(View itemView) {
            super(itemView);
            mIvCrystal = itemView.findViewById(R.id.picker_iv_crystal);
            mTvName = itemView.findViewById(R.id.picker_tv_name);
        }

        @Override
        public void bind() {
            final CrystalCategory.Category item = getItem(getAdapterPosition());
            Glide.with(mContext)
                    .load(item.getImage())
                    .into(mIvCrystal);
            mTvName.setText(item.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnItemClickListener) mOnItemClickListener.onClick(item);
                }
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
