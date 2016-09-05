package com.ihandy.a2014011415;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private TestRecyclerViewAdapter mAdapter;
    private List<Object> mContentItems = new ArrayList<>();
    private HashMap<Long, String> mFavoriteNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_favorite);
        mToolbar.setTitle("Favorites");
        mToolbar.setNavigationIcon(R.drawable.backward_arrow);
        setSupportActionBar(mToolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_favorite);
        RecyclerView.LayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new MaterialViewPagerHeaderDecorator());

        mAdapter = new TestRecyclerViewAdapter(mContentItems);
        mAdapter.setShowFooter(false);
        mRecyclerView.setAdapter(mAdapter);

        mFavoriteNews = MainActivity.getFavoriteNews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mContentItems = new ArrayList<>();
        for (Object key : mFavoriteNews.keySet()) {
            String value = mFavoriteNews.get(key);
            try {
                JSONObject jsonObject = new JSONObject(value);
                mContentItems.add(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mAdapter.UpdateList(mContentItems);
    }
}
