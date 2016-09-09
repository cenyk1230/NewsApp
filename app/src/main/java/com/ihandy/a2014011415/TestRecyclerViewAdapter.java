package com.ihandy.a2014011415;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TestRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Object> contents;

    static final int TYPE_FOOTER = 0;
    static final int TYPE_CELL = 1;

    private boolean mShowFooter = true;

    public TestRecyclerViewAdapter(List<Object> contents) {
        this.contents = contents;
    }

    public void updateList(List<Object> contents) {
        this.contents.clear();
        this.contents = contents;
        notifyDataSetChanged();
    }

    public void setShowFooter(boolean showFooter) {
        this.mShowFooter = showFooter;
    }

    public boolean isShowFooter() {
        return this.mShowFooter;
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowFooter && position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_CELL;
        }
    }

    @Override
    public int getItemCount() {
        int begin = mShowFooter ? 1 : 0;
        return contents.size() + begin;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CELL) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_card_small, parent, false);
            return new TestRecyclerViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer, null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new FooterViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOOTER) {
            return;
        }
        TestRecyclerViewHolder testRecyclerViewHolder = (TestRecyclerViewHolder) holder;
        JSONObject news = (JSONObject)contents.get(position);
        JSONObject source = null;
        try {
            testRecyclerViewHolder.titleTextView.setText(news.getString("title"));
            testRecyclerViewHolder.sourceTextView.setText(news.getString("origin"));
            //testRecyclerViewHolder.sourceTextView.setText(source.getString("name"));
            JSONArray imgs = news.getJSONArray("imgs");
            JSONObject img = imgs.getJSONObject(0);
            final ImageView iv = testRecyclerViewHolder.newsImageView;
            MainActivity.getImageLoader().downloadImage(img.getString("url"), true, new AsyncImageLoader.ImageCallback() {
                @Override
                public void onImageLoaded(Bitmap bitmap, String imageUrl) {
                    if (bitmap != null)
                        iv.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            source = news.getJSONObject("source");
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        if (source != null) {
            try {
                final String newsURL = source.getString("url");
                final long newsID = news.getLong("news_id");
                final String newsString = news.toString();
                testRecyclerViewHolder.rootLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("TestRecyclerViewAdapter", "Clicked");
                        Intent intent = new Intent(MainActivity.getContext(), WebViewActivity.class);
                        intent.putExtra("news url", newsURL);
                        intent.putExtra("news id", newsID);
                        intent.putExtra("news json", newsString);
                        MainActivity.getContext().startActivity(intent);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View view) {
            super(view);
        }
    }

    public class TestRecyclerViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView sourceTextView;
        public ImageView newsImageView;
        public LinearLayout rootLinearLayout;

        public TestRecyclerViewHolder(View view) {
            super(view);
            titleTextView = (TextView)view.findViewById(R.id.titleTextView);
            sourceTextView = (TextView)view.findViewById(R.id.sourceTextView);
            newsImageView = (ImageView)view.findViewById(R.id.newsImageView);
            rootLinearLayout = (LinearLayout)view.findViewById(R.id.rootLinearLayout);
        }
    }
}
