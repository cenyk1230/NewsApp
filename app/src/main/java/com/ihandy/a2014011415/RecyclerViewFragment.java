package com.ihandy.a2014011415;

import android.os.AsyncTask;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RecyclerViewFragment extends Fragment {

    private long mNextId = -1;
    private boolean mLoading = false;
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

    private Long getNewsIdFromJson(JSONObject news) {
        long newsId = 0;
        try {
            newsId = news.getLong("news_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newsId;
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

        mRecyclerView.addItemDecoration(new MaterialViewPagerHeaderDecorator());

        mAdapter = new TestRecyclerViewAdapter(mContentItems);
        mAdapter.setShowFooter(false);
        mRecyclerView.setAdapter(mAdapter);

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mLoading) {
                    mLoading = true;
                    new UpdateAsyncTask().execute();
                }
            }
        };
        mRefreshLayout.setOnRefreshListener(onRefreshListener);


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == mAdapter.getItemCount())
                {
                    if (!mLoading) {
                        mLoading = true;
                        new GetOlderAsyncTask().execute();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLayoutManager.findLastCompletelyVisibleItemPosition();
            }
        });

        if (!mLoading) {
            mLoading = true;
            new InitAsyncTask().execute();
        }
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

    private JSONArray getNewsList(int flag) {
        JSONArray newsList = null;
        URL url = null;
        BufferedReader in = null;
        String text = "", inputLine;
        try {
            if (flag == 2 && mNextId != -1)
                url = new URL("http://assignment.crazz.cn/news/query?locale=en&category=" + mCategory + "&max_news_id=" + String.valueOf(mNextId));
            else
                url = new URL("http://assignment.crazz.cn/news/query?locale=en&category=" + mCategory);
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((inputLine = in.readLine()) != null) {
                text = text + inputLine + "\n";
            }
            in.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject(text);
            JSONObject data = jsonObject.getJSONObject("data");
            long tmpNextId = -1;
            if (data.has("next_id")) {
                tmpNextId = data.getLong("next_id");
            }
            if (tmpNextId != -1 && (mNextId == -1 || tmpNextId < mNextId)) {
                mNextId = tmpNextId;
            }
            newsList = data.getJSONArray("news");
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return newsList;
    }


    class InitAsyncTask extends AsyncTask<Integer, Integer, String> {
        private JSONArray newsList = null;

        @Override
        protected String doInBackground(Integer... integers) {
            newsList = getNewsList(0);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
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
            mLoading = false;
            mRefreshLayout.setRefreshing(false);
        }

        @Override
        protected void onPreExecute() {
            mRefreshLayout.setRefreshing(true);
        }
    }

    class UpdateAsyncTask extends AsyncTask<Integer, Integer, String> {
        private JSONArray tmpNewsList = null;

        @Override
        protected String doInBackground(Integer... integers) {
            tmpNewsList = getNewsList(1);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (tmpNewsList == null || tmpNewsList.length() == 0) {
                showToast("No updated news");
            } else {
                List<Object> tmpContentItems = new ArrayList<>();
                boolean flag = false;
                Set<Long> set = new HashSet<>();
                for (int i = 0; i < mContentItems.size(); ++i) {
                    set.add(getNewsIdFromJson((JSONObject)(mContentItems.get(i))));
                }
                for (int i = 0; i < tmpNewsList.length(); ++i) {
                    try {
                        JSONObject obj = tmpNewsList.getJSONObject((i));
                        Long newsId = getNewsIdFromJson(obj);
                        if (!set.contains(newsId)) {
                            flag = true;
                            tmpContentItems.add(obj);
                        }
                    } catch (JSONException e) {
                        System.out.println(e);
                        //e.printStackTrace();
                    }
                }
                if (flag) {
                    showToast("Updated news has loaded");
                    for (int i = 0; i < mContentItems.size(); ++i) {
                        tmpContentItems.add(mContentItems.get(i));
                    }
                    mContentItems = tmpContentItems;
                    saveNewsList();
                    mAdapter.updateList(mContentItems);
                } else {
                    showToast("No updated news");
                }
            }
            mLoading = false;
            mRefreshLayout.setRefreshing(false);
        }

        @Override
        protected void onPreExecute() {
            mRefreshLayout.setRefreshing(true);
        }
    }


    class GetOlderAsyncTask extends AsyncTask<Integer, Integer, String> {
        private JSONArray tmpNewsList = null;

        @Override
        protected String doInBackground(Integer... integers) {
            tmpNewsList = getNewsList(2);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mNextId == -1) {
                showToast("No older news");
            } else {
                List<Object> tmpContentItems = new ArrayList<>();
                tmpContentItems.addAll(mContentItems);
                if (tmpNewsList == null || tmpNewsList.length() == 0) {
                    showToast("No older news");
                } else {
                    showToast("Older news has loaded");
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
            mLoading = false;
            mRefreshLayout.setRefreshing(false);
        }

        @Override
        protected void onPreExecute() {
            mRefreshLayout.setRefreshing(true);
        }
    }
}



