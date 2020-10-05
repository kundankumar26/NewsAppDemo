package com.example.newsapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> newsTitleList = new ArrayList<>();
    ArrayList<String> newsUrlList = new ArrayList<>();
    ArrayList<NewsObject> newsList = new ArrayList<>();
    NewsAdapter newsAdapter;
    SQLiteDatabase sqlDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        //CREATING DATABASE
        sqlDatabase = this.openOrCreateDatabase("news", MODE_PRIVATE, null);

        //CREATING SCHEMA FOR ARTICLES TABLE
        sqlDatabase.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleid INTEGER, title VARCHAR, url VARCHAR)");

        //BACKGROUND TASK FOR GETTING ARTICLES INFO
        DownloadTask task = new DownloadTask();
        try {
            String apilink = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
            task.execute(apilink);
            updateDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //CREATING AND SETTING LISTVIEW
        ListView listView = findViewById(R.id.news_listview);
        newsAdapter = new NewsAdapter(this, newsList);
        listView.setAdapter(newsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                intent.putExtra("url", newsList.get(position).getUrl());
                startActivity(intent);
            }
        });

    }

    private void updateDatabase() {
        Cursor c = sqlDatabase.rawQuery("SELECT * FROM articles", null);
        int articleIdIndex = c.getColumnIndex("articleid");
        int articleTitleIndex = c.getColumnIndex("title");
        int articleUrlIndex = c.getColumnIndex("url");

        if(c.moveToFirst()){
            //String num = c.getString(c.getColumnIndex("ContactNumber"));
            newsTitleList.clear();
            newsUrlList.clear();
            newsList.clear();
            do {
                newsList.add(new NewsObject(c.getString(articleTitleIndex), c.getString(articleUrlIndex)));
                newsTitleList.add(c.getString(articleTitleIndex));
                newsUrlList.add(c.getString(articleUrlIndex));
            }
            while(c.moveToNext());
            //arrayAdapter.notifyDataSetChanged();
        }
        c.close();
    }


    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            try {
                //GET THE JSON STRING FROM FIRST API
                String jsonString = getJsonString(urls[0]);

                //CHECK IF THE RETURNED STRING IS NULL
                if(jsonString != null){
                    JSONArray jsonArray = new JSONArray(jsonString);
//                    int limitArticles = 20;
//                    if(jsonArray.length() < limitArticles){
//                        limitArticles = jsonArray.length();
//                    }
                    sqlDatabase.execSQL("DELETE FROM articles");

                    //CREATING THE TOP ARTICLES AND STORING IN DATABASE
                    for(int i = 0; i < jsonArray.length(); i++){

                        //GET THE JSON OBJECT STRING OF EACH ARTICLE
                        String articleNumber = jsonArray.getString(i);
                        String articlesLink = "https://hacker-news.firebaseio.com/v0/item/" + articleNumber + ".json?print=pretty";
                        try {
                            String articleContents = getJsonString(articlesLink);
                            assert articleContents != null;
                            JSONObject articleJsonObject = new JSONObject(articleContents);
                            String articleId = articleJsonObject.getString("id");
                            String articleTitle = articleJsonObject.getString("title");
                            String articleUrl = articleJsonObject.getString("url");

                            String sql = "INSERT INTO articles(articleid, title, url) VALUES(?, ?, ?)";
                            SQLiteStatement statement = sqlDatabase.compileStatement(sql);
                            statement.bindString(1, articleId);
                            statement.bindString(2, articleTitle);
                            statement.bindString(3, articleUrl);
                            statement.execute();

                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        /**
         * THIS METHOD GETS THE DATA FROM URL AND RETURN IT
         */
        private String getJsonString(String urls) throws IOException {
            URL url = null;
            HttpURLConnection httpURLConnection = null;

            //SETTING THE CONNECTION AND GETTING DATA
            try {
                url = new URL(urls);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                //STRINGBUILDER TO CREATE THE FINAL STRING
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                return sb.toString();

            } catch (MalformedURLException e) {

                e.printStackTrace();
            }

            return null;
        }
    }
}