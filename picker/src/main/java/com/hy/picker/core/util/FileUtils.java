package com.hy.picker.core.util;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.hy.picker.core.CrystalResult;
import com.hy.picker.core.ExistBean;

/**
 * Created time : 2018/4/11 15:51.
 *
 * @author HY
 */
public final class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("This class can't be instantiate(这个工具类不能被实例化)");
    }


    /**
     * 利用签名辅助类，将字符串转为字节数组
     *
     * @param str 路径
     * @return md5
     */
    @NonNull
    public static String md5(@NonNull String str) {
        byte[] digest;
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            digest = md.digest(str.getBytes());
            return bytes2hex(digest);
        } catch (NoSuchAlgorithmException ignore) {

        }
        return str;
    }

    /**
     * 字节数组转16进制字符串
     *
     * @param digest 字节数组
     * @return md5
     */
    @NonNull
    private static String bytes2hex(byte[] digest) {
        StringBuilder sbl = new StringBuilder();
        String tmp;
        for (byte b : digest) {
            //将每个字符与0xFF进行运算得到10进制，再借助Integer转为16进制
            tmp = Integer.toHexString(0xFF & b);
            if (tmp.length() == 1) {//每个字节8位，转换为16进制，两个16进制位
                tmp = "0" + tmp;
            }
            sbl.append(tmp);
        }

        return sbl.toString();
    }

    public static ExistBean isExist(Context context, String cate, CrystalResult.Crystal crystal) {
        String md5 = md5(crystal.getRes());
        File cachePicDir = getCachePicDir(context, cate);
        File imgFile = new File(cachePicDir, md5 + ".png");
        return new ExistBean(imgFile, imgFile.exists());
    }


    @NonNull
    public static String formatFileSize(long fileSize) {
        if (fileSize > 0 && fileSize < 1023) {
            return fileSize + "B";
        } else if (fileSize >= 1024 && fileSize < 1024 * 1024) {
            return fileSize / 1024 + "KB";
        } else if (fileSize >= 1024 * 1024 && fileSize < 1024 * 1024 * 1024) {
            return fileSize / (1024 * 1024) + "MB";
        } else {
            return fileSize / (1024 * 1024 * 1024) + "GB";
        }
    }


    public static File getCachePicDir(@NonNull Context context, String cate) {
        File cacheDir = context.getExternalFilesDir(null);
        if (cacheDir != null) {
            String path = cacheDir.getAbsolutePath();
            return new File(path + File.separator + "image-cache" + File.separator + cate);
        }
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "Android" + File.separator +
                context.getPackageName() + File.separator +
                "files" + File.separator + "image-cache" + File.separator
                + cate);
    }

    public static String getFileName(String filePath) {
        File file = new File(filePath);
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index == -1) return name;
        return name.substring(0, index);
    }

}
