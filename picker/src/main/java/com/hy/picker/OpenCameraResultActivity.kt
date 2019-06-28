package com.hy.picker

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hy.picker.model.Photo
import com.hy.picker.utils.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.*

/**
 * Created time : 2018/8/23 10:56.
 *
 * @author HY
 */
class OpenCameraResultActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {
    //    public static final int REQUEST_EDIT = 0x753;
    private val video by lazy {
        intent.getBooleanExtra(EXTRA_PICK_VIDEO, false)
    }

    private val sureReceiver by lazy {
        SureReceiver()
    }

    private val isEdit by lazy {
        intent.getBooleanExtra(EXTRA_EDIT, false)
    }

    private var takePictureUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        val intentFilter = IntentFilter()
        intentFilter.addAction(PICKER_ACTION_MEDIA_SURE)
        registerReceiver(sureReceiver, intentFilter)

        if (EasyPermissions.hasPermissions(this, *CAMERA_PERMISSION)) {
            requestCamera()
        } else {
            val p = when {
                EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> arrayOf(Manifest.permission.CAMERA)
                EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA) -> arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                else -> CAMERA_PERMISSION
            }
            val permissionNames = Permission.transformText(this, *p)
            val message = getString(R.string.picker_message_permission_rationale, TextUtils.join("\n", permissionNames))
            EasyPermissions.requestPermissions(
                    this,
                    message,
                    RC_CAMERA_STORAGE, *p)
        }

    }


    override fun onDestroy() {
        unregisterReceiver(sureReceiver)
        super.onDestroy()
    }

    private fun toEdit(uri: Uri) {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!path.exists()) {
            val mkdirs = path.mkdirs()
            if (BuildConfig.DEBUG) {
                Log.d("TAG", "文件夹：" + path + "创建" + if (mkdirs) "成功" else "失败")
            }

        }

        val name = "IMG-EDIT-" + CommonUtils.format(Date(), "yyyy-MM-dd-HHmmss") + ".jpg"
        val editFile = File(path, name)

        startActivity(Intent(this, IMGEditActivity::class.java)
                .putExtra(EXTRA_IMAGE_URI, uri)
                .putExtra(EXTRA_IMAGE_SAVE_PATH, editFile.absolutePath))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_CAMERA_STORAGE) {
            if (EasyPermissions.hasPermissions(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
                requestCamera()
            } else {
                Toast.makeText(this, R.string.picker_str_permission_denied, Toast.LENGTH_SHORT).show()
                finish()
            }
            return
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                if (takePictureUri != null) {
                    var path = takePictureUri!!.encodedPath// getPathFromUri(this, mTakePhotoUri);

                    if (path == null) {
                        Toast.makeText(this, if (video) R.string.picker_video_failure else R.string.picker_photo_failure, Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                    if (takePictureUri!!.toString().startsWith("content")) {
                        path = path.replace("/external_storage_root".toRegex(), "")

                        path = Environment.getExternalStorageDirectory().toString() + path
                    }

                    val file = File(path)

                    if (file.exists()) {
                        SingleMediaScanner(PhotoContext.context, path, object : ImgScanListener<OpenCameraResultActivity>(this) {
                            override fun onScanFinish(t: OpenCameraResultActivity, path: String) {
                                t.getPhoto(path, file)
                            }
                        })
                    } else {
                        Toast.makeText(this, if (video) R.string.picker_video_failure else R.string.picker_photo_failure, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, if (video) R.string.picker_video_failure else R.string.picker_photo_failure, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.hasPermissions(this, *CAMERA_PERMISSION)) {
            requestCamera()
        } else {
            Toast.makeText(this, R.string.picker_str_permission_denied, Toast.LENGTH_SHORT).show()
            finish()
        }

    }


    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            val p = when {
                EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> arrayOf(Manifest.permission.CAMERA)
                EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA) -> arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                else -> CAMERA_PERMISSION
            }
            val permissionNames = Permission.transformText(this, *p)
            val message = getString(R.string.picker_message_permission_always_failed, TextUtils.join("\n", permissionNames))

            AppSettingsDialog.Builder(this)
                    .setRationale(message)
                    .setRequestCode(requestCode)
                    .build()
                    .show()
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    override fun onRationaleDenied(requestCode: Int) {
        Toast.makeText(this, R.string.picker_str_permission_denied, Toast.LENGTH_SHORT).show()
        finish()
    }


    private inner class SureReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (PICKER_ACTION_MEDIA_SURE != intent?.action) return
            runOnUiThread {
                val photo = intent.getParcelableExtra<Photo>(PICKER_EXTRA_PHOTO)
                this@OpenCameraResultActivity.setResult(RESULT_OK, Intent()
                        .putExtra(EXTRA_ITEM, photo))
                finish()
            }

        }
    }


    private fun getPhoto(path: String, file: File) {
        MediaScannerUtils.Builder()
                .path(path)
                .video(video)
                .add(false)
                .build()
                .scanner { photo, _ ->
                    if (photo == null) {
                        Toast.makeText(this@OpenCameraResultActivity, if (video) R.string.picker_video_failure else R.string.picker_photo_failure, Toast.LENGTH_SHORT).show()
                        return@scanner
                    }
                    if (video) {
                        setResult(RESULT_OK, Intent()
                                .putExtra(EXTRA_ITEM, photo))
                        finish()
                    } else {
                        if (isEdit) {
                            toEdit(Uri.fromFile(file))
                        } else {
                            setResult(RESULT_OK, Intent()
                                    .putExtra(EXTRA_ITEM, photo))

                            finish()
                        }
                    }
                }

    }

    private fun requestCamera() {
        if (!CommonUtils.existSDCard()) {
            Toast.makeText(this, R.string.picker_empty_sdcard, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!path.exists()) {
            val mkdirs = path.mkdirs()
            if (BuildConfig.DEBUG) {
                Log.d("TAG", "文件夹：" + path + "创建" + if (mkdirs) "成功" else "失败")
            }

        }


        val name = (if (video) "VIDEO-" else "IMG-") + CommonUtils.format(Date(), "yyyy-MM-dd-HHmmss") + if (video) ".mp4" else ".jpg"
        val file = File(path, name)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
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

}
