package com.pradeep.videoplayercollection;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by pradeep on 12-02-2018.
 */

public class RecordedAdapter extends RecyclerView.Adapter<RecordedAdapter.ViewHolder> {
    private static final String TAG = "RecordedAdapter";
    private Context mContext;
    private ArrayList<Object> mRecordedList;
    private CustomItemClickListener mListener;

    public RecordedAdapter(Context context, ArrayList<Object> recordedList, CustomItemClickListener listener) {
        mContext = context;
        mRecordedList = recordedList;
        mListener = listener;
    }

    @Override
    public RecordedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.record_list_items, parent, false);
        final ViewHolder viewHolder1 = new RecordedAdapter.ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v, viewHolder1.getAdapterPosition(), mRecordedList);
            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                mListener.onItemLongClick(v, viewHolder1.getAdapterPosition(), mRecordedList);
                return true;
            }
        });

        return viewHolder1;
    }

    @Override
    public void onBindViewHolder(RecordedAdapter.ViewHolder holder, final int position) {
        String filePath = ""+mRecordedList.get(position);
        File file = new File(filePath);
        String fileName = ""+file.getName();
        holder.fileName.setText(fileName);
        Glide.with(mContext).load(filePath).placeholder(R.drawable.maxresdefault).error(R.drawable.no_thumbnail).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return mRecordedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail;
        public TextView fileName;
        public LinearLayout linearLayout;

        public ViewHolder(View v) {
            super(v);
            thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            fileName = (TextView) v.findViewById(R.id.fileName);
            linearLayout = (LinearLayout)v.findViewById(R.id.recorded_linear_layout);
        }
    }

}



