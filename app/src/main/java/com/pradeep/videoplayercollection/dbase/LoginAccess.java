package com.pradeep.videoplayercollection.dbase;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;

public class LoginAccess {
    String TAG = "LoginAccess";
    private static LoginAccess sInstance = null;
    private LoginDataBase mUserData = null;
    private Context mcontext = null;
    private LoginAccess(Context context){
        mcontext = context;
        Log.e(TAG,"login");
        mUserData = new LoginDataBase(context);
    }

    public static LoginAccess getInstance(Context context) {
        synchronized (LoginAccess.class) {
            if (sInstance == null) {
                sInstance = new LoginAccess(context);
            }
        }
        return sInstance;
    }

    public int fetchUserData(Parcel info, int type) {
        Log.v(TAG, "fetch User Data");
        if (mUserData == null) {
            Log.e(TAG, "Database connection is lost. Unable to fetch requested data");
            info = null;
            return -1;
        }
        return mUserData.fetchUserDataBase(type, info);
    }
}

