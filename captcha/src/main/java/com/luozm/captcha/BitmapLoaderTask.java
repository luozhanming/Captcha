package com.luozm.captcha;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 网络加载图片的Task类
 * Created by cdc4512 on 2018/5/7.
 */

class BitmapLoaderTask extends AsyncTask<String, Integer, Bitmap> {

    private Callback callback;

    public BitmapLoaderTask(Callback callback) {
        this.callback = callback;
    }

    interface Callback{
         void result(Bitmap bitmap);
    }
    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap result = null;
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            url = new URL(params[0]);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code == 200) {
                is = conn.getInputStream();
                result = BitmapFactory.decodeStream(is);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        callback.result(bitmap);
    }
}
