package com.pradeep.videoplayercollection;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SEND_SMS;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RecognitionListener {
    private final String TAG = "MainActivity";
    private ProgressBar mTaskInProgress;
    private MediaRecorder recorder;
    private File audiofile = null;
    private Context mContext;
    public static final int RequestPermissionCode = 1;
    private Dialog mDisplayPopup;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private ImageView listenerVoice;
    private TextView mVoiceText;
    private Boolean mVoiceListener = false;
    private Activity mActivity;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ImageView userImage;
    private TextView userName;
    private SessionManager mSession;
    private LinearLayout mainLayout;
    private LinearLayout webViewLayout;
    private SwipeRefreshLayout swipe;
    private WebView webView;


    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy, state : ");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (mDisplayPopup != null) {
            if (speech != null) {
                speech.destroy();
                Log.d("Log", "destroy");
            }
            mDisplayPopup.dismiss();
            mDisplayPopup = null;
            return;
        }
        if (webView.canGoBack()){
            webView.goBack();
        } else {
            if(webViewLayout.getVisibility() == View.VISIBLE) {
                webViewLayout.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mActivity = this;
        mTaskInProgress = (ProgressBar) findViewById(R.id.main_progress_bar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        mSession = SessionManager.getInstance(MainActivity.this);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView =  navigationView.getHeaderView(0);
        userImage = (ImageView) hView.findViewById(R.id.user_login_image);
        userName = (TextView) hView.findViewById(R.id.user_login_name);
        mainLayout = (LinearLayout)findViewById(R.id.mainLayout);

        swipe = (SwipeRefreshLayout)findViewById(R.id.swipe);
        webView = (WebView) findViewById(R.id.webView);
        webViewLayout = (LinearLayout)findViewById(R.id.web_view_data);
        userName.setText(mSession.getUserName());
        String imageUrl = mSession.getUserImage();
        if(imageUrl!= null && imageUrl.length() > 0) {
            Glide.with(MainActivity.this).load(imageUrl).placeholder(R.drawable.maxresdefault).error(R.drawable.no_thumbnail).into(userImage);
        }
        listenerAllRequest();
        mContext = this;
        if (checkPermission()) {

        } else {
            requestPermission();
            Toast.makeText(MainActivity.this, "All Permissions Granted Successfully", Toast.LENGTH_LONG).show();
        }
    }


    public void WebAction(){
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.loadUrl("https://www.google.com/");
        swipe.setRefreshing(true);
        webView.setHorizontalFadingEdgeEnabled(true);
        webView.setVerticalFadingEdgeEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.loadUrl("file:///android_assets/error.html");
            }
            public void onPageFinished(WebView view, String url) {
                swipe.setRefreshing(false);
            }

        });

    }

    private void listenerAllRequest() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainLayout.setVisibility(View.GONE);
                webViewLayout.setVisibility(View.VISIBLE);
                swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        WebAction();
                    }
                });

                WebAction();
            }
        });
        CardView image = (CardView) findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Log.v(TAG, "Profile....image");
                    Intent i = new Intent(MainActivity.this, GalleryImageData.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }
        });
        CardView video = (CardView) findViewById(R.id.video);
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Log.v(TAG, "Profile....video");
                    Intent i = new Intent(MainActivity.this, RecordPlaybackActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }
        });
        CardView appDetails = (CardView) findViewById(R.id.app_detail);
        appDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Log.v(TAG, "Profile....app details");
                    Intent i = new Intent(MainActivity.this, AppDisplayActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }
        });
        CardView speechListener = (CardView) findViewById(R.id.speech_listener);
        speechListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Log.v(TAG, "Profile....speech_listener");
                    speechToText();
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }
        });
        CardView textToSpeech = (CardView) findViewById(R.id.text_to_speech);
        textToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Log.v(TAG, "Profile....speechListener");
                    Intent i = new Intent(MainActivity.this, TextSpeech.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }

            }
        });

        CardView recorder = (CardView) findViewById(R.id.recorder);
        recorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Log.v(TAG, "Profile....VoiceRecorder");
                    voiceRecorder();
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }
        });
        CardView phoneDetail = (CardView) findViewById(R.id.phone_detail);
        phoneDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    phoneDetails();
                    Log.v(TAG, "Profile....phone Details");
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }
        });
        CardView sms = (CardView) findViewById(R.id.sms);
        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Intent i = new Intent(MainActivity.this, SmsSendActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }

        });

        CardView audio = (CardView) findViewById(R.id.audio);
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Log.v(TAG, "Profile....audio");
                    Intent i = new Intent(MainActivity.this, AudioPlaybackActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }
        });

        CardView otherFile = (CardView) findViewById(R.id.other_file);
        otherFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Log.v(TAG, "Profile....other_file");
                    Toast.makeText(MainActivity.this, "Coming soon....", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }
        });
        CardView memoryDetails = (CardView) findViewById(R.id.memory_details);
        memoryDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Log.v(TAG, "Profile....app details");

                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }
        });

        CardView callData = (CardView) findViewById(R.id.call_data);
        callData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Intent i = new Intent(MainActivity.this, CallUserActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(i);
                    finish();
                    Log.v(TAG, "Profile....callData");
                } else {
                    Toast.makeText(MainActivity.this, "Permission not allow", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.e(TAG,"login logout");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Log.e(TAG,"login logout");

        if (id == R.id.nav_home) {
            Toast.makeText(mContext, "Coming soon....", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_guide) {

        } else if (id == R.id.logout) {
            Log.e(TAG,"login");
            mSession = new SessionManager(getApplicationContext());
            mSession.setLogin(false, 0);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            Log.e(TAG,"login done");
            return true;
        } else if (id == R.id.nav_settings) {
            // set profile.....img gmail, phone , name etc...

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     *
     */

    private void showProgressBar() {
        Log.e(TAG, "pradeep show");
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.e(TAG, "pradeep show");
                if (mTaskInProgress.getVisibility() != View.VISIBLE) {
                    MainActivity.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
                    MainActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    mTaskInProgress.setVisibility(View.GONE);
                }
            }
        });
    }

    private void voiceRecorder() {
        final Dialog voiceRecorderData = new Dialog(mContext);
        voiceRecorderData.setContentView(R.layout.voice_recorder_view);
        final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        LinearLayout otherLayout = (LinearLayout) voiceRecorderData.findViewById(R.id.recordLayout);
        mainLayout.setVisibility(View.GONE);
        otherLayout.setVisibility(View.VISIBLE);
        final Button recordStart = (Button) voiceRecorderData.findViewById(R.id.start_record);
        final Button stopStart = (Button) voiceRecorderData.findViewById(R.id.stop_record);
        recordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....start record");
                recordStart.setEnabled(false);
                stopStart.setEnabled(true);
                startRecording();
            }
        });
        stopStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....stop record");
                recordStart.setEnabled(true);
                stopStart.setEnabled(false);
                stopRecording();
                voiceRecorderData.dismiss();
                mainLayout.setVisibility(View.VISIBLE);
            }
        });
        voiceRecorderData.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        voiceRecorderData.show();
        voiceRecorderData.setCancelable(false);
    }

    public void startRecording() {
        File dir = Environment.getExternalStorageDirectory();
        try {
            audiofile = File.createTempFile("sound", ".3gp", dir);
        } catch (IOException e) {
            Log.e(TAG, "external storage access error");
            return;
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
    }

    public void stopRecording() {
        recorder.stop();
        recorder.release();
        addRecordingToMediaLibrary();
        return;
    }

    protected void addRecordingToMediaLibrary() {
        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, "audio" + audiofile.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());
        ContentResolver contentResolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
        Toast.makeText(this, "Added File " + newUri, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {
        listenerVoice.setImageDrawable(mContext.getDrawable(R.drawable.music_player));
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        listenerVoice.setEnabled(true);
        mVoiceListener = false;
        speech.stopListening();
        listenerVoice.setImageDrawable(mContext.getDrawable(R.drawable.muted_speech_listener));
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        listenerVoice.setEnabled(true);
    }

    @Override
    public void onResults(Bundle results) {

    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d("Log", "onPartialResults");
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        text = matches.get(0);
        mVoiceText.setText(text);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    public static class HomeReceiver extends BroadcastReceiver {
        public HomeReceiver() {
            super();
            Log.v("HomeReceiver", "HomeReceiver");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("HomeReceiver", "onRecive  action : " + intent.getAction());
        }
    }



    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{CAMERA, READ_CONTACTS, READ_PHONE_STATE, READ_EXTERNAL_STORAGE,
                ACCESS_COARSE_LOCATION, RECORD_AUDIO, SEND_SMS, CALL_PHONE, READ_SMS}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadContactsPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadPhoneStatePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean smsPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean locationContactsPermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean TelephoneStatePermission = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean microPhoneStatePermission = grantResults[6] == PackageManager.PERMISSION_GRANTED;
                    boolean phoneCallPermission = grantResults[7] == PackageManager.PERMISSION_GRANTED;
                    boolean readSmsPermission = grantResults[8] == PackageManager.PERMISSION_GRANTED;
                    if (CameraPermission && ReadContactsPermission && ReadPhoneStatePermission
                            && smsPermission && locationContactsPermission && TelephoneStatePermission
                            && microPhoneStatePermission && phoneCallPermission && readSmsPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }

                break;
        }
    }

    public boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int FourPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int FifthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int SixPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int SevenPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS);
        int EightPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE);
        int NinePermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_SMS);
        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FourPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FifthPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SixPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SevenPermissionResult == PackageManager.PERMISSION_GRANTED &&
                EightPermissionResult == PackageManager.PERMISSION_GRANTED&&
                NinePermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    private void phoneDetails() {
        final Runtime runtime = Runtime.getRuntime();
        final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
        final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
        String details = "\tVERSION.RELEASE : " + Build.VERSION.RELEASE
                + "\t\nVERSION.INCREMENTAL : " + Build.VERSION.INCREMENTAL
                + "\t\nVERSION.SDK.NUMBER : " + Build.VERSION.SDK_INT
                + "\t\nBOARD : " + Build.BOARD
                + "\t\nBOOTLOADER : " + Build.BOOTLOADER
                + "\t\nBRAND : " + Build.BRAND
                + "\t\nCPU_ABI : " + Build.CPU_ABI
                + "\t\nCPU_ABI2 : " + Build.CPU_ABI2
                + "\t\nDISPLAY : " + Build.DISPLAY
                + "\t\nFINGERPRINT : " + Build.FINGERPRINT
                + "\t\nHARDWARE : " + Build.HARDWARE
                + "\t\nHOST : " + Build.HOST
                + "\t\nID : " + Build.ID
                + "\t\nMANUFACTURER : " + Build.MANUFACTURER
                + "\t\nMODEL : " + Build.MODEL
                + "\t\nPRODUCT : " + Build.PRODUCT
                + "\t\nSERIAL : " + Build.SERIAL
                + "\t\nTAGS : " + Build.TAGS
                + "\t\nTIME : " + Build.TIME
                + "\t\nTYPE : " + Build.TYPE
                + "\t\nUNKNOWN : " + Build.UNKNOWN
                + "\t\nUSER : " + Build.USER
                + "\t\nAvailable Heap size : " + availHeapSizeInMB
                + "\t\nTotal Heap size : " + maxHeapSizeInMB;

        mDisplayPopup = new Dialog(MainActivity.this);
        mDisplayPopup.setContentView(R.layout.phone_status_popup);
        final TextView text_dialog = (TextView) mDisplayPopup.findViewById(R.id.text_dialog);
        text_dialog.setText(details);
        text_dialog.setMovementMethod(new ScrollingMovementMethod());
        Button cancel = (Button) mDisplayPopup.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisplayPopup.dismiss();
                mDisplayPopup = null;
            }
        });
        mDisplayPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDisplayPopup.show();
    }

    private void speechToText() {
        mDisplayPopup = new Dialog(MainActivity.this);
        mDisplayPopup.setContentView(R.layout.speech_to_text_popup);
        mVoiceText = (TextView) mDisplayPopup.findViewById(R.id.text_dialog);
        mVoiceText.setMovementMethod(new ScrollingMovementMethod());
        listenerVoice = (ImageView) mDisplayPopup.findViewById(R.id.btn_voice_listener);
        Button cancel = (Button) mDisplayPopup.findViewById(R.id.btn_cancel);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        recognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);
        listenerVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVoiceText.setText("");
                if (mVoiceListener) {
                    listenerVoice.setEnabled(true);
                    mVoiceListener = false;
                    speech.stopListening();
                    listenerVoice.setImageDrawable(mContext.getDrawable(R.drawable.muted_speech_listener));
                } else {
                    listenerVoice.setEnabled(false);
                    listenerVoice.setImageDrawable(mContext.getDrawable(R.drawable.speech_listener));
                    mVoiceListener = true;
                    speech.startListening(recognizerIntent);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speech != null) {
                    speech.destroy();
                    Log.d("Log", "destroy");
                }
                listenerVoice.setEnabled(true);
                mVoiceListener = false;
                mDisplayPopup.dismiss();
                mDisplayPopup = null;
            }
        });
        mDisplayPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDisplayPopup.show();
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

}