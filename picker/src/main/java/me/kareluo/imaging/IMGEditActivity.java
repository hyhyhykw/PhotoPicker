package me.kareluo.imaging;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.hy.picker.PickerCrystalCategoryActivity;
import com.hy.picker.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import me.kareluo.imaging.core.IMGMode;
import me.kareluo.imaging.core.IMGText;
import me.kareluo.imaging.core.file.IMGAssetFileDecoder;
import me.kareluo.imaging.core.file.IMGDecoder;
import me.kareluo.imaging.core.file.IMGFileDecoder;
import me.kareluo.imaging.core.util.IMGUtils;
import me.kareluo.imaging.core.util.SizeUtils;

/**
 * Created by felix on 2017/11/14 下午2:26.
 */

public class IMGEditActivity extends IMGEditBaseActivity {

    private static final int MAX_WIDTH = 1024;

    private static final int MAX_HEIGHT = 1024;

    public static final String EXTRA_IMAGE_URI = "IMAGE_URI";

    public static final String EXTRA_IMAGE_SAVE_PATH = "IMAGE_SAVE_PATH";

    @Override
    public void onCreated() {
        dp100 = SizeUtils.dp2px(this, 100);
    }


    @Override
    public Bitmap getBitmap() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }

        Uri uri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
        if (uri == null) {
            return null;
        }

        IMGDecoder decoder = null;

        String path = uri.getPath();

        int degree = 0;
        if (!TextUtils.isEmpty(path)) {
            degree = readPictureDegree(path);
            switch (uri.getScheme()) {
                case "asset":
                    decoder = new IMGAssetFileDecoder(this, uri);
                    break;
                case "file":
                    decoder = new IMGFileDecoder(uri);
                    break;
            }
        }

        if (decoder == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;

        decoder.decode(options);

        if (options.outWidth > MAX_WIDTH) {
            options.inSampleSize = IMGUtils.inSampleSize(Math.round(1f * options.outWidth / MAX_WIDTH));
        }

        if (options.outHeight > MAX_HEIGHT) {
            options.inSampleSize = Math.max(options.inSampleSize,
                    IMGUtils.inSampleSize(Math.round(1f * options.outHeight / MAX_HEIGHT)));
        }

        options.inJustDecodeBounds = false;

        Bitmap bitmap = decoder.decode(options);
        if (bitmap == null) {
            return null;
        }

        return degree == 0 ? bitmap : rotatingImageView(degree, bitmap);
    }

    /**
     * 旋转图片
     *
     * @param angle  被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    public static Bitmap rotatingImageView(int angle, Bitmap bitmap) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            Log.d("Error", "图片太大，内存溢出");
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }


    @Override
    public void onText(IMGText text) {
        mImgView.addStickerText(text);
    }

    @Override
    protected void onImageModeClick() {
        startActivityForResult(new Intent(this, PickerCrystalCategoryActivity.class), 666);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            String path = data.getStringExtra("path");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;

            if (options.outWidth > MAX_WIDTH) {
                options.inSampleSize = IMGUtils.inSampleSize(Math.round(1f * options.outWidth / MAX_WIDTH));
            }

            if (options.outHeight > MAX_HEIGHT) {
                options.inSampleSize = Math.max(options.inSampleSize,
                        IMGUtils.inSampleSize(Math.round(1f * options.outHeight / MAX_HEIGHT)));
            }

            options.inJustDecodeBounds = false;
            Bitmap localBitmap = BitmapFactory.decodeFile(path, options);

            Bitmap bitmap = scaleBitmap(localBitmap);
            mImgView.addStickerImage(bitmap);
        }
    }

    private int dp100;

    private Bitmap scaleBitmap(Bitmap bitmap) {
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) dp100) / width;
        float scaleHeight = ((float) dp100) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newBitmap = null;
        try {
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        } catch (OutOfMemoryError e) {
            Logger.d("图片太大，内存溢出");
        }
        if (newBitmap == null) {
            newBitmap = bitmap;
        }
        if (bitmap != newBitmap) {
            bitmap.recycle();
        }

        return newBitmap;
    }

    @Override
    public void onModeClick(IMGMode mode) {
        IMGMode cm = mImgView.getMode();
        if (cm == mode) {
            mode = IMGMode.NONE;
        }
        mImgView.setMode(mode);
        updateModeUI();

        if (mode == IMGMode.CLIP) {
            setOpDisplay(OP_CLIP);
        }
    }

    @Override
    public void onUndoClick() {
        IMGMode mode = mImgView.getMode();
        if (mode == IMGMode.DOODLE) {
            mImgView.undoDoodle();
        } else if (mode == IMGMode.MOSAIC) {
            mImgView.undoMosaic();
        }
    }

    @Override
    public void onCancelClick() {
        finish();
    }

    @Override
    public void onDoneClick() {
        String path = getIntent().getStringExtra(EXTRA_IMAGE_SAVE_PATH);
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (!file.exists()) {
                try {
                    boolean newFile = file.createNewFile();
                    Log.d("TAG", "文件：" + file + "创建" + (newFile ? "成功" : "失败"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Bitmap bitmap = mImgView.saveBitmap();
            if (bitmap != null) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(path);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setResult(RESULT_OK);
                finish();
                return;
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onCancelClipClick() {
        mImgView.cancelClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onDoneClipClick() {
        mImgView.doClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onResetClipClick() {
        mImgView.resetClip();
    }

    @Override
    public void onRotateClipClick() {
        mImgView.doRotate();
    }

    @Override
    public void onColorChanged(int checkedColor) {
        mImgView.setPenColor(checkedColor);
    }
}
