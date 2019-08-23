package com.hy.picker.adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.drawable.ScalingUtils
import com.hy.picker.*
import com.hy.picker.model.Photo
import com.hy.picker.utils.CommonUtils
import com.hy.picker.utils.MediaListHolder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.picker_grid_camera.*
import kotlinx.android.synthetic.main.picker_grid_item.*
import java.io.File
import java.util.*

/**
 * Created time : 2019/4/18 1:06 PM.
 *
 * @author HY
 */
class PictureAdapter(private val max: Int,
                     private val preview: Boolean,
                     private val camera: Boolean,
                     private val video: Boolean,
                     private val defaultDrawable: Drawable) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val photos = ArrayList<Photo>()

    private val totalSelectedNum: Int
        get() = MediaListHolder.selectPhotos.size


    fun reset(photos: List<Photo>) {
        this.photos.clear()
        this.photos.addAll(photos)
        notifyDataSetChanged()
    }

    fun add(index: Int, photo: Photo) {
        photos.add(index, photo)
        notifyItemInserted(index)
        notifyItemRangeChanged(index,photos.size-index)
    }

    fun add(photo: Photo) {
        if (photos.isEmpty()) {
            photos.add(photo)
            notifyDataSetChanged()
        } else {
            photos.add(photo)
            notifyItemInserted(photos.size - 1)
            notifyItemRangeChanged(photos.size - 1,1)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context

        val holder: RecyclerView.ViewHolder = if (viewType == 0) {

            val cameraView = LayoutInflater.from(context).inflate(R.layout.picker_grid_camera, parent, false)

            CameraHolder(cameraView)
        } else {

            val convertView = LayoutInflater.from(context).inflate(R.layout.picker_grid_item, parent, false)
            ItemHolder(convertView)
        }

        return holder
    }

    private var _listener: ((Int, Photo) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int, Photo) -> Unit) {
        _listener = listener
    }

    private inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), LayoutContainer {
        override val containerView: View?
            get() = itemView

        init {
            if (video) {
                pickerItemCheckBox.visibility = View.GONE
                pickerLytVideo.visibility = View.VISIBLE
            } else {
                val states = arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))
                val colors = intArrayOf(PhotoPicker.theme.sendBgColor, PhotoPicker.theme.sendBgColor)
                pickerItemCheckBox.supportButtonTintList = ColorStateList(states, colors)
            }
        }

        fun bind() {
            val position = if (camera) {
                adapterPosition - 1
            } else {
                adapterPosition
            }

            val item = photos[position]

            if (item.isGif) {
                pickerIvGif.visibility = View.VISIBLE
            } else {
                pickerIvGif.visibility = View.GONE
            }

            val layoutParams = pickerPhotoImage.layoutParams
            var change = false
            if (layoutParams.height != PhotoContext.imageItemSize) {
                layoutParams.height = PhotoContext.imageItemSize
                change = true
            }
            if (layoutParams.width != PhotoContext.imageItemSize) {
                layoutParams.width = PhotoContext.imageItemSize
                change = true
            }
            if (change) {
                pickerPhotoImage.layoutParams = layoutParams
            }

            val hierarchy = pickerPhotoImage.hierarchy
            hierarchy.setPlaceholderImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.setFailureImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)

            pickerPhotoImage.controller = PictureSelectorActivity.getDraweeController(pickerPhotoImage,
                    Uri.fromFile(File(item.uri)),
                    PhotoContext.imageItemSize,
                    PhotoContext.imageItemSize)

            pickerItemCheckBox.isChecked = MediaListHolder.selectPhotos.contains(item)

            pickerItemCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    if (totalSelectedNum == max && isChecked) {
                        Toast.makeText(
                                PhotoContext.context.applicationContext,
                                PhotoContext.context.resources.getQuantityString(R.plurals.picker_picsel_selected_max, 1, max),
                                Toast.LENGTH_SHORT).show()
                        buttonView.isChecked = false
                    } else {
                        //                            item.setSelected(isChecked);
                        if (isChecked) {
                            item.isSelected = true
                            MediaListHolder.selectPhotos.add(item)
                            pickerItemMask.setBackgroundResource(R.drawable.picker_item_bg_selected)
                        } else {
                            item.isSelected = false
                            MediaListHolder.selectPhotos.remove(item)
                            pickerItemMask.setBackgroundResource(R.drawable.picker_item_bg_normal)
                        }
                    }
                    _listener?.invoke(2, item)
                }
            }


            if (item.isSelected) {
                pickerItemMask.setBackgroundResource(R.drawable.picker_item_bg_selected)
            } else {
                pickerItemMask.setBackgroundResource(R.drawable.picker_item_bg_normal)
            }


            if (video) {
                pickerVideoTime.text = CommonUtils.format(item.duration)
                pickerItemMask.setOnClickListener {
                    item.isSelected = true
                    MediaListHolder.selectPhotos.add(item)

                    _listener?.invoke(1, item)
                }
                return
            }
            pickerItemMask.setOnClickListener {
                if (preview) {
                    val intent = Intent(itemView.context, PicturePreviewActivity::class.java)
                            .putExtra(EXTRA_INDEX, position)
                            .putExtra(EXTRA_IS_GIF, item.isGif)
                            .putExtra(EXTRA_MAX, max)

                    itemView.context.startActivity(intent)
                } else {
                    if (max == 1) {
                        _listener?.invoke(1, item)
                    } else {
                        pickerItemCheckBox.toggle()
                        val isChecked = pickerItemCheckBox.isChecked
                        if (totalSelectedNum == max && isChecked) {
                            Toast.makeText(PhotoContext.context.applicationContext,
                                    PhotoContext.context.resources.getQuantityString(R.plurals.picker_picsel_selected_max, 1, max),
                                    Toast.LENGTH_SHORT).show()
                            pickerItemCheckBox.isChecked = false
                        } else {
                            if (isChecked) {
                                item.isSelected = true
                                MediaListHolder.selectPhotos.add(item)
                            } else {
                                item.isSelected = false
                                MediaListHolder.selectPhotos.remove(item)
                            }
                        }
                        if (item.isSelected) {
                            pickerItemMask.setBackgroundResource(R.drawable.picker_item_bg_selected)
                        } else {
                            pickerItemMask.setBackgroundResource(R.drawable.picker_item_bg_normal)
                        }
                        _listener?.invoke(2, item)
                    }
                }


            }

        }
    }


    internal inner class CameraHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View?
            get() = itemView

        fun bind() {
            pickerTakePicTv.setText(if (video)
                R.string.picker_picsel_record_video
            else
                R.string.picker_picsel_take_picture)
            pickerCameraMask.setOnClickListener {
                _listener?.invoke(3, Photo())
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder) {
            holder.bind()
        } else if (holder is CameraHolder) {
            holder.bind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!camera)
            1
        else {
            if (position == 0)
                0
            else
                1
        }
    }


    override fun getItemCount(): Int {
        return if (camera) photos.size + 1 else photos.size
    }
}
