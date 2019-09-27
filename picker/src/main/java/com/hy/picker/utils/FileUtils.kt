package com.hy.picker.utils

import android.content.Context
import android.os.Environment

import com.hy.picker.core.Crystal
import com.hy.picker.core.ExistBean

import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created time : 2018/4/11 15:51.
 *
 * @author HY
 */
object FileUtils {


    /**
     * 利用签名辅助类，将字符串转为字节数组
     *
     * @param str 路径
     * @return md5
     */
    @JvmStatic
    fun md5(str: String): String {
        val digest: ByteArray
        try {
            val md = MessageDigest.getInstance("md5")
            digest = md.digest(str.toByteArray())
            return bytes2hex(digest)
        } catch (ignore: NoSuchAlgorithmException) {

        }

        return str
    }

    /**
     * 字节数组转16进制字符串
     *
     * @param digest 字节数组
     * @return md5
     */
    private fun bytes2hex(digest: ByteArray): String {
        val sbl = StringBuilder()
        var tmp: String
        for (b in digest) {
            //将每个字符与0xFF进行运算得到10进制，再借助Integer转为16进制
            tmp = Integer.toHexString(0xFF and b.toInt())
            if (tmp.length == 1) {//每个字节8位，转换为16进制，两个16进制位
                tmp = "0$tmp"
            }
            sbl.append(tmp)
        }

        return sbl.toString()
    }

    @JvmStatic
    fun isExist(context: Context, cate: String, crystal: Crystal): ExistBean {
        val md5 = md5(crystal.res)
        val cachePicDir = getCachePicDir(context, cate)
        val imgFile = File(cachePicDir, "$md5.png")
        return ExistBean(imgFile, imgFile.exists())
    }


    @JvmStatic
    fun formatFileSize(fileSize: Long): String {
        return when {
            fileSize in 1..1023 -> fileSize.toString() + "B"
            fileSize < 1024 * 1024 -> (fileSize / 1024).toString() + "KB"
            fileSize < 1024 * 1024 * 1024 -> (fileSize / (1024 * 1024)).toString() + "MB"
            else -> (fileSize / (1024 * 1024 * 1024)).toString() + "GB"
        }
    }


    @JvmStatic
    fun getCachePicDir(context: Context, cate: String): File {
        val cacheDir = context.getExternalFilesDir(null)
        if (cacheDir != null) {
            val path = cacheDir.absolutePath
            return File(path + File.separator + "image-cache" + File.separator + cate)
        }
        return File(Environment.getExternalStorageDirectory().absolutePath + File.separator
                + "Android" + File.separator +
                context.packageName + File.separator +
                "files" + File.separator + "image-cache" + File.separator
                + cate)
    }


}
