package com.hy.picker.utils;

import android.os.Build;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created time : 2018/5/17 15:22.
 * ObjectUtils
 *
 * @author HY
 */
public class ObjectsUtils {


    public static boolean equals(Object a, Object b) {
        if (Build.VERSION.SDK_INT >= 19)
            return Objects.equals(a, b);
        return (a == b) || (a != null && a.equals(b));
    }

    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }

}
