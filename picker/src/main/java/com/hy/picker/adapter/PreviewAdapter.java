package com.hy.picker.adapter;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hy.picker.R;
import com.hy.picker.model.Photo;
import com.hy.picker.utils.DisplayOptimizeListener;
import com.hy.picker.utils.OnItemClickListener;
import com.hy.picker.view.ImageSource;
import com.hy.picker.view.PickerScaleImageView;

import java.io.File;

import androidx.annotation.NonNull;
import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created time : 2019/4/18 11:40 AM.
 *
 * @author HY
 */
public class PreviewAdapter extends BaseRecyclerAdapter<Photo, BaseRecyclerAdapter.BaseViewHolder> {

    private final Drawable mDefaultDrawable;

    public PreviewAdapter(Drawable defaultDrawable) {
        mDefaultDrawable = defaultDrawable;
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    public void add(int index, Photo photo) {
        mData.add(index, photo);
        notifyItemInserted(index);
    }


    @NonNull
    @Override
    protected BaseRecyclerAdapter.BaseViewHolder createViewHolder(View view, int viewType) {
        if (viewType == GIF) {
            return new GifHolder(view);
        }

        if (viewType == LONG) {
            return new LongHolder(view);
        }

        return new NormalHolder(view);
    }

    @Override
    protected int getLayoutByType(int viewType) {
        if (viewType == GIF) {
            return R.layout.picker_item_gif;
        }

        if (viewType == LONG) {
            return R.layout.picker_item_long;
        }
        return R.layout.picker_item_normal;
    }

    @Override
    protected int layout() {
        return 0;
    }

    private static final int NORMAL = 0;
    private static final int LONG = 1;
    private static final int GIF = 2;

    @Override
    public int getItemViewType(int position) {
        Photo item = getItem(position);
        if (item.isGif()) {
            return GIF;
        }
        if (item.isLong()) {
            return LONG;
        }
        return NORMAL;
    }


    class LongHolder extends BaseRecyclerAdapter.BaseViewHolder {
        private PickerScaleImageView mImageView;

        LongHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.picker_item_image);
        }

        public void bind() {
            int position = getAdapterPosition();
            Photo item = getItem(position);
            mImageView.setOnClickListener(v -> {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onClick(position, false);
                }
            });
            mImageView.setMinimumTileDpi(160);

            mImageView.setOnImageEventListener(new DisplayOptimizeListener(mImageView));
            mImageView.setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_INSIDE);
            mImageView.setImage(ImageSource.uri(Uri.fromFile(new File(item.getUri()))));

        }
    }

    class NormalHolder extends BaseRecyclerAdapter.BaseViewHolder {
        private PhotoDraweeView mDraweeView;

        NormalHolder(View itemView) {
            super(itemView);
            mDraweeView = itemView.findViewById(R.id.picker_item_image);
        }

        public void bind() {
            int position = getAdapterPosition();
            Photo item = getItem(position);
            mDraweeView.setOnViewTapListener((view, x, y) -> {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onClick(position, false);
                }
            });
            mDraweeView.setPhotoUri(Uri.fromFile(new File(item.getUri())));
        }
    }

    class GifHolder extends BaseRecyclerAdapter.BaseViewHolder {
        private SimpleDraweeView mDraweeView;

        GifHolder(View itemView) {
            super(itemView);
            mDraweeView = itemView.findViewById(R.id.picker_item_image);
        }

        public void bind() {
            int position = getAdapterPosition();
            Photo item = getItem(position);

            GenericDraweeHierarchy hierarchy = mDraweeView.getHierarchy();
            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            mDraweeView.setOnClickListener(v -> {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onClick(position, false);
                }
            });
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.fromFile(new File(item.getUri())))
                    .setAutoPlayAnimations(true)
                    .build();
            mDraweeView.setController(controller);
            hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
        }

    }


//    @NonNull
//    public Object instantiateItem(@NonNull ViewGroup container, int position) {
//
//        Photo photo = mPhotos.get(position);
//        String uri = photo.getUri();
//        if (photo.isGif()) {
//            SimpleDraweeView imageView = new SimpleDraweeView(container.getContext());
//            GenericDraweeHierarchy hierarchy = imageView.getHierarchy();
//            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
//            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
//            imageView.setOnClickListener(v -> {
//                if (null != mOnItemClickListener) {
//                    mOnItemClickListener.onClick(position, false);
//                }
//            });
//            DraweeController controller = Fresco.newDraweeControllerBuilder()
//                    .setUri(Uri.fromFile(new File(photo.getUri())))
//                    .setAutoPlayAnimations(true)
//                    .build();
//            imageView.setController(controller);
//            hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
//            container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            return imageView;
//        } else {
//            if (photo.isLong()) {
//
//                PickerScaleImageView imageView = new PickerScaleImageView(container.getContext());
//                imageView.setOnClickListener(v -> {
//                    if (null != mOnItemClickListener) {
//                        mOnItemClickListener.onClick(position, false);
//                    }
//                });
//                imageView.setMinimumTileDpi(160);
//
//                imageView.setOnImageEventListener(new DisplayOptimizeListener(imageView));
//                imageView.setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_INSIDE);
//                imageView.setImage(ImageSource.uri(Uri.fromFile(new File(photo.getUri()))));
//                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                return imageView;
//            } else {
//                PhotoDraweeView imageView = new PhotoDraweeView(container.getContext());
//                imageView.setOnViewTapListener((view, x, y) -> {
//                    if (null != mOnItemClickListener) {
//                        mOnItemClickListener.onClick(position, false);
//                    }
//                });
//                imageView.setPhotoUri(Uri.fromFile(new File(uri)));
//
//                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                return imageView;
//            }
//        }
//
//
//    }

}
