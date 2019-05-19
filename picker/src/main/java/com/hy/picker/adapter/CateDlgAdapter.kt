package com.hy.picker.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.hy.picker.*
import com.hy.picker.model.PhotoDirectory
import kotlinx.android.synthetic.main.picker_item_lst_catalog.view.*
import java.io.File
import java.util.*

/**
 * Created time : 2019/4/18 12:31 PM.
 *
 * @author HY
 */
class CateDlgAdapter(private val mDefaultDrawable: Drawable) : BaseAdapter() {
    private val mDirectories = ArrayList<PhotoDirectory>()

    private var selectCateIndex = 0
    private var dp75 = 0

    fun reset(directories: List<PhotoDirectory>) {
        mDirectories.clear()
        mDirectories.addAll(directories)
        notifyDataSetChanged()
    }

    fun add(index: Int, directory: PhotoDirectory) {
        mDirectories.add(index, directory)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return mDirectories.size
    }

    override fun getItem(position: Int): PhotoDirectory {
        return mDirectories[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun selected(index: Int) {
        selectCateIndex = index
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent!!.context
        if (dp75 == 0) {
            dp75 = context.dp(75f)
        }

        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.picker_item_lst_catalog, parent, false)
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
    constructor(internal val itemView: View) {
        internal val image: SimpleDraweeView = itemView.picker_catalog_image
        internal val tvName: TextView = itemView.picker_catalog_name
        internal val tvNumber: TextView = itemView.findViewById(R.id.picker_catalog_photo_number)
        internal val selected: ImageView = itemView.findViewById(R.id.picker_catalog_selected)

        init {
            selected.setColorFilter(PhotoPicker.theme.sendBgColor)
        }

        internal fun bind(position: Int) {

            val showSelected = selectCateIndex == position

            val item = getItem(position)

            val hierarchy = image.hierarchy
            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            if (null != item.coverPath) {
                image.controller = PictureSelectorActivity.getDraweeController(image,
                        Uri.fromFile(File(item.coverPath)),
                        dp75, dp75)
            }
            tvNumber.text = String.format(
                    PhotoContext.context.resources
                            .getString(R.string.picker_picsel_catalog_number),
                    item.photos.size)

            tvName.text = item.name
            selected.visibility = if (showSelected) View.VISIBLE else View.INVISIBLE

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
