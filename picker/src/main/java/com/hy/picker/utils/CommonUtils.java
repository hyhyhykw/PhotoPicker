package com.hy.picker.utils;

import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created time : 2018/4/3 11:42.
 *
 * @author HY
 */
public class CommonUtils {
    /**
     * 判断SDCard是否可用
     */
    public static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
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

}
