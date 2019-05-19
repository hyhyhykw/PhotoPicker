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
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.transition.AutoTransition
import android.transition.Transition
import android.transition.TransitionManager
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.AbsListView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private var mSize = 0
    private var mCount = 0

    private val mConstraintSet1 = ConstraintSet()
    private val mConstraintSet2 = ConstraintSet()


    private var listLastY = 0f
    private var mScrollState = 0
    private var canDown = false

    private var isAnimating = false
    private var isShowing = false


    private val totalSelectedNum: Int
        get() = MediaListHolder.selectPhotos.size


    private var selectCateIndex = 0


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


        mDefaultDrawable = ContextCompat.getDrawable(this,PhotoPicker.mDefaultDrawable)!!

        val screenHeight = screenHeight()
        val statusBarHeight = getStatusBarHeight()
        val gridHeight = screenHeight - statusBarHeight - dp(96f)

        mCount = Math.ceil(gridHeight * 1.0 / mSize).toInt()

        val intent = intent
        max = intent.getIntExtra(EXTRA_MAX, 9)
        gif = intent.getBooleanExtra(EXTRA_SHOW_GIF, true)
        preview = intent.getBooleanExtra(EXTRA_PREVIEW, true)
        gifOnly = intent.getBooleanExtra(EXTRA_ONLY_GIF, false)
        video = intent.getBooleanExtra(EXTRA_PICK_VIDEO, false)
        isShowCamera = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, false)

        mSelectItems = intent.getParcelableArrayListExtra(EXTRA_ITEMS)

        mConstraintSet1.clone(picker_selector_root)
        mConstraintSet1.setMargin(R.id.picker_back, ConstraintSet.TOP, statusBarHeight)
        mConstraintSet1.applyTo(picker_selector_root)

        mConstraintSet2.clone(picker_selector_root)
        mConstraintSet2.setMargin(R.id.picker_back, ConstraintSet.TOP, statusBarHeight)
        mConstraintSet2.setVisibility(R.id.picker_photo_load, ConstraintSet.GONE)
        mConstraintSet2.clear(R.id.picker_catalog_lst, ConstraintSet.TOP)

        mConstraintSet2.connect(
                R.id.picker_catalog_lst,
                ConstraintSet.BOTTOM,
                R.id.picker_photo_grd,
                ConstraintSet.BOTTOM)
        mConstraintSet2.setVisibility(R.id.picker_selector_mask, ConstraintSet.VISIBLE)
        mConstraintSet2.setVisibility(R.id.picker_catalog_mask, ConstraintSet.VISIBLE)
        mConstraintSet2.setVisibility(R.id.picker_bottom_mask, ConstraintSet.VISIBLE)

        picker_back.setOnClickListener { onBackPressed() }

        picker_type_text.isEnabled = false
        picker_type_text.setText(if (video) R.string.picker_all_video else R.string.picker_all_image)

        picker_preview_text.isEnabled = !mSelectItems.isNullOrEmpty()

        picker_title.setText(if (video) R.string.picker_picsel_videotype else R.string.picker_picsel_pictype)

//        picker_preview_type.setImageDrawable(typeDrawable)
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

//        val spanCount = AttrsUtils.getTypeValueInt(this, R.attr.picker_grid_span)

        mSize = (PhotoContext.screenWidth - dp(4f) * 3) / 4
        mGridViewAdapter = PictureAdapter(max, preview, isShowCamera, video, mDefaultDrawable, mSize)

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
                MediaListHolder.currentPhotos.clear()
                MediaListHolder.currentPhotos.addAll(MediaListHolder.allDirectories[position].photos)

                mGridViewAdapter.reset(MediaListHolder.currentPhotos)
                val layoutManager = picker_photo_grd.layoutManager as GridLayoutManager?
                if (layoutManager != null && layoutManager.findFirstVisibleItemPosition() != 0) {
                    if (MediaListHolder.currentPhotos.size > mCount) {
                        Fresco.getImagePipeline().pause()
                    }
                    postDelay(Runnable { picker_photo_grd.smoothScrollToPosition(0) }, 350)
                }
            }
        }
        picker_catalog_lst.adapter = mCateDlgAdapter
        //        mCatalogListView.setTranslationY(catalogHeight);

        Looper.myQueue().addIdleHandler {
            val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val permissionNames = Permission.transformText(this, *perms)
            val message = getString(R.string.picker_message_permission_rationale, TextUtils.join("\n", permissionNames))
            EasyPermissions.requestPermissions(
                    this,
                    message,
                    RC_WRITE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            false
        }
    }

    private fun initTheme() {
        val theme = PhotoPicker.theme

        picker_title_bg.setBackgroundColor(theme.titleBgColor)
        picker_back.setColorFilter(theme.backIvColor)
        picker_title.setTextColor(theme.titleTvColor)

        val drawable = GradientDrawable()
        drawable.setColor(theme.sendBgColor)
        drawable.cornerRadius = dp(5f).toFloat()
        picker_send.background = drawable


        val sendColors = intArrayOf(theme.sendTvColorDisable, theme.sendTvColorEnable)
        val sendStates = arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled))

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
                    if (success)
                        runOnUiThread {
                            mCateDlgAdapter.reset(MediaListHolder.allDirectories)
                            mGridViewAdapter.reset(MediaListHolder.currentPhotos)
                            updateToolbar()
                            mConstraintSet1.setVisibility(R.id.picker_photo_load, ConstraintSet.GONE)
                            mConstraintSet1.applyTo(picker_selector_root)
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

        picker_catalog_lst.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                mScrollState = scrollState
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                canDown = if (firstVisibleItem == 0) {
                    val view1 = picker_catalog_lst.getChildAt(0)
                    view1 != null && view1.top == 0
                } else {
                    false
                }
            }
        })
        picker_catalog_lst.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> listLastY = event.y
                MotionEvent.ACTION_UP -> if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {//判断是否在滑动
                    if (canDown && event.y - listLastY >= 20) {//判断到达顶部后是否又向下滑动了20像素 可以修改
                        hideCatalog()
                        return@setOnTouchListener true
                    }
                }
            }
            false
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

    private inner class MaskClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            hideCatalog()
        }
    }

    private fun showCatalog() {
        if (isAnimating) return
        val autoTransition = AutoTransition()
        autoTransition.duration = 300
        autoTransition.addListener(object : MyTransitionListener() {
            override fun onTransitionStart(transition: Transition) {
                isAnimating = true
                isShowing = true
            }

            override fun onTransitionEnd(transition: Transition) {
                isAnimating = false
                transition.removeListener(this)
            }
        })
        TransitionManager.beginDelayedTransition(picker_selector_root, autoTransition)
        mConstraintSet2.applyTo(picker_selector_root)
    }

    private fun hideCatalog() {
        if (isAnimating) return

        val autoTransition = AutoTransition()
        autoTransition.duration = 300
        autoTransition.addListener(object : MyTransitionListener() {
            override fun onTransitionStart(transition: Transition) {
                isAnimating = true
            }

            override fun onTransitionEnd(transition: Transition) {
                isAnimating = false
                isShowing = false
                transition.removeListener(this)
            }
        })
        TransitionManager.beginDelayedTransition(picker_selector_root, autoTransition)
        mConstraintSet1.applyTo(picker_selector_root)

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

                    postDelay(Runnable { picker_photo_grd.smoothScrollToPosition(0) }, 50)
                    mCateDlgAdapter.reset(MediaListHolder.allDirectories)
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

                    if (mGridViewAdapter.itemCount > mCount) {
                        Fresco.getImagePipeline().pause()
                    }
                    postDelay(Runnable { picker_photo_grd.smoothScrollToPosition(0) }, 50)

                    mCateDlgAdapter.reset(MediaListHolder.allDirectories)
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
