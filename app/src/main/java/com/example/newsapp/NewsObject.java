package com.example.newsapp;

public class NewsObject {
    private String mTitle;
    private String mUrl;

    public NewsObject(String title, String url){
        mTitle = title;
        mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }
}
