package com.hy.photopicker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.hy.crash.CrashModule;
import com.hy.picker.PhotoModule;
import com.hy.picker.PhotoPicker;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created time : 2018/8/20 9:55.
 *
 * @author HY
 */
public class MyApp extends Application implements PhotoModule, CrashModule {
    @Override
    public void onCreate() {
        super.onCreate();
        PhotoPicker.init(this);
//        Fresco.initialize(this);
//        CrashHandler.getInstance().install(this, this);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

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
}
