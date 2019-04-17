package com.hy.picker.adapter;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hy.picker.PhotoContext;
import com.hy.picker.PictureSelectorActivity;
import com.hy.picker.R;
import com.hy.picker.core.CrystalResult;
import com.hy.picker.core.ExistBean;
import com.hy.picker.core.util.CrystalDownloadUtils;
import com.hy.picker.core.util.FileUtils;
import com.hy.picker.core.util.SizeUtils;
import com.hy.picker.view.CompletedView;

import androidx.annotation.NonNull;

/**
 * Created time : 2018/8/27 15:53.
 *
 * @author HY
 */
public class CrystalAdapter extends BaseRecyclerAdapter<CrystalResult.Crystal, CrystalAdapter.ViewHolder> {

    private final String cate;
    private final Drawable mDefaultDrawable;

    public CrystalAdapter(String cate, Drawable defaultDrawable) {
        this.cate = cate;
        mDefaultDrawable = defaultDrawable;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view);
    }

    @Override
    protected int layout() {
        return R.layout.picker_item_crystal;
    }


    private int imageSize;

    public class ViewHolder extends BaseRecyclerAdapter.BaseViewHolder {
        private final SimpleDraweeView mIvCrystal;
        private final ImageView mIvDownload;
        private final TextView mTvSize;
        private final TextView mTvWait;
        private final CompletedView mProgress;

        ViewHolder(View itemView) {
            super(itemView);
            mIvCrystal = itemView.findViewById(R.id.picker_iv_crystal);
            mTvSize = itemView.findViewById(R.id.picker_tv_size);
            mProgress = itemView.findViewById(R.id.picker_download_progress);
            mTvWait = itemView.findViewById(R.id.picker_tv_wait);
            mIvDownload = itemView.findViewById(R.id.picker_iv_download);
        }

        @Override
        public void bind() {
            final int position = getAdapterPosition();
            CrystalResult.Crystal item = getItem(position);

            if (imageSize == 0) {
                imageSize = (PhotoContext.getScreenWidth() - 4) / 4 - SizeUtils.dp2px(mContext, 20);
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
                    .getDraweeController(mIvCrystal, Uri.parse(item.getRes()), imageSize, imageSize));

            mTvSize.setText(FileUtils.formatFileSize(item.getLength()));

            final ExistBean exist = FileUtils.isExist(mContext, cate, item);
            if (exist.isExist()) {
                if (mTvWait.getVisibility() == View.VISIBLE)
                    mTvWait.setVisibility(View.GONE);
                if (mProgress.getVisibility() == View.VISIBLE)
                    mProgress.setVisibility(View.GONE);
                if (mIvDownload.getVisibility() != View.VISIBLE)
                    mIvDownload.setVisibility(View.VISIBLE);
                mIvDownload.setImageResource(R.drawable.picker_complete);
                mIvDownload.setEnabled(false);
            } else {
                mIvDownload.setEnabled(true);
                mIvDownload.setImageResource(R.drawable.picker_download);
                mIvDownload.setOnClickListener(v -> download(position, exist));
            }
            itemView.setOnClickListener(v -> {
                if (exist.isExist()) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onClick(exist);
                    }
                } else {
                    showDownloadDialog(position, exist);
                }
            });
        }

        private void showDownloadDialog(final int position, final ExistBean exist) {
            new MaterialDialog.Builder(mContext)
                    .title(R.string.picker_title_dialog)
                    .content(R.string.picker_sticker_not_download)
                    .positiveText(android.R.string.ok)
                    .negativeText(R.string.picker_cancel)
                    .onPositive((dialog, which) -> download(position, exist))
                    .show();
        }

        private void download(final int position, ExistBean exist) {
            CrystalResult.Crystal item = getItem(position);
            CrystalDownloadUtils.getInstance()
                    .file(exist.getFile())
                    .url(item.getRes())
                    .length(item.getLength())
                    .download(new CrystalDownloadUtils.DownloadListener() {
                        @Override
                        public void onStart() {
                            if (mTvWait.getVisibility() != View.VISIBLE)
                                mTvWait.setVisibility(View.VISIBLE);
                            if (mProgress.getVisibility() == View.VISIBLE)
                                mProgress.setVisibility(View.GONE);
                            if (mIvDownload.getVisibility() == View.VISIBLE)
                                mIvDownload.setVisibility(View.GONE);
                        }

                        @Override
                        public void onProgress(int progress) {
                            if (mTvWait.getVisibility() == View.VISIBLE)
                                mTvWait.setVisibility(View.GONE);
                            if (mProgress.getVisibility() != View.VISIBLE)
                                mProgress.setVisibility(View.VISIBLE);
                            if (mIvDownload.getVisibility() == View.VISIBLE)
                                mIvDownload.setVisibility(View.GONE);
                            mProgress.setProgress(progress);
                        }

                        @Override
                        public void onSuccess() {
                            notifyItemChanged(position);
                        }

                        @Override
                        public void onFailed() {
                            notifyItemChanged(position);
                        }
                    });
        }

    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(ExistBean exist);
    }
}
