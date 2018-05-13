package com.din.asyncdemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<News> list;
    private ImageLoader imageLoader;
    public static String[] urls;

    @NonNull
    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.ViewHolder holder, int position) {
        News news = list.get(position);
        holder.title.setText(news.getTitle());

        // 实现图片加载的三种方法，
        // 第一种使用第三方库Glide加载，使用Glide不能通过setTag(tag);需要使用setTag(id, tag);
//        Glide.with(context).load(news.getPic()).into(holder.imageView);

        String tag = news.getPic();
        holder.imageView.setTag(tag);
        // 第二种使用多线程加载
//        imageLoader.showImageByThread(holder.imageView, tag);
        // 第三种使用异步加载图片
        imageLoader.showImageViewByAsyncTask(holder.imageView, tag);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.text);
        }
    }

    public NewsAdapter(List<News> list, RecyclerView recyclerView) {

        this.list = list;
        imageLoader = new ImageLoader(recyclerView);
        urls = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            urls[i] = list.get(i).getPic();
        }
    }
}