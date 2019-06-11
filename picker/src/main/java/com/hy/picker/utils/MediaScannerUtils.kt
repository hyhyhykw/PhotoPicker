package com.hy.picker.utils

import android.annotation.SuppressLint
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns.DATA
import android.provider.MediaStore.MediaColumns.MIME_TYPE
import com.hy.picker.PhotoContext
import com.hy.picker.R
import com.hy.picker.model.Photo
import com.hy.picker.model.PhotoDirectory


//                             _ooOoo_
//                            o8888888o
//                            88" . "88
//                            (| -_- |)
//                            O\  =  /O
//                         ____/`---'\____
//                       .'  \\|     |//  `.
//                      /  \\|||  :  |||//  \
//                     /  _||||| -:- |||||-  \
//                     |   | \\\  -  /// |   |
//                     | \_|  ''\---/''  |   |
//                     \  .-\__  `-`  ___/-. /
//                   ___`. .'  /--.--\  `. . __
//                ."" '<  `.___\_<|>_/___.'  >'"".
//               | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//               \  \ `-.   \_ __\ /__ _/   .-` /  /
//          ======`-.____`-.___\_____/___.-`____.-'======
//                             `=---='
//         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//                   佛祖保佑             永无BUG
//
//             佛曰:
//                   写字楼里写字间，写字间中程序员；
//                   程序人员写程序，又将程序换酒钱；
//                   酒醒只在屏前坐，酒醉还来屏下眠；
//                   酒醉酒醒日复日，屏前屏下年复年；
//                   但愿老死电脑间，不愿鞠躬老板前；
//                   奔驰宝马贵者趣，公交自行程序员；
//                   别人笑我太疯癫，我笑自己命太贱；
//                   但见满街漂亮妹，哪个归得程序员？
class MediaScannerUtils {

    private val builder: Builder

    private constructor(builder: Builder) {
        this.builder = builder
    }

    constructor() {
        builder = Builder()
    }


    fun scanner(resultListener: (Boolean) -> Unit) {
        MultiScannerTask(builder, resultListener).execute()
    }

    fun scanner(listener: (Photo?, Int) -> Unit) {
        SingleScannerTask(builder, listener).execute()
    }

    class Builder {
        var gif = false
        var gifOnly = false
        var video = false

        var add = true
        var path: String = ""

        var max = 0

        fun gif(gif: Boolean): Builder {
            this.gif = gif
            return this
        }

        fun gifOnly(gifOnly: Boolean): Builder {
            this.gifOnly = gifOnly
            return this
        }

        fun video(video: Boolean): Builder {
            this.video = video
            return this
        }

        fun add(add: Boolean): Builder {
            this.add = add
            return this
        }

        fun path(path: String): Builder {
            this.path = path
            return this
        }

        fun max(max: Int): Builder {
            this.max = max
            return this
        }

        fun build(): MediaScannerUtils {
            return MediaScannerUtils(this)
        }
    }


    class MultiScannerTask internal constructor(private val builder: Builder, private val listener: (Boolean) -> Unit) : AsyncTask<String, Void, List<PhotoDirectory>>() {

        @SuppressLint("Recycle")
        override fun doInBackground(vararg strings: String): List<PhotoDirectory>? {
            val projection: Array<String>
            val queryUri: Uri
            val order: String
            val selections: String?
            val selectionArgs: Array<String>?
            if (builder.video) {
                projection = VIDEO_PROJECTION
                queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                order = MediaStore.Video.Media.DATE_ADDED + " DESC"

                selections = null
                selectionArgs = null
            } else {
                projection = IMAGE_PROJECTION
                queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                order = MediaStore.Images.Media.DATE_ADDED + " DESC"
                if (builder.gifOnly) {
                    selections = "$MIME_TYPE=?"
                    selectionArgs = arrayOf("image/gif")
                } else {
                    if (builder.gif) {
                        selections = "$MIME_TYPE=? or $MIME_TYPE=? or $MIME_TYPE=? or $MIME_TYPE=?"
                        selectionArgs = arrayOf("image/jpeg", "image/png", "image/bmp", "image/gif")
                    } else {
                        selections = "$MIME_TYPE=? or $MIME_TYPE=? or $MIME_TYPE=?"
                        selectionArgs = arrayOf("image/jpeg", "image/png", "image/bmp")
                    }
                }
            }

            val context = PhotoContext.context
            val resolver = context.contentResolver
            val cursor = resolver.query(queryUri, projection, selections, selectionArgs, order)
                    ?: return null

            val directories = MediaListHolder.allDirectories
            val photoDirectoryAll = PhotoDirectory()

            val bucketIdColumn: String
            val titleColumn: String
            val dataColumn: String
            val mimeTypeColumn: String
            val dateAddedColumn: String
            val sizeColumn: String
            val widthColumn: String
            val dateTakenColumn: String
            val heightColumn: String
            val bucketDisplayNameColumn: String
            if (builder.video) {
                bucketIdColumn = MediaStore.Video.Media.BUCKET_ID
                titleColumn = MediaStore.Video.Media.TITLE
                dataColumn = MediaStore.Video.Media.DATA
                mimeTypeColumn = MediaStore.Video.Media.MIME_TYPE
                dateAddedColumn = MediaStore.Video.Media.DATE_ADDED
                sizeColumn = MediaStore.Video.Media.SIZE
                widthColumn = MediaStore.Video.Media.WIDTH
                heightColumn = MediaStore.Video.Media.HEIGHT
                bucketDisplayNameColumn = MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                dateTakenColumn = MediaStore.Video.Media.DATE_TAKEN
                photoDirectoryAll.name = context.getString(R.string.picker_all_video)
            } else {
                bucketIdColumn = MediaStore.Images.Media.BUCKET_ID
                titleColumn = MediaStore.Images.Media.TITLE
                dataColumn = MediaStore.Images.Media.DATA
                mimeTypeColumn = MediaStore.Images.Media.MIME_TYPE
                dateAddedColumn = MediaStore.Images.Media.DATE_ADDED
                sizeColumn = MediaStore.Images.Media.SIZE
                widthColumn = MediaStore.Images.Media.WIDTH
                heightColumn = MediaStore.Images.Media.HEIGHT
                dateTakenColumn = MediaStore.Images.Media.DATE_TAKEN
                bucketDisplayNameColumn = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                photoDirectoryAll.name = context.getString(R.string.picker_all_image)
            }
            photoDirectoryAll.id = "ALL"

            while (cursor.moveToNext()) {
                val size = cursor.getInt(cursor.getColumnIndexOrThrow(sizeColumn)).toLong()
                if (size < 1) continue


                val datetaken = cursor.getLong(cursor.getColumnIndexOrThrow(dateTakenColumn))
                val bucketId = cursor.getString(cursor.getColumnIndexOrThrow(bucketIdColumn))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(bucketDisplayNameColumn))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(dataColumn))

                val title = cursor.getString(cursor.getColumnIndexOrThrow(titleColumn))
                val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(mimeTypeColumn))
                val width = cursor.getInt(cursor.getColumnIndexOrThrow(widthColumn))
                val height = cursor.getInt(cursor.getColumnIndexOrThrow(heightColumn))
                val duration: Long

                val resolution: String?
                if (builder.video) {
                    if (path.length < 4) continue
                    val suffix = path.substring(path.length - 4)
                    if (!".mp4".equals(suffix, ignoreCase = true)) {
                        continue
                    }
                    duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                    resolution = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION))
                    if (duration < 1000) continue
                } else {
                    duration = 0
                    resolution = width.toString() + "x" + height
                }


                val photoDirectory = PhotoDirectory()
                photoDirectory.id = bucketId
                photoDirectory.name = name

                val photo = Photo(path, title, size, duration, width, height, mimeType, datetaken, resolution)

                if (!directories.contains(photoDirectory)) {
                    photoDirectory.coverPath = path
                    photoDirectory.addPhoto(photo)
                    photoDirectory.dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(dateAddedColumn))
                    directories.add(photoDirectory)
                } else {
                    directories[directories.indexOf(photoDirectory)]
                            .addPhoto(photo)
                }

                photoDirectoryAll.addPhoto(photo)
                MediaListHolder.currentPhotos.add(photo)
            }
            if (photoDirectoryAll.photos.size > 0) {
                photoDirectoryAll.coverPath = photoDirectoryAll.photos[0].uri
            }
            directories.add(0, photoDirectoryAll)

            cursor.close()
            return directories
        }

        override fun onPostExecute(photoDirectories: List<PhotoDirectory>?) {
            super.onPostExecute(photoDirectories)
            if (null == photoDirectories) {
                listener.invoke(false)
            } else {
                listener.invoke(true)
            }
        }
    }

    private class ResultParams internal constructor(internal val mPhoto: Photo, internal val position: Int)

    private class SingleScannerTask internal constructor(private val builder: Builder, private val listener: (Photo?, Int) -> Unit) : AsyncTask<String, Void, ResultParams>() {

        @SuppressLint("Recycle")
        override fun doInBackground(vararg strings: String): ResultParams? {
            val projection: Array<String>
            val queryUri: Uri
            val order: String

            val selections = "$DATA=?"
            val selectionArgs = arrayOf(builder.path)

            if (builder.video) {
                projection = VIDEO_PROJECTION
                queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                order = MediaStore.Video.Media.DATE_ADDED + " DESC"
            } else {
                projection = IMAGE_PROJECTION
                queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                order = MediaStore.Images.Media.DATE_ADDED + " DESC"
            }


            val context = PhotoContext.context

            val resolver = context.contentResolver
            val cursor = resolver.query(queryUri, projection, selections, selectionArgs, order)
                    ?: return null

            val directories = MediaListHolder.allDirectories

            val bucketIdColumn: String
            val titleColumn: String
            val dataColumn: String
            val mimeTypeColumn: String
            val dateAddedColumn: String
            val sizeColumn: String
            val widthColumn: String
            val dateTakenColumn: String
            val heightColumn: String
            val bucketNameColumn: String
            if (builder.video) {
                bucketIdColumn = MediaStore.Video.Media.BUCKET_ID
                titleColumn = MediaStore.Video.Media.TITLE
                dataColumn = MediaStore.Video.Media.DATA
                mimeTypeColumn = MediaStore.Video.Media.MIME_TYPE
                dateAddedColumn = MediaStore.Video.Media.DATE_ADDED
                sizeColumn = MediaStore.Video.Media.SIZE
                widthColumn = MediaStore.Video.Media.WIDTH
                heightColumn = MediaStore.Video.Media.HEIGHT
                bucketNameColumn = MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                dateTakenColumn = MediaStore.Video.Media.DATE_TAKEN
            } else {
                bucketIdColumn = MediaStore.Images.Media.BUCKET_ID
                titleColumn = MediaStore.Images.Media.TITLE
                dataColumn = MediaStore.Images.Media.DATA
                mimeTypeColumn = MediaStore.Images.Media.MIME_TYPE
                dateAddedColumn = MediaStore.Images.Media.DATE_ADDED
                sizeColumn = MediaStore.Images.Media.SIZE
                widthColumn = MediaStore.Images.Media.WIDTH
                heightColumn = MediaStore.Images.Media.HEIGHT
                dateTakenColumn = MediaStore.Images.Media.DATE_TAKEN
                bucketNameColumn = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            }


            if (cursor.moveToFirst()) {

                val datetaken = cursor.getLong(cursor.getColumnIndexOrThrow(dateTakenColumn))

                val bucketId = cursor.getString(cursor.getColumnIndexOrThrow(bucketIdColumn))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(bucketNameColumn))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(dataColumn))
                val size = cursor.getInt(cursor.getColumnIndexOrThrow(sizeColumn)).toLong()

                val title = cursor.getString(cursor.getColumnIndexOrThrow(titleColumn))
                val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(mimeTypeColumn))
                val width = cursor.getInt(cursor.getColumnIndexOrThrow(widthColumn))
                val height = cursor.getInt(cursor.getColumnIndexOrThrow(heightColumn))
                val duration: Long

                val resolution: String
                if (builder.video) {
                    duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                    resolution = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION))
                    if (duration < 1000) return null
                } else {
                    duration = 0
                    resolution = width.toString() + "x" + height
                }

                val photoDirectory = PhotoDirectory()
                photoDirectory.id = bucketId
                photoDirectory.name = name


                val photo = Photo(path, title, size, duration, width, height, mimeType, datetaken, resolution)

                val updateIndex: Int
                if (!directories.contains(photoDirectory)) {
                    photoDirectory.coverPath = path
                    photoDirectory.addPhoto(photo)
                    photoDirectory.dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(dateAddedColumn))

                    if (directories.size == 1 || directories.size == 0) {
                        directories.add(photoDirectory)
                    } else {
                        directories.add(1, photoDirectory)
                    }
                    updateIndex = -1
                } else {

                    val index = directories.indexOf(photoDirectory)
                    val directory = directories[index]
                    directory.coverPath = path
                    if (directory.photos.isEmpty()) {
                        directory.addPhoto(photo)
                    } else {
                        directory.addPhoto(0, photo)
                    }
                    updateIndex = index
                }

                if (builder.add && MediaListHolder.selectPhotos.size != builder.max) {
                    photo.isSelected = true
                    if (MediaListHolder.selectPhotos.isEmpty()) {
                        MediaListHolder.selectPhotos.add(photo)
                    } else {
                        MediaListHolder.selectPhotos.add(0, photo)
                    }
                }

                val allDirectory = MediaListHolder.allDirectories[0]
                allDirectory.coverPath = path
                if (allDirectory.photos.isEmpty()) {
                    allDirectory.addPhoto(photo)
                } else {
                    allDirectory.addPhoto(0, photo)
                }


                cursor.close()
                return ResultParams(photo, updateIndex)
            }

            return null
        }

        override fun onPostExecute(resultParams: ResultParams?) {
            super.onPostExecute(resultParams)
            if (null == resultParams) {
                listener.invoke(null, -1)
            } else {
                listener.invoke(resultParams.mPhoto, resultParams.position)
            }
        }
    }

    companion object {


        private val IMAGE_PROJECTION = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        private val VIDEO_PROJECTION = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.BUCKET_ID, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media.DATE_TAKEN, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.WIDTH, MediaStore.Video.Media.HEIGHT, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.RESOLUTION)
    }

}
