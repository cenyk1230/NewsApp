package com.ihandy.a2014011415;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    static final boolean GRID_LAYOUT = false;
    //private static final int ITEM_COUNT = 10;
    private int mPosition;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<Object> mContentItems = new ArrayList<>();
    private ContentThread mContentThread;

    public static RecyclerViewFragment newInstance() {
        return new RecyclerViewFragment();
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager;

        if (GRID_LAYOUT) {
            layoutManager = new GridLayoutManager(getActivity(), 2);
        } else {
            layoutManager = new LinearLayoutManager(getActivity());
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        //Use this now
        mRecyclerView.addItemDecoration(new MaterialViewPagerHeaderDecorator());

        mAdapter = new TestRecyclerViewAdapter(mContentItems);

        //mAdapter = new RecyclerViewMaterialAdapter();
        mRecyclerView.setAdapter(mAdapter);

        JSONArray newsList = getNewsList();

        if (newsList != null) {
            for (int i = 0; i < newsList.length(); ++i) {
                try {
                    mContentItems.add(newsList.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public JSONArray getNewsList() {
        mContentThread = new ContentThread();
        mContentThread.setCategory(mPosition);
        mContentThread.start();
        mContentThread.join();
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
                url = new URL("http://assignment.crazz.cn/news/query?locale=en&category=" + MainActivity.getNewsCategories().get(num));
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
