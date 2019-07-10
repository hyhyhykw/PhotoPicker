package com.hy.photopicker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmableRegistry;
import com.facebook.common.memory.NoOpMemoryTrimmableRegistry;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.hy.crash.CrashModule;
import com.hy.picker.PhotoModule;
import com.hy.picker.PhotoPicker;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;

import androidx.annotation.NonNull;

/**
 * Created time : 2018/8/20 9:55.
 *
 * @author HY
 */
public class MyApp extends Application implements PhotoModule, CrashModule {
    @Override
    public void onCreate() {
        super.onCreate();
        PhotoPicker.Companion.init(this);

        MemoryTrimmableRegistry memoryTrimmableRegistry = NoOpMemoryTrimmableRegistry.getInstance();
        memoryTrimmableRegistry.registerMemoryTrimmable(trimType -> {
            final double suggestedTrimRatio = trimType.getSuggestedTrimRatio();


            if (MemoryTrimType.OnCloseToDalvikHeapLimit.getSuggestedTrimRatio() == suggestedTrimRatio
                    || MemoryTrimType.OnSystemLowMemoryWhileAppInBackground.getSuggestedTrimRatio() == suggestedTrimRatio
                    || MemoryTrimType.OnSystemLowMemoryWhileAppInForeground.getSuggestedTrimRatio() == suggestedTrimRatio
            ) {
                //清除内存缓存
                Fresco.getImagePipeline().clearMemoryCaches();
//                Fresco.getImagePipeline().clearCaches();
            }
        });

        File externalCacheDir = getExternalCacheDir();
        if (externalCacheDir==null){
            externalCacheDir=getCacheDir();
        }

        //小图片的磁盘配置,用来储存用户头像之类的小图
        DiskCacheConfig diskSmallCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryPath(externalCacheDir)//缓存图片基路径
                .setBaseDirectoryName("fresco")//文件夹名
                .setMaxCacheSize(20 * ByteConstants.MB)//默认缓存的最大大小。
                .setMaxCacheSizeOnLowDiskSpace(10 * ByteConstants.MB)//缓存的最大大小,使用设备时低磁盘空间。
                .setMaxCacheSizeOnVeryLowDiskSpace(5 * ByteConstants.MB)//缓存的最大大小,当设备极低磁盘空间
                .build();
        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig
                .newBuilder(this)
                .setDownsampleEnabled(true)
                .setResizeAndRotateEnabledForNetwork(true)
                .setBitmapMemoryCacheParamsSupplier(new MyBitmapMemoryCacheParamsSupplier((ActivityManager) getSystemService(ACTIVITY_SERVICE)))
                .setMemoryTrimmableRegistry(memoryTrimmableRegistry)
                .setSmallImageDiskCacheConfig(diskSmallCacheConfig)
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();
        Fresco.initialize(this, imagePipelineConfig);
//        CrashHandler.getInstance().install(this, this);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    @NonNull
    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Class<? extends Activity> getLaunchActivity() {
        return MainActivity.class;
    }

    @Override
    public void upload(String filePath) {

    }

    @Override
    public boolean debug() {
        return false;
    }
}
