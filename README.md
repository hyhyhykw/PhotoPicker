#图片选择<br>
[![](https://jitpack.io/v/hyhyhykw/PhotoPicker.svg)](https://jitpack.io/#hyhyhykw/PhotoPicker)<br>
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
	        implementation 'com.github.hyhyhykw:PhotoPicker:4.6'
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

有修改 具体见sample

第六步：把大象放进去
```Java
new PhotoPicker()
    .max(9)//最大数量
    .select(new ArrayList<PictureSelectorActivity.PicItem>())//已经选择的图片
    .start(PhotoListener);//图片选择回调
```

混淆配置<br>
-keep class com.hy.picker.core.CrystalCategory{*;}
-keep class com.hy.picker.core.CrystalResult{*;}
