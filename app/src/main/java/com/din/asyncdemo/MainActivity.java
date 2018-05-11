package com.din.asyncdemo;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.din.asyncdemo.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding bind;
    private static final String URL = "http://www.imooc.com/api/teacher?type=4&num=30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 异步加载运行在UI线程里
        new NewsAsync().execute();
    }

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
}