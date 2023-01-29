package com.pradeep.videoplayercollection;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.Manifest.permission.SEND_SMS;

@SuppressWarnings("unchecked")
public class SmsSendActivity extends AppCompatActivity {
    private static final String TAG = "SmsSendActivity";
    private Toolbar mToolbar;
    private Activity mActivity;
    private Context mContext;
    private ArrayMap<String, ContactData> mPhoneNumberList;
    private EditText textData;
    private Button sendSmsSim1;
    private Button sendSmsSim2;
    private Button closeSms;
    private EditText mSearchView;
    private RecyclerView mRecyclerSearchView;
    private LinearLayout mSmsSendLayout;
    private LinearLayout mAllView;
    private LinearLayout mSmsSendViewLayout;
    private SearchViewAdapter mSearchViewAdapter;
    private List<Object> mContactList;
    private String mPhoneNumber;
    private ProgressBar mTaskInProgress;
    private smsViewAdaptor smsViewAdaptor;
    private RecyclerView mSmsViewRecyclerview;
    private ArrayList<Object> mAllMessage;
    private RecyclerView mRecyclerview;
    private RelativeLayout mAllUserListLinearLayout;
    private int mSimSelect = 0;
    private String mSendUserAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_snd);
        mActivity = this;
        mContext = this;
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(mToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setVisibility(View.VISIBLE);
        textData = (EditText) findViewById(R.id.edit_sms);
        sendSmsSim1 = (Button) findViewById(R.id.btn_sim1);
        sendSmsSim2 = (Button) findViewById(R.id.btn_sim2);
        closeSms = (Button) findViewById(R.id.btn_close);
        mSearchView = (EditText) findViewById(R.id.search_edit_text);
        mSmsSendLayout = (LinearLayout) findViewById(R.id.sms_send_layout);
        mAllView = (LinearLayout) findViewById(R.id.all_view);
        mRecyclerSearchView = (RecyclerView) findViewById(R.id.recycler_search_view);
        mTaskInProgress = (ProgressBar) findViewById(R.id.main_progress_bar);

        mSmsSendViewLayout = (LinearLayout) findViewById(R.id.sms_send_list_layout);
        mSmsViewRecyclerview = (RecyclerView) findViewById(R.id.recyclerview_send_sms);
        mAllUserListLinearLayout = (RelativeLayout) findViewById(R.id.user_sms_list_layout);
        mRecyclerview = (RecyclerView) findViewById(R.id.recyclerview_user_send_sms);

        mAllMessage = new ArrayList<>();
        mContactList = new ArrayList<>();
        mPhoneNumberList = new ArrayMap<>();
        CardView send_view = (CardView) findViewById(R.id.send_sms_view);
        CardView sms_view = (CardView) findViewById(R.id.view_sms);
        CardView app_view = (CardView) findViewById(R.id.app_sms);
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                getContactList();
            }
        };
        thread.start();
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        send_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAllView.setVisibility(View.GONE);
                mSmsSendLayout.setVisibility(View.VISIBLE);
                mSmsSendViewLayout.setVisibility(View.GONE);
                smsSendDisplay();

            }
        });
        sms_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAllView.setVisibility(View.GONE);
                mSmsSendLayout.setVisibility(View.GONE);
                mSmsSendViewLayout.setVisibility(View.VISIBLE);
                getAllSms(mContext);
                displayAllSms();
            }
        });
        app_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(SmsSendActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(i);
        finish();
    }

    private void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME+" ASC");
        Log.v(TAG, "Profile....start send "+cur.getCount());
        while (cur != null && cur.moveToNext()) {
            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String thumbNel = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
            Log.e(TAG, " uri:"+thumbNel);
            if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                while (pCur.moveToNext()) {
                    String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (mPhoneNumberList.containsKey(name)) {
                        Log.e(TAG, "added  "+name + " number"+phoneNo);
                        int index = mPhoneNumberList.indexOfKey(name);
                        ContactData data = mPhoneNumberList.valueAt(index);
                        data.setUserNumerList(phoneNo);
                    } else {
                        ContactData userClass = new ContactData(name,thumbNel);
                        mPhoneNumberList.put(name, userClass);
                        userClass.setUserNumerList(phoneNo);
                        mContactList.add(userClass);
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                if (mSearchViewAdapter != null)
                                    mSearchViewAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
                pCur.close();
            }
        }
        if (cur != null) {
            cur.close();
        }

    }

    private void displayAllSms() {
        mSmsViewRecyclerview.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        RecyclerView.LayoutManager mLayoutManager2 = new LinearLayoutManager(mContext);
        mSmsViewRecyclerview.setLayoutManager(mLayoutManager2);
        mSmsViewRecyclerview.setItemAnimator(new DefaultItemAnimator());
        Log.e("tags", "" + mAllMessage.size());
        smsViewAdaptor = new smsViewAdaptor(mContext, mAllMessage, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position, final List<Object> list) {
                mAllUserListLinearLayout.setVisibility(View.VISIBLE);
                mAllView.setVisibility(View.GONE);
                mSmsSendLayout.setVisibility(View.GONE);
                mSmsSendViewLayout.setVisibility(View.GONE);
                mRecyclerview.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
                RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(mContext);
                mRecyclerview.setLayoutManager(mLayoutManager1);
                mRecyclerview.setItemAnimator(new DefaultItemAnimator());
                UserLessSmsData address = (UserLessSmsData)list.get(position);
                final ArrayList <Object> dataList = getAllSmsUser(address.getUserName());
                final UserAllSmsAdaptor data = new UserAllSmsAdaptor(mContext,dataList);
                mRecyclerview.setAdapter(data);
                final TextView smsSend = (TextView) findViewById(R.id.edit_sms_to_user);
                final Button simSelect = (Button) findViewById(R.id.button_sim_select);
                final Button sendSms = (Button) findViewById(R.id.button_sms_send);
                simSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context wrapper = new ContextThemeWrapper(SmsSendActivity.this, R.style.programClickStyle);
                        android.widget.PopupMenu popup = new android.widget.PopupMenu(wrapper, v);
                        popup.inflate(R.menu.sim_select_option);
                        popup.setGravity(Gravity.CENTER);
                        popup.setOnMenuItemClickListener(new android.widget.PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.sim_1:
                                        mSimSelect = 0;
                                        break;
                                    case R.id.sim_2:
                                        mSimSelect = 1;
                                        break;
                                }
                                return false;
                            }
                        });
                        popup.show();
                    }
                });
                sendSms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendSMS(mSendUserAddress,smsSend.getText().toString(),mSimSelect);
                        Calendar c = Calendar.getInstance();
                        long timestamp = c.getTimeInMillis();
                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(timestamp);
                        String strbody = "\t\t"+formatter.format(calendar.getTime())+"\n\n"+smsSend.getText().toString();
                        dataList.add(strbody);
                        data.notifyDataSetChanged();
                        smsSend.setText("");
                        smsSend.clearFocus();

                    }
                });

            }

            @Override
            public boolean onItemLongClick(final View v, int position, List<Object> list) {
                return true;
            }
        });
        mSmsViewRecyclerview.setAdapter(smsViewAdaptor);
    }

    public void getAllSms(Context context) {
        ArrayMap<String, UserLessSmsData> filterList = new ArrayMap<>();
        smsSendData smsSendData;
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.ADDRESS+" ASC");
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                Log.e("ddagaaa", "" + totalSMS);
                for (int j = 0; j < totalSMS; j++) {
                    String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    String address = number;
                    address.replaceAll(" ","");
                    if(address.length() == 10){
                        address = "+91"+address;
                    }
                    if (!filterList.containsKey(address)) {
                        UserLessSmsData data = new UserLessSmsData(number, body);
                        mAllMessage.add(data);
                        filterList.put(number, data);
                    }
                    c.moveToNext();
                }
            }
            c.close();
        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList <Object> getAllSmsUser(String address) {
        mSendUserAddress = address;
        Log.e("ddafaa","ddd"+address);
        ArrayList <Object> dataList = new ArrayList();
        StringBuilder smsBuilder = new StringBuilder();
        final String SMS_URI_ALL = "content://sms/";
        try {
            Uri uri = Uri.parse(SMS_URI_ALL);
            String[] projection = new String[]{ "body", "date"};
            Cursor cur = getContentResolver().query(uri, projection, "address='" + address + "'", null, "date desc");
            if (cur.moveToFirst()) {
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                do {
                    String strbody = cur.getString(index_Body);
                    long longDate = cur.getLong(index_Date);
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(longDate);
                    smsBuilder.append("\t\t"+formatter.format(calendar.getTime())+"\n\n");
                    smsBuilder.append(strbody + "");
                    smsBuilder.append("\n\n");
                    dataList.add(smsBuilder.toString());
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            } else {
                smsBuilder.append("no result!");
            }
        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
        return dataList;
    }

    private void smsSendDisplay() {
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
                } else if (data.getUserNumerList().size() > 0) {
                    mRecyclerSearchView.setVisibility(View.GONE);
                    mPhoneNumber = data.getUserNumerList().get(0);
                    mSearchView.setText("" + data.getUserName() + "(" + data.getUserNumerList().get(0) + ")");
                } else {
                    mRecyclerSearchView.setVisibility(View.GONE);
                    mSearchView.setText("error occurred");
                }
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
        sendSmsSim1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....start send");
                String sms = textData.getText().toString();
                Log.e(TAG, "Profile....sms send" + sms + mPhoneNumber);
                sendSMS(mPhoneNumber, sms, 0);
                Intent i = new Intent(SmsSendActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(i);
                finish();
            }
        });
        sendSmsSim2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....start send");
                String sms = textData.getText().toString();
                Log.e(TAG, "Profile....sms send" + sms + mPhoneNumber);
                sendSMS(mPhoneNumber, sms, 1);
                Intent i = new Intent(SmsSendActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(i);
                finish();
            }
        });
        closeSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....stop send");
                Intent i = new Intent(SmsSendActivity.this, MainActivity.class);
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

    public void sendSMS(String phoneNo, String msg, int sim) {
        Log.e(TAG, "hideProgressBar " + phoneNo);
        try {
            if (ActivityCompat.checkSelfPermission(this, SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                try {
                    final ArrayList<Integer> simCardList = new ArrayList<>();
                    SubscriptionManager subscriptionManager;
                    subscriptionManager = SubscriptionManager.from(mActivity);
                    final List<SubscriptionInfo> subscriptionInfoList = subscriptionManager
                            .getActiveSubscriptionInfoList();
                    for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                        int subscriptionId = subscriptionInfo.getSubscriptionId();
                        simCardList.add(subscriptionId);
                    }
                    int smsToSendFrom = simCardList.get(sim);
                    SmsManager.getSmsManagerForSubscriptionId(smsToSendFrom).sendTextMessage(phoneNo, null, msg, null, null);
                    Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
                } catch (Exception ErrVar) {
                    Toast.makeText(getApplicationContext(), ErrVar.getMessage().toString(), Toast.LENGTH_LONG).show();
                    ErrVar.printStackTrace();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{SEND_SMS}, 10);
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
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
        Log.e(TAG, "pradeep hide");
        if (mTaskInProgress.getVisibility() == View.VISIBLE) {
            Log.v(TAG, "pradeep gone visible");
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mTaskInProgress.setVisibility(View.GONE);
        }
    }


}