package com.ihandy.a2014011415;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TestRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Object> contents;

    static final int TYPE_FOOTER = 0;
    static final int TYPE_CELL = 1;

    private boolean mShowFooter = true;

    public TestRecyclerViewAdapter(List<Object> contents) {
        this.contents = contents;
    }

    public void UpdateList(List<Object> contents) {
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
        //Log.d("TestRecyclerViewAdapter", String.valueOf(position));
//        switch (getItemViewType(position)) {
//            case TYPE_HEADER:
//                break;
//            case TYPE_CELL:
//                break;
//        }
        if (getItemViewType(position) == TYPE_FOOTER) {
            return;
        }
        TestRecyclerViewHolder testRecyclerViewHolder = (TestRecyclerViewHolder) holder;
        JSONObject news = (JSONObject)contents.get(position);
        JSONObject source = null;
        try {
            testRecyclerViewHolder.titleTextView.setText(news.getString("title"));
            source = news.getJSONObject("source");
            testRecyclerViewHolder.sourceTextView.setText(source.getString("name"));
            JSONArray imgs = news.getJSONArray("imgs");
            JSONObject img = imgs.getJSONObject(0);
            new NormalLoadPictrue().getPicture(img.getString("url"), testRecyclerViewHolder.newsImageView);
        } catch (Exception e) {
            e.printStackTrace();
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
//                        Uri uri = Uri.parse(newsURL);
//                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
//                        MainActivity.getContext().startActivity(launchBrowser);
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

    public class NormalLoadPictrue {

        private String uri;
        private ImageView imageView;
        private byte[] picByte;


        public void getPicture(String uri,ImageView imageView){
            this.uri = uri;
            this.imageView = imageView;
            new Thread(runnable).start();
        }

        @SuppressLint("HandlerLeak")
        Handler handle = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    if (picByte != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(picByte, 0, picByte.length);
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(uri);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setReadTimeout(10000);

                    if (conn.getResponseCode() == 200) {
                        InputStream fis =  conn.getInputStream();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] bytes = new byte[1024];
                        int length = -1;
                        while ((length = fis.read(bytes)) != -1) {
                            bos.write(bytes, 0, length);
                        }
                        picByte = bos.toByteArray();
                        bos.close();
                        fis.close();

                        Message message = new Message();
                        message.what = 1;
                        handle.sendMessage(message);
                    }

                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

    }
}
