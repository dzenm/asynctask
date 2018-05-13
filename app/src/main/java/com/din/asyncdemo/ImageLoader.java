package com.din.asyncdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageLoader {

    private String url;
    private LruCache<String, Bitmap> lruCache;      // 创建Cache
    private RecyclerView recyclerView;
    private List<MyAsyncTask> myAsyncTasks;


    // 第一部分，图片缓存
    public ImageLoader(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        myAsyncTasks = new ArrayList<>();
        int macMemory = (int) Runtime.getRuntime().maxMemory();         // 获取最大可用内存
        int cacheSize = macMemory / 4;
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();        // 在每次存入缓存的时候调用
            }
        };
    }

    // 增加到缓存
    private void addBitmapToCache(String url, Bitmap bitmap) {
        // 判断是否存在缓存数据
        if (getBitmapFromCache(url) == null) {
            lruCache.put(url, bitmap);
        }
    }

    // 从缓存种获取数据
    private Bitmap getBitmapFromCache(String url) {
        return lruCache.get(url);
    }

    // 第二部分，使用多线程异步加载网络图片
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ImageView imageView = (ImageView) recyclerView.findViewWithTag(url);
            if (imageView.getTag().equals(url)) {
                imageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    public void showImageByThread(final ImageView imageView, final String url) {
        this.url = url;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getBitmapFromCache(url);
                if (bitmap == null) {
                    imageView.setImageResource(R.drawable.ic_launcher_background);
                    // 通过Handler通知下载图片
                    Bitmap mBitmap = getBitmapFromURL(url);
                    Message message = Message.obtain();
                    message.obj = mBitmap;
                    handler.sendMessage(message);
                } else {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }).start();
    }


    // 第三部分，使用AysncTask异步加载网络图片
    public void showImageViewByAsyncTask(ImageView imageView, String url) {
        Bitmap bitmap = getBitmapFromCache(url);
        // 如果缓存不存在图片，设为默认值
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private String url;

        public MyAsyncTask(String url) {
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = getBitmapFromURL(url);
            // 从网络获取图片
            if (bitmap != null) {
                addBitmapToCache(url, bitmap);           // 将不在缓存的图片缓存起来
                return bitmap;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) recyclerView.findViewWithTag(url);
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    // 第四部分，通过从网络加载图片
    public Bitmap getBitmapFromURL(String urlString) {
        Bitmap bitmap;
        try {
            // 通过okhttp加载图片
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(urlString).build();
            Response response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();            // 返回图片字节输入流
            bitmap = BitmapFactory.decodeStream(inputStream);            // 通过BitmapFactory的转换流方法转化为bitmap
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 第五部分，加载可见部分的图片
    public void loadImages(int start, int end) {
        String url;
        // 对可见部分的图片url进行获取
        for (int i = start; i < end; i++) {
            url = NewsAdapter.urls[i];
            Bitmap bitmap = getBitmapFromCache(url);
            // 如果缓存中不存在，则下载图片
            if (bitmap == null) {
                MyAsyncTask task = new MyAsyncTask(url);
                task.execute();
                myAsyncTasks.add(task);         // 保存下载图片任务
            } else {
                ImageView imageView = (ImageView) recyclerView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }


    // 第六部分，取消所有的加载任务
    public void canaelAllTask() {
        if (myAsyncTasks != null) {
            for (MyAsyncTask task : myAsyncTasks) {
                task.cancel(false);
            }
        }
    }
}