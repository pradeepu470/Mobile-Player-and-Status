package com.pradeep.videoplayercollection;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class CallUserActivity extends AppCompatActivity {
    private static final String TAG = "SmsSendActivity";
    private Toolbar mToolbar;
    private Activity mActivity;
    private Context mContext;
    private ArrayMap<String, ContactData> mPhoneNumberList;
    private EditText textData;
    private Button calllSim1;
    private Button calllSim2;
    private Button closeCall;
    private EditText mSearchView;
    private RecyclerView mRecyclerSearchView;
    private SearchViewAdapter mSearchViewAdapter;
    private List<Object> mContactList;
    private String mPhoneNumber;
    private ProgressBar mTaskInProgress;
    private List<PhoneAccountHandle> phoneAccountHandleList;
    private final static String simSlotName[] = {
            "extra_asus_dial_use_dualsim",
            "com.android.phone.extra.slot",
            "slot",
            "simslot",
            "sim_slot",
            "subscription",
            "Subscription",
            "phone",
            "com.android.phone.DialingMode",
            "simSlot",
            "slot_id",
            "simId",
            "simnum",
            "phone_type",
            "slotId",
            "slotIdx"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_user);
        mActivity = this;
        mContext = this;
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(mToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setVisibility(View.VISIBLE);
        mPhoneNumberList = new ArrayMap<>();
        mContactList = new ArrayList<>();
        calllSim1 = (Button) findViewById(R.id.btn_sim1);
        calllSim2 = (Button) findViewById(R.id.btn_sim2);
        closeCall = (Button) findViewById(R.id.btn_close);
        mSearchView = (EditText) findViewById(R.id.search_edit_text);
        mRecyclerSearchView = (RecyclerView) findViewById(R.id.recycler_search_view);
        mTaskInProgress = (ProgressBar) findViewById(R.id.main_progress_bar);
        showProgressBar();
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                getContactList();
            }
        };
        thread.start();
    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent(CallUserActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(i);
        finish();
    }


    private void getContactList() {
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME+" ASC");
            if ((cur != null ? cur.getCount() : 0) > 0) {
                while (cur != null && cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String thumbNel = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (mPhoneNumberList.containsKey(name)) {
                            int index = mPhoneNumberList.indexOfKey(name);
                            ContactData data = mPhoneNumberList.valueAt(index);
                            data.setUserNumerList(phoneNo);
                        } else {
                            ContactData userClass = new ContactData(name,thumbNel);
                            mPhoneNumberList.put(name, userClass);
                            userClass.setUserNumerList(phoneNo);
                            mContactList.add(userClass);
                        }
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        hideProgressBar();

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                callUserDisplay();
            }
        });

    }

    private void callUserDisplay() {
        mRecyclerSearchView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerSearchView.setLayoutManager(mLayoutManager);
        mRecyclerSearchView.setItemAnimator(new DefaultItemAnimator());
        mSearchViewAdapter = new SearchViewAdapter(mContext, mContactList, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position, final List<Object> list) {
                final ContactData data = (ContactData) list.get(position);
                if (data.getUserNumerList().size() > 1) {
                    Context wrapper = new ContextThemeWrapper(mActivity, R.style.programClickStyle);
                    PopupMenu popup = new PopupMenu(wrapper, v);
                    for (int i = 0; i < data.getUserNumerList().size(); i++) {
                        popup.getMenu().add(data.getNumber(i));
                    }
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            mSearchView.setText("" + data.getUserName() + "(" + item.toString() + ")");
                            mPhoneNumber = item.toString();
                            mRecyclerSearchView.setVisibility(View.GONE);
                            return false;
                        }
                    });
                    popup.show();
                    return;
                } else if (data.getUserNumerList().size() > 0) {
                    mPhoneNumber = data.getUserNumerList().get(0);
                    mSearchView.setText("" + data.getUserName() + "(" + data.getUserNumerList().get(0) + ")");
                } else {
                    mSearchView.setText("error occurred");
                }
                mRecyclerSearchView.setVisibility(View.GONE);
            }

            @Override
            public boolean onItemLongClick(View v, int position, List<Object> list) {
                return false;
            }
        });
        mRecyclerSearchView.setAdapter(mSearchViewAdapter);
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable sshowProgressBar) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchViewAdapter.getFilter().filter(s);
            }
        });
        calllSim1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TelecomManager telecomManager = (TelecomManager) mActivity.getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
                Intent intent = new Intent(Intent.ACTION_CALL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("tel:" + mPhoneNumber));
                intent.putExtra("com.android.phone.force.slot", true);
                intent.putExtra("Cdma_Supp", true);
                for (String s : simSlotName) {
                    intent.putExtra(s, 0);
                }
                if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 0) {
                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(0));
                }
                startActivity(intent);
            }
        });
        calllSim2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TelecomManager telecomManager = (TelecomManager) mActivity.getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
                Intent intent = new Intent(Intent.ACTION_CALL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("tel:" + mPhoneNumber));
                intent.putExtra("com.android.phone.force.slot", true);
                intent.putExtra("Cdma_Supp", true);
                for (String s : simSlotName) {
                    intent.putExtra(s, 1); //0 or 1 according to sim.......
                }
                if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 0) {
                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(1));
                }
                startActivity(intent);
            }
        });
        closeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....stop send");
                Intent i = new Intent(CallUserActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(i);
                finish();
            }
        });


        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e(TAG, "onTextChanged: " + s);
                if (mRecyclerSearchView.getVisibility() != View.VISIBLE) {
                    mRecyclerSearchView.setVisibility(View.VISIBLE);
                }
            }
        });
        Log.v(TAG, "search completed");
    }


    private void showProgressBar() {
        Log.e(TAG, "pradeep show");
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.e(TAG, "pradeep show");
                if (mTaskInProgress.getVisibility() != View.VISIBLE) {
                    mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    mTaskInProgress.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void hideProgressBar() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.e(TAG, "pradeep hide");
                if (mTaskInProgress.getVisibility() == View.VISIBLE) {
                    Log.v(TAG, "pradeep gone visible");
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    mTaskInProgress.setVisibility(View.GONE);
                }
            }
        });
    }

}