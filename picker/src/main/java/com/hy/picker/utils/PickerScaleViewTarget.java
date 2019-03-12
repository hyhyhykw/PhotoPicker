package com.hy.picker.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.PickerScaleImageView;
import com.hy.picker.IMGEditActivity;
import com.hy.picker.IMGEditBaseActivity;
import com.hy.picker.PhotoContext;

import java.io.File;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created time : 2018/12/24 11:18.
 *
 * @author HY
 */
public class PickerScaleViewTarget extends CustomViewTarget<PickerScaleImageView, File> {

    private InitTask mInitTask;

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
        String path = resource.getAbsolutePath();
        mInitTask = new InitTask(this);
        mInitTask.execute(path);
    }


    private static class InitTask extends AsyncTask<String, Void, _ScaleBean> {
        private WeakReference<PickerScaleViewTarget> mReference;

        InitTask(PickerScaleViewTarget progressScaleViewTarget) {
            mReference = new WeakReference<>(progressScaleViewTarget);
        }

        @Override
        protected _ScaleBean doInBackground(String... strings) {
            String path = strings[0];
            int degree = IMGEditBaseActivity.readPictureDegree(path);

            //在不加载图片的前提下获得图片的宽高
            BitmapFactory.Options options = new BitmapFactory.Options();
            /*
             * 最关键在此，把options.inJustDecodeBounds = true;
             * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
             */
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null


            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(path, options);

            Bitmap rotateBmp;
            if (degree == 0) {
                rotateBmp = bitmap;
            } else {
                rotateBmp = IMGEditActivity.rotatingImageView(degree, bitmap);
                bitmap.recycle();
            }

            /*
             *options.outHeight为原始图片的高
             */
            int dw = rotateBmp.getWidth();
            int dh = rotateBmp.getHeight();

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

            return new _ScaleBean(rotateBmp, scale);
        }

        @Override
        protected void onPostExecute(_ScaleBean scaleBean) {
            super.onPostExecute(scaleBean);
            if (mReference == null) return;
            PickerScaleViewTarget target = mReference.get();
            if (null == target) return;
            PickerScaleImageView view = target.getView();
            view.setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CUSTOM);
            view.setMinScale(scaleBean.scale);

            view.setMaxScale(scaleBean.scale + 2.0f);//最大显示比例


            view.setImage(ImageSource.bitmap(scaleBean.mBitmap),
                    new ImageViewState(scaleBean.scale, new PointF(0, 0), 0));
        }
    }


    private static class _ScaleBean {
        private final Bitmap mBitmap;
        private final float scale;

        _ScaleBean(Bitmap bitmap, float scale) {
            mBitmap = bitmap;
            this.scale = scale;
        }
    }


    @Override
    protected void onResourceLoading(@Nullable Drawable placeholder) {
        if (placeholder instanceof BitmapDrawable) {
            getView().setImage(ImageSource.cachedBitmap(((BitmapDrawable) placeholder).getBitmap()));
        }
    }


}
