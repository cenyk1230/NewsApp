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

import java.util.ArrayList;
import java.util.List;


public class RecyclerViewFragment extends Fragment {

    static final boolean GRID_LAYOUT = false;
    //private static final int ITEM_COUNT = 10;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<Object> mContentItems = new ArrayList<>();

    public static RecyclerViewFragment newInstance() {
        return new RecyclerViewFragment();
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

        MainActivity activity = (MainActivity)MainActivity.getContext();
        JSONArray newsList = activity.getNewsList();
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
}
