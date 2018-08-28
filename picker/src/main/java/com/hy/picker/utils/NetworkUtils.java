package com.hy.picker.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created time : 2018/8/28 9:32.
 *
 * @author HY
 */
public class NetworkUtils {
    private static NetworkUtils instance;

    private NetworkUtils() {
    }

    public synchronized static NetworkUtils getInstance() {
        if (null == instance) {
            instance = new NetworkUtils();
        }
        url = null;
        return instance;
    }

    private static String url;

    public NetworkUtils url(String url) {
        NetworkUtils.url = url;
        return this;
    }

    public interface TaskListener {
        void onSuccess(String json);

        void onFailed();
    }

    private static TaskListener mTaskListener;

    public void start(TaskListener taskListener) {
        mTaskListener = taskListener;
        new RequestTask(this).execute();
    }

    public static class RequestTask extends AsyncTask<String, Void, String> {
        private WeakReference<NetworkUtils> mWeakReference;

        RequestTask(NetworkUtils utils) {
            mWeakReference = new WeakReference<>(utils);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (null == mWeakReference) return null;
            NetworkUtils utils = mWeakReference.get();
            if (utils == null) return null;

            URL url;
            try {
                url = new URL(NetworkUtils.url);
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
                conn.setDoInput(true);
                conn.connect();
                input = conn.getInputStream();
            } catch (IOException e) {
                return null;
            }


            byte[] bytes = new byte[1024];
            int len;
            StringBuilder sbl = new StringBuilder();
            try {
                while ((len = input.read(bytes)) != -1) {
                    sbl.append(new String(bytes, 0, len));
                }
            } catch (IOException e) {
                return null;
            }

            try {
                input.close();
            } catch (IOException e) {
                Log.d("TAG", "input close error");
            }
            conn.disconnect();

            return sbl.toString();
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            if (null == mWeakReference) return;
            NetworkUtils utils = mWeakReference.get();
            if (utils == null) return;
            if (null == json) {
                mTaskListener.onFailed();
            } else {
                mTaskListener.onSuccess(json);
            }
        }
    }
}
