package com.hy.picker.utils;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.PickerScaleImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created time : 2018/12/24 11:18.
 *
 * @author HY
 */
public class PickerScaleViewTarget extends CustomViewTarget<PickerScaleImageView, Bitmap> {


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
    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
        getView().setQuickScaleEnabled(true);
        getView().setZoomEnabled(true);
        getView().setPanEnabled(true);
        getView().setDoubleTapZoomDuration(100);
        getView().setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_CROP);
        getView().setDoubleTapZoomDpi(PickerScaleImageView.ZOOM_FOCUS_CENTER);
        getView().setImage(ImageSource.cachedBitmap(resource), new ImageViewState(0, new PointF(0, 0), 0));
    }


}
