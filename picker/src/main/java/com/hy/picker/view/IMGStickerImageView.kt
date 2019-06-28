package com.hy.picker.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

import androidx.annotation.DrawableRes

/**
 * Created by felix on 2017/12/21 下午10:58.
 */

class IMGStickerImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : IMGStickerView(context, attrs, defStyleAttr) {

    private lateinit var imageView: ImageView

    private var bitmap: Bitmap? = null

    fun setImageBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        imageView.setImageBitmap(bitmap)
    }

    fun destroy() {
        bitmap?.recycle()
        bitmap = null
    }

    override fun onRemove() {
        super.onRemove()
        destroy()
    }

    fun setImageResource(@DrawableRes resId: Int) {
        imageView.setImageResource(resId)
    }

    override fun onCreateContentView(context: Context): View {
        imageView = ImageView(context)
        return imageView
    }
}
