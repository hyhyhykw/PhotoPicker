package com.hy.picker.utils

import java.lang.ref.WeakReference

/**
 * Created time : 2019/3/23 8:54 AM.
 *
 * @author HY
 */
abstract class ImgScanListener<T>(t: T) : SingleMediaScanner.ScanListener {

    private var mReference: WeakReference<T>? = WeakReference(t)

    override fun onScanFinish(path: String) {
        val t = mReference?.get() ?: return
        onScanFinish(t, path)
    }

    protected abstract fun onScanFinish(t: T, path: String)
}
