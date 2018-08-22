package com.hy.photopicker;

import android.app.Application;
import android.content.Context;

import com.hy.picker.PhotoModule;
import com.hy.picker.PhotoPicker;

/**
 * Created time : 2018/8/20 9:55.
 *
 * @author HY
 */
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
