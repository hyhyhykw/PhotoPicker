#PhotoPicker<br>
[![](https://jitpack.io/v/hyhyhykw/PhotoPicker.svg)](https://jitpack.io/#hyhyhykw/PhotoPicker)<br>
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
	        implementation 'com.github.hyhyhykw:PhotoPicker:4.1'
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
