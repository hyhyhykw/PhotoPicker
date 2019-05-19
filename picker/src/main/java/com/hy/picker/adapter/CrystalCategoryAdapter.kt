package com.hy.picker.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.TextView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.hy.picker.PictureSelectorActivity
import com.hy.picker.R
import com.hy.picker.core.Category
import com.hy.picker.dp
import kotlinx.android.synthetic.main.picker_item_category.view.*

/**
 * Created time : 2018/8/27 15:42.
 *
 * @author HY
 */
class CrystalCategoryAdapter(private val mDefaultDrawable: Drawable) : BaseRecyclerAdapter<Category, CrystalCategoryAdapter.ViewHolder>() {

    private var imageSize: Int = 0

//    private var mOnItemClickListener: OnItemClickListener? = null

    override fun createViewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(view)
    }

    override fun layout(): Int {
        return R.layout.picker_item_category
    }

    inner class ViewHolder internal constructor(itemView: View) : BaseRecyclerAdapter.BaseViewHolder(itemView) {
        private val mIvCrystal: SimpleDraweeView = itemView.picker_iv_crystal
        private val mTvName: TextView = itemView.picker_tv_name

        override fun bind() {
            val item = getItem(adapterPosition)

            if (imageSize == 0) {
                imageSize = itemView.dp(50f)
            }

            val layoutParams = mIvCrystal.layoutParams
            if (layoutParams.height != imageSize || layoutParams.width != imageSize) {
                layoutParams.width = imageSize
                layoutParams.height = imageSize
            }

            val hierarchy = mIvCrystal.hierarchy
            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)

            mIvCrystal.controller = PictureSelectorActivity
                    .getDraweeController(mIvCrystal, Uri.parse(item.image), imageSize, imageSize)


            mTvName.text = item.name
            itemView.setOnClickListener { _listener?.invoke(item) }
        }
    }

    fun setOnItemClickListener(listener:(Category)->Unit) {
        _listener = listener
    }


    private var _listener:((Category)->Unit)?=null

//    interface OnItemClickListener {
//        fun onClick(item: CrystalCategory.Category)
//    }
}
