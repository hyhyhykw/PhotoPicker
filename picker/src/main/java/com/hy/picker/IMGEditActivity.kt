package com.hy.picker

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.hy.picker.core.IMGMode
import com.hy.picker.core.IMGText
import com.hy.picker.core.file.IMGAssetFileDecoder
import com.hy.picker.core.file.IMGFileDecoder
import com.hy.picker.core.util.IMGUtils
import com.hy.picker.utils.ImgScanListener
import com.hy.picker.utils.MediaListHolder
import com.hy.picker.utils.MediaScannerUtils
import com.hy.picker.utils.SingleMediaScanner
import kotlinx.android.synthetic.main.picker_edit_activity.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Created by felix on 2017/11/14 下午2:26.
 */

class IMGEditActivity : IMGEditBaseActivity() {


    private var max = 0

    private val dp100 by lazy {
        dp(100f)
    }

    override fun onCreated() {
        max = intent.getIntExtra(EXTRA_MAX, 1)
    }


    override fun getBitmap(): Bitmap? {
        val intent = intent ?: return null

        val uri = intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI) ?: return null


        val path = uri.path
        if (path.isNullOrEmpty()) return null

        val degree = readPictureDegree(path)

        val decoder = when (uri.scheme) {
            "asset" -> IMGAssetFileDecoder(this, uri)
            else -> IMGFileDecoder(uri)
        }

        val options = BitmapFactory.Options()
        options.inSampleSize = 1
        options.inJustDecodeBounds = true

        decoder.decode(options)

        if (options.outWidth > MAX_WIDTH) {
            options.inSampleSize = IMGUtils.inSampleSize((1f * options.outWidth / MAX_WIDTH).roundToInt())
        }

        if (options.outHeight > MAX_HEIGHT) {
            options.inSampleSize = max(options.inSampleSize, IMGUtils.inSampleSize((1f * options.outHeight / MAX_HEIGHT).roundToInt()))
        }

        options.inJustDecodeBounds = false

        val bitmap = decoder.decode(options) ?: return null

        return if (degree == 0) bitmap else rotatingImageView(degree, bitmap)
    }


    override fun onText(text: IMGText) {
        picker_image_canvas.addStickerText(text)
    }

    override fun onImageModeClick() {
        startActivityForResult(Intent(this, PickerCrystalCategoryActivity::class.java), 666)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && null != data) {
            val path = data.getStringExtra(EXTRA_PATH)
            if (BuildConfig.DEBUG) {
                Log.e("TAG", path)
            }
            val options = BitmapFactory.Options()
            options.inSampleSize = 1
            options.inJustDecodeBounds = true

            if (options.outWidth > MAX_WIDTH) {
                options.inSampleSize = IMGUtils.inSampleSize((1f * options.outWidth / MAX_WIDTH).roundToInt())
            }

            if (options.outHeight > MAX_HEIGHT) {
                options.inSampleSize = max(options.inSampleSize, IMGUtils.inSampleSize((1f * options.outHeight / MAX_HEIGHT).roundToInt()))
            }

            options.inJustDecodeBounds = false
            val localBitmap = BitmapFactory.decodeFile(path, options)

            if (localBitmap == null) {
                Log.e("TAG", "" + File(path).exists())
                return
            }

            val bitmap = scaleBitmap(localBitmap)
            picker_image_canvas.addStickerImage(bitmap)
        }
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        // 获得图片的宽高
        val width = bitmap.width
        val height = bitmap.height
        // 计算缩放比例
        val scaleWidth = dp100.toFloat() / width

        //        float scaleHeight = scaleWidth * height / height;
        //        float scaleHeight = ((float) dp100) / height;
        // 取得想要缩放的matrix参数
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleWidth)
        // 得到新的图片
        var newBitmap: Bitmap? = null
        try {
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        } catch (ignore: OutOfMemoryError) {
        }

        if (newBitmap == null) {
            newBitmap = bitmap
        }
        if (bitmap != newBitmap) {
            bitmap.recycle()
        }

        return newBitmap
    }

    override fun onModeClick(mode: IMGMode) {
        var imgMode = mode
        val cm = picker_image_canvas.mode
        if (cm == imgMode) {
            imgMode = IMGMode.NONE
        }
        picker_image_canvas.mode = imgMode
        updateModeUI()

        if (imgMode == IMGMode.CLIP) {
            setOpDisplay(OP_CLIP)
        }
    }

    override fun onUndoClick() {
        val mode = picker_image_canvas.mode
        if (mode == IMGMode.DOODLE) {
            picker_image_canvas.undoDoodle()
        } else if (mode == IMGMode.MOSAIC) {
            picker_image_canvas.undoMosaic()
        }
    }

    override fun onCancelClick() {
        finish()
    }

    override fun onDoneClick() {
        val path = intent.getStringExtra(EXTRA_IMAGE_SAVE_PATH)
        if (!TextUtils.isEmpty(path)) {
            val file = File(path)
            if (!file.exists()) {
                try {
                    val newFile = file.createNewFile()
                    if (BuildConfig.DEBUG) {
                        Log.d("TAG", "文件：" + file + "创建" + if (newFile) "成功" else "失败")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }


            val bitmap = picker_image_canvas.saveBitmap()
            if (bitmap != null) {
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(path)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } finally {
                    if (fos != null) {
                        try {
                            fos.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                }


                SingleMediaScanner(PhotoContext.context, path, object : ImgScanListener<IMGEditActivity>(this) {
                    override fun onScanFinish(t: IMGEditActivity, path: String) {
                        t.getPhoto(path)
                    }
                })
                return
            } else {
                //                setResult(RESULT_CANCELED);
                Toast.makeText(this, R.string.picker_str_picture_save_failed, Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        }
        Toast.makeText(this, R.string.picker_str_picture_save_failed, Toast.LENGTH_SHORT).show()
        //        setResult(RESULT_CANCELED);
        finish()
    }


    private fun getPhoto(path: String) {
        MediaScannerUtils.Builder()
                .video(false)
                .path(path)
                .max(max)
                .build()
                .scanner { photo, updateIndex ->
                    if (photo == null) {
                        //                        setResult(RESULT_CANCELED);
                        Toast.makeText(this, R.string.picker_str_picture_save_failed, Toast.LENGTH_SHORT).show()

                        finish()
                        return@scanner
                    }

                    val selectPhotos = MediaListHolder.selectPhotos

                    var selectNum = 0
                    for (selectPhoto in selectPhotos) {
                        if (selectPhoto.isSelected) {
                            selectNum += 1
                        }
                    }

                    if (selectNum < max) {
                        photo.isSelected = true
                        MediaListHolder.selectPhotos.add(photo)
                        val intent = Intent(PICKER_ACTION_MEDIA_ADD)
                        intent.putExtra(PICKER_EXTRA_PHOTO, photo)
                        intent.putExtra(PICKER_EXTRA_UPDATE_INDEX, updateIndex)
                        sendBroadcast(intent)
                    }

                    startActivity(Intent(this@IMGEditActivity, PictureEditPreviewActivity::class.java)
                            .putExtra(EXTRA_ITEM, photo))

                    finish()
                }
    }

    override fun onCancelClipClick() {
        picker_image_canvas.cancelClip()
        setOpDisplay(if (picker_image_canvas.mode == IMGMode.CLIP) OP_CLIP else OP_NORMAL)
    }

    override fun onDoneClipClick() {
        picker_image_canvas.doClip()
        setOpDisplay(if (picker_image_canvas.mode == IMGMode.CLIP) OP_CLIP else OP_NORMAL)
    }

    override fun onResetClipClick() {
        picker_image_canvas.resetClip()
    }

    override fun onRotateClipClick() {
        picker_image_canvas.doRotate()
    }

    override fun onColorChanged(checkedColor: Int) {
        picker_image_canvas.setPenColor(checkedColor)
    }

    companion object {

        private const val MAX_WIDTH = 1024

        private const val MAX_HEIGHT = 1024

        /**
         * 旋转图片
         *
         * @param angle  被旋转角度
         * @param bitmap 图片对象
         * @return 旋转后的图片
         */
        fun rotatingImageView(angle: Int, bitmap: Bitmap): Bitmap {
            var returnBm: Bitmap? = null
            // 根据旋转角度，生成旋转矩阵
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            try {
                // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
                returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } catch (ignore: OutOfMemoryError) {
            }

            if (returnBm == null) {
                returnBm = bitmap
            }
            if (bitmap != returnBm) {
                bitmap.recycle()
            }
            return returnBm
        }
    }
}
