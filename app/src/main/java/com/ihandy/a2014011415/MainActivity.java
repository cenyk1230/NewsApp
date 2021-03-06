package com.ihandy.a2014011415;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MaterialViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private NavigationView mNaviView;
    private Toolbar toolbar;

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private static Context context;
    private static AsyncImageLoader imageLoader;
    private static HashMap<Long, String> favoriteNews = new HashMap<>();
    private static ArrayList<String> newsCategories = new ArrayList<>();
    private static HashMap<String, String> categoryMap = new HashMap<>();
    private static ArrayList<String> watchedStringList = new ArrayList<>();
    private static ArrayList<String> unwatchedStringList = new ArrayList<>();

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

        imageLoader = new AsyncImageLoader(getApplicationContext());
        imageLoader.setCache2File(true);
        String cachedDir = context.getExternalFilesDir("").getAbsolutePath() + "/pictures";
        if (!new File(cachedDir).exists()) {
            new File(cachedDir).mkdirs();
        }
        imageLoader.setCachedDir(cachedDir);

        mNaviView = (NavigationView) findViewById(R.id.nav_view);
        mNaviView.setItemIconTintList(null);
        mNaviView.setNavigationItemSelectedListener(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (!loadCategoriesFromFile()) {
            loadCategoriesFromWeb();
            watchedStringList.clear();
            unwatchedStringList.clear();
            for (int i = 0; i < newsCategories.size(); ++i) {
                watchedStringList.add(newsCategories.get(i));
            }
            saveCategories();
        }

        loadFavoriteNews();

        ArrayList<RecyclerViewFragment> list = new ArrayList<>();
        for (int i = 0; i < watchedStringList.size(); ++i) {
            RecyclerViewFragment fragment = RecyclerViewFragment.newInstance();
            fragment.setCategory(watchedStringList.get(i));
            list.add(fragment);
        }

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), context, list);
        mViewPager.getViewPager().setAdapter(mViewPagerAdapter);

        mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {
                switch (page) {
                    case 0:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.cyan,
                                "http://desk.fd.zol-img.com.cn/t_s960x600c5/g5/M00/02/03/ChMkJlbKxtyIdDnTAAeeucd7V_kAALHrgEghoQAB57R951.jpg");
                    case 1:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.blue,
                                "http://desk.fd.zol-img.com.cn/t_s960x600c5/g5/M00/02/03/ChMkJlbKxtyIBse3AAYCe8hdaXMAALHrgGo8cUABgKT703.jpg");
                    case 2:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.purple,
                                "http://desk.fd.zol-img.com.cn/t_s960x600c5/g5/M00/02/03/ChMkJ1bKxtqIXy1OAAi9LK5qQwgAALHrQK3PAoACL1E420.jpg");
                    case 3:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.red,
                                "http://desk.fd.zol-img.com.cn/t_s960x600c5/g5/M00/02/03/ChMkJ1bKxr-IcOo6AAP2TZRDqxIAALHpwCb7wwAA_Zl187.jpg");
                    case 4:
                        return HeaderDesign.fromColorAndUrl(
                                Color.parseColor("#FF8C00"),
                                "http://desk.fd.zol-img.com.cn/t_s960x600c5/g5/M00/02/03/ChMkJ1bKxv6IZ2B4AAnNuJxJwggAALHtgCDixUACc3Q752.jpg");
                    case 5:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.lime,
                                "http://desk.fd.zol-img.com.cn/t_s960x600c5/g5/M00/02/03/ChMkJ1bKxuaIFf-LAAfmjVhkIBUAALHsAJ77rgAB-al019.jpg");
                    case 6:
                        return HeaderDesign.fromColorResAndUrl(
                                R.color.green,
                                "http://desk.fd.zol-img.com.cn/t_s960x600c5/g5/M00/02/03/ChMkJlbKx1KIfcrjABed5wqRc1YAALHyACZPYYAF53_331.jpg");
                }
                return null;
            }
        });

        mViewPager.getViewPager().setOffscreenPageLimit(mViewPager.getViewPager().getAdapter().getCount());
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());
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
            Intent intent = new Intent(this, FavoriteActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_category_management) {
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

    public static HashMap<Long, String> getFavoriteNews() {
        return favoriteNews;
    }

    public static HashMap<String, String> getCategoryMap() {
        return categoryMap;
    }

    public static ArrayList<String> getNewsCategories() {
        return newsCategories;
    }

    public static ArrayList<String> getWatchedStringList() {
        return watchedStringList;
    }

    public static ArrayList<String> getUnwatchedStringList() {
        return unwatchedStringList;
    }

    public static AsyncImageLoader getImageLoader() {
        return imageLoader;
    }

    public static Context getContext() {
        return context;
    }

    public static void saveCategories() {
        JSONObject subJsonObj = new JSONObject();
        for (int i = 0; i < watchedStringList.size(); ++i) {
            try {
                subJsonObj.put(watchedStringList.get(i), "1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < unwatchedStringList.size(); ++i) {
            try {
                subJsonObj.put(unwatchedStringList.get(i), "0");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONObject subJsonObj2 = new JSONObject();
        for (String key: categoryMap.keySet()) {
            try {
                subJsonObj2.put(key, categoryMap.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("favorite", subJsonObj);
            jsonObj.put("name", subJsonObj2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonData = jsonObj.toString();
        //System.out.println(jsonData);
        File file = new File(context.getExternalFilesDir(""), "Category.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte bytes[] = jsonData.getBytes();
        int len = jsonData.length();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes, 0, len);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCategoriesFromWeb() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long timeStamp = System.currentTimeMillis();
                URL url = null;
                BufferedReader in = null;
                String text = "", inputLine;
                try {
                    url = new URL("http://assignment.crazz.cn/news/en/category?timestamp=" + timeStamp);
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
                    JSONObject categories = data.getJSONObject("categories");
                    Iterator<?> it = categories.keys();
                    while (it.hasNext()) {
                        String key = it.next().toString();
                        newsCategories.add(key);
                        categoryMap.put(key, categories.getString(key));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean loadCategoriesFromFile() {
        File file = new File(context.getExternalFilesDir(""), "Category.txt");
        if (!file.exists())
            return false;
        BufferedReader in;
        String text = "", inputLine;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while ((inputLine = in.readLine()) != null) {
                text = text + inputLine + "\n";
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        newsCategories.clear();
        categoryMap.clear();
        watchedStringList.clear();
        unwatchedStringList.clear();
        try {
            JSONObject jsonObj = new JSONObject(text);
            JSONObject jsonObj1 = jsonObj.getJSONObject("favorite");
            Iterator<?> it = jsonObj1.keys();
            while (it.hasNext()) {
                String tmpString = it.next().toString();
                newsCategories.add(tmpString);
                if (jsonObj1.getString(tmpString).equals("1")) {
                    watchedStringList.add(tmpString);
                } else {
                    unwatchedStringList.add(tmpString);
                }
            }
            JSONObject jsonObj2 = jsonObj.getJSONObject("name");
            it = jsonObj2.keys();
            while (it.hasNext()) {
                String tmpString = it.next().toString();
                categoryMap.put(tmpString, jsonObj2.getString(tmpString));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //System.out.println(newsCategories.size());
        return newsCategories.size() > 0;
    }

    private void loadFavoriteNews() {
        SQLiteDao sqLiteDao = new SQLiteDao();
        List<SQLiteDao.News> newsList = sqLiteDao.findAllInCollection();
        //System.out.println(newsList);
        for (int i = 0; i < newsList.size(); ++i) {
            favoriteNews.put(Long.valueOf(newsList.get(i).newsId), newsList.get(i).jsonData);
        }
    }
}
