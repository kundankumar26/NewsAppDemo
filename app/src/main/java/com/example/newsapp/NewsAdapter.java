package com.example.newsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class NewsAdapter extends ArrayAdapter<NewsObject> {


    public NewsAdapter(@NonNull Context context, ArrayList<NewsObject> newss) {
        super(context, 0, newss);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.news_item, parent, false);
        }

        View listview = convertView;

        NewsObject currentNewsItem = getItem(position);

        TextView newsItemTitle = listview.findViewById(R.id.news_item_textview);

        assert currentNewsItem != null;
        newsItemTitle.setText(currentNewsItem.getTitle());

        return listview;
    }
}
