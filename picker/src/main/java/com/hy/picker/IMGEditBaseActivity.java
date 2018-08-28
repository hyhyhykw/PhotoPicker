package com.hy.picker;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.media.ExifInterface;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ViewSwitcher;

import java.io.IOException;

import com.hy.picker.core.IMGMode;
import com.hy.picker.core.IMGText;
import com.hy.picker.view.IMGColorGroup;
import com.hy.picker.view.IMGView;

/**
 * Created by felix on 2017/12/5 下午3:08.
 */

abstract class IMGEditBaseActivity extends BaseActivity implements View.OnClickListener,
        IMGTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener,
        DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    protected IMGView mImgView;

    private RadioGroup mModeGroup;

    private IMGColorGroup mColorGroup;

    private IMGTextEditDialog mTextDialog;

    private View mLayoutOpSub;

    private ViewSwitcher mOpSwitcher, mOpSubSwitcher;

    public static final int OP_HIDE = -1;

    public static final int OP_NORMAL = 0;

    public static final int OP_CLIP = 1;

    public static final int OP_SUB_DOODLE = 0;

    public static final int OP_SUB_MOSAIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            setContentView(R.layout.picker_edit_activity);
            initViews();
            mImgView.setImageBitmap(bitmap);

            onCreated();
        } else finish();
    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


    public void onCreated() {

    }

    private void initViews() {
        mImgView = findViewById(R.id.picker_image_canvas);
        mModeGroup = findViewById(R.id.picker_rg_modes);

        mOpSwitcher = findViewById(R.id.picker_vs_op);
        mOpSubSwitcher = findViewById(R.id.picker_vs_op_sub);

        mColorGroup = findViewById(R.id.picker_cg_colors);
        mColorGroup.setOnCheckedChangeListener(this);

        mLayoutOpSub = findViewById(R.id.picker_layout_op_sub);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.picker_rb_doodle) {
            onModeClick(IMGMode.DOODLE);
        } else if (vid == R.id.picker_btn_text) {
            onTextModeClick();
        } else if (vid == R.id.picker_rb_mosaic) {
            onModeClick(IMGMode.MOSAIC);
        } else if (vid == R.id.picker_btn_clip) {
            onModeClick(IMGMode.CLIP);
        } else if (vid == R.id.picker_btn_undo) {
            onUndoClick();
        } else if (vid == R.id.picker_tv_done) {
            onDoneClick();
        } else if (vid == R.id.picker_tv_cancel) {
            onCancelClick();
        } else if (vid == R.id.picker_ib_clip_cancel) {
            onCancelClipClick();
        } else if (vid == R.id.picker_ib_clip_done) {
            onDoneClipClick();
        } else if (vid == R.id.picker_tv_clip_reset) {
            onResetClipClick();
        } else if (vid == R.id.picker_ib_clip_rotate) {
            onRotateClipClick();
        } else if (vid == R.id.picker_btn_image) {
            onImageModeClick();
        }
    }

    protected abstract void onImageModeClick();

    public void updateModeUI() {
        IMGMode mode = mImgView.getMode();
        switch (mode) {
            case DOODLE:
                mModeGroup.check(R.id.picker_rb_doodle);
                setOpSubDisplay(OP_SUB_DOODLE);
                break;
            case MOSAIC:
                mModeGroup.check(R.id.picker_rb_mosaic);
                setOpSubDisplay(OP_SUB_MOSAIC);
                break;
            case NONE:
                mModeGroup.clearCheck();
                setOpSubDisplay(OP_HIDE);
                break;
        }
    }

    public void onTextModeClick() {
        if (mTextDialog == null) {
            mTextDialog = new IMGTextEditDialog(this, this);
            mTextDialog.setOnShowListener(this);
            mTextDialog.setOnDismissListener(this);
        }
        mTextDialog.show();
    }

    @Override
    public final void onCheckedChanged(RadioGroup group, int checkedId) {
        onColorChanged(mColorGroup.getCheckColor());
    }

    public void setOpDisplay(int op) {
        if (op >= 0) {
            mOpSwitcher.setDisplayedChild(op);
        }
    }

    public void setOpSubDisplay(int opSub) {
        if (opSub < 0) {
            mLayoutOpSub.setVisibility(View.GONE);
        } else {
            mOpSubSwitcher.setDisplayedChild(opSub);
            mLayoutOpSub.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onShow(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.GONE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.VISIBLE);
    }

    public abstract Bitmap getBitmap();

    public abstract void onModeClick(IMGMode mode);

    public abstract void onUndoClick();

    public abstract void onCancelClick();

    public abstract void onDoneClick();

    public abstract void onCancelClipClick();

    public abstract void onDoneClipClick();

    public abstract void onResetClipClick();

    public abstract void onRotateClipClick();

    public abstract void onColorChanged(int checkedColor);

    @Override
    public abstract void onText(IMGText text);
}
