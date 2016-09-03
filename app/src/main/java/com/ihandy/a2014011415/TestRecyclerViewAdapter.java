package com.ihandy.a2014011415;

import android.annotation.SuppressLint;
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
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TestRecyclerViewAdapter extends RecyclerView.Adapter<TestRecyclerViewAdapter.TestRecyclerViewHolder> {

    List<Object> contents;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;

    public TestRecyclerViewAdapter(List<Object> contents) {
        this.contents = contents;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
//            case 0:
//                return TYPE_HEADER;
            default:
                return TYPE_CELL;
        }
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    @Override
    public TestRecyclerViewAdapter.TestRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;

        switch (viewType) {
            case TYPE_HEADER: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_card_big, parent, false);
                return new TestRecyclerViewAdapter.TestRecyclerViewHolder(view) {
                };
            }
            case TYPE_CELL: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_card_small, parent, false);
                return new TestRecyclerViewAdapter.TestRecyclerViewHolder(view) {
                };
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(TestRecyclerViewAdapter.TestRecyclerViewHolder holder, int position) {
        //Log.d("TestRecyclerViewAdapter", String.valueOf(position));
//        switch (getItemViewType(position)) {
//            case TYPE_HEADER:
//                break;
//            case TYPE_CELL:
//                break;
//        }
        JSONObject news = (JSONObject)contents.get(position);
        try {
            holder.titleTextView.setText(news.getString("title"));
            JSONObject source = news.getJSONObject("source");
            holder.sourceTextView.setText(source.getString("name"));
            JSONArray imgs = news.getJSONArray("imgs");
            JSONObject img = imgs.getJSONObject(0);
            new NormalLoadPictrue().getPicture(img.getString("url"), holder.newsImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.rootLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TestRecyclerViewAdapter", "Clicked");
            }
        });
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
