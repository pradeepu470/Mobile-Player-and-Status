package com.pradeep.videoplayercollection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> listImage;
    LayoutInflater inflter;
    public CustomAdapter(Context applicationContext, ArrayList<String> listImage) {
        this.context = applicationContext;
        this.listImage = listImage;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return listImage.size();
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
        view = inflter.inflate(R.layout.image_item, null); // inflate the layout
        ImageView icon = (ImageView) view.findViewById(R.id.thumbnail); // get the reference of ImageView
        String filePath = ""+listImage.get(i);
        Glide.with(context).load(filePath).placeholder(R.drawable.maxresdefault).error(R.drawable.no_thumbnail).into(icon);
        return view;
    }
}
