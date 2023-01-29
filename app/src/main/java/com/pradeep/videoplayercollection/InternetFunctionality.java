package com.pradeep.videoplayercollection;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;


public class InternetFunctionality extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final String mTempDbPath = "/data/data/com.pradeep.videoplayercollection/tempdb/";
    private SessionManager mSession;
    private ProgressBar mTaskInProgress;
    private MediaRecorder recorder;
    private File audiofile = null;
    private Context mContext;
    private String mPhoneNumber;

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

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTaskInProgress = (ProgressBar) findViewById(R.id.main_progress_bar);
        listenerAllRequest();
        mContext = this;

    }

    private void listenerAllRequest() {
        CardView image = (CardView) findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....image");

            }
        });
        CardView video = (CardView) findViewById(R.id.video);
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....video");

            }
        });
        CardView audio = (CardView) findViewById(R.id.audio);
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....audio");
            }
        });

        CardView otherFile = (CardView) findViewById(R.id.other_file);
        otherFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....other_file");
            }
        });
        CardView speechListener = (CardView) findViewById(R.id.speech_listener);
        speechListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....speech_listener");
            }
        });
        CardView textToSpeech = (CardView) findViewById(R.id.text_to_speech);
        textToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....speechListener");
            }
        });

        CardView recorder = (CardView) findViewById(R.id.recorder);
        recorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....recorder");
            }
        });
        CardView phoneDetail = (CardView) findViewById(R.id.phone_detail);
        phoneDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Profile....phoneDetail");
            }
        });
        CardView sms = (CardView) findViewById(R.id.sms);
        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

}
