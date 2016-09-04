package com.ihandy.a2014011415;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class CategoryManagementActivity extends AppCompatActivity {
    private ArrayList<HashMap<String, Object>> mData;
    private ArrayList<String> watchedStringList;
    private ArrayList<String> unwatchedStringList;
    private ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);


        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_category_management);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(R.drawable.backward_arrow);
        setSupportActionBar(mToolbar);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setEnabled(false);

        watchedStringList = new ArrayList<>();
        unwatchedStringList = new ArrayList<>();
        for (int i = 0; i < MainActivity.getWatchedStringList().size(); ++i) {
            watchedStringList.add(MainActivity.getWatchedStringList().get(i));
        }
        for (int i = 0; i < MainActivity.getUnwatchedStringList().size(); ++i) {
            unwatchedStringList.add(MainActivity.getUnwatchedStringList().get(i));
        }

        mData = getData();

        adapter = new ListViewAdapter(this);
        listView.setAdapter(adapter);
    }

    private ArrayList<HashMap<String, Object>> getData() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> map;
        map = new HashMap<>();
        map.put("ItemTitle", "Watched");
        list.add(map);

        for (int i = 0; i < watchedStringList.size(); ++i) {
            map = new HashMap<>();
            map.put("SmallItemTitle", watchedStringList.get(i));
            map.put("Orientation", "Down");
            list.add(map);
        }

        map = new HashMap<>();
        map.put("ItemTitle", "UnWatched");
        list.add(map);

        for (int i = 0; i < unwatchedStringList.size(); ++i) {
            map = new HashMap<>();
            map.put("SmallItemTitle", unwatchedStringList.get(i));
            map.put("Orientation", "Up");
            list.add(map);
        }

        System.out.println(list);
        return list;
    }

    public class ViewHolder {
        public ImageView imageView;
        public TextView itemTitle;
        public TextView smallItemTitle;
    }

    public class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public ListViewAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                holder = new ViewHolder();
                view = mInflater.inflate(R.layout.list_item_menu, null);
                holder.imageView = (ImageView) view.findViewById(R.id.ArrowImageView);
                holder.itemTitle = (TextView) view.findViewById(R.id.ItemTitle);
                holder.smallItemTitle = (TextView) view.findViewById(R.id.SmallItemTitle);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            ItemListener itemListener = new ItemListener(i);
            if (mData.get(i).containsKey("ItemTitle")) {
                holder.itemTitle.setVisibility(View.VISIBLE);
                holder.smallItemTitle.setVisibility(View.INVISIBLE);
                holder.imageView.setVisibility(View.INVISIBLE);
                holder.itemTitle.setText((String)mData.get(i).get("ItemTitle"));
            } else {
                holder.itemTitle.setVisibility(View.INVISIBLE);
                holder.smallItemTitle.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.smallItemTitle.setText((String)mData.get(i).get("SmallItemTitle"));
                if (((String)(mData.get(i).get("Orientation"))).equals("Down")) {
                    holder.imageView.setImageResource(R.drawable.down_arrow);
                } else {
                    holder.imageView.setImageResource(R.drawable.up_arrow);
                }
                holder.imageView.setOnClickListener(itemListener);
            }
            return view;
        }
    }

    class ItemListener implements View.OnClickListener {
        private int mPosition;
        ItemListener(int position) {
            mPosition = position;
        }
        @Override
        public void onClick(View view) {
            int size = watchedStringList.size();
            //Log.v("MyListView-click", "line: " + mPosition + " size: " + size);
            if (mPosition <= size) {
                unwatchedStringList.add(watchedStringList.get(mPosition - 1));
                watchedStringList.remove(mPosition - 1);
            } else {
                watchedStringList.add(unwatchedStringList.get(mPosition - size - 2));
                unwatchedStringList.remove(mPosition - size - 2);
            }
            mData = getData();
            adapter.notifyDataSetChanged();
        }
    }
}
