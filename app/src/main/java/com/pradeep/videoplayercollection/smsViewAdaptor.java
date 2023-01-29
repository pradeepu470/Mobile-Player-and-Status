package com.pradeep.videoplayercollection;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by pradeep on 12-02-2018.
 */

public class smsViewAdaptor extends RecyclerView.Adapter<smsViewAdaptor.ViewHolder> {
    private static final String TAG = "RecordedAdapter";
    private Context mContext;
    private ArrayList<Object> mRecordedList;
    private CustomItemClickListener mListener;

    public smsViewAdaptor(Context context, ArrayList<Object> recordedList, CustomItemClickListener listener) {
        mContext = context;
        mRecordedList = recordedList;
        mListener = listener;
    }

    @Override
    public smsViewAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sms_send_view_list_items, parent, false);
        final ViewHolder viewHolder1 = new smsViewAdaptor.ViewHolder(view);
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
    public void onBindViewHolder(smsViewAdaptor.ViewHolder holder, final int position) {
        UserLessSmsData filePath = (UserLessSmsData) mRecordedList.get(position);
        holder.fileName.setText("" + filePath.getBody());
        holder.user_number.setText(""+filePath.getUserName());
    }

    @Override
    public int getItemCount() {
        return mRecordedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView user_number;
        public TextView fileName;
        public LinearLayout linearLayout;

        public ViewHolder(View v) {
            super(v);
            user_number = (TextView) v.findViewById(R.id.user_number);
            fileName = (TextView) v.findViewById(R.id.sms_look);
            linearLayout = (LinearLayout)v.findViewById(R.id.recorded_linear_layout);
        }
    }

}



