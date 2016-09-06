package com.ihandy.a2014011415;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDao {
    private String name;
    private SQLiteDatabase db;

    public SQLiteDao() {
        name = MainActivity.getContext().getExternalFilesDir("") + "/news.db";
        db = SQLiteDatabase.openOrCreateDatabase(name, null);
        db.execSQL("create table if not exists news (id Integer primary key autoincrement, newsId varchar(20), category varchar(20), collection varchar(5), jsonData varchar(2000))");
        db.close();
    }

    public void add(String newsId, String category, String collection, String jsonData) {
        if (!find(newsId)) {
            db = SQLiteDatabase.openOrCreateDatabase(name, null);
            db.execSQL("insert into news (newsId,category,collection,jsonData) values (?,?,?,?)", new String[]{newsId, category, collection, jsonData});
            db.close();
        }
    }

    public boolean find(String newsId) {
        db = SQLiteDatabase.openOrCreateDatabase(name, null);
        Cursor cursor = db.rawQuery("select * from news where newsId=?", new String[]{newsId});
        boolean b = cursor.moveToNext();
        cursor.close();
        db.close();
        return b;
    }

    public void update(String newsId, String collection){
        db = SQLiteDatabase.openOrCreateDatabase(name, null);
        db.execSQL("update news set collection=? where newsId=?", new String[]{collection, newsId});
        db.close();
    }

    public void delete(String newsId){
        db = SQLiteDatabase.openOrCreateDatabase(name, null);
        db.execSQL("delete from news where newsId=?", new String[]{newsId});
        db.close();
    }

    public List<News> findAllInCollection() {
        db = SQLiteDatabase.openOrCreateDatabase(name, null);
        List<News> newsList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from news where collection=?", new String[]{"1"});
        while(cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String newsId = cursor.getString(cursor.getColumnIndex("newsId"));
            String category = cursor.getString(cursor.getColumnIndex("category"));
            String collection = cursor.getString(cursor.getColumnIndex("collection"));
            String jsonData = cursor.getString(cursor.getColumnIndex("jsonData"));
            News news = new News(id, newsId, category, collection, jsonData);
            newsList.add(news);
        }
        cursor.close();
        db.close();
        return newsList;
    }

    public List<News> findAllInCategory(String category){
        db = SQLiteDatabase.openOrCreateDatabase(name, null);
        List<News> newsList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from news where category=?", new String[]{category});
        while(cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String newsId = cursor.getString(cursor.getColumnIndex("newsId"));
            String collection = cursor.getString(cursor.getColumnIndex("collection"));
            String jsonData = cursor.getString(cursor.getColumnIndex("jsonData"));
            News news = new News(id, newsId, category, collection, jsonData);
            newsList.add(news);
        }
        cursor.close();
        db.close();
        return newsList;
    }

    public class News {
        public int id;
        public String newsId;
        public String category;
        public String collection;
        public String jsonData;

        public News() {
        }

        public News(int id, String newsId, String category, String collection, String jsonData) {
            this.id = id;
            this.newsId = newsId;
            this.category = category;
            this.collection = collection;
            this.jsonData = jsonData;
        }
    }

}
