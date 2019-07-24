package com.hy.picker

import android.Manifest
import androidx.annotation.StringRes

/**
 * Created time : 2019-05-18 12:28.
 *
 * @author HY
 */

const val PICKER_ACTION_MEDIA_ADD = "com.hy.picker.action.MEDIA_ADD"

const val PICKER_ACTION_MEDIA_SURE = "com.hy.picker.action.MEDIA_SURE"

const val PICKER_ACTION_MEDIA_SELECT = "com.hy.picker.action.MEDIA_SELECT"
const val PICKER_ACTION_MEDIA_SEND = "com.hy.picker.action.MEDIA_SEND"

const val PICKER_EXTRA_PHOTO = "com.hy.picker.extra.PHOTO"
const val PICKER_EXTRA_UPDATE_INDEX = "com.hy.picker.extra.UPDATE_INDEX"
const val PICKER_EXTRA_ADD = "com.hy.picker.extra.ADD"
const val EXTRA_SHOW_GIF = "com.hy.picker.extra.GIF"
const val EXTRA_IS_GIF = "com.hy.picker.extra.IS_GIF"
const val EXTRA_ONLY_GIF = "com.hy.picker.extra.ONLY_GIF"
const val EXTRA_PICK_VIDEO = "com.hy.picker.extra.VIDEO"
const val EXTRA_ITEMS = "com.hy.picker.extra.ITEMS"
const val EXTRA_EXTRA = "com.hy.picker.extra.EXTRA"
const val EXTRA_SHOW_CAMERA = "com.hy.picker.extra.SHOW_CAMERA"
const val EXTRA_PREVIEW = "com.hy.picker.extra.PREVIEW"
const val EXTRA_MAX = "com.hy.picker.extra.MAX"
const val EXTRA_EDIT = "com.hy.picker.extra.EDIT"
const val EXTRA_INDEX = "com.hy.picker.extra.INDEX"
const val EXTRA_IS_PREVIEW = "com.hy.picker.extra.IS_PREVIEW"
const val EXTRA_OTHER = "com.hy.picker.extra.OTHER"
const val EXTRA_ID = "com.hy.picker.extra.ID"
const val EXTRA_PATH = "com.hy.picker.extra.PATH"
const val EXTRA_ITEM = "com.hy.picker.extra.ITEM"
const val EXTRA_IMAGE_URI = "com.hy.picker.extra.IMAGE_URL"
const val EXTRA_IMAGE_SAVE_PATH = "com.hy.picker.extra.IMAGE_SAVE_PATH"


const val REQUEST_CAMERA = 0x357

const val PICKER_REQUEST_TAKE_VIDEO = 0x20
const val PICKER_REQUEST_TAKE_PHOTO = 0x1f
const val PICKER_REQUEST_MULTI_PICK = 0x1e
const val PICKER_REQUEST_MULTI_VIDEO = 0x1d
const val REQUEST_EDIT = 0x987
const val PERMISSION_REQUEST_EXTERNAL_STORAGE = 0x1c
const val PERMISSION_REQUEST_EXTERNAL_CAMERA = 0x1b
const val PICKER_REQUEST_PREVIEW = 0x1a

const val RC_WRITE_STORAGE = 0x19
const val RC_CAMERA = 0x18
const val RC_CAMERA_STORAGE = 0x17


@StringRes
val CHILD_CATEGORY = R.string.picker_child_category
@StringRes
val CATEGORY = R.string.picker_category
const val JSON_BASE = "https://gitee.com/hyhyhykw/Sucai/raw/master/json/"

val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)


//val dp144 by lazy {
//    PhotoContext.context.dp(144f)
//}

