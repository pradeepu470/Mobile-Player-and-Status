package com.pradeep.videoplayercollection;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Created by pradeep on 28-11-2018.
 */

public class SessionManager {

    private static String TAG = SessionManager.class.getSimpleName();
    private SharedPreferences pref;
    private static SessionManager mSessionManager;
    private Editor editor;
    private Context _context;
    private int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "FBoxLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_IS_SESSION_ID = "SessionId";
    private static final String KEY_IS_REGISTER = "isRegister";
    private static final String KEY_IS_USER_ID = "UserId";
    private static final String KEY_USER_IMAGE_URL = "Image_url";
    private static final String KEY_USER_NAME = "User_name";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String KEY_IS_PIN_GENERATED = "isPinGenerated";


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    public static SessionManager getInstance(Context context) {
        if(mSessionManager == null){
            mSessionManager = new SessionManager(context);
        }
        return mSessionManager;
    }

    public void setLogin(boolean isLoggedIn,  Integer sessionId) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.putInt(KEY_IS_SESSION_ID, sessionId);
        editor.commit();
        Log.d(TAG, "User login session modified!");
    }

    public void setRegister(boolean isRegister,  Integer userId,String image_url,String name) {
        editor.putBoolean(KEY_IS_REGISTER, isRegister);
        editor.putInt(KEY_IS_USER_ID, userId);
        editor.putString(KEY_USER_IMAGE_URL, image_url);
        editor.putString(KEY_USER_NAME,name);
        editor.commit();
        Log.d(TAG, "User register session modified!");
    }


    public void setPinGenerated(boolean isPinGenerated) {
        editor.putBoolean(KEY_IS_PIN_GENERATED, isPinGenerated);
        editor.commit();
        Log.d(TAG, "User pin session modified!");
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }
  
    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, false);
    }

    public boolean isRegister(){
        return pref.getBoolean(KEY_IS_REGISTER, false);
    }
  
    public boolean isPinGenerated(){
        return pref.getBoolean(KEY_IS_PIN_GENERATED, false);
    }

    public int getUserID(){
        return pref.getInt(KEY_IS_USER_ID, -1);
    }
  
    public int getSessionID(){
        return pref.getInt(KEY_IS_SESSION_ID, -1);
    }

    public String getUserImage(){
        return pref.getString(KEY_USER_IMAGE_URL,"");
    }
    public String getUserName(){
        return pref.getString(KEY_USER_NAME,"");
    }
    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

}
