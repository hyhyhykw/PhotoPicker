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
import org.jetbrains.anko.toast

/**
 * Created time : 2019-05-18 11:18.
 *
 * @author HY
 */

private const val EXTRA_KEY_STRING = "bundle"

@JvmOverloads
fun Fragment.toActivity(cla: Class<out Activity>, bundle: Bundle? = null, data: Uri? = null) =
        context?.toActivity(cla, bundle, data)


/**
 * skip to other activity and take extra data and uri data

 * @param cla    class object  where you want skip
 * *
 * @param bundle extra bundle data
 * *
 * @param data   uri data
 */
@JvmOverloads
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


@JvmOverloads
fun Activity.toActivityForResult(
        cla: Class<out Activity>,
        requestCode: Int,
        bundle: Bundle? = null
) {
    val intent = Intent(this, cla)
    if (null != bundle) intent.putExtra(EXTRA_KEY_STRING, bundle)
    startActivityForResult(intent, requestCode)
}

@JvmOverloads
fun Fragment.toActivityForResult(
        cla: Class<out Activity>,
        requestCode: Int,
        bundle: Bundle? = null
) {
    activity?.toActivityForResult(cla, requestCode, bundle)
}

@JvmOverloads
fun Fragment.toNewActivity(cla: Class<out Activity>, bundle: Bundle? = null) = context?.toNewActivity(cla, bundle)

@JvmOverloads
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

//val screenWidth
//    get() = MyApp.application.screenWidth()
//val screenHeight
//    get() = MyApp.application.screenHeight()


fun Resources.dp(dpValue: Float): Int {
    val scale = displayMetrics.density
    return Math.round(dpValue * scale)
}

fun Resources.sp(spValue: Float): Int {
    val fontScale = displayMetrics.scaledDensity
    return (spValue * fontScale + 0.5f).toInt()
}

/**
 * sp转px
 *
 * @param spValue sp值
 * @return px值
 */
fun Context.sp(spValue: Float): Int = resources.sp(spValue)

/**
 * dp转px
 *
 * @param dpValue dp值
 * @return px值
 */
fun Context.dp(dpValue: Float): Int = resources.dp(dpValue)

/**
 * sp转px
 *
 * @param spValue sp值
 * @return px值
 */
fun View.sp(spValue: Float): Int = resources.sp(spValue)

/**
 * dp转px
 *
 * @param dpValue dp值
 * @return px值
 */
fun View.dp(dpValue: Float): Int = resources.dp(dpValue)

/**
 * sp转px
 *
 * @param spValue sp值
 * @return px值
 */
fun Fragment.sp(spValue: Float): Int = resources.sp(spValue)

/**
 * dp转px
 *
 * @param dpValue dp值
 * @return px值
 */
fun Fragment.dp(dpValue: Float): Int = resources.dp(dpValue)

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


private var lastClickTime: Long = 0

/**
 * 防止多次点击，造成重复操作
 */
@JvmOverloads
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

/**
 * 视图点击事件的封装，防止点击过快
 *
 * @param view     视图
 * @param listener 点击事件
 */
fun setViewClick(view: View, listener: (View)->Unit) {
    setViewClick(view, listener, true)
}


/**
 * 视图点击事件的封装，防止点击过快
 *
 * @param view     视图
 * @param listener 点击事件
 */
fun setViewClick(view: View, listener: (View)->Unit, isShow: Boolean) {
    view.setOnClickListener { v ->
        if (isFastDoubleClick(isShow)) return@setOnClickListener
        listener(v)
    }
}


fun Activity.setStatusTransparent() {
    val window = window
    window.attributes.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
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
    try {
        @SuppressLint("PrivateApi")
        val clazz = Class.forName("com.android.internal.R\$dimen")
        val obj = clazz.newInstance()
        val field = clazz.getField("status_bar_height")
        val temp = Integer.parseInt(field.get(obj).toString())
        statusBarHeight = resources.getDimensionPixelSize(temp)
    } catch (e: Exception) {
    }

    return statusBarHeight
}
