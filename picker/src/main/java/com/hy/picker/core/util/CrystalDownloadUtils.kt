package com.hy.picker.core.util

import android.os.AsyncTask
import android.util.Log
import com.hy.picker.BuildConfig
import java.io.*
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors

/**
 * Created time : 2018/8/27 11:25.
 *
 * @author HY
 */
class CrystalDownloadUtils private constructor() {

    private var url: String? = null
    private var file: File? = null
    private var length: Int = 0

    private var mDownloadListener: DownloadListener? = null

    fun url(url: String): CrystalDownloadUtils {
        this.url = url
        return this
    }

    fun file(file: File): CrystalDownloadUtils {
        this.file = file
        return this
    }

    fun length(length: Int): CrystalDownloadUtils {
        this.length = length
        return this
    }

    fun download(downloadListener: DownloadListener) {
        mDownloadListener = downloadListener
        addTask(DownloadTask(this))
    }

    private fun addTask(task: AsyncTask<String, Int, File>) {
        mDownloadListener!!.onStart()
        singleThreadPool.execute { task.execute() }
    }

    class DownloadTask internal constructor(utils: CrystalDownloadUtils) : AsyncTask<String, Int, File>() {
        private var mWeakReference: WeakReference<CrystalDownloadUtils>? = WeakReference(utils)

        override fun doInBackground(vararg strings: String): File? {
            val utils = mWeakReference?.get() ?: return null
            if (!utils.file!!.parentFile.exists()) {
                val mkdirs = utils.file!!.parentFile.mkdirs()
                if (BuildConfig.DEBUG) {
                    Log.d("TAG", "文件夹创建" + if (mkdirs) "成功" else "失败")
                }
            }

            if (!utils.file!!.exists()) {
                try {
                    val newFile = utils.file!!.createNewFile()
                    if (BuildConfig.DEBUG) {
                        Log.d("TAG", "文件" + utils.file + "创建" + if (newFile) "成功" else "失败")
                    }
                } catch (e: IOException) {
                    if (BuildConfig.DEBUG) {
                        Log.d("TAG", "文件创建失败")
                    }
                    return null
                }

            }

            val url: URL
            try {
                url = URL(utils.url)
            } catch (e: MalformedURLException) {
                return null
            }

            val conn: HttpURLConnection
            try {
                conn = url.openConnection() as HttpURLConnection
            } catch (e: IOException) {
                return null
            }

            val input: InputStream
            try {
                //                conn.setRequestProperty("Accept-Encoding", "identity");
                //                conn.setRequestMethod("POST");
                conn.doInput = true
                input = conn.inputStream
            } catch (e: IOException) {
                return null
            }

            val fos: FileOutputStream
            try {
                fos = FileOutputStream(utils.file)
            } catch (e: FileNotFoundException) {
                return null
            }

            var sum = 0

            val bytes = ByteArray(1024)

            try {
                var len: Int = input.read(bytes)
                while (len != -1) {
                    fos.write(bytes, 0, len)
                    sum += len
                    val progress = (sum / utils.length.toFloat() * 100).toInt()
                    publishProgress(progress)
                    len = input.read(bytes)
                }
            } catch (e: IOException) {
                return null
            }

            try {
                fos.close()
            } catch (e: IOException) {
                if (BuildConfig.DEBUG) {
                    Log.d("TAG", "fos close error")
                }
            }

            try {
                input.close()
            } catch (e: IOException) {
                if (BuildConfig.DEBUG) {
                    Log.d("TAG", "input close error")
                }
            }

            conn.disconnect()

            return utils.file
        }

        override fun onPostExecute(file: File?) {
            super.onPostExecute(file)
            val utils = mWeakReference?.get() ?: return
            if (null == file) {
                val delete = utils.file!!.delete()
                if (BuildConfig.DEBUG) {
                    Log.d("TAG", "文件删除" + if (delete) "成功" else "失败")
                }
                utils.mDownloadListener?.onFailed()
            } else {
                utils.mDownloadListener?.onSuccess()
            }
            utils.file = null
            utils.url = null
            utils.length = 0
            utils.mDownloadListener = null
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            val utils = mWeakReference?.get() ?: return
            utils.mDownloadListener?.onProgress(values[0] ?: 0)
        }
    }

    interface DownloadListener {

        fun onStart()

        fun onProgress(progress: Int)

        fun onSuccess()

        fun onFailed()
    }

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CrystalDownloadUtils()
        }

        private val singleThreadPool = Executors.newSingleThreadExecutor()

    }
}
