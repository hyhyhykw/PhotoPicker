package com.hy.picker.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.hy.picker.PhotoContext
import com.hy.picker.PictureSelectorActivity
import com.hy.picker.R
import com.hy.picker.core.Crystal
import com.hy.picker.core.ExistBean
import com.hy.picker.core.util.CrystalDownloadUtils
import com.hy.picker.dp
import com.hy.picker.utils.FileUtils
import com.hy.picker.view.CompletedView
import kotlinx.android.synthetic.main.picker_item_crystal.view.*

/**
 * Created time : 2018/8/27 15:53.
 *
 * @author HY
 */
class CrystalAdapter(private val cate: String, private val mDefaultDrawable: Drawable) :
        BaseRecyclerAdapter<Crystal, CrystalAdapter.ViewHolder>() {


    private var imageSize = 0


    override fun createViewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(view)
    }

    override fun layout(): Int {
        return R.layout.picker_item_crystal
    }

    inner class ViewHolder constructor(itemView: View) :
            BaseRecyclerAdapter.BaseViewHolder(itemView) {
        private val mIvCrystal: SimpleDraweeView = itemView.picker_iv_crystal
        private val mIvDownload: ImageView = itemView.findViewById(R.id.picker_iv_download)
        private val mTvSize: TextView = itemView.findViewById(R.id.picker_tv_size)
        private val mTvWait: TextView = itemView.findViewById(R.id.picker_tv_wait)
        private val mProgress: CompletedView = itemView.findViewById(R.id.picker_download_progress)

        override fun bind() {
            val position = adapterPosition
            val item = getItem(position)

            if (imageSize == 0) {
                imageSize = (PhotoContext.screenWidth - 4) / 4 - itemView.context.dp(20f)
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
                    .getDraweeController(mIvCrystal, Uri.parse(item.res), imageSize, imageSize)

            mTvSize.text = FileUtils.formatFileSize(item.length.toLong())

            val exist = FileUtils.isExist(itemView.context, cate, item)
            if (exist.isExist) {
                if (mTvWait.visibility == View.VISIBLE)
                    mTvWait.visibility = View.GONE
                if (mProgress.visibility == View.VISIBLE)
                    mProgress.visibility = View.GONE
                if (mIvDownload.visibility != View.VISIBLE)
                    mIvDownload.visibility = View.VISIBLE
                mIvDownload.setImageResource(R.drawable.picker_complete)
                mIvDownload.isEnabled = false
            } else {
                mIvDownload.isEnabled = true
                mIvDownload.setImageResource(R.drawable.picker_download)
                mIvDownload.setOnClickListener { download(position, exist) }
            }

            itemView.setOnClickListener {
                if (exist.isExist) {
                    _listener?.invoke(exist)
                } else {
                    showDownloadDialog(itemView.context,position, exist)
                }
            }
        }

        private fun showDownloadDialog(context: Context,position: Int, exist: ExistBean) {
            MaterialDialog(context)
                    .title(R.string.picker_title_dialog)
                    .message(R.string.picker_sticker_not_download)
                    .positiveButton(android.R.string.ok,click = {
                        download(position, exist)
                    })
                    .negativeButton(R.string.picker_cancel)
                    .show()
        }

        private fun download(position: Int, exist: ExistBean) {
            val item = getItem(position)
            CrystalDownloadUtils.getInstance()
                    .file(exist.file)
                    .url(item.res)
                    .length(item.length)
                    .download(object : CrystalDownloadUtils.DownloadListener {
                        override fun onStart() {
                            if (mTvWait.visibility != View.VISIBLE)
                                mTvWait.visibility = View.VISIBLE
                            if (mProgress.visibility == View.VISIBLE)
                                mProgress.visibility = View.GONE
                            if (mIvDownload.visibility == View.VISIBLE)
                                mIvDownload.visibility = View.GONE
                        }

                        override fun onProgress(progress: Int) {
                            if (mTvWait.visibility == View.VISIBLE)
                                mTvWait.visibility = View.GONE
                            if (mProgress.visibility != View.VISIBLE)
                                mProgress.visibility = View.VISIBLE
                            if (mIvDownload.visibility == View.VISIBLE)
                                mIvDownload.visibility = View.GONE
                            mProgress.setProgress(progress)
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
