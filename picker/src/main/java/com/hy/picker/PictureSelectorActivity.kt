package com.hy.picker

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.DraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.hy.picker.adapter.CateDlgAdapter
import com.hy.picker.adapter.PictureAdapter
import com.hy.picker.model.Photo
import com.hy.picker.model.PickerTheme
import com.hy.picker.model.PickerWhiteTheme
import com.hy.picker.utils.*
import kotlinx.android.synthetic.main.picker_activity_selector.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class PictureSelectorActivity : BaseActivity(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    private var mTakePictureUri: Uri? = null
    private var max = 0
    private var mSelectItems: ArrayList<Photo>? = null

    private var gif = false
    private var preview = false
    private var video = false
    private var gifOnly = false
    private lateinit var mGridViewAdapter: PictureAdapter
    private var isShowCamera = false

    private val mSelectReceiver = SelectReceiver()
    private lateinit var mDefaultDrawable: Drawable

    private lateinit var mCateDlgAdapter: CateDlgAdapter

    private var isAnimating = false
    private var isShowing = false


    private val totalSelectedNum: Int
        get() = MediaListHolder.selectPhotos.size


    private var selectCateIndex = 0

    private var cateHeight = 0

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


        mDefaultDrawable = ContextCompat.getDrawable(this, PhotoPicker.mDefaultDrawable)!!

//        val screenHeight = screenHeight()
        val statusBarHeight = getStatusBarHeight()
//        val gridHeight = screenHeight - statusBarHeight - dp(96f)

//        mCount = Math.ceil(gridHeight * 1.0 / mSize).toInt()

        val intent = intent
        max = intent.getIntExtra(EXTRA_MAX, 9)
        gif = intent.getBooleanExtra(EXTRA_SHOW_GIF, true)
        preview = intent.getBooleanExtra(EXTRA_PREVIEW, true)
        gifOnly = intent.getBooleanExtra(EXTRA_ONLY_GIF, false)
        video = intent.getBooleanExtra(EXTRA_PICK_VIDEO, false)
        isShowCamera = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, false)

        mSelectItems = intent.getParcelableArrayListExtra(EXTRA_ITEMS)

        picker_back.setOnClickListener { onBackPressed() }

        picker_type_text.isEnabled = false
        picker_type_text.setText(if (video) R.string.picker_all_video else R.string.picker_all_image)

        picker_preview_text.isEnabled = !mSelectItems.isNullOrEmpty()

        picker_title.setText(if (video) R.string.picker_picsel_videotype else R.string.picker_picsel_pictype)

        cateHeight = (screenWidth() * 1.3f).toInt()

        val constraintSet = ConstraintSet()
        constraintSet.clone(picker_selector_root)
        constraintSet.setMargin(R.id.picker_back, ConstraintSet.TOP, statusBarHeight)
        constraintSet.constrainHeight(R.id.picker_catalog_lst, cateHeight)
        constraintSet.applyTo(picker_selector_root)

        picker_catalog_lst.translationY = cateHeight.toFloat()

        if (video) {
            picker_preview_text.visibility = View.GONE
            picker_send.visibility = View.GONE
        }
        if (null != mSelectItems) {
            val size = mSelectItems!!.size
            if (size == 0) {
                picker_send.isEnabled = false
                picker_send.setText(R.string.picker_picsel_toolbar_send)
                picker_preview_text.isEnabled = false
                picker_preview_text.setText(R.string.picker_picsel_toolbar_preview)
            } else if (size <= max) {
                picker_send.isEnabled = true
                picker_send.text = resources.getString(R.string.picker_picsel_toolbar_send_num, size, max)
                picker_preview_text.isEnabled = true
                picker_preview_text.text = String.format(resources.getString(R.string.picker_picsel_toolbar_preview_num), size)
            }
        }

        mGridViewAdapter = PictureAdapter(max, preview, isShowCamera, video, mDefaultDrawable)

        mGridViewAdapter.setOnItemClickListener { which, photo ->
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

        (picker_photo_grd.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        picker_photo_grd.setHasFixedSize(true)
        picker_photo_grd.adapter = mGridViewAdapter
        val gridLayoutManager = GridLayoutManager(this, 4)
        picker_photo_grd.layoutManager = gridLayoutManager
        picker_photo_grd.addItemDecoration(MyGridItemDecoration(this))
        picker_photo_grd.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        //        mCatalogAdapter = new CatalogAdapter();
        mCateDlgAdapter = CateDlgAdapter(mDefaultDrawable)
        mCateDlgAdapter.setOnItemClickListener { position, isChange ->
            hideCatalog()
            selectCateIndex = position
            if (isChange) {
                val item = mCateDlgAdapter.getItem(position)
                picker_type_text.text = item.name

                postDelay(Runnable {
                    thread {
                        MediaListHolder.currentPhotos.clear()
                        MediaListHolder.currentPhotos.addAll(MediaListHolder.allDirectories[position].photos)
                        runOnUiThread {

                            mGridViewAdapter.reset(MediaListHolder.currentPhotos)
                        }
                    }
                }, 350)
            }
        }
        picker_catalog_lst.adapter = mCateDlgAdapter
        //        mCatalogListView.setTranslationY(catalogHeight);


        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionNames = Permission.transformText(this, *perms)
        val message = getString(R.string.picker_message_permission_rationale, TextUtils.join("\n", permissionNames))
        EasyPermissions.requestPermissions(
                this,
                message,
                RC_WRITE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        Looper.myQueue().addIdleHandler {
//
//            false
//        }
    }

    private fun initTheme() {
        val theme = PhotoPicker.theme

        picker_title_bg.setBackgroundColor(theme.titleBgColor)
        picker_back.setColorFilter(theme.backIvColor)
        picker_title.setTextColor(theme.titleTvColor)

        val drawable = GradientDrawable()
        drawable.setColor(theme.sendBgColor)
        drawable.cornerRadius = dp(5f).toFloat()


        val colorDrawable = GradientDrawable()
        colorDrawable.setColor(0x4f000000)
        colorDrawable.cornerRadius= dp(5f).toFloat()

        val layerDrawable = LayerDrawable(arrayOf(drawable, colorDrawable))
        val sendStates = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(-android.R.attr.state_enabled),layerDrawable)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_enabled),drawable)
//        val sendBg= stateListDrawable
        picker_send.background = stateListDrawable

        val sendColors = intArrayOf(theme.sendTvColorDisable, theme.sendTvColorEnable)

        val sendColorStateList = ColorStateList(sendStates, sendColors)
        picker_send.setTextColor(sendColorStateList)


        val previewColors = intArrayOf(theme.previewTvColorDisable, theme.previewTvColorEnable)
        val previewStates = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))

        val previewColorStateList = ColorStateList(previewStates, previewColors)

        picker_type_text.setTextColor(previewColorStateList)
        picker_preview_text.setTextColor(previewColorStateList)

        picker_bottom_bg.setBackgroundColor(theme.titleBgColor)

        val typeDrawable: Int =
                if (PickerTheme.isLightColor(theme.titleBgColor)) {
                    line1.setBackgroundColor(PickerWhiteTheme.color3)
                    R.drawable.picker_type_selector_white
                } else {
                    line1.setBackgroundColor(PickerWhiteTheme.color1)
                    R.drawable.picker_type_selector_wechat
                }
        picker_preview_type.setImageResource(typeDrawable)


    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        MediaListHolder.selectPhotos.clear()
        if (!mSelectItems.isNullOrEmpty()) {
            for (selectItem in mSelectItems!!) {
                selectItem.isSelected = true
            }
            MediaListHolder.selectPhotos.addAll(mSelectItems!!)
        }


        MediaScannerUtils.Builder(this)
                .gif(gif)
                .gifOnly(gifOnly)
                .video(video)
                .build()
                .scanner { success ->
                    if (success) {

                        picker_photo_load.visibility = View.GONE
                        mGridViewAdapter.reset(MediaListHolder.currentPhotos)
                        updateToolbar()

                        postDelay(Runnable {
                            mCateDlgAdapter.reset(MediaListHolder.allDirectories)
                        }, 350)
                    }


                }


        picker_send.setOnClickListener {

            setResult(Activity.RESULT_OK, Intent()
                    .putParcelableArrayListExtra(EXTRA_ITEMS, ArrayList(MediaListHolder.selectPhotos)))
            finish()
        }

        picker_type_text.setText(if (video) R.string.picker_all_video else R.string.picker_all_image)

        picker_type_text.isEnabled = true
        picker_type_text.setOnClickListener { showCatalog() }

        if (preview && !video) {
            picker_preview_text.visibility = View.VISIBLE
        } else {
            picker_preview_text.visibility = View.GONE
        }

        picker_preview_text.setOnClickListener {

            val item = MediaListHolder.selectPhotos[0]
            val intent = Intent(this@PictureSelectorActivity, PicturePreviewActivity::class.java)
            intent.putExtra(EXTRA_IS_GIF, item.isGif)
            intent.putExtra(EXTRA_MAX, max)
            intent.putExtra(EXTRA_IS_PREVIEW, true)

            startActivityForResult(intent, PICKER_REQUEST_PREVIEW)
        }


        picker_selector_mask.setOnClickListener(MaskClickListener())
        picker_bottom_mask.setOnClickListener(MaskClickListener())
        picker_catalog_mask.setOnClickListener(MaskClickListener())

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

    private inner class MaskClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            hideCatalog()
        }
    }

    private fun showCatalog() {
        if (isAnimating) return

        picker_catalog_mask.visibility = View.VISIBLE
        val translationY = ObjectAnimator.ofFloat(picker_catalog_lst, "translationY", cateHeight.toFloat(), 0f)
        translationY.duration = 300
        translationY.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isAnimating = false
                translationY.removeAllListeners()
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                picker_selector_mask.visibility = View.VISIBLE
                picker_bottom_mask.visibility = View.VISIBLE
                isAnimating = true
                isShowing = true
            }
        })
        translationY.start()
    }

    private fun hideCatalog() {
        if (isAnimating) return
        val translationY = ObjectAnimator.ofFloat(picker_catalog_lst, "translationY", 0f, cateHeight.toFloat())
        translationY.duration = 300
        translationY.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                picker_selector_mask.visibility = View.GONE
                picker_bottom_mask.visibility = View.GONE
                picker_catalog_mask.visibility = View.GONE
                isAnimating = false
                isShowing = false
                translationY.removeAllListeners()
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                isAnimating = true
            }
        })
        translationY.start()
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
                    mGridViewAdapter.notifyDataSetChanged()
                    updateToolbar()
                }

            } else if (requestCode == REQUEST_CAMERA) {
                if (mTakePictureUri != null) {
                    var path = mTakePictureUri!!.encodedPath// getPathFromUri(this, mTakePhotoUri);

                    if (path == null) {
                        Toast.makeText(this, if (video)
                            R.string.picker_video_failure
                        else
                            R.string.picker_photo_failure,
                                Toast.LENGTH_SHORT).show()
                        return
                    }
                    if (mTakePictureUri!!.toString().startsWith("content")) {
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
        MediaScannerUtils.Builder(this@PictureSelectorActivity)
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
                            mGridViewAdapter.add(photo)
                        } else {
                            MediaListHolder.currentPhotos.add(0, photo)
                            mGridViewAdapter.add(0, photo)
                        }
                    } else {
                        if (selectCateIndex == updateIndex) {
                            if (MediaListHolder.currentPhotos.isEmpty()) {
                                MediaListHolder.currentPhotos.add(photo)
                                mGridViewAdapter.add(photo)
                            } else {
                                MediaListHolder.currentPhotos.add(0, photo)
                                mGridViewAdapter.add(0, photo)
                            }
                        }
                    }

//                    mCateDlgAdapter.reset(MediaListHolder.allDirectories)
                    updateToolbar()
                }

    }

    override fun onBackPressed() {
        if (isShowing) {
            hideCatalog()
            return
        }
        if (!mSelectItems.isNullOrEmpty()) {
            setResult(Activity.RESULT_OK, Intent()
                    .putParcelableArrayListExtra(EXTRA_ITEMS, mSelectItems))
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
                mTakePictureUri = MyFileProvider.getUriForFile(this, applicationContext.packageName + ".demo.file_provider", file)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                mTakePictureUri = Uri.fromFile(file)
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePictureUri)
            startActivityForResult(intent, REQUEST_CAMERA)
        }
    }


    inner class SelectReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action ?: return) {
                PICKER_ACTION_MEDIA_SELECT -> {
                    val photo = intent.getParcelableExtra<Photo>(PICKER_EXTRA_PHOTO)
                    if (::mGridViewAdapter.isInitialized) {
                        val index = MediaListHolder.currentPhotos.indexOf(photo)

                        mGridViewAdapter.notifyItemChanged(if (isShowCamera) index + 1 else index)

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
                            mGridViewAdapter.add(photo)
                        } else {
                            MediaListHolder.currentPhotos.add(0, photo)
                            mGridViewAdapter.add(0, photo)
                        }
                    } else {
                        if (selectCateIndex == updateIndex) {
                            if (MediaListHolder.currentPhotos.isEmpty()) {
                                MediaListHolder.currentPhotos.add(photo)
                                mGridViewAdapter.add(photo)
                            } else {
                                MediaListHolder.currentPhotos.add(0, photo)
                                mGridViewAdapter.add(0, photo)
                            }
                        }
                    }

//                    if (mGridViewAdapter.itemCount > mCount) {
//                        Fresco.getImagePipeline().pause()
//                    }
//                    postDelay(Runnable { picker_photo_grd.smoothScrollToPosition(0) }, 50)

//                    mCateDlgAdapter.reset(MediaListHolder.allDirectories)
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
            picker_send.isEnabled = false
            picker_send.setText(R.string.picker_picsel_toolbar_send)
            picker_preview_text.isEnabled = false
            picker_preview_text.setText(R.string.picker_picsel_toolbar_preview)
        } else if (sum <= max) {
            picker_send.isEnabled = true
            picker_send.text = resources.getString(R.string.picker_picsel_toolbar_send_num, sum, max)
            picker_preview_text.isEnabled = true
            picker_preview_text.text = String.format(resources.getString(R.string.picker_picsel_toolbar_preview_num), sum)
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
