package com.hy.picker.utils;

import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.PickerScaleImageView;
import com.hy.picker.PhotoContext;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        float initImageScale = getInitImageScale(resource.getAbsolutePath());

        getView().setMaxScale(initImageScale + 2.0f);//最大显示比例

        getView().setImage(ImageSource.uri(Uri.fromFile(resource)),
                new ImageViewState(initImageScale, new PointF(0, 0), 0));
    }

    /**
     * 计算出图片初次显示需要放大倍数
     *
     * @param imagePath 图片的绝对路径
     */
    private float getInitImageScale(String imagePath) {
        //在不加载图片的前提下获得图片的宽高
        BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options); // 此时返回的bitmap为null
        /*
         *options.outHeight为原始图片的高
         */
        int dw = options.outWidth;
        int dh = options.outHeight;

        int width = PhotoContext.getScreenWidth();
        int height = PhotoContext.getScreenHeight();
        // 拿到图片的宽和高

        float scale = 1.0f;
        //图片宽度大于屏幕，但高度小于屏幕，则缩小图片至填满屏幕宽
        if (dw > width && dh <= height) {
            scale = width * 1.0f / dw;
        }
        //图片宽度小于屏幕，但高度大于屏幕，则放大图片至填满屏幕宽
        if (dw <= width && dh > height) {
            scale = width * 1.0f / dw;
        }
        //图片高度和宽度都小于屏幕，则放大图片至填满屏幕宽
        if (dw < width && dh < height) {
            scale = width * 1.0f / dw;
        }
        //图片高度和宽度都大于屏幕，则缩小图片至填满屏幕宽
        if (dw > width && dh > height) {
            scale = width * 1.0f / dw;
        }
        return scale;
    }


    @Override
    protected void onResourceLoading(@Nullable Drawable placeholder) {
        if (placeholder instanceof BitmapDrawable) {
            getView().setImage(ImageSource.cachedBitmap(((BitmapDrawable) placeholder).getBitmap()));
        }
    }

}
