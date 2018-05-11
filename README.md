# 网络图片的异步加载实现



#### 一、网络请求

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



------



#### 四、demo的运行结果：



![2018-05-12](https://github.com/freedomeden/AsyncTaskDemo/blob/master/2018-05-12.jpg?raw=true)

