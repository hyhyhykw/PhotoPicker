package com.hy.picker.utils

import android.os.AsyncTask
import android.util.Log

import com.hy.picker.BuildConfig

import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * Created time : 2018/8/28 9:32.
 *
 * @author HY
 */
class NetworkUtils private constructor() {

    fun url(url: String): NetworkUtils {
        NetworkUtils.url = url
        return this
    }

    interface TaskListener {
        fun onSuccess(json: String)

        fun onFailed()
    }


    fun start(taskListener: TaskListener) {
        RequestTask(taskListener).execute()
    }

    class RequestTask internal constructor(listener: TaskListener) : AsyncTask<String, Void, String>() {
        private var reference: WeakReference<TaskListener>? = WeakReference(listener)

        override fun doInBackground(vararg strings: String): String? {

            val url: URL
            try {
                url = URL(NetworkUtils.url)
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
                conn.doInput = true
                conn.connect()
                input = conn.inputStream
            } catch (e: IOException) {
                return null
            }


            val bytes = ByteArray(1024)
            var len: Int
            val sbl = StringBuilder()
            try {
                len = input.read(bytes)
                while (len != -1) {
                    sbl.append(String(bytes, 0, len))
                    len = input.read(bytes)
                }
            } catch (e: IOException) {
                return null
            }

            try {
                input.close()
            } catch (e: IOException) {
                if (BuildConfig.DEBUG) {
                    Log.d("TAG", "input close error")
                }
            }

            conn.disconnect()

            return sbl.toString()
        }

        override fun onPostExecute(json: String?) {
            super.onPostExecute(json)
            val listener = reference?.get() ?: return
            if (null == json) {
                listener.onFailed()
            } else {
                listener.onSuccess(json)
            }
        }
    }

    companion object {

        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NetworkUtils()
        }

        private var url: String? = null
    }
}
