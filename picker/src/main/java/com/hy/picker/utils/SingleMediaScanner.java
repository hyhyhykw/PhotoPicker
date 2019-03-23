package com.hy.picker.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.lang.ref.WeakReference;

/**
 * Created time : 2019/3/18 10:56 AM.
 *
 * @author HY
 */
public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    public interface ScanListener {
        void onScanFinish(String path);
    }

    private final MediaScannerConnection mMs;
    private final String mPath;
    private WeakReference<ScanListener> mReference;

    public SingleMediaScanner(Context context, String path, ScanListener l) {
        mReference = new WeakReference<>(l);
        mPath = path;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mMs.scanFile(mPath, null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mMs.disconnect();
        if (null == mReference) return;
        ScanListener listener = mReference.get();
        if (null == listener) return;
        listener.onScanFinish(path);
    }
}
