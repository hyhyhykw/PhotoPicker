package com.hy.picker.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri

import java.lang.ref.WeakReference

/**
 * Created time : 2019/3/18 10:56 AM.
 *
 * @author HY
 */
class SingleMediaScanner(context: Context, private val mPath: String, l: ScanListener) : MediaScannerConnection.MediaScannerConnectionClient {

    private val mMs: MediaScannerConnection = MediaScannerConnection(context, this)
    private var mReference: WeakReference<ScanListener>? = WeakReference(l)

    interface ScanListener {
        fun onScanFinish(path: String)
    }

    init {
        mMs.connect()
    }

    override fun onMediaScannerConnected() {
        mMs.scanFile(mPath, null)
    }

    override fun onScanCompleted(path: String, uri: Uri) {
        mMs.disconnect()
        if (null == mReference) return
        val listener = mReference?.get() ?: return
        listener.onScanFinish(path)
    }
}
