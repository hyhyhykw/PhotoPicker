#PhotoPicker<br>
[![](https://jitpack.io/v/hyhyhykw/photopicker.svg)](https://jitpack.io/#hyhyhykw/photopicker)<br>
Use：[中文版](README.md)<br>
Step 1. Add the JitPack repository to your build file.<br>
Add it in your root build.gradle at the end of repositories:
```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Step 2. Add the dependency.
```gradle
	dependencies {
	        implementation 'com.github.hyhyhykw:PhotoPicker:1.8'
	}
```

Step 3. Initialization,Call PhotoPicker.init method in Application and implement PhotoModule interface.
```Java
public class MyApp extends Application implements PhotoModule {
    @Override
    public void onCreate() {
        super.onCreate();
        PhotoPicker.init(this);
    }

    @Override
    public Context getContext() {
        return this;
    }
}
```

Step 4. Configuring FileProvider in AndroidManifest.xml<br>
Create a new xml folder in your project res folder and create file_path.xml where external-path must be configured
```xml
<paths>

    <!-- /data/user/0/YourPackageName/cache-->
    <cache-path
        name="picker_private_cache"
        path=""/>

    <files-path
        name="picker_private_file"
        path=""/>
    <!--/storage/emulated/0/Android/data/YourPackageName/cache-->
    <external-cache-path
        name="picker_external_cache"
        path=""/>

    <external-files-path
        name="picker_external_file"
        path=""/>

    <external-path
        name="external_storage_root"
        path="."/>

    <external-path
        name="files_root"
        path="Android/data/YourPackageName/"/>
</paths>
```

Then copy the following to AndroidManifest.xml
```xml
<provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="YourPackageName.file_provider"
            android:exported="false"
            android:grantUriPermissions="true">

            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths"/>

        </provider>
```

Step 5. Add com.hy.picker.PictureSelectorActivity and com.hy.picker.PicturePreviewActivity to AndroidManifest.xml<br>
Then configuration theme is PickerWeChatTheme or PickerWhiteTheme or PickerQQTheme, or the custom theme must include the following attributes<br>
picker_send_color : Choose button background color<br>
picker_title_bg : Title bar background color<br>
picker_title_color : Title text color<br>
picker_bottom_color : Bottom bar background color<br>
picker_back_color : Back button color<br>
picker_send_color_disable :  The text color when the choose button disable<br>
picker_send_color_enable : The text color when the choose button enable<br>
picker_preview_color_disable : The text color when the preview button disable<br>
picker_preview_color_enable : The text color when the preview button enable<br>
picker_status_black ：Whether to set the status bar black font(Set if the theme color is a bright color)

Step 6. select photo
```Java
new PhotoPicker()
    .max(9)//Maximum number of pictures
    .select(new ArrayList<PictureSelectorActivity.PicItem>())//Selected image
    .start(PhotoListener);//Picture selection callback
```

proguard-rules<br>
-keep class com.hy.picker.core.CrystalCategory{*;}
-keep class com.hy.picker.core.CrystalResult{*;}
