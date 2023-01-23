package com.pradeep.videoplayercollection;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.ImageView;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class AppDataDisplayAdaptor extends RecyclerView.Adapter<AppDataDisplayAdaptor.ViewHolder> {
    private Context mContext;
    private List<Object> stringList;
    private CustomItemClickListener mListener;
    private ArrayMap<String,Object> listSize = new ArrayMap<>();
    private final int FETCH_PACKAGE_SIZE_COMPLETED = 1001;

    public AppDataDisplayAdaptor(Context context, List<Object> list, CustomItemClickListener listener) {
        mContext = context;
        stringList = list;
        mListener = listener;
        for (int i = 0; i<list.size(); i++) {
            getpackageSize(mContext.getPackageManager(),(String)stringList.get(i));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView imageView;
        public TextView textView_App_Name;
        public TextView textView_App_Package_Name;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.card_view);
            imageView = (ImageView) view.findViewById(R.id.image_view);
            textView_App_Name = (TextView) view.findViewById(R.id.app_name);
            textView_App_Package_Name = (TextView) view.findViewById(R.id.app_package_Name);
        }
    }

    @Override
    public AppDataDisplayAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cardview_layout, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final String ApplicationPackageName = (String) stringList.get(position);
        String ApplicationLabelName = "";
        ApplicationInfo applicationInfo;
        Drawable drawable;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(ApplicationPackageName, 0);
            if (applicationInfo != null) {
                ApplicationLabelName = (String) packageManager.getApplicationLabel(applicationInfo);
            }
            drawable = packageManager.getApplicationIcon(ApplicationPackageName);
            viewHolder.textView_App_Name.setText(ApplicationLabelName);
            int index = listSize.indexOfKey(ApplicationPackageName);
            sizeInfo data = (sizeInfo) listSize.valueAt(index);
            double size = data.packageSize;
            String packagesize = "";
            if(size >1000000) {
                size = Math.round(size/1000000);
                packagesize += size +"MB";
            } else if(size > 1000) {
                size = Math.round(size/1000);
                packagesize += size +"KB";
            } else {
                size = Math.round(size);
                packagesize += size +"B";
            }
            viewHolder.textView_App_Package_Name.setText(packagesize);
            viewHolder.imageView.setImageDrawable(drawable);
            viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemClick(view, viewHolder.getAdapterPosition(), stringList);
                }
            });
            viewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mListener.onItemLongClick(v, viewHolder.getAdapterPosition(), stringList);
                    return true;
                }
            });
        } catch (Exception e){
            viewHolder.textView_App_Package_Name.setText("" + 0 + "MB");
        }
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }
    private String formateFileSize(long size) {
        return Formatter.formatFileSize(mContext, size);
    }

    public void getpackageSize(PackageManager pm,String packageName) {
            Method getPackageSizeInfo;
            try {
                getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class,IPackageStatsObserver.class);
                getPackageSizeInfo.invoke(pm,packageName, new cachePackState()); //Call the inner class
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
    }

    private Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FETCH_PACKAGE_SIZE_COMPLETED:
                    notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    /*
     * Inner class which will get the data size for application
     * */
    private class cachePackState extends IPackageStatsObserver.Stub {
        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            long packageSize = pStats.dataSize+pStats.externalDataSize + pStats.cacheSize+pStats.externalCacheSize+pStats.codeSize+pStats.externalCodeSize;
            listSize.put(pStats.packageName,new sizeInfo(pStats.cacheSize+pStats.externalCacheSize,
                    pStats.dataSize+pStats.externalDataSize,packageSize,pStats.codeSize+pStats.externalCodeSize));
            Log.e("package"+pStats.packageName," datasize:"+pStats.dataSize+" cacheSize:"+pStats.cacheSize
                    +" codesize:"+pStats.codeSize+" externalCodeSize:"+pStats.externalCodeSize+" externalDataSize:"
                    +pStats.externalDataSize+" externalMediaSize:"+pStats.externalMediaSize+" externalCacheSize:"
                    +pStats.externalCacheSize+" externalObbSize:"+pStats.externalObbSize);
            handle.sendEmptyMessage(FETCH_PACKAGE_SIZE_COMPLETED);
        }
    }
    private class sizeInfo {
        long cacheSize;
        long dataSize;
        long packageSize;
        long apkSize;
        sizeInfo(long cacheSize,long dataSize,long packageSize,long apkSize) {
            this.cacheSize = cacheSize;
            this.dataSize = dataSize;
            this.packageSize = packageSize;
            this.apkSize = apkSize;
        }
    }
}