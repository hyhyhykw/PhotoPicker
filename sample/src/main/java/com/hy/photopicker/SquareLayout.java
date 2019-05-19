package com.hy.photopicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created time : 2018/5/30 14:54.
 *
 * @author HY
 */
public class SquareLayout extends FrameLayout{
    public SquareLayout(@NonNull Context context) {
        super(context);
    }

    public SquareLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        int i = getMeasuredWidth();
        getMeasuredHeight();

        int j = MeasureSpec.makeMeasureSpec(i, MeasureSpec.EXACTLY);
        super.onMeasure(j, j);
    }
}
