package com.hy.picker.utils

import android.os.Environment
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created time : 2018/4/3 11:42.
 *
 * @author HY
 */
object CommonUtils {

    private val sdf = SimpleDateFormat.getInstance() as SimpleDateFormat
    /**
     * 判断SDCard是否可用
     */
    @JvmStatic
    fun existSDCard(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * 功能描述：格式化输出日期
     *
     * @param date   Date 日期
     * @param format String 格式
     * @return 返回字符型日期
     */
    @JvmStatic
    fun format(date: Date, format: String): String {
        var result = ""
        try {
            sdf.applyPattern(format)
            result = sdf.format(date)
        } catch (ignored: Exception) {
        }

        return result
    }

    @JvmStatic
    fun format(time: Long): String {
        val sumSec = time / 1000

        val second = sumSec % 60
        val sumMin = sumSec / 60
        val minute = sumMin % 60

        val sumHour = sumMin / 60
        return if (sumHour == 0L) {
            formatNum(minute) + ":" + formatNum(second)
        } else sumHour.toString() + ":" + formatNum(minute) + ":" + formatNum(second)
    }

    @JvmStatic
    private fun formatNum(num: Long): String {
        return if (num < 10) {
            "0$num"
        } else "" + num
    }

}
