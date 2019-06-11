package com.hy.picker

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.annotation.DrawableRes
import com.hy.picker.model.Photo
import com.hy.picker.model.PickerTheme
import com.hy.picker.model.PickerWhiteTheme
import com.hy.picker.utils.MediaListHolder
import java.io.File
import java.util.*
import kotlin.properties.Delegates

/**
 * Created time : 2018/8/20 8:17.
 *
 * @author HY
 */
class PhotoPicker {


    private var isEdit = false


    private var isVideo = false

    private var max = 1

    private var picItems: ArrayList<Photo>? = null

    private var gif = true

    private var gifOnly = false

    private var preview = true

    private var isShowCamera = true

    init {
        isEdit = false
        MediaListHolder.currentPhotos.clear()
        MediaListHolder.selectPhotos.clear()
        MediaListHolder.allDirectories.clear()
    }

    fun video(): PhotoPicker {
        isVideo = true
        return this
    }

    fun max(max: Int): PhotoPicker {
        this.max = max
        return this
    }

    fun edit(edit: Boolean): PhotoPicker {
        isEdit = edit
        return this
    }

    fun select(picItems: ArrayList<Photo>): PhotoPicker {
        this.picItems = picItems
        return this
    }

    fun gif(gif: Boolean): PhotoPicker {
        this.gif = gif
        return this
    }

    fun gifOnly(gifOnly: Boolean): PhotoPicker {
        this.gifOnly = gifOnly
        this.isShowCamera = false
        return this
    }

    fun preview(preview: Boolean): PhotoPicker {
        this.preview = preview
        return this
    }

    fun showCamera(camera: Boolean): PhotoPicker {
        isShowCamera = camera
        return this
    }

    fun start(activity: Activity) {
        val intent = Intent(activity, PictureSelectorActivity::class.java)
        intent.putExtra(EXTRA_MAX, max)
        intent.putExtra(EXTRA_SHOW_GIF, gif)
        intent.putExtra(EXTRA_ONLY_GIF, gifOnly)
        intent.putExtra(EXTRA_PICK_VIDEO, isVideo)
        intent.putExtra(EXTRA_SHOW_CAMERA, isShowCamera)
        intent.putExtra(EXTRA_PREVIEW, preview)
        //        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (null != picItems) {
            intent.putParcelableArrayListExtra(EXTRA_ITEMS, picItems)
        }
        activity.startActivityForResult(intent, if (isVideo) PICKER_REQUEST_MULTI_VIDEO else PICKER_REQUEST_MULTI_PICK)
    }

    fun openCamera(context: Activity) {
        context.startActivityForResult(Intent(context, OpenCameraResultActivity::class.java)
                .putExtra(EXTRA_EDIT, isEdit)
                .putExtra(EXTRA_PICK_VIDEO, isVideo),
                if (isVideo) PICKER_REQUEST_TAKE_VIDEO else PICKER_REQUEST_TAKE_PHOTO)

    }

    companion object {

        var theme: PickerTheme by Delegates.notNull()
            private set

        @DrawableRes
        var defaultDrawable = R.drawable.picker_grid_image_default
            private set

        @JvmOverloads
        fun init(photoModule: PhotoModule,
                 @DrawableRes defaultDrawable: Int = R.drawable.picker_grid_image_default,
                 pickerTheme: PickerTheme = PickerWhiteTheme()) {
            PhotoContext.setPhotoModule(photoModule)
            theme = pickerTheme
            this.defaultDrawable = defaultDrawable
        }


        fun isSingle(requestCode: Int): Boolean {
            return PICKER_REQUEST_TAKE_PHOTO == requestCode || requestCode == PICKER_REQUEST_TAKE_VIDEO
        }

        fun isVideo(requestCode: Int): Boolean {
            return requestCode == PICKER_REQUEST_MULTI_VIDEO || requestCode == PICKER_REQUEST_TAKE_VIDEO
        }

        fun obtainTakeResult(requestCode: Int, resultCode: Int, intent: Intent?): Photo? {
            return if (resultCode != Activity.RESULT_OK || requestCode != PICKER_REQUEST_TAKE_PHOTO && requestCode != PICKER_REQUEST_TAKE_VIDEO || intent == null) {
                null
            } else intent.getParcelableExtra(EXTRA_ITEM)
        }

        fun obtainMultiResult(requestCode: Int, resultCode: Int, intent: Intent?): ArrayList<Photo> {
            return if (resultCode != Activity.RESULT_OK || requestCode != PICKER_REQUEST_MULTI_PICK && requestCode != PICKER_REQUEST_MULTI_VIDEO || intent == null) {
                ArrayList()
            } else intent.getParcelableArrayListExtra(EXTRA_ITEMS)
                    ?: return ArrayList()

        }


        /**
         * 删除编辑缓存
         */
        fun deleteEditCache() {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (!path.exists()) {
                return
            }
            delete(path)
        }

        private fun delete(cache: File) {
            if (cache.isDirectory) {

                val files = cache.listFiles { pathname -> !pathname.isDirectory && pathname.absolutePath.startsWith("IMG-EDIT") }
                for (file in files) {
                    delete(file)
                }
            } else {
                val delete = cache.delete()
                if (BuildConfig.DEBUG) {
                    Log.d("TAG", "缓存文件：" + cache + "删除" + if (delete) "成功" else "失败")
                }
            }
        }


        fun preview(index: Int, items: ArrayList<Photo>) {
            val intent = Intent(PhotoContext.context, SelectedPicturePreviewActivity::class.java)
                    .putExtra(EXTRA_INDEX, index)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(EXTRA_ITEMS, items)
            PhotoContext.context.startActivity(intent)
        }
    }
}
