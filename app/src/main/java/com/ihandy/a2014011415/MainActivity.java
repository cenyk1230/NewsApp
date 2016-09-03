package com.ihandy.a2014011415;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MaterialViewPager mViewPager;
    private NavigationView mNaviView;
    private Toolbar toolbar;

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private static Context context;

    private Thread mThread;
    private ContentThread mContentThread;
    private ArrayList<String> newsCategories = new ArrayList<>();

    private int DefaultCategoryCount = 10;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long timeStamp = System.currentTimeMillis();
            URL url = null;
            BufferedReader in = null;
            String text = "", inputLine;
            try {
                url = new URL("http://assignment.crazz.cn/news/en/category?timestamp=" + timeStamp);
                //Log.d("MainActivity", Long.toString(timeStamp));
                in = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((inputLine = in.readLine()) != null) {
                    text = text + inputLine + "\n";
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.d("MainActivity", text);
            try {
                JSONObject jsonObject = new JSONObject(text);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONObject categories = data.getJSONObject("categories");
                Iterator<?> it = categories.keys();
                while (it.hasNext()) {
                    newsCategories.add(it.next().toString());
                    //Log.d("MainActivity", it.next().toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < newsCategories.size(); ++i) {
                Log.d("MainActivity", newsCategories.get(i));
            }
        }
    };

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, 0, 0);
        mDrawer.setDrawerListener(mDrawerToggle);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
        }

        setTitle("");

        mViewPager = (MaterialViewPager) findViewById(R.id.materialViewPager);

        toolbar = mViewPager.getToolbar();

        context = this;

        mNaviView = (NavigationView) findViewById(R.id.nav_view);
        mNaviView.setItemIconTintList(null);
        mNaviView.setNavigationItemSelectedListener(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mThread = new Thread(runnable);
        mThread.start();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (newsCategories.size() > 0) {
            DefaultCategoryCount = newsCategories.size();
            mContentThread = new ContentThread();
            mContentThread.setCategory(0);
            mContentThread.start();
            mContentThread.join();
        }

        mViewPager.getViewPager().setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return RecyclerViewFragment.newInstance();
            }

            @Override
            public int getCount() {
                return DefaultCategoryCount;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                if (newsCategories.size() > position)
                    return newsCategories.get(position);
                return "";
            }
        });

        mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {
                mContentThread = new ContentThread();
                mContentThread.setCategory(page);
                mContentThread.start();
                mContentThread.join();

                switch (page) {
                    case 0:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.green,
                                "https://fs01.androidpit.info/a/63/0e/android-l-wallpapers-630ea6-h900.jpg");
                    case 1:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.blue,
                                "http://cdn1.tnwcdn.com/wp-content/blogs.dir/1/files/2014/06/wallpaper_51.jpg");
                    case 2:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.cyan,
                                "http://www.droid-life.com/wp-content/uploads/2014/10/lollipop-wallpapers10.jpg");
                    case 3:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.red,
                                "http://www.tothemobile.com/wp-content/uploads/2014/07/original.jpg");
                }

                //execute others actions if needed (ex : modify your header logo)

                return null;
            }
        });

        mViewPager.getViewPager().setOffscreenPageLimit(mViewPager.getViewPager().getAdapter().getCount());
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());

        View logo = findViewById(R.id.logo_white);
        if (logo != null) {
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.notifyHeaderChanged();
                    Toast.makeText(getApplicationContext(), "Yes, the title is clickable", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favorites) {
            // Handle the action
        } else if (id == R.id.nav_category_management) {

        } else if (id == R.id.nav_about_me) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) ||
                super.onOptionsItemSelected(item);
    }

    public JSONArray getNewsList() {
        return mContentThread.getNewsList();
    }


    class ContentThread implements Runnable {
        private int num = 0;
        private Thread thread;
        private JSONArray newsList;

        public ContentThread() {
            thread = new Thread(this);
        }
        public void setCategory(int num) {
            this.num = num;
        }
        public JSONArray getNewsList() {
            return newsList;
        }
        public void start() {
            thread.start();
        }
        public void join() {
            try {
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void run() {
            URL url = null;
            BufferedReader in = null;
            String text = "", inputLine;
            try {
                url = new URL("http://assignment.crazz.cn/news/query?locale=en&category=" + newsCategories.get(num));
                in = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((inputLine = in.readLine()) != null) {
                    text = text + inputLine + "\n";
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                JSONObject jsonObject = new JSONObject(text);
                JSONObject data = jsonObject.getJSONObject("data");
                newsList = data.getJSONArray("news");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
