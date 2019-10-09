package com.hy.picker.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.facebook.drawee.drawable.ScalingUtils
import com.hy.picker.*
import com.hy.picker.core.Crystal
import com.hy.picker.core.ExistBean
import com.hy.picker.core.util.CrystalDownloadUtils
import com.hy.picker.utils.FileUtils
import kotlinx.android.synthetic.main.picker_item_crystal.*

/**
 * Created time : 2018/8/27 15:53.
 *
 * @author HY
 */
class CrystalAdapter(private val cate: String, private val defaultDrawable: Drawable) :
        PickerBaseRecyclerAdapter<Crystal, CrystalAdapter.ViewHolder>() {

    private val imageSize by lazy{
        (PhotoContext.context.screenWidth() - 4) / 4 - PhotoContext.context.dp(20f)
    }


    override fun createViewHolder(view: View, viewType: Int) = ViewHolder(view)

    override fun layout() = R.layout.picker_item_crystal

    inner class ViewHolder constructor(itemView: View) :
            PickerBaseRecyclerAdapter.BaseViewHolder(itemView) {

        override fun bind() {
            val position = adapterPosition
            val item = getItem(position)


            val layoutParams = pickerIvCrystal.layoutParams
            if (layoutParams.height != imageSize || layoutParams.width != imageSize) {
                layoutParams.width = imageSize
                layoutParams.height = imageSize
            }
            val hierarchy = pickerIvCrystal.hierarchy
            hierarchy.setFailureImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)
            hierarchy.setPlaceholderImage(defaultDrawable, ScalingUtils.ScaleType.CENTER_CROP)

            pickerIvCrystal.controller = PictureSelectorActivity
                    .getDraweeController(pickerIvCrystal, Uri.parse(item.res), imageSize, imageSize)

            pickerTvSize.text = FileUtils.formatFileSize(item.length.toLong())

            val exist = FileUtils.isExist(itemView.context, cate, item)
            if (exist.exist) {
                if (pickerTvWait.visibility == View.VISIBLE)
                    pickerTvWait.visibility = View.GONE
                if (pickerDownloadProgress.visibility == View.VISIBLE)
                    pickerDownloadProgress.visibility = View.GONE
                if (pickerIvDownload.visibility != View.VISIBLE)
                    pickerIvDownload.visibility = View.VISIBLE
                pickerIvDownload.setImageResource(R.drawable.picker_complete)
                pickerIvDownload.isEnabled = false
            } else {
                pickerIvDownload.isEnabled = true
                pickerIvDownload.setImageResource(R.drawable.picker_download)
                pickerIvDownload.setOnClickListener { download(position, exist) }
            }

            itemView.setOnClickListener {
                if (exist.exist) {
                    _listener?.invoke(exist)
                } else {
                    showDownloadDialog(itemView.context, position, exist)
                }
            }
        }

        private fun showDownloadDialog(context: Context, position: Int, exist: ExistBean) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.pickerTitle_dialog)
                    .setMessage(R.string.picker_sticker_not_download)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        download(position, exist)
                        dialog.cancel()
                    }
                    .setNegativeButton(R.string.picker_cancel) { dialog, _->
                        dialog.cancel()
                    }
                    .show()
//            MaterialDialog(context)
//                    .title(R.string.pickerTitle_dialog)
//                    .message(R.string.picker_sticker_not_download)
//                    .positiveButton(android.R.string.ok, click = {
//                        download(position, exist)
//                    })
//                    .negativeButton(R.string.picker_cancel)
//                    .show()
        }

        private fun download(position: Int, exist: ExistBean) {
            val item = getItem(position)
            CrystalDownloadUtils.instance
                    .file(exist.file)
                    .url(item.res)
                    .length(item.length)
                    .download(object : CrystalDownloadUtils.DownloadListener {
                        override fun onStart() {
                            if (pickerTvWait.visibility != View.VISIBLE)
                                pickerTvWait.visibility = View.VISIBLE
                            if (pickerDownloadProgress.visibility == View.VISIBLE)
                                pickerDownloadProgress.visibility = View.GONE
                            if (pickerIvDownload.visibility == View.VISIBLE)
                                pickerIvDownload.visibility = View.GONE
                        }

                        override fun onProgress(progress: Int) {
                            if (pickerTvWait.visibility == View.VISIBLE)
                                pickerTvWait.visibility = View.GONE
                            if (pickerDownloadProgress.visibility != View.VISIBLE)
                                pickerDownloadProgress.visibility = View.VISIBLE
                            if (pickerIvDownload.visibility == View.VISIBLE)
                                pickerIvDownload.visibility = View.GONE
                            pickerDownloadProgress.progress=progress
                        }

                        override fun onSuccess() {
                            notifyItemChanged(position)
                        }

                        override fun onFailed() {
                            notifyItemChanged(position)
                        }
                    })
        }

    }

    fun setOnItemClickListener(listener: (ExistBean) -> Unit) {
        _listener = listener
    }

    private var _listener: ((ExistBean) -> Unit)? = null
}
