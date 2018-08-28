#图片选择<br>
使用：[English](README-en.md)<br>
第一步：将 JitPack 仓库添加到你的build文件中<br>
将下列代码添加到项目中的build.gradle文件中:
```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

第二步：添加依赖
```gradle
	dependencies {
	        implementation 'com.github.hyhyhykw:PhotoPicker:1.3'
	}
```

第三步：初始化，在Application中调用PhotoPicker.init方法，并实现PhotoModule接口
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

第四步：在AndroidManifest.xml中配置FileProvider<br>
在你的项目资源文件中中新建xml文件夹，并且创建file_path.xml，其中external-path必须配置
```xml
<paths>

    <!-- /data/user/0/你的包名/cache-->
    <cache-path
        name="picker_private_cache"
        path=""/>

    <files-path
        name="picker_private_file"
        path=""/>
    <!--/storage/emulated/0/Android/data/你的包名/cache-->
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
        path="Android/data/你的包名/"/>
</paths>
```

然后复制以下内容到AndroidManifest.xml中
```xml
<provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="你的包名.file_provider"
            android:exported="false"
            android:grantUriPermissions="true">

            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths"/>

        </provider>
```
第五步：添加com.hy.picker.PictureSelectorActivity和com.hy.picker.PicturePreviewActivity到AndroidManifest.xml中<br>
并且配置主题为PickerWeChatTheme或PickerWhiteTheme或PickerQQTheme，或者自定义主题其中必须包括以下属性<br>
picker_send_color ：选择按钮的颜色<br>
picker_title_bg ：顶部标题栏的背景颜色<br>
picker_title_color ：标题字体颜色<br>
picker_bottom_color ：底部栏的颜色<br>
picker_back_color ：返回按钮的颜色<br>
picker_send_color_disable ：选择按钮禁用时的字体颜色<br>
picker_send_color_enable ：选择按钮启用时的字体颜色<br>
picker_preview_color_disable ：预览按钮禁用时的字体颜色<br>
picker_preview_color_enable ：预览按钮启用时的字体颜色<br>
picker_status_black ：是否设置状态栏黑色字体(如果主题颜色是明亮的颜色时设置)

第六步：把大象放进去
```Java
new PhotoPicker()
    .max(9)//最大数量
    .select(new ArrayList<PictureSelectorActivity.PicItem>())//已经选择的图片
    .start(PhotoListener);//图片选择回调
```

混淆配置<br>
-keep class me.kareluo.imaging.core.CrystalCategory{*;}
-keep class me.kareluo.imaging.core.CrystalResult{*;}