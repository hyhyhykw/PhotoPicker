package com.hy.picker.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.hy.picker.R
import com.hy.picker.model.Photo
import com.hy.picker.utils.DisplayOptimizeListener
import com.hy.picker.view.ImageSource
import com.hy.picker.view.PickerScaleImageView
import me.relex.photodraweeview.PhotoDraweeView
import java.io.File

/**
 * Created time : 2019/4/18 11:40 AM.
 *
 * @author HY
 */
class PreviewAdapter(private val mDefaultDrawable: Drawable) : BaseRecyclerAdapter<Photo, BaseRecyclerAdapter.BaseViewHolder>() {


    fun setOnItemClickListener(listener: () -> Unit) {
        _listener = listener
    }


    private var _listener: (() -> Unit)? = null


    fun add(index: Int, photo: Photo) {
        mData.add(index, photo)
        notifyItemInserted(index)
    }


    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder {
        if (viewType == GIF) {
            return GifHolder(view)
        }

        return if (viewType == LONG) {
            LongHolder(view)
        } else NormalHolder(view)

    }

    override fun getLayoutByType(viewType: Int): Int {
        if (viewType == GIF) {
            return R.layout.picker_item_gif
        }

        return if (viewType == LONG) {
            R.layout.picker_item_long
        } else R.layout.picker_item_normal
    }

    override fun layout(): Int {
        return 0
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if (item.isGif) {
            return GIF
        }
        return if (item.isLong) {
            LONG
        } else NORMAL
    }


    internal inner class LongHolder(itemView: View) : BaseViewHolder(itemView) {
        private val mImageView: PickerScaleImageView = itemView.findViewById(R.id.pickerItemImage)

        override fun bind() {
            val position = adapterPosition
            val item = getItem(position)
            mImageView.setOnClickListener {
                _listener?.invoke()
            }
            mImageView.setMinimumTileDpi(160)

            mImageView.setOnImageEventListener(DisplayOptimizeListener(mImageView))
            mImageView.setMinimumScaleType(PickerScaleImageView.SCALE_TYPE_CENTER_INSIDE)
            mImageView.setImage(ImageSource.uri(Uri.fromFile(File(item.uri))))

        }
    }

    internal inner class NormalHolder(itemView: View) : BaseViewHolder(itemView) {
        private val mDraweeView: PhotoDraweeView = itemView.findViewById(R.id.pickerItemImage)

        override fun bind() {
            val position = adapterPosition
            val item = getItem(position)
            mDraweeView.setOnViewTapListener { _, _, _ ->
                _listener?.invoke()
            }
            mDraweeView.setPhotoUri(Uri.fromFile(File(item.uri)))
        }
    }

    internal inner class GifHolder(itemView: View) : BaseViewHolder(itemView) {
        private val mDraweeView: SimpleDraweeView = itemView.findViewById(R.id.pickerItemImage)

        override fun bind() {
            val position = adapterPosition
            val item = getItem(position)

            val hierarchy = mDraweeView.hierarchy
            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            mDraweeView.setOnClickListener {
                _listener?.invoke()
            }
            val controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.fromFile(File(item.uri)))
                    .setAutoPlayAnimations(true)
                    .build()
            mDraweeView.controller = controller
            hierarchy.actualImageScaleType = ScalingUtils.ScaleType.FIT_CENTER
        }

    }

    companion object {

        private const val NORMAL = 0
        private const val LONG = 1
        private const val GIF = 2
    }

}
