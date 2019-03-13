package com.hy.picker.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public class AttrsUtils {
    /**
     * get attrs color
     *
     * @param mContext
     * @param attr
     * @return
     */
    public static int getTypeValueColor(Context mContext, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = mContext.obtainStyledAttributes(typedValue.resourceId, attribute);
        int color = array.getColor(0, -1);
        array.recycle();
        return color;
    }

    /**
     * get attrs color
     *
     * @param mContext
     * @param attr
     * @return
     */
    public static Drawable getTypeValueDrawable(Context mContext, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = mContext.obtainStyledAttributes(typedValue.resourceId, attribute);
        Drawable drawable = array.getDrawable(0);
        array.recycle();
        return drawable;
    }

    /**
     * attrs status color or black
     *
     * @param mContext
     * @param attr
     * @return
     */
    public static boolean getTypeValueBoolean(Context mContext, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = mContext.obtainStyledAttributes(typedValue.resourceId, attribute);
        boolean statusFont = array.getBoolean(0, false);
        array.recycle();
        return statusFont;
    }

    /**
     * attrs status color or black
     *
     * @param mContext
     * @param attr
     * @return
     */
    public static int getTypeValueInt(Context mContext, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = mContext.obtainStyledAttributes(typedValue.resourceId, attribute);
        int i = array.getInt(0, 3);
        array.recycle();
        return i;
    }
}
