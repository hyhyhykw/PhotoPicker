package com.hy.picker.core.util;

import android.content.Context;

/**
 * Created time : 2018/5/2 11:19.
 * 尺寸转换工具类
 *
 * @author HY
 */
public class SizeUtils {
    private SizeUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * dp转px
     *
     * @param context 上下文
     * @param dpValue dp值
     * @return px值
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(dpValue * scale);
    }

}
