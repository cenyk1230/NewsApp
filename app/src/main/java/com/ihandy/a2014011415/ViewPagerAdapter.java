package com.ihandy.a2014011415;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<RecyclerViewFragment> mList;
    private Context mContext;

    public ViewPagerAdapter(FragmentManager fm, Context context, ArrayList<RecyclerViewFragment> list) {
        super(fm);
        this.mContext = context;
        this.mList = list;
    }

    public void updateList(ArrayList<RecyclerViewFragment> list) {
        this.mList.clear();
        this.mList = list;

        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return MainActivity.getCategoryMap().get(mList.get(position).getCategory());
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}
