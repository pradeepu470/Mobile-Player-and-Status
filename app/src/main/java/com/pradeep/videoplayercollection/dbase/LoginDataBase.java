package com.pradeep.videoplayercollection.dbase;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcel;
import android.util.Log;
import java.io.File;

public class LoginDataBase {
    private static final String TAG = "LoginDataBase";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LoginDataBase.db";
    private static final String TABLE_NAME = "user_table";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + "Userid INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "FullName TEXT, PhoneNumber TEXT, Email TEXT, Password TEXT)";
    private static final String DATA_BASE_LOCATION = "/data/data/com.pradeep.videoplayercollection/databases/LoginDataBase.db";

    LoginDataBase(Context context) {
        SQLiteDatabase db;
        File mDataBase = new File(DATA_BASE_LOCATION);
        if (!mDataBase.exists()) {
            db = new myDbHelper(context, DATABASE_NAME).getWritableDatabase();
            db.close();
        }
    }

    class myDbHelper extends SQLiteOpenHelper {
        private String database;

        public myDbHelper(Context context, String database) {
            super(context, database, null, DATABASE_VERSION);
            this.database = database;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                if (database.equals(DATABASE_NAME)) {
                    db.execSQL(CREATE_TABLE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        protected void finalize() throws Throwable {
            this.close();
            super.finalize();
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(TAG, "upgrade db");
        }
    }

    public int fetchUserDataBase(int parameter, Parcel info) {
        int status = 0;
        if (parameter == 1) {  // register...
            status = register_new_user(info);
        } else if (parameter == 2) { // login
            status = verify_user(info);
        } else {
            status = -1;
        }
        return status;
    }

    private int verify_user(Parcel info) {
        int status = 0;
        info.setDataPosition(0);
        SQLiteDatabase mydatabase = null;
        Cursor insertUserData = null;
        String id = info.readString();
        String password = info.readString();
        String mInsertQuery = "SELECT Userid FROM user_table WHERE Email='" + id + "' AND Password ='" + password +
                "' OR PhoneNumber='"+id+"' AND Password ='"+password+"';";
        try {
            mydatabase = SQLiteDatabase.openDatabase(DATA_BASE_LOCATION, null, SQLiteDatabase.OPEN_READWRITE);
            insertUserData = mydatabase.rawQuery(mInsertQuery, null);
            insertUserData.moveToNext();
            int userId = insertUserData.getInt(0);
            info.setDataPosition(0);
            info.writeInt(userId);
            insertUserData.close();
            mydatabase.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (mydatabase != null) {
                mydatabase.close();
            }
            if (insertUserData != null) {
                insertUserData.close();
            }
            status = -1;
        }
        return status;
    }

    private int register_new_user(Parcel info) {
        int status = 0;
        SQLiteDatabase mydatabase = null;
        Cursor insertUserData = null;
        info.setDataPosition(0);
        String name = info.readString();
        String phoneNumber = info.readString();
        String email = info.readString();
        String password = info.readString();
        String mInsertQuery = "INSERT INTO user_table (FullName,PhoneNumber,Email,Password) Values('" +
                name + "','" + phoneNumber + "','" + email + "','" + password + "')";
        try {
            mydatabase = SQLiteDatabase.openDatabase(DATA_BASE_LOCATION, null, SQLiteDatabase.OPEN_READWRITE);
            insertUserData = mydatabase.rawQuery(mInsertQuery, null);
            insertUserData.moveToNext();
            insertUserData = null;
            insertUserData = mydatabase.rawQuery("SELECT Userid FROM user_table WHERE FullName = '" + name + "'", null);
            insertUserData.moveToNext();
            int userId = insertUserData.getInt(0);
            info.setDataPosition(0);
            info.writeInt(userId);
            insertUserData.close();
            mydatabase.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (mydatabase != null) {
                mydatabase.close();
            }
            if (insertUserData != null) {
                insertUserData.close();
            }
            status = -1;
        }
        return status;
    }


}
