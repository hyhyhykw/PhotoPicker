package com.hy.picker.utils;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

/**
 * Created time : 2019/3/23 8:54 AM.
 *
 * @author HY
 */
public abstract class ImgScanListener<T> implements SingleMediaScanner.ScanListener {

    private WeakReference<T> mReference;

    public ImgScanListener(T t) {
        mReference = new WeakReference<>(t);
    }

    @Override
    public void onScanFinish(String path) {
        if (null == mReference) return;
        T t = mReference.get();
        if (null == t) return;
        onScanFinish(t, path);
    }

    protected abstract void onScanFinish(@NonNull T t, String path);
}
