@file:Suppress("unused")

package com.hy.picker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import kotlinx.android.extensions.LayoutContainer
import org.jetbrains.anko.toast
import kotlin.math.roundToInt

/**
 * Created time : 2019-05-18 11:18.
 *
 * @author HY
 */

private const val EXTRA_KEY_STRING = "bundle"

fun Fragment.toActivity(cla: Class<out Activity>, bundle: Bundle? = null, data: Uri? = null) {

    val intent = Intent(context, cla)
    if (null != bundle) intent.putExtra(EXTRA_KEY_STRING, bundle)
    if (null != data) intent.data = data

    startActivity(intent)
}


/**
 * skip to other activity and take extra data and uri data

 * @param cla    class object  where you want skip
 * *
 * @param bundle extra bundle data
 * *
 * @param data   uri data
 */

fun Context.toActivity(
        cla: Class<out Activity>,
        bundle: Bundle? = null,
        data: Uri? = null,
        vararg flags: Int = intArrayOf()
) {

    val intent = Intent(this, cla)
    if (null != bundle) intent.putExtra(EXTRA_KEY_STRING, bundle)
    if (null != data) intent.data = data

    for (flag in flags) {
        intent.addFlags(flag)
    }
    startActivity(intent)
}

fun View.toActivity(cla: Class<out Activity>, bundle: Bundle? = null, data: Uri? = null) {
    context.toActivity(cla, bundle, data)
}

fun LayoutContainer.toActivity(cla: Class<out Activity>, bundle: Bundle? = null, data: Uri? = null) {
    containerView?.toActivity(cla, bundle, data)
}

fun Activity.toActivityForResult(
        cla: Class<out Activity>,
        requestCode: Int,
        bundle: Bundle? = null
) {
    val intent = Intent(this, cla)
    if (null != bundle) intent.putExtra(EXTRA_KEY_STRING, bundle)
    startActivityForResult(intent, requestCode)
}

fun Fragment.toActivityForResult(
        cla: Class<out Activity>,
        requestCode: Int,
        bundle: Bundle? = null
) {

    val intent = Intent(context, cla)
    if (null != bundle) intent.putExtra(EXTRA_KEY_STRING, bundle)
    startActivityForResult(intent, requestCode)
}

fun Fragment.toNewActivity(cla: Class<out Activity>, bundle: Bundle? = null) {
    val intent = Intent(context, cla)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    if (null != bundle) {
        intent.putExtra(EXTRA_KEY_STRING, bundle)
    }
    startActivity(intent)
}

fun Context.toNewActivity(cla: Class<out Activity>, bundle: Bundle? = null) {
    val intent = Intent(this, cla)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    if (null != bundle) {
        intent.putExtra(EXTRA_KEY_STRING, bundle)
    }
    startActivity(intent)
}

fun Activity.getBundle(): Bundle? = intent.getBundleExtra(EXTRA_KEY_STRING)

fun Context.screenWidth() = resources.displayMetrics.widthPixels
fun Context.screenHeight() = resources.displayMetrics.heightPixels

fun Fragment.screenWidth() = context?.screenWidth()
fun Fragment.screenHeight() = context?.screenHeight()

fun View.screenWidth() = context.screenWidth()
fun View.screenHeight() = context.screenHeight()


infix  fun Resources.dp(dpValue: Float): Int {
    val scale = displayMetrics.density
    return (dpValue * scale).roundToInt()
}

infix fun Resources.sp(spValue: Float): Int {
    val fontScale = displayMetrics.scaledDensity
    return (spValue * fontScale + 0.5f).toInt()
}

fun Resources.px2sp(pxValue: Float): Int {
    val fontScale = displayMetrics.scaledDensity
    return (pxValue / fontScale + 0.5f).toInt()
}

/**
 * px转dp
 *
 * @param pxValue px值
 * @return dp值
 */
fun Resources.px2dp(pxValue: Float): Int {
    val scale = displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

/**
 * px转sp
 *
 * @param pxValue px值
 * @return sp值
 */
fun Context.px2sp(pxValue: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (pxValue / fontScale + 0.5f).toInt()
}

/**
 * px转dp
 *
 * @param pxValue px值
 * @return dp值
 */
fun Context.px2dp(pxValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

/**
 * px转sp
 *
 * @param pxValue px值
 * @return sp值
 */
fun Fragment.px2sp(pxValue: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (pxValue / fontScale + 0.5f).toInt()
}

/**
 * px转dp
 *
 * @param pxValue px值
 * @return dp值
 */
fun Fragment.px2dp(pxValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

/**
 * px转sp
 *
 * @param pxValue px值
 * @return sp值
 */
fun View.px2sp(pxValue: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (pxValue / fontScale + 0.5f).toInt()
}

/**
 * px转dp
 *
 * @param pxValue px值
 * @return dp值
 */
fun View.px2dp(pxValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}


/**
 * sp转px
 *
 * @param spValue sp值
 * @return px值
 */
fun Context.sp(spValue: Float): Int = resources  sp spValue

/**
 * dp转px
 *
 * @param dpValue dp值
 * @return px值
 */
fun Context.dp(dpValue: Float): Int = resources dp dpValue

/**
 * sp转px
 *
 * @param spValue sp值
 * @return px值
 */
fun View.sp(spValue: Float): Int = resources sp spValue

/**
 * dp转px
 *
 * @param dpValue dp值
 * @return px值
 */
fun View.dp(dpValue: Float): Int = resources dp dpValue

/**
 * sp转px
 *
 * @param spValue sp值
 * @return px值
 */
fun Fragment.sp(spValue: Float): Int = resources sp spValue

/**
 * dp转px
 *
 * @param dpValue dp值
 * @return px值
 */
fun Fragment.dp(dpValue: Float): Int = resources dp dpValue

val MAIN_HANDLER = Handler(Looper.getMainLooper())

fun postDelay(block: () -> Unit, delay: Long) {

    MAIN_HANDLER.postDelayed({ block() }, delay)
}

fun post(block: () -> Unit) {
    postDelay(block, 0)
}

fun Fragment?.canLoadImage(): Boolean {

    if (this == null) {
        return false
    }


    return activity.canLoadImage()
}

fun Context?.canLoadImage(): Boolean {
    if (this == null) {
        return false
    }

    if (this !is Activity) {
        return true
    }

    return this.canLoadImage()
}

fun Activity?.canLoadImage(): Boolean {
    if (this == null) {
        return false
    }

    return !isDestroyed && !isFinishing
}


fun View?.setClick(listener: (View) -> Unit) {
    setClick(listener, true)
}

fun View?.setClick(listener: (View) -> Unit, isShow: Boolean) {
    this?.setOnClickListener { v ->
        if (isFastDoubleClick(isShow)) return@setOnClickListener
        listener(v)
    }
}


private var lastClickTime = 0L

/**
 * 防止多次点击，造成重复操作
 */
fun isFastDoubleClick(isShow: Boolean = true): Boolean {
    val time = System.currentTimeMillis()
    val timeD = time - lastClickTime

    if (timeD in 1..499) {
        if (isShow) {
            PhotoContext.context.toast(R.string.picker_str_click_too_fast)
        }
        return true
    }
    lastClickTime = time

    return false
}

fun Activity.setStatusTransparent() {
    val window = window
    window.attributes.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //兼容5.0及以上支持全透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        try {
            @SuppressLint("PrivateApi")
            val decorViewClazz = Class.forName("com.android.internal.policy.DecorView")
            val field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor")
            field.isAccessible = true
            field.setInt(window.decorView, Color.TRANSPARENT)  //改为透明
            field.isAccessible = false
        } catch (ignore: Exception) {
        }
    }

}

fun Activity.setStatusTransparentLight() {
    window.attributes.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
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
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    val brand = Build.BRAND
    if ("Xiaomi".equals(brand, ignoreCase = true)) {
        //针对小米
        val clazz = window.javaClass
        try {
            val darkModeFlag: Int

            @SuppressLint("PrivateApi")
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")

            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")

            darkModeFlag = field.getInt(layoutParams)

            val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)

            extraFlagField.invoke(window, darkModeFlag, darkModeFlag)

        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.v("TAG", e.message, e)
            }
        }

    } else if ("Meizu".equals(brand, ignoreCase = true)) {
        //针对魅族
        try {
            val lp = window.attributes
            val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
            val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
            darkFlag.isAccessible = true
            meizuFlags.isAccessible = true
            val bit = darkFlag.getInt(null)
            var value = meizuFlags.getInt(lp)
            value = value or bit
            meizuFlags.setInt(lp, value)
            window.attributes = lp
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.v("TAG", e.message, e)
            }
        }

    }
    if ("Xiaomi".equals(brand, ignoreCase = true)) {
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        try {
            @SuppressLint("PrivateApi")
            val decorViewClazz = Class.forName("com.android.internal.policy.DecorView")
            val field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor")
            field.isAccessible = true
            field.setInt(window.decorView, Color.TRANSPARENT)  //改为透明
            field.isAccessible = false
        } catch (ignore: Exception) {
        }

    }
}


private var statusBarHeight = 0
/**
 * 获取状态栏高度
 *
 * @return 通知栏高度
 */
fun Context?.getStatusBarHeight(): Int {
    if (statusBarHeight != 0) return statusBarHeight
    if (null == this) return 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        statusBarHeight = resources.getDimensionPixelSize(resourceId)
    }

    return statusBarHeight
}


inline val Float.dp
    get() = PhotoContext.context.dp(this)

inline val Int.dp
    get() = toFloat().dp

inline val Double.dp
    get() = toFloat().dp
