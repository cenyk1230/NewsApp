package com.ihandy.a2014011415;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
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
    private ViewPagerAdapter mViewPagerAdapter;
    private NavigationView mNaviView;
    private Toolbar toolbar;

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private static Context context;
    private static ArrayList<String> newsCategories = new ArrayList<>();
    private static ArrayList<String> watchedStringList = new ArrayList<>();
    private static ArrayList<String> unwatchedStringList = new ArrayList<>();

    private Thread mThread;

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

    public static ArrayList<String> getNewsCategories() {
        return newsCategories;
    }

    public static ArrayList<String> getWatchedStringList() {
        return watchedStringList;
    }

    public static ArrayList<String> getUnwatchedStringList() {
        return unwatchedStringList;
    }

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

        watchedStringList.clear();
        unwatchedStringList.clear();
        for (int i = 0; i < newsCategories.size(); ++i) {
            watchedStringList.add(newsCategories.get(i));
        }

        ArrayList<RecyclerViewFragment> list = new ArrayList<>();
        for (int i = 0; i < newsCategories.size(); ++i) {
            RecyclerViewFragment fragment = RecyclerViewFragment.newInstance();
            fragment.setCategory(newsCategories.get(i));
            list.add(fragment);
        }

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), context, list);
        mViewPager.getViewPager().setAdapter(mViewPagerAdapter);

        mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {
                //System.out.println(page);
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
        //System.out.println(id);
        if (id == R.id.nav_favorites) {
            // Handle the action
        } else if (id == R.id.nav_category_management) {
//            ArrayList<RecyclerViewFragment> list = new ArrayList<>();
//            for (int i = 2; i < newsCategories.size(); ++i) {
//                RecyclerViewFragment fragment = RecyclerViewFragment.newInstance();
//                fragment.setCategory(newsCategories.get(i));
//                list.add(fragment);
//            }
//            mViewPagerAdapter.updateList(list);
            Intent intent = new Intent(this, CategoryManagementActivity.class);
            startActivityForResult(intent, 0);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //System.out.println(unwatchedStringList.size());
        //System.out.println(resultCode + " : " + RESULT_OK);
        ArrayList<RecyclerViewFragment> list = new ArrayList<>();
        for (int i = 0; i < newsCategories.size(); ++i) {
            Boolean watched = true;
            for (int j = 0; j < unwatchedStringList.size(); ++j) {
                if (newsCategories.get(i).equals(unwatchedStringList.get(j))) {
                    watched = false;
                    break;
                }
            }
            if (!watched) {
                continue;
            }
            RecyclerViewFragment fragment = RecyclerViewFragment.newInstance();
            fragment.setCategory(newsCategories.get(i));
            list.add(fragment);
        }
        mViewPagerAdapter.updateList(list);
    }
}
