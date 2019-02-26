package com.hy.picker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.davemorrissey.labs.subscaleview.PickerScaleImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created time : 2019/2/25 11:13 PM.
 *
 * @author HY
 */
public class ProgressScaleImageView extends FrameLayout {
    public ProgressScaleImageView(@NonNull Context context) {
        this(context,null);
    }

    public ProgressScaleImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    private final PickerScaleImageView mScaleImageView;
    private final PickerProgressView mProgressView;
    public ProgressScaleImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleImageView=new PickerScaleImageView(context);
        mProgressView=new PickerProgressView(context);
        LayoutParams ivParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mScaleImageView,ivParams);

        LayoutParams pvParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pvParams.gravity= Gravity.CENTER;
        addView(mProgressView,pvParams);
    }

    public PickerScaleImageView getScaleImageView() {
        return mScaleImageView;
    }

    public PickerProgressView getProgressView() {
        return mProgressView;
    }
}
