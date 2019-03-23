package com.hy.picker.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.Nullable;

/**
 * Created time : 2018/4/3 11:42.
 *
 * @author HY
 */
@SuppressWarnings("unchecked")
public class CommonUtils {
    /**
     * 判断SDCard是否可用
     */
    public static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static void setStatusTransparent(Activity activity) {
        Window window = activity.getWindow();
        window.getAttributes().flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT);
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        String brand = getDeviceBrand();
        if ("Xiaomi".equalsIgnoreCase(brand)) {
            //针对小米
            Class clazz = window.getClass();
            try {
                int darkModeFlag;

                @SuppressLint("PrivateApi")
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");

                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");

                darkModeFlag = field.getInt(layoutParams);

                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);

                extraFlagField.invoke(window,  darkModeFlag , darkModeFlag);

            } catch (Exception ignore) {
            }
        } else if ("Meizu".equalsIgnoreCase(brand)) {
            //针对魅族
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                @SuppressWarnings("JavaReflectionMemberAccess")
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                @SuppressWarnings("JavaReflectionMemberAccess")
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                value = value | bit ;
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
            } catch (Exception ignore) {

            }

        }
        if ("Xiaomi".equalsIgnoreCase(getDeviceBrand())) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                @SuppressLint("PrivateApi")
                Class decorViewClazz = Class.forName("com.android.internal.policy.DecorView");
                Field field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor");
                field.setAccessible(true);
                field.setInt(window.getDecorView(), Color.TRANSPARENT);  //改为透明
                field.setAccessible(false);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 改变状态栏字体颜色为黑色, 要求MIUI6以上
     *
     * @param lightStatusBar 为真时表示黑色字体
     */

    public static void processMIUI(Activity activity, boolean lightStatusBar) {
        processMIUI(activity, lightStatusBar, Color.WHITE);
    }

    /**
     * 改变状态栏字体颜色为黑色, 要求MIUI6以上
     *
     * @param lightStatusBar 为真时表示黑色字体
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void processMIUI(Activity activity, boolean lightStatusBar, int color) {

        Window window = activity.getWindow();
        //针对安卓6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && lightStatusBar) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        String brand = getDeviceBrand();
        if ("Xiaomi".equalsIgnoreCase(brand)) {
            //针对小米
            Class clazz = window.getClass();
            try {
                int darkModeFlag;

                @SuppressLint("PrivateApi")
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");

                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");

                darkModeFlag = field.getInt(layoutParams);

                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);

                extraFlagField.invoke(window, lightStatusBar ? darkModeFlag : 0, darkModeFlag);

            } catch (Exception ignore) {
            }
        } else if ("Meizu".equalsIgnoreCase(brand)) {
            //针对魅族
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                value = lightStatusBar ? value | bit : value & bit;
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
            } catch (Exception ignore) {

            }

        }
    }

    /**
     * 获取状态栏高度
     *
     * @return 通知栏高度
     */
    public static int getStatusBarHeight(@Nullable Context context) {
        int statusBarHeight = 0;
        if (null == context) return 0;
        try {
            @SuppressLint("PrivateApi")
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object obj = clazz.newInstance();
            Field field = clazz.getField("status_bar_height");
            int temp = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(temp);
        } catch (Exception ignore) {
        }

        return statusBarHeight;
    }

    /**
     * 功能描述：格式化输出日期
     *
     * @param date   Date 日期
     * @param format String 格式
     * @return 返回字符型日期
     */
    public static String format(Date date, String format) {
        String result = "";
        try {
            if (date != null) {
                SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getInstance();
                sdf.applyPattern(format);
                result = sdf.format(date);
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public static String format(long time) {
        long sumSec = time / 1000;

        long second = sumSec % 60;
        long sumMin = sumSec / 60;
        long minute = sumMin % 60;

        long sumHour = sumMin / 60;
        if (sumHour == 0) {
            return formatNum(minute) + ":" + formatNum(second);
        }
        return sumHour + ":" + formatNum(minute) + ":" + formatNum(second);
    }

    private static String formatNum(long num) {
        if (num < 10) {
            return "0" + num;
        }
        return "" + num;
    }



    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }


    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public static void postDelay(Runnable action, long delay) {
        MAIN_HANDLER.postDelayed(action, delay);
    }

    public static void post(Runnable action) {
        postDelay(action, 0);
    }
}
