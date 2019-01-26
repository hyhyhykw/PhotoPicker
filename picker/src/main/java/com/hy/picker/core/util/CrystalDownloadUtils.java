package com.hy.picker.core.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created time : 2018/8/27 11:25.
 *
 * @author HY
 */
public class CrystalDownloadUtils {

    private String url;
    private File file;
    private int length;
    private static CrystalDownloadUtils instance;

    private static final ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    public synchronized static CrystalDownloadUtils getInstance() {
        if (null == instance) {
            instance = new CrystalDownloadUtils();
        }
        return instance;
    }

    private CrystalDownloadUtils() {
    }

    public CrystalDownloadUtils url(String url) {
        this.url = url;
        return this;
    }

    public CrystalDownloadUtils file(File file) {
        this.file = file;
        return this;
    }

    public CrystalDownloadUtils length(int length) {
        this.length = length;
        return this;
    }

    private DownloadListener mDownloadListener;

    public void download(DownloadListener downloadListener) {
        mDownloadListener = downloadListener;
        addTask(new DownloadTask(this));
    }

    private void addTask(final AsyncTask<String, Integer, File> task) {
        mDownloadListener.onStart();
        singleThreadPool.execute(task::execute);
    }

    public static class DownloadTask extends AsyncTask<String, Integer, File> {
        private WeakReference<CrystalDownloadUtils> mWeakReference;

        DownloadTask(CrystalDownloadUtils utils) {
            mWeakReference = new WeakReference<>(utils);
        }

        @Override
        protected File doInBackground(String... strings) {
            if (null == mWeakReference) return null;
            CrystalDownloadUtils utils = mWeakReference.get();
            if (utils == null) return null;
            if (!utils.file.getParentFile().exists()) {
                boolean mkdirs = utils.file.getParentFile().mkdirs();
                Log.d("TAG", "文件夹创建" + (mkdirs ? "成功" : "失败"));
            }

            if (!utils.file.exists()) {
                try {
                    boolean newFile = utils.file.createNewFile();
                    Log.d("TAG", "文件" + utils.file + "创建" + (newFile ? "成功" : "失败"));
                } catch (IOException e) {
                    Log.d("TAG", "文件创建失败");
                    return null;
                }
            }

            URL url;
            try {
                url = new URL(utils.url);
            } catch (MalformedURLException e) {
                return null;
            }
            HttpURLConnection conn;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                return null;
            }
            InputStream input;
            try {
//                conn.setRequestProperty("Accept-Encoding", "identity");
//                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                input = conn.getInputStream();
            } catch (IOException e) {
                return null;
            }
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(utils.file);
            } catch (FileNotFoundException e) {
                return null;
            }
            int sum = 0;

            byte[] bytes = new byte[1024];
            int len;

            try {
                while ((len = input.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                    sum += len;
                    int progress = (int) (sum / (float) utils.length * 100);
                    publishProgress(progress);
                }
            } catch (IOException e) {
                return null;
            }

            try {
                fos.close();
            } catch (IOException e) {
                Log.d("TAG", "fos close error");
            }
            try {
                input.close();
            } catch (IOException e) {
                Log.d("TAG", "input close error");
            }
            conn.disconnect();

            return utils.file;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (null == mWeakReference) return;
            CrystalDownloadUtils utils = mWeakReference.get();
            if (utils == null) return;
            if (null == file) {
                boolean delete = utils.file.delete();
                Log.d("TAG", "文件删除" + (delete ? "成功" : "失败"));
                utils.mDownloadListener.onFailed();
            } else {
                utils.mDownloadListener.onSuccess();
            }
            utils.file = null;
            utils.url = null;
            utils.length = 0;
            utils.mDownloadListener = null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (null == mWeakReference) return;
            CrystalDownloadUtils utils = mWeakReference.get();
            if (utils == null) return;
            utils.mDownloadListener.onProgress(values[0]);
        }
    }

    public interface DownloadListener {

        void onStart();

        void onProgress(int progress);

        void onSuccess();

        void onFailed();
    }
}
