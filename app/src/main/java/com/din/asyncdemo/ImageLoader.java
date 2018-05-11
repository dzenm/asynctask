package com.din.asyncdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageLoader {

    private ImageView imageView;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            imageView.setImageBitmap((Bitmap) msg.obj);
        }
    };

    public void showImageByThread(ImageView imageView, final String url) {
        this.imageView = imageView;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getBitmapForURL(url);
                Message message = Message.obtain();
                message.obj = bitmap;
                handler.sendMessage(message);
            }
        }).start();
    }

    public Bitmap getBitmapForURL(String urlString) {
        Bitmap bitmap;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(urlString).build();
            Response response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}