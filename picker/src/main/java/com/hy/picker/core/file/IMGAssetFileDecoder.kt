package com.hy.picker.core.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.IOException

/**
 * Created by felix on 2017/12/26 下午2:57.
 */

class IMGAssetFileDecoder(private val context: Context, uri: Uri) : IMGDecoder(uri) {

    override fun decode(options: BitmapFactory.Options?): Bitmap? {
        val uri = uri ?: return null

        var path = uri.path
        if (path.isNullOrEmpty()) {
            return null
        }

        path = path.substring(1)

        try {
            val iStream = context.assets.open(path)
            return BitmapFactory.decodeStream(iStream, null, options)
        } catch (ignore: IOException) {

        }

        return null
    }
}
