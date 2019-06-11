package com.hy.picker.core.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * Created by felix on 2017/12/26 下午3:07.
 */

class IMGFileDecoder(uri: Uri) : IMGDecoder(uri) {

    override fun decode(options: BitmapFactory.Options?): Bitmap? {
        val uri = uri ?: return null

        val path = uri.path
        if (path.isNullOrEmpty()) {
            return null
        }

        val file = File(path)
        return if (file.exists()) {
            BitmapFactory.decodeFile(path, options)
        } else null

    }
}
