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

public class WebViewActivity extends AppCompatActivity {

    private WebView newsWebView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_webview);

        mToolbar = (Toolbar)findViewById(R.id.toolbar_news_webview);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(R.drawable.backward_arrow);
        setSupportActionBar(mToolbar);

        Intent intent = getIntent();
        String url = intent.getStringExtra("news url");
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_collection) {
            if (item.getTitle().equals("collection_0")) {
                item.setTitle("collection_1");
                item.setIcon(R.drawable.favorite);
            } else {
                item.setTitle("collection_0");
                item.setIcon(R.drawable.non_favorite);
            }
        } else if (id == R.id.action_share) {

        }
        return super.onOptionsItemSelected(item);
    }
}
