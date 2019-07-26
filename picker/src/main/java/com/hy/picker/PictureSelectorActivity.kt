package com.hy.picker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.DraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.hy.picker.adapter.PictureAdapter
import com.hy.picker.model.Photo
import com.hy.picker.model.PickerTheme
import com.hy.picker.model.PickerWhiteTheme
import com.hy.picker.utils.*
import com.hy.picker.view.FolderPopupWindow
import kotlinx.android.synthetic.main.picker_activity_selector.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.*

class PictureSelectorActivity : BaseActivity(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    private var takePictureUri: Uri? = null
    private val max by lazy {
        intent.getIntExtra(EXTRA_MAX, 9)
    }

    private val selectItems: ArrayList<Photo>? by lazy {
        intent.getParcelableArrayListExtra<Photo>(EXTRA_ITEMS)
    }

    private val gif by lazy {
        intent.getBooleanExtra(EXTRA_SHOW_GIF, true)
    }
    private val preview by lazy {
        intent.getBooleanExtra(EXTRA_PREVIEW, true)
    }
    private val video by lazy {
        intent.getBooleanExtra(EXTRA_PICK_VIDEO, false)
    }
    private val gifOnly by lazy {
        intent.getBooleanExtra(EXTRA_ONLY_GIF, false)
    }
    private lateinit var gridViewAdapter: PictureAdapter

    private val isShowCamera by lazy {
        intent.getBooleanExtra(EXTRA_SHOW_CAMERA, false)
    }

    private val mSelectReceiver = SelectReceiver()
    private val defaultDrawable by lazy {
        ContextCompat.getDrawable(this, PhotoPicker.defaultDrawable)!!
    }


    private lateinit var folderPopupWindow: FolderPopupWindow


    private val totalSelectedNum: Int
        get() = MediaListHolder.selectPhotos.size


    private var selectCateIndex = 0

    private val cateHeight by lazy {
        (screenWidth() * 1.3f).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(PhotoPicker.theme.windowBgColor))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picker_activity_selector)

        val intentFilter = IntentFilter()
        intentFilter.addAction(PICKER_ACTION_MEDIA_ADD)
        intentFilter.addAction(PICKER_ACTION_MEDIA_SELECT)
        intentFilter.addAction(PICKER_ACTION_MEDIA_SEND)
        registerReceiver(mSelectReceiver, intentFilter)

        initTheme()


        pickerBackIv.setOnClickListener { onBackPressed() }

        pickerCateTv.isEnabled = false
        pickerCateTv.setText(if (video) R.string.picker_all_video else R.string.picker_all_image)

        pickerPreviewTv.isEnabled = !selectItems.isNullOrEmpty()

        pickerTitle.setText(if (video) R.string.picker_picsel_videotype else R.string.picker_picsel_pictype)


        if (video) {
            pickerPreviewTv.visibility = View.GONE
            pickerSend.visibility = View.GONE
        }
        if (null != selectItems) {
            val size = selectItems!!.size
            if (size == 0) {
                pickerSend.isEnabled = false
                pickerSend.setText(R.string.picker_picsel_toolbar_send)
                pickerPreviewTv.isEnabled = false
                pickerPreviewTv.setText(R.string.picker_picsel_toolbar_preview)
            } else if (size <= max) {
                pickerSend.isEnabled = true
                pickerSend.text = resources.getString(R.string.picker_picsel_toolbar_send_num, size, max)
                pickerPreviewTv.isEnabled = true
                pickerPreviewTv.text = String.format(resources.getString(R.string.picker_picsel_toolbar_preview_num), size)
            }
        }

        gridViewAdapter = PictureAdapter(max, preview, isShowCamera, video, defaultDrawable)

        gridViewAdapter.setOnItemClickListener { which, photo ->
            when (which) {
                1 -> {
                    if (video) {
                        setResult(Activity.RESULT_OK, Intent()
                                .putParcelableArrayListExtra(EXTRA_ITEMS, ArrayList(MediaListHolder.selectPhotos)))
                        finish()
                        return@setOnItemClickListener
                    }
                    if (!preview && max == 1) {
                        val list = ArrayList<Photo>()
                        list.add(photo)
                        setResult(Activity.RESULT_OK, Intent()
                                .putParcelableArrayListExtra(EXTRA_ITEMS, list))
                        finish()
                    }
                }
                2 -> {
                    updateToolbar()
                }
                3 -> {
                    val perms = arrayOf(Manifest.permission.CAMERA)
                    if (EasyPermissions.hasPermissions(this@PictureSelectorActivity, *perms)) {
                        requestCamera()
                    } else {
                        val permissionNames = Permission.transformText(this@PictureSelectorActivity, *perms)
                        val message = getString(R.string.picker_message_permission_rationale, TextUtils.join("\n", permissionNames))
                        EasyPermissions.requestPermissions(
                                this@PictureSelectorActivity,
                                message,
                                RC_CAMERA, Manifest.permission.CAMERA)
                    }
                }
            }
        }

        pickerPhotoGrd.setHasFixedSize(true)
        pickerPhotoGrd.adapter = gridViewAdapter
        val gridLayoutManager = GridLayoutManager(this, 4)
        pickerPhotoGrd.layoutManager = gridLayoutManager
        pickerPhotoGrd.addItemDecoration(MyGridItemDecoration(this))
        pickerPhotoGrd.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!canLoadImage()) return
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Fresco.getImagePipeline().resume()
                } else {
                    Fresco.getImagePipeline().pause()
                }
            }
        })

        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionNames = Permission.transformText(this, *perms)
        val message = getString(R.string.picker_message_permission_rationale, TextUtils.join("\n", permissionNames))
        EasyPermissions.requestPermissions(
                this,
                message,
                RC_WRITE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun initTheme() {
        val theme = PhotoPicker.theme

        pickerTitleBg.setBackgroundColor(theme.titleBgColor)
        pickerBackIv.setColorFilter(theme.backIvColor)
        pickerTitle.setTextColor(theme.titleTvColor)

        val drawable = GradientDrawable()
        drawable.setColor(theme.sendBgColor)
        drawable.cornerRadius = dp(5f).toFloat()


        val colorDrawable = GradientDrawable()
        colorDrawable.setColor(0x4f000000)
        colorDrawable.cornerRadius = dp(5f).toFloat()

        val layerDrawable = LayerDrawable(arrayOf(drawable, colorDrawable))
        val sendStates = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(-android.R.attr.state_enabled), layerDrawable)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_enabled), drawable)
//        val sendBg= stateListDrawable
        pickerSend.background = stateListDrawable

        val sendColors = intArrayOf(theme.sendTvColorDisable, theme.sendTvColorEnable)

        val sendColorStateList = ColorStateList(sendStates, sendColors)
        pickerSend.setTextColor(sendColorStateList)


        val previewColors = intArrayOf(theme.previewTvColorDisable, theme.previewTvColorEnable)
        val previewStates = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))

        val previewColorStateList = ColorStateList(previewStates, previewColors)

        pickerCateTv.setTextColor(previewColorStateList)
        pickerPreviewTv.setTextColor(previewColorStateList)

        pickerBottomBg.setBackgroundColor(theme.titleBgColor)

        val typeDrawable: Int =
                if (PickerTheme.isLightColor(theme.titleBgColor)) {
                    line1.setBackgroundColor(PickerWhiteTheme.color3)
                    R.drawable.picker_type_selector_white
                } else {
                    line1.setBackgroundColor(PickerWhiteTheme.color1)
                    R.drawable.picker_type_selector_wechat
                }
        pickerCateIv.setImageResource(typeDrawable)

    }


    private val listener = { position: Int, name: String?, isChange: Boolean ->
        hideCatalog()
        selectCateIndex = position
        if (isChange) {
            pickerCateTv.text = name
            MediaListHolder.currentPhotos.clear()
            MediaListHolder.currentPhotos.addAll(MediaListHolder.allDirectories[position].photos)
//
            gridViewAdapter.reset(MediaListHolder.currentPhotos)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        MediaListHolder.selectPhotos.clear()
        if (!selectItems.isNullOrEmpty()) {
            for (selectItem in selectItems!!) {
                selectItem.isSelected = true
            }
            MediaListHolder.selectPhotos.addAll(selectItems!!)
        }

        MediaScannerUtils.Builder()
                .gif(gif)
                .gifOnly(gifOnly)
                .video(video)
                .build()
                .scanner { success ->
                    if (success) {

                        pickerPhotoLoad.visibility = View.GONE
                        postDelay({ gridViewAdapter.reset(MediaListHolder.currentPhotos) }, 2)

                        updateToolbar()
//
                        folderPopupWindow = FolderPopupWindow(
                                this, screenWidth(), defaultDrawable,
                                listener, cateHeight
                        )

                        folderPopupWindow.setOnDismissAnimatorListener(
                                onDismiss = {
                                    pickerCateDlgMask.visibility = View.GONE
                                }
                        )
                    }


                }


        pickerSend.setOnClickListener {

            setResult(Activity.RESULT_OK, Intent()
                    .putParcelableArrayListExtra(EXTRA_ITEMS, ArrayList(MediaListHolder.selectPhotos)))
            finish()
        }

        pickerCateTv.setText(if (video) R.string.picker_all_video else R.string.picker_all_image)

        pickerCateTv.isEnabled = true
        pickerCateTv.setOnClickListener {
            if (!::folderPopupWindow.isInitialized) return@setOnClickListener
            pickerCateDlgMask.visibility = View.VISIBLE
            folderPopupWindow.showAsDropDown(pickerBottomBg)

        }

        if (preview && !video) {
            pickerPreviewTv.visibility = View.VISIBLE
        } else {
            pickerPreviewTv.visibility = View.GONE
        }

        pickerPreviewTv.setOnClickListener {

            val item = MediaListHolder.selectPhotos[0]
            val intent = Intent(this@PictureSelectorActivity, PicturePreviewActivity::class.java)
            intent.putExtra(EXTRA_IS_GIF, item.isGif)
            intent.putExtra(EXTRA_MAX, max)
            intent.putExtra(EXTRA_IS_PREVIEW, true)

            startActivityForResult(intent, PICKER_REQUEST_PREVIEW)
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == RC_WRITE_STORAGE) {
            initView()
        } else if (requestCode == RC_CAMERA) {
            requestCamera()
        }
    }


    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (requestCode == RC_WRITE_STORAGE) {
                val permissionNames = Permission.transformText(this, *perms.toTypedArray())
                val message = getString(R.string.picker_message_permission_always_failed, TextUtils.join("\n", permissionNames))

                AppSettingsDialog.Builder(this)
                        .setRationale(message)
                        .setRequestCode(requestCode)
                        .build()
                        .show()
            }
        } else {
            if (requestCode == RC_WRITE_STORAGE) {
                finish()
            }
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {}

    override fun onRationaleDenied(requestCode: Int) {
        if (requestCode == RC_WRITE_STORAGE) {
            finish()
        }
    }


    private fun hideCatalog() {
        if (!::folderPopupWindow.isInitialized) return
        if (folderPopupWindow.isShowing) {
            folderPopupWindow.dismiss()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_WRITE_STORAGE) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                initView()
            } else {
                Toast.makeText(this, R.string.picker_str_permission_denied, Toast.LENGTH_SHORT).show()
                finish()
            }
            return
        }
        if (requestCode == RC_CAMERA) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
                requestCamera()
            }
            return
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICKER_REQUEST_PREVIEW) {
                var hasChange = false
                for (i in MediaListHolder.selectPhotos.indices.reversed()) {
                    val photo = MediaListHolder.selectPhotos[i]
                    if (!photo.isSelected) {
                        if (!hasChange) {
                            hasChange = true
                        }
                        MediaListHolder.selectPhotos.removeAt(i)
                    }
                }

                if (hasChange) {
                    gridViewAdapter.notifyDataSetChanged()
                    updateToolbar()
                }

            } else if (requestCode == REQUEST_CAMERA) {
                if (takePictureUri != null) {
                    var path = takePictureUri!!.encodedPath// getPathFromUri(this, mTakePhotoUri);

                    if (path == null) {
                        Toast.makeText(this, if (video)
                            R.string.picker_video_failure
                        else
                            R.string.picker_photo_failure,
                                Toast.LENGTH_SHORT).show()
                        return
                    }
                    if (takePictureUri!!.toString().startsWith("content")) {
                        path = path.replace("/external_storage_root".toRegex(), "")

                        path = Environment.getExternalStorageDirectory().toString() + path
                    }

                    val file = File(path)

                    if (file.exists()) {
                        //                        MediaScannerConnection.scanFile(this, new String[]{path}, null, (path1, uri) -> getPhoto(path1));
                        SingleMediaScanner(PhotoContext.context, path, object : ImgScanListener<PictureSelectorActivity>(this) {
                            override fun onScanFinish(t: PictureSelectorActivity, path: String) {
                                t.getPhoto(path)
                            }
                        })
                    } else {
                        Toast.makeText(this, if (video)
                            R.string.picker_video_failure
                        else
                            R.string.picker_photo_failure,
                                Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    private fun getPhoto(path: String) {
        MediaScannerUtils.Builder()
                .path(path)
                .video(video)
                .max(max)
                .build()
                .scanner { photo, updateIndex ->
                    if (photo == null) {
                        Toast.makeText(this@PictureSelectorActivity, if (video)
                            R.string.picker_video_failure
                        else
                            R.string.picker_photo_failure,
                                Toast.LENGTH_SHORT).show()
                        return@scanner
                    }

                    if (selectCateIndex == 0) {
                        if (MediaListHolder.currentPhotos.isEmpty()) {
                            MediaListHolder.currentPhotos.add(photo)
                            gridViewAdapter.add(photo)
                        } else {
                            MediaListHolder.currentPhotos.add(0, photo)
                            gridViewAdapter.add(0, photo)
                        }
                    } else {
                        if (selectCateIndex == updateIndex) {
                            if (MediaListHolder.currentPhotos.isEmpty()) {
                                MediaListHolder.currentPhotos.add(photo)
                                gridViewAdapter.add(photo)
                            } else {
                                MediaListHolder.currentPhotos.add(0, photo)
                                gridViewAdapter.add(0, photo)
                            }
                        }
                    }

//                    cateDlgAdapter.reset(MediaListHolder.allDirectories)
                    updateToolbar()
                }

    }

    override fun onBackPressed() {
        if (::folderPopupWindow.isInitialized && folderPopupWindow.isShowing) {
            folderPopupWindow.dismiss()
            return
        }
        if (!selectItems.isNullOrEmpty()) {
            setResult(Activity.RESULT_OK, Intent()
                    .putParcelableArrayListExtra(EXTRA_ITEMS, selectItems))
        } else {
            setResult(Activity.RESULT_CANCELED)
        }

        super.onBackPressed()
    }

    private fun requestCamera() {
        if (!CommonUtils.existSDCard()) {
            Toast.makeText(this, R.string.picker_empty_sdcard, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = if (video) Intent(MediaStore.ACTION_VIDEO_CAPTURE) else Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!path.exists()) {
            path.mkdirs()
        }

        val name = (if (video) "VIDEO-" else "IMG-") + CommonUtils.format(Date(), "yyyy-MM-dd-HHmmss") + if (video) ".mp4" else ".jpg"
        val file = File(path, name)
        val resInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resInfoList.size <= 0) {
            Toast.makeText(this, resources.getString(R.string.picker_voip_cpu_error), Toast.LENGTH_SHORT).show()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                takePictureUri = MyFileProvider.getUriForFile(this, applicationContext.packageName + ".demo.file_provider", file)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                takePictureUri = Uri.fromFile(file)
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, takePictureUri)
            startActivityForResult(intent, REQUEST_CAMERA)
        }
    }


    inner class SelectReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action ?: return) {
                PICKER_ACTION_MEDIA_SELECT -> {
                    val photo = intent.getParcelableExtra<Photo>(PICKER_EXTRA_PHOTO)
                    if (::gridViewAdapter.isInitialized) {
                        val index = MediaListHolder.currentPhotos.indexOf(photo)

                        gridViewAdapter.notifyItemChanged(if (isShowCamera) index + 1 else index)

                        updateToolbar()
                    }
                }
                PICKER_ACTION_MEDIA_ADD -> {
                    val photo = intent.getParcelableExtra<Photo>(PICKER_EXTRA_PHOTO)
                    //                    MediaListHolder.selectPhotos.add(photo);
                    val updateIndex = intent.getIntExtra(PICKER_EXTRA_UPDATE_INDEX, selectCateIndex)
                    if (selectCateIndex == 0) {
                        if (MediaListHolder.currentPhotos.isEmpty()) {
                            MediaListHolder.currentPhotos.add(photo)
                            gridViewAdapter.add(photo)
                        } else {
                            MediaListHolder.currentPhotos.add(0, photo)
                            gridViewAdapter.add(0, photo)
                        }
                    } else {
                        if (selectCateIndex == updateIndex) {
                            if (MediaListHolder.currentPhotos.isEmpty()) {
                                MediaListHolder.currentPhotos.add(photo)
                                gridViewAdapter.add(photo)
                            } else {
                                MediaListHolder.currentPhotos.add(0, photo)
                                gridViewAdapter.add(0, photo)
                            }
                        }
                    }

                    updateToolbar()

                }
                PICKER_ACTION_MEDIA_SEND -> {
                    this@PictureSelectorActivity.setResult(Activity.RESULT_OK, Intent()
                            .putParcelableArrayListExtra(EXTRA_ITEMS, ArrayList(MediaListHolder.selectPhotos)))
                    finish()

                }
            }


        }
    }

    private fun updateToolbar() {
        val sum = totalSelectedNum
        if (sum == 0) {
            pickerSend.isEnabled = false
            pickerSend.setText(R.string.picker_picsel_toolbar_send)
            pickerPreviewTv.isEnabled = false
            pickerPreviewTv.setText(R.string.picker_picsel_toolbar_preview)
        } else if (sum <= max) {
            pickerSend.isEnabled = true
            pickerSend.text = resources.getString(R.string.picker_picsel_toolbar_send_num, sum, max)
            pickerPreviewTv.isEnabled = true
            pickerPreviewTv.text = String.format(resources.getString(R.string.picker_picsel_toolbar_preview_num), sum)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mSelectReceiver)
    }


    companion object {

        fun getDraweeController(targetView: DraweeView<*>, uri: Uri,
                                width: Int, height: Int): DraweeController {

            val request = ImageRequestBuilder.newBuilderWithSource(uri)
                    //根据View的尺寸放缩图片
                    .setResizeOptions(ResizeOptions(width, height))
                    .build()

            return Fresco.newDraweeControllerBuilder()
                    .setOldController(targetView.controller)
                    .setImageRequest(request)
                    .setCallerContext(uri)
                    .build()
        }
    }
}
