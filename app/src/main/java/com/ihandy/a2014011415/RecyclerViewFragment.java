package com.ihandy.a2014011415;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class RecyclerViewFragment extends Fragment {

    //private static final int ITEM_COUNT = 10;
    private long mNextId = -1;
    private String mCategory;
    private Toast mToast;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private TestRecyclerViewAdapter mAdapter;
    private List<Object> mContentItems = new ArrayList<>();

    public static RecyclerViewFragment newInstance() {
        return new RecyclerViewFragment();
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public String getCategory() {
        return mCategory;
    }

    private void showToast(String text) {
        if (mToast != null) {
            mToast.setText(text);
        } else {
            mToast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        //Use this now
        mRecyclerView.addItemDecoration(new MaterialViewPagerHeaderDecorator());

        mAdapter = new TestRecyclerViewAdapter(mContentItems);
        mAdapter.setShowFooter(false);
        mRecyclerView.setAdapter(mAdapter);

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Toast.makeText(getActivity().getApplicationContext(), "No Updated News", Toast.LENGTH_SHORT).show();
                showToast("No updated news");
                mRefreshLayout.setRefreshing(false);
            }
        });


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //System.out.println("" + lastVisibleItem + "  " + mAdapter.getItemCount());
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == mAdapter.getItemCount())
                {
                    //System.out.println("onScrollStateChanged");
                    //Toast.makeText(getActivity().getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
                    if (mNextId == -1) {
                        //mAdapter.setShowFooter(false);
                        showToast("No older news");
                        //Toast.makeText(getActivity().getApplicationContext(), "No Older News", Toast.LENGTH_SHORT).show();
                    } else {
                        List<Object> tmpContentItems = new ArrayList<>();
                        tmpContentItems.addAll(mContentItems);
                        JSONArray tmpNewsList = getNewsList(mNextId);
                        //System.out.println(tmpNewsList);
                        if (tmpNewsList == null || tmpNewsList.length() == 0) {
                            showToast("No older news");
                            //Toast.makeText(getActivity().getApplicationContext(), "No Older News", Toast.LENGTH_SHORT).show();
                        } else {
                            showToast("Older news has loaded");
                            //Toast.makeText(getActivity().getApplicationContext(), "Older News Has Loaded", Toast.LENGTH_SHORT).show();
                            for (int i = 0; i < tmpNewsList.length(); ++i) {
                                try {
                                    tmpContentItems.add(tmpNewsList.getJSONObject(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            mContentItems = tmpContentItems;
                            saveNewsList();
                            mAdapter.updateList(mContentItems);
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }
        });


        JSONArray newsList = getNewsList();

        if (newsList == null || newsList.length() == 0) {
            newsList = loadNewsListFromDB();
        }

        if (newsList != null) {
            for (int i = 0; i < newsList.length(); ++i) {
                try {
                    mContentItems.add(newsList.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            saveNewsList();
            mAdapter.notifyDataSetChanged();
        }
    }

    public JSONArray getNewsList() {
        ContentThread mContentThread = new ContentThread();
        mContentThread.setCategory(mCategory);
        mContentThread.start();
        mContentThread.join();
        return mContentThread.getNewsList();
    }

    public JSONArray getNewsList(long next_id) {
        ContentThread mContentThread = new ContentThread(next_id);
        mContentThread.setCategory(mCategory);
        mContentThread.start();
        mContentThread.join();
        return mContentThread.getNewsList();
    }

    public void saveNewsList() {
        SQLiteDao sqLiteDao = new SQLiteDao();
        for (int i = 0; i < mContentItems.size(); ++i) {
            try {
                long newsId = ((JSONObject)(mContentItems.get(i))).getLong("news_id");
                String collection = "0";
                if (MainActivity.getFavoriteNews().containsKey(newsId)) {
                    collection = "1";
                }
                sqLiteDao.add(String.valueOf(newsId), mCategory, collection, ((JSONObject)(mContentItems.get(i))).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        List<SQLiteDao.News> newsList = sqLiteDao.findAllInCategory(mCategory);
//        System.out.println(newsList);
    }

    public JSONArray loadNewsListFromDB() {
        SQLiteDao sqLiteDao = new SQLiteDao();
        List<SQLiteDao.News> newsList = sqLiteDao.findAllInCategory(mCategory);
        JSONArray retList = new JSONArray();
        for (int i = 0; i < newsList.size(); ++i) {
            try {
                retList.put(new JSONObject(newsList.get(i).jsonData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return retList;
    }

    class ContentThread implements Runnable {
        private String category;
        private Thread thread;
        private JSONArray newsList;
        private long next_id = -1;

        public ContentThread() {
            thread = new Thread(this);
        }
        public ContentThread(long next_id) {
            this.next_id = next_id;
            thread = new Thread(this);
        }
        public void setCategory(String category) {
            this.category = category;
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
                if (next_id == -1)
                    url = new URL("http://assignment.crazz.cn/news/query?locale=en&category=" + category);
                else
                    url = new URL("http://assignment.crazz.cn/news/query?locale=en&category=" + category + "&max_news_id=" + String.valueOf(next_id));
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
                if (data.has("next_id")) {
                    mNextId = data.getLong("next_id");
                } else {
                    mNextId = -1;
                }
                newsList = data.getJSONArray("news");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
