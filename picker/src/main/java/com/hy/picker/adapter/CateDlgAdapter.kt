package com.hy.picker.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.facebook.drawee.drawable.ScalingUtils
import com.hy.picker.*
import com.hy.picker.model.PhotoDirectory
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.picker_item_lst_catalog.*
import java.io.File
import java.util.*

/**
 * Created time : 2019/4/18 12:31 PM.
 *
 * @author HY
 */
class CateDlgAdapter(private val defaultDrawable: Drawable) : BaseAdapter() {
    private val directories = ArrayList<PhotoDirectory>()

    private var selectCateIndex = 0
    private val dp75  by lazy{
        PhotoContext.context.dp(75f)
    }

    fun reset(directories: List<PhotoDirectory>) {
        this.directories.clear()
        this.directories.addAll(directories)
        notifyDataSetChanged()
    }

    fun add(index: Int, directory: PhotoDirectory) {
        directories.add(index, directory)
        notifyDataSetChanged()
    }

    override fun getCount() = directories.size

    override fun getItem(position: Int) = directories[position]

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun selected(index: Int) {
        selectCateIndex = index
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent!!.context

        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = View.inflate(context, R.layout.picker_item_lst_catalog, null)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        holder.bind(position)

        return view
    }


    private inner class ViewHolder
    constructor(internal val itemView: View) : LayoutContainer {
        override val containerView: View?
            get() = itemView

        init {
            pickerCatalogSelected.setColorFilter(PhotoPicker.theme.sendBgColor)
        }

        internal fun bind(position: Int) {

            val showSelected = selectCateIndex == position

            val item = getItem(position)

            if (null != item.coverPath) {
                val hierarchy = pickerCateLogImage.hierarchy
                hierarchy.setPlaceholderImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
                hierarchy.setFailureImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
                pickerCateLogImage.controller = PictureSelectorActivity.getDraweeController(pickerCateLogImage,
                        Uri.fromFile(File(item.coverPath)),
                        dp75, dp75)

            }


            pickerCatePhotoNum.text = String.format(
                    PhotoContext.context.resources
                            .getString(R.string.picker_picsel_catalog_number),
                    item.photos.size)

            pickerCateName.text = item.name
            pickerCatalogSelected.visibility = if (showSelected) View.VISIBLE else View.INVISIBLE

            itemView.setOnClickListener {
                if (position == selectCateIndex) {
                    _listener?.invoke(position, false)
                } else {
                    _listener?.invoke(position, true)
                    selected(position)
                }
            }
        }
    }


    private var _listener: ((Int, Boolean) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int, Boolean) -> Unit) {
        _listener = listener
    }
}
