package com.hy.picker.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import com.facebook.drawee.drawable.ScalingUtils
import com.hy.picker.PhotoContext
import com.hy.picker.PictureSelectorActivity
import com.hy.picker.R
import com.hy.picker.core.Category
import com.hy.picker.dp
import kotlinx.android.synthetic.main.picker_item_category.*

/**
 * Created time : 2018/8/27 15:42.
 *
 * @author HY
 */
class CrystalCategoryAdapter(private val defaultDrawable: Drawable) : BaseRecyclerAdapter<Category, CrystalCategoryAdapter.ViewHolder>() {

    private val imageSize by lazy { PhotoContext.context.dp(50f) }

    override fun createViewHolder(view: View, viewType: Int) = ViewHolder(view)

    override fun layout() = R.layout.picker_item_category

    inner class ViewHolder internal constructor(itemView: View) : BaseRecyclerAdapter.BaseViewHolder(itemView) {

        override fun bind() {
            val item = getItem(adapterPosition)


            val layoutParams = pickerIvCrystal.layoutParams
            if (layoutParams.height != imageSize || layoutParams.width != imageSize) {
                layoutParams.width = imageSize
                layoutParams.height = imageSize
            }

            val hierarchy = pickerIvCrystal.hierarchy
            hierarchy.setFailureImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.setPlaceholderImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)

            pickerIvCrystal.controller = PictureSelectorActivity
                    .getDraweeController(pickerIvCrystal, Uri.parse(item.image), imageSize, imageSize)


            pickerTvName.text = item.name
            itemView.setOnClickListener { _listener?.invoke(item) }
        }
    }

    fun setOnItemClickListener(listener: (Category) -> Unit) {
        _listener = listener
    }


    private var _listener: ((Category) -> Unit)? = null

}
