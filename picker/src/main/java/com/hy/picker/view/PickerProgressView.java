package com.hy.picker.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.hy.picker.R;
import com.hy.picker.core.util.SizeUtils;
import com.hy.picker.utils.AttrsUtils;

import androidx.annotation.Nullable;

/**
 * Created time : 2019/2/25 10:33 PM.
 *
 * @author HY
 */
public class PickerProgressView extends View {
    public PickerProgressView(Context context) {
        this(context, null);
    }

    public PickerProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private final Paint mOuterPaint;
    private final Paint mInnerPaint;
    private final int outerSize;
    private final int dp25;
    private final int dp2;
    private static final RectF RECT_F = new RectF();

    public PickerProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        outerSize = SizeUtils.dp2px(context, 54);
        int innerSize = SizeUtils.dp2px(context, 46);
        dp2 = SizeUtils.dp2px(context, 2);
//         dp1 = SizeUtilsizeUtils.dp2px(context, 1);
        dp25 = SizeUtils.dp2px(context, 25);

        int color = AttrsUtils.getTypeValueColor(context, R.attr.picker_ppv_color);

        mOuterPaint = new Paint();
        mOuterPaint.setAntiAlias(true);
        mOuterPaint.setStyle(Paint.Style.STROKE);
        mOuterPaint.setColor(color);
        mOuterPaint.setStrokeWidth(SizeUtils.dp2px(context, 1));

        mInnerPaint = new Paint();
        mInnerPaint.setAntiAlias(true);
        mInnerPaint.setStyle(Paint.Style.FILL);
        mInnerPaint.setColor(color);


        RECT_F.set(dp2 + dp2, dp2 + dp2, dp2 + dp2 + innerSize, dp2 + dp2 + innerSize);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(outerSize, outerSize);
    }

    private float sweepAngle = 0f;

    private ValueAnimator halfAnimator;

    public void toHalf() {
        halfAnimator = ValueAnimator.ofFloat(0f, 180f);
        halfAnimator.addUpdateListener(animation -> {
            sweepAngle = (float) animation.getAnimatedValue();
            postInvalidate();
        });

        halfAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                halfAnimator.removeAllUpdateListeners();
                halfAnimator.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                halfAnimator.removeAllUpdateListeners();
                halfAnimator.removeAllListeners();
            }
        });
        halfAnimator.setDuration(500);
        halfAnimator.start();
    }

    public interface OnEndListener {
        void onEnd();
    }

    private ValueAnimator nextAnimator;

    public void toNext(OnEndListener onEndListener) {
        if (null != halfAnimator && halfAnimator.isRunning()) {
            halfAnimator.cancel();
        }

        nextAnimator = ValueAnimator.ofFloat(0f, 360f - sweepAngle);
        nextAnimator.addUpdateListener(animation -> {
            sweepAngle = (float) animation.getAnimatedValue();
            postInvalidate();
        });
        nextAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onEndListener.onEnd();
                nextAnimator.removeAllUpdateListeners();
                nextAnimator.removeAllListeners();
                sweepAngle = 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onEndListener.onEnd();
                nextAnimator.removeAllUpdateListeners();
                nextAnimator.removeAllListeners();
                sweepAngle = 0;
            }
        });
        nextAnimator.setDuration(500);
        nextAnimator.start();
    }

    public void cancel() {
        if (nextAnimator != null && nextAnimator.isRunning()) {
            nextAnimator.cancel();
        }
        setVisibility(GONE);
    }

    public void progress(int progress) {
        sweepAngle = progress / 100f * 360;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(dp25 + dp2, dp25 + dp2, dp25, mOuterPaint);
        canvas.drawArc(RECT_F, 0, sweepAngle, true, mInnerPaint);
    }
}
