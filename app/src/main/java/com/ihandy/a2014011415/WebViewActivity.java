package com.ihandy.a2014011415;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class WebViewActivity extends AppCompatActivity {

    private WebView newsWebView;
    private Toolbar mToolbar;
    private MenuItem mFavoriteItem;
    private HashMap<Long, String> mFavoriteNews;
    private long newsID;
    private String newsString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_webview);

        mToolbar = (Toolbar)findViewById(R.id.toolbar_news_webview);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(R.drawable.backward_arrow);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFavoriteNews = MainActivity.getFavoriteNews();

        Intent intent = getIntent();
        String url = intent.getStringExtra("news url");
        newsID = intent.getLongExtra("news id", -1L);
        newsString = intent.getStringExtra("news json");

        newsWebView = (WebView)findViewById(R.id.newsWebView);

        //System.out.println(url);
        newsWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        newsWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                WebViewActivity.this.setProgress(progress * 100);
            }
        });
        WebSettings webSettings = newsWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);

        newsWebView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_webview, menu);

        mFavoriteItem = menu.findItem(R.id.action_collection);

        if (mFavoriteNews.containsKey(newsID)) {
            mFavoriteItem.setIcon(R.drawable.favorite);
        } else {
            mFavoriteItem.setIcon(R.drawable.non_favorite);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_collection) {
            if (!mFavoriteNews.containsKey(newsID)) {
                setCollection("1");
                item.setIcon(R.drawable.favorite);
                MainActivity.getFavoriteNews().put(newsID, newsString);
            } else {
                setCollection("0");
                item.setIcon(R.drawable.non_favorite);
                MainActivity.getFavoriteNews().remove(newsID);
            }
            //System.out.println(MainActivity.getFavoriteNews());
        } else if (id == R.id.action_share) {
            shareMsg();
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareMsg() {
        Intent intent=new Intent(Intent.ACTION_SEND);
        JSONObject news = null;
        String picUrl = null;
        String title = "This is a good news.";
        JSONObject source = null;
        String newsUrl = null;
        try {
            news = new JSONObject(newsString);
            title = news.getString("title");
            JSONArray imgs = news.getJSONArray("imgs");
            JSONObject img = imgs.getJSONObject(0);
            picUrl = img.getString("url");
            source = news.getJSONObject("source");
            if (source != null)
                newsUrl = source.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        if (picUrl == null) {
//            intent.setType("text/plain");
//        } else {
//            String fileName = LoaderImpl.getMD5Str(picUrl);
//            File picFile = new File(this.getCacheDir().getAbsolutePath() + "/" + fileName);
//            if (picFile != null && picFile.exists() && picFile.isFile()) {
//                intent.setType("image/*");
//                Uri u = Uri.fromFile(picFile);
//                intent.putExtra(Intent.EXTRA_STREAM, u);
//            } else {
//                intent.setType("text/plain");
//            }
//        }
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
        String text = "Title: " + title + "\n";
        if (newsUrl != null) {
            text += "Url: " + newsUrl + "\n";
        }
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "Share to"));
    }

    private void setCollection(String collection) {
        SQLiteDao sqLiteDao = new SQLiteDao();
        sqLiteDao.update(String.valueOf(newsID), collection);
    }
}
