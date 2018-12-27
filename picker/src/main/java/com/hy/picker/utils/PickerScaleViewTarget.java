package com.hy.picker.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.PickerScaleImageView;
import com.hy.picker.PhotoContext;

import java.io.File;

/**
 * Created time : 2018/12/24 11:18.
 *
 * @author HY
 */
public class PickerScaleViewTarget extends CustomViewTarget<PickerScaleImageView, File> {
    /**
     * Constructor that defaults {@code waitForLayout} to {@code false}.
     *
     * @param view View
     */
    public PickerScaleViewTarget(@NonNull PickerScaleImageView view) {
        super(view);
    }

    @Override
    protected void onResourceCleared(@Nullable Drawable placeholder) {

    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        if (errorDrawable instanceof BitmapDrawable) {
            getView().setImage(ImageSource.cachedBitmap(((BitmapDrawable) errorDrawable).getBitmap()));
        }
    }

    @Override
    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
        // 将保存的图片地址给PickerScaleImageView,这里注意设置ImageViewState设置初始显示比例
//        ImageSource imageSource = ImageSource.uri(Uri.fromFile(resource));
        Bitmap bitmap = BitmapFactory.decodeFile(resource.getAbsolutePath());
        int sWidth = bitmap.getWidth();
        int sHeight = bitmap.getHeight();

        int width = PhotoContext.getScreenWidth();
        int height = PhotoContext.getScreenHeight();
//        float scale = SystemUtil.displaySize.x / (float) sWidth;
        if (sHeight >= height
                && sHeight / sWidth >= 3) {
            getView().setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_CROP);
            getView().setImage(ImageSource.uri(Uri.fromFile(resource)), new ImageViewState(2.0F, new PointF(0, 0), 0));
        } else {
            getView().setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CUSTOM);
            getView().setImage(ImageSource.uri(Uri.fromFile(resource)));
//            getView().setDoubleTapZoomStyle(PickerScaleImageView.ZOOM_FOCUS_CENTER_IMMEDIATE);
        }

//        ImageSource source = ImageSource.uri(Uri.fromFile(resource));
//        getView().setImage(source);
    }

    @Override
    protected void onResourceLoading(@Nullable Drawable placeholder) {
        if (placeholder instanceof BitmapDrawable) {
            getView().setImage(ImageSource.cachedBitmap(((BitmapDrawable) placeholder).getBitmap()));
        }
    }

}
