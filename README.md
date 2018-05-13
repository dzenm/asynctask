# 网络图片的异步加载实现



##**网络图片的异步加载图片的实现**



> * 通过异步加载，避免阻塞UI线程
> * 通过LruCache，将已下载图片放到内存中
> * 通过判断RecyclerView滑动状态，决定何时加载图片
> * 不仅仅是RecyclerView，任何控件都可以使用异步加载





----



####  一、网络请求

```java
private List<News> getHttpRequestData() {
        List<News> list = new ArrayList<>();
        try {
            // 通过OKHttp发起网络请求
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(URL).build();
            Response response = client.newCall(request).execute();
            // 请求返回的数据
            String data = response.body().string();
            // 开始JSON解析
            JSONObject jsonObject = new JSONObject(data);
            // 判断JSON解析获取的状态值
            String status = jsonObject.getString("status");
            if (status.equals("1")) {
                // JSON解析数组
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject dataJSONObject = jsonArray.getJSONObject(i);
                    int id = dataJSONObject.getInt("id");
                    String pic = dataJSONObject.getString("picBig");
                    String name = dataJSONObject.getString("name");
                    // 解析的结果放在adapter的list中
                    list.add(new News(id, pic, name));
                }
            } else {
                Toast.makeText(this, "获取JSON数据失败", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    return list;
}
```



- 网络请求使用已经封装好的okhttp3，okhttp3需要传入一个URL，返回JSON数据，然后开始JSON数据解析，最后把解析之后需要显示的数据放在Adapter需要用到的List里。



------



#### 二、异步加载



```
	/**
     * 实现网络的异步访问
     */
    class NewsAsync extends AsyncTask<String, Void, List<News>> {
        // 运行在后台，不能运行在UI线程，会阻塞UI线程
        @Override
        protected List<News> doInBackground(String... strings) {
            return getHttpRequestData();
        }

        // 运行在UI线程中，主要为设置adapter和设置布局方向
        @Override
        protected void onPostExecute(List<News> list) {
            super.onPostExecute(list);
            NewsAdapter newsAdapter = new NewsAdapter(MainActivity.this, list);
            final LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
            bind.recyclerView.setLayoutManager(manager);
            bind.recyclerView.setAdapter(newsAdapter);
        }
    }
```



- 实现异步加载需要实现AsyncTask的doInBackground方法。新建一个类继承AsyncTask，AsyncTask的三个参数，最后一个参数是异步加载数据时返回的数据的存储。
- doInBackground()方法是唯一一个运行在后台中的，并且改方法不能运行在UI线程，否则会阻塞UI线程。因此网络请求及数据解析耗时操作应放在此处
- 异步加载的结果处理在onPostExcute中执行，解析完数据需要使用的是一个RecyclerView来展示数据，在onPostExcute方法中将解析的结果通过RecyclerView展列出来。RecyclerView展示数据则通过设置adapter，数据和布局。



------



#### 三、图片的缓存机制

```
Glide.with(context).load(news.getPic()).into(holder.imageView);
```

- 因为是通过异步加载的数据，所以RecyclerView设置的数据不会存在耗时的问题，不需要设置tag



##### 2018/5/13更新图片的缓存机制



> 通过RecyclerView的滑动监听事件，监听滑动到哪然后去加载当前可见项的图片。



* RecyclerView的滑动监听事件

```
bind.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 获取RecyclerView布局管理
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                // 判断是否是LinearLayoutManager
                if (manager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayout = (LinearLayoutManager) manager;
                    startItem = linearLayout.findFirstVisibleItemPosition();
                    endItem = linearLayout.findLastVisibleItemPosition();
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        // 滑动停止时加载可见项
                        imageLoader.loadImages(startItem, endItem);
                    } else {
                        // 停止加载
                        imageLoader.canaelAllTask();
                    }
                }
            }
        });
    }
```



* NewsAdapter绑定数据时通过调用异步加载任务去下载网络图片

```
 String tag = news.getPic();
        holder.imageView.setTag(tag);
        // 第二种使用多线程加载
//        imageLoader.showImageByThread(holder.imageView, tag);
        // 第三种使用异步加载图片
        imageLoader.showImageViewByAsyncTask(holder.imageView, tag);
```



* LoadImages需要添加缓存机制的方法



```
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
```



* 使用异步加载下载图片



```
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

```



* 下载任务通过okhttp3连接

  

```
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

```



* 加载可见部分的图片，并将该任务添加到List里管理



```
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
```



------



#### 四、demo的运行结果：



![2018-05-12](https://github.com/freedomeden/AsyncTaskDemo/blob/master/2018-05-12.jpg?raw=true)

