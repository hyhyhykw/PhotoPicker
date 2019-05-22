package com.hy.picker.adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.hy.picker.*
import com.hy.picker.model.Photo
import com.hy.picker.utils.CommonUtils
import com.hy.picker.utils.MediaListHolder
import kotlinx.android.synthetic.main.picker_grid_camera.view.*
import kotlinx.android.synthetic.main.picker_grid_item.view.*
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
                     private val mDefaultDrawable: Drawable) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mPhotos = ArrayList<Photo>()

    private val totalSelectedNum: Int
        get() = MediaListHolder.selectPhotos.size


    fun reset(photos: List<Photo>) {
        mPhotos.clear()
        mPhotos.addAll(photos)
        notifyDataSetChanged()
    }

    fun add(index: Int, photo: Photo) {
        mPhotos.add(index, photo)
        notifyItemInserted(index)

    }

    fun add(photo: Photo) {
        if (mPhotos.isEmpty()) {
            mPhotos.add(photo)
            notifyItemInserted(0)
        } else {
            mPhotos.add(photo)
            notifyItemInserted(mPhotos.size - 1)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context

//        val inflater = LayoutInflater.from(context)
        val holder: RecyclerView.ViewHolder

        if (viewType == 0) {

            val cameraView =View.inflate(context,R.layout.picker_grid_camera,null)

            holder = CameraHolder(cameraView)
        } else {

            val convertView = View.inflate(context,R.layout.picker_grid_item,null)
            holder = ItemHolder(convertView)
        }

        return holder
    }

    private var _listener: ((Int, Photo) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int, Photo) -> Unit) {
        _listener = listener
    }

    private inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val image: SimpleDraweeView = view.picker_photo_image
        private val mask: View = view.picker_item_mask
        private val checkBox: AppCompatCheckBox = view.picker_item_checkbox
        private val ivGif: ImageView = view.picker_iv_gif
        private val tvTime: TextView = view.picker_video_time

        init {
            if (video) {
                checkBox.visibility = View.GONE
                itemView.picker_lyt_video.visibility = View.VISIBLE
            } else {
                val states = arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))
                val colors = intArrayOf(PhotoPicker.theme.sendBgColor, PhotoPicker.theme.sendBgColor)
                checkBox.supportButtonTintList = ColorStateList(states, colors)
            }
        }

        fun bind() {
            val position = if (camera) {
                adapterPosition - 1
            } else {
                adapterPosition
            }

            val item = mPhotos[position]

            if (item.isGif) {
                ivGif.visibility = View.VISIBLE
            } else {
                ivGif.visibility = View.GONE
            }


            val hierarchy = image.hierarchy
            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            image.controller = PictureSelectorActivity.getDraweeController(image, Uri.fromFile(File(item.uri)), PhotoContext.imageItemSize, PhotoContext.imageItemSize)
            checkBox.isChecked = MediaListHolder.selectPhotos.contains(item)

            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
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
                            mask.setBackgroundResource(R.drawable.picker_item_bg_selected)
                        } else {
                            item.isSelected = false
                            MediaListHolder.selectPhotos.remove(item)
                            mask.setBackgroundResource(R.drawable.picker_item_bg_normal)
                        }
                    }
                    _listener?.invoke(2, item)
                }
            }


            if (item.isSelected) {
                mask.setBackgroundResource(R.drawable.picker_item_bg_selected)
            } else {
                mask.setBackgroundResource(R.drawable.picker_item_bg_normal)
            }


            if (video) {
                tvTime.text = CommonUtils.format(item.duration)
                mask.setOnClickListener {
                    item.isSelected = true
                    MediaListHolder.selectPhotos.add(item)

                    _listener?.invoke(1, item)
                }
                return
            }
            mask.setOnClickListener {
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
                        checkBox.toggle()
                        val isChecked = checkBox.isChecked
                        if (totalSelectedNum == max && isChecked) {
                            Toast.makeText(PhotoContext.context.applicationContext,
                                    PhotoContext.context.resources.getQuantityString(R.plurals.picker_picsel_selected_max, 1, max),
                                    Toast.LENGTH_SHORT).show()
                            checkBox.isChecked = false
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
                            mask.setBackgroundResource(R.drawable.picker_item_bg_selected)
                        } else {
                            mask.setBackgroundResource(R.drawable.picker_item_bg_normal)
                        }
                        _listener?.invoke(2, item)
//                        if (null != mOnItemListener) {
//                            mOnItemListener!!.onItemChecked()
//                        }
                    }
                }


            }

        }
    }


    internal inner class CameraHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val mMask: ImageButton = itemView.picker_camera_mask
        private val mTvTitle: TextView = itemView.picker_take_picture

        fun bind() {
            mTvTitle.setText(if (video)
                R.string.picker_picsel_record_video
            else
                R.string.picker_picsel_take_picture)
            mMask.setOnClickListener {
                _listener?.invoke(3, Photo())
//                if (null != mOnItemListener) {
//                    mOnItemListener!!.onCameraClick()
//                }
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
        return if (camera) mPhotos.size + 1 else mPhotos.size
    }
}
