package com.hy.picker.adapter;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hy.picker.utils.DisplayOptimizeListener;
import com.hy.picker.utils.OnItemClickListener;
import com.hy.picker.view.ImageSource;
import com.hy.picker.view.PickerScaleImageView;
import com.hy.picker.model.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created time : 2019/4/18 11:40 AM.
 *
 * @author HY
 */
public class PreviewAdapter extends PagerAdapter {
    private final ArrayList<Photo> mPhotos = new ArrayList<>();

    private final Drawable mDefaultDrawable;

    public PreviewAdapter(Drawable defaultDrawable) {
        mDefaultDrawable = defaultDrawable;
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void add(int index, Photo photo) {
        mPhotos.add(index, photo);
        notifyDataSetChanged();
    }


    public void reset(List<Photo> photos) {
        mPhotos.clear();
        mPhotos.addAll(photos);
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @Override
    public int getCount() {
        return mPhotos.size();
    }


    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        Photo photo = mPhotos.get(position);
        String uri = photo.getUri();
        if (photo.isGif()) {
            SimpleDraweeView imageView = new SimpleDraweeView(container.getContext());
            GenericDraweeHierarchy hierarchy = imageView.getHierarchy();
            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            imageView.setOnClickListener(v -> {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onClick(position,false);
                }
            });
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.fromFile(new File(photo.getUri())))
                    .setAutoPlayAnimations(true)
                    .build();
            imageView.setController(controller);
            hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
            container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return imageView;
        } else {
            if (photo.isLong()) {

                PickerScaleImageView imageView = new PickerScaleImageView(container.getContext());
                imageView.setOnClickListener(v -> {
                    if (null != mOnItemClickListener) {
                        mOnItemClickListener.onClick(position,false);
                    }
                });
                imageView.setMinimumTileDpi(160);

                imageView.setOnImageEventListener(new DisplayOptimizeListener(imageView));
                imageView.setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                imageView.setImage(ImageSource.uri(Uri.fromFile(new File(photo.getUri()))));
                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return imageView;
            } else {
                PhotoDraweeView imageView = new PhotoDraweeView(container.getContext());
                imageView.setOnViewTapListener((view, x, y) -> {
                    if (null != mOnItemClickListener) {
                        mOnItemClickListener.onClick(position,false);
                    }
                });
                imageView.setPhotoUri(Uri.fromFile(new File(uri)));

                container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return imageView;
            }
        }


    }


    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
