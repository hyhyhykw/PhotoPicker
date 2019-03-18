package com.hy.picker.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

/**
 * Created time : 2019/3/18 10:56 AM.
 *
 * @author HY
 */
public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    public interface ScanListener {
        void onScanFinish(String path);
    }

    private MediaScannerConnection mMs;
    private String mPath;
    private ScanListener listener;

    public SingleMediaScanner(Context context, String path, ScanListener l) {
        listener = l;
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
        listener.onScanFinish(path);
    }
}
