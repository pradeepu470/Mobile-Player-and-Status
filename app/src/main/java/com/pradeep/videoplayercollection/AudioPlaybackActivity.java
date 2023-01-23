package com.pradeep.videoplayercollection;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AudioPlaybackActivity extends AppCompatActivity
        implements MediaPlayer.OnPreparedListener {
    String mPath;
    String mNextPath;
    private Context mContext;
    private static final String TAG = "RecordPlaybackActivity";
    private SeekBar mSeekBar;
    private TextView mStartTime;
    private TextView mEndTime;
    private Runnable mRunnable;
    private LinearLayout mSeekBarLinearLayout;
    private Object mSeekBarHideToken = new Object();
    private Handler mHandler = new Handler();
    private IntentFilter mIntentFilter;
    private RecordedPlaybackReceiver mDvbBroadcastReceiver;
    private RecyclerView recorded_content_list;
    private ArrayList<Object> mRecordedEvent;
    private int playerIndex = 0;
    private RecordedAdapter mRecordListAdapter;
    private VideoView mVideoView;
    private boolean touched = false;
    private Activity mActivity;
    private Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_audio_playback);
        mActivity = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mContext = this;
        mPath = (this.getIntent()).getStringExtra("uri");
        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBarLinearLayout = findViewById(R.id.record_seek_data);
        mStartTime = findViewById(R.id.start_time);
        mEndTime = findViewById(R.id.end_time);
        recorded_content_list = (RecyclerView) findViewById(R.id.my_recycler_view);
        Log.e(TAG, "Record file path : " + mPath);
        mRecordedEvent = new ArrayList<>();
        mIntentFilter = new IntentFilter();
        mDvbBroadcastReceiver = new RecordedPlaybackReceiver();
        mContext.registerReceiver(mDvbBroadcastReceiver, mIntentFilter);
        getAllAudioFromDevice();
        displayRecordedList();
        addPlayerListners();
        if(mRecordedEvent.size() > 0) {
            mPath = "" + mRecordedEvent.get(0);
        } else {
            mPath = "";
        }
        addClickListeners();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.i(TAG, "onPrepared");
        if (!mediaPlayer.isPlaying()) {
            initializeSeekBar();
            mediaPlayer.start();
            mHandler.removeCallbacksAndMessages(mSeekBarHideToken);
            Log.v(TAG, "MotionEvent.on prepared");
            updateSeekBarData();
        }
    }


    private void addClickListeners() {
        Log.v(TAG, "addClickListeners");
        mVideoView.setVideoPath(mPath);
        mVideoView.requestFocus();
        mVideoView.start();
        seekBarClick();
        mSeekBarLinearLayout.setOnTouchListener(screenTouch);
        recorded_content_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateSeekBarData();
            }

        });
    }

    private void seekBarClick() {
        Log.e(TAG, "seekBarClick");
        ImageButton play = (ImageButton) findViewById(R.id.btn_play_pause);
        ImageButton next = (ImageButton) findViewById(R.id.btn_next);
        ImageButton previous = (ImageButton) findViewById(R.id.btn_previous);
        final ImageButton muteButton = (ImageButton) findViewById(R.id.btn_mute);
        final ImageButton zoomButton = (ImageButton) findViewById(R.id.btn_zoom) ;
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout view = (RelativeLayout) findViewById(R.id.activity_record_playback);
                if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "play video");
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                } else {
                    mVideoView.start();
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Record file path : " + mPath);
                Log.v(TAG, "play video");
                if (playerIndex < mRecordedEvent.size() - 1) {
                    playerIndex++;
                } else {
                    playerIndex = 0;
                }
                mNextPath = "" + mRecordedEvent.get(playerIndex);
                nextPlayer();
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "play video");
                if (playerIndex > 0) {
                    playerIndex--;
                } else {
                    playerIndex = mRecordedEvent.size();
                }
                mNextPath = "" + mRecordedEvent.get(playerIndex);
                nextPlayer();
            }
        });

        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "play video");
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, 0);
                boolean mute = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
                if (mute == true) {
                    muteButton.setImageResource(R.drawable.volume);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
                } else {
                    muteButton.setImageResource(R.drawable.mute);
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        mContext.unregisterReceiver(mDvbBroadcastReceiver);
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        mVideoView.stopPlayback();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        super.onBackPressed();
        Intent i = new Intent(AudioPlaybackActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(i);
        finish();
    }

    public void addPlayerListners() {
        Log.i(TAG, "addPlayerListners");
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.i(TAG, "onError, what :" + what + ", extra :" + extra);
                return false;
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e(TAG, "onCompletion");
                if (playerIndex < mRecordedEvent.size() - 1) {
                    playerIndex++;
                } else {
                    playerIndex = 0;
                }
                mNextPath = "" + mRecordedEvent.get(playerIndex);
                nextPlayer();
            }
        });

        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.i(TAG, "received info : " + what + " : " + extra);
                return true;
            }
        });
        RelativeLayout viewData = (RelativeLayout)findViewById(R.id.activity_record_playback);
        viewData.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mSeekBarLinearLayout.getVisibility() == View.VISIBLE) {
                        mHandler.removeCallbacksAndMessages(mSeekBarHideToken);
                        mSeekBarLinearLayout.setVisibility(View.GONE);
                        return true;
                    }
                    updateSeekBarData();
                }
                return true;
            }
        });
    }

    private void seekBarPosition() {
        long totalTime = mVideoView.getDuration(); // In milliseconds
        long currentTime = mVideoView.getCurrentPosition();
        int currentTimeHours = (int) (currentTime / (60 * 60 * 1000));
        int currentTimeMinutes = (int) (currentTime / (60 * 1000));
        int currentTimeSeconds = (int) ((currentTime / 1000) % 60);
        int totalTimeHours = (int) (totalTime / (60 * 60 * 1000));
        int totalTimeMinutes = (int) (totalTime / (60 * 1000));
        int totalTimeSeconds = (int) ((totalTime / 1000) % 60);
        if (currentTimeHours == 0) {
            mStartTime.setText("" + currentTimeMinutes + " :" + currentTimeSeconds);
        } else {
            mStartTime.setText("" + currentTimeHours + ":" + currentTimeMinutes + " :" + currentTimeSeconds);
        }
        if (totalTimeHours == 0) {
            mEndTime.setText("" + totalTimeMinutes + " :" + totalTimeSeconds);
        } else {
            mEndTime.setText("" + totalTimeHours + ":" + totalTimeMinutes + " :" + totalTimeSeconds);
        }
    }


    private void initializeSeekBar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mVideoView != null && b) {
                    Log.e(TAG, "seek to data" + i * 10);
                    mVideoView.seekTo(i * 10);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.v(TAG, "onStartTrackingTouch");
                mHandler.removeCallbacksAndMessages(mSeekBarHideToken);
                mSeekBarLinearLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.v(TAG, "onStopTrackingTouch");
                updateSeekBarData();
            }
        });
        mSeekBar.setMax(mVideoView.getDuration() / 10);
        final Handler handler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mVideoView != null) {
                    int currentPosition = mVideoView.getCurrentPosition() / 10; // In seconds
                    mSeekBar.setProgress(currentPosition);
                    seekBarPosition();
                }
                handler.postDelayed(mRunnable, 10);
            }
        };
        handler.postDelayed(mRunnable, 10);
    }

    private void updateSeekBarData() {
        Log.v(TAG, "updateSeekBarData");
        mHandler.removeCallbacksAndMessages(mSeekBarHideToken);
        mSeekBarLinearLayout.setVisibility(View.VISIBLE);
        mHandler.postAtTime(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "updateSeekBarData gone");
                mSeekBarLinearLayout.setVisibility(View.GONE);
            }
        }, mSeekBarHideToken, SystemClock.uptimeMillis() + 5000);
    }

    private void getAllAudioFromDevice() {
        ContentResolver cr = mActivity.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;

        if(cur != null)
        {
            count = cur.getCount();

            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    Log.e("Path :" + data, " Artist :" );
                    mRecordedEvent.add(data);
                }

            }
        }

        cur.close();
        return;
    }


    private void displayRecordedList() {
        mRecordListAdapter = new RecordedAdapter(AudioPlaybackActivity.this, mRecordedEvent, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, final int position, final List<Object> recordedEvent) {
                try {
                    playerIndex = position;
                    mNextPath = "" + mRecordedEvent.get(playerIndex);
                    nextPlayer();
                } catch (IndexOutOfBoundsException ex) {
                    Log.w(TAG, "No such Item exists");
                    return;
                }
            }

            @Override
            public boolean onItemLongClick(View v, final int position, final List<Object> recordedEvent) {
                try {
                    Log.v(TAG, "record content onLongClicked");
                    mNextPath = "" + mRecordedEvent.get(playerIndex);
                    showAlert(1,position,0);
                    mRecordListAdapter.notifyDataSetChanged();
                } catch (IndexOutOfBoundsException ex) {
                    Log.w(TAG, "No such Item exists");
                }
                return true;
            }
        });
        recorded_content_list.setAdapter(mRecordListAdapter);

    }


    private void showAlert(final int what, final int etc1,final int etc2) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                myDialog = new Dialog(mContext);
                myDialog.setContentView(R.layout.app_display_popup);
                final TextView text_dialog = (TextView) myDialog.findViewById(R.id.text_dialog);
                final TextView dialog_tital = (TextView) myDialog.findViewById(R.id.app_tital);
                final Button btn_yes = (Button) myDialog.findViewById(R.id.btn_yes);
                final Button btn_no = (Button) myDialog.findViewById(R.id.btn_no);
                dialog_tital.setText("ALERT");
                btn_yes.setText(R.string.ok);
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myDialog.dismiss();
                    }
                });
                switch (what) {
                    case 1: {
                        btn_yes.setText("conform");
                        text_dialog.setText("Are you sure want to uninstall app?");
                        btn_yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    String fileName = ""+mRecordedEvent.get(etc1);
                                    File deleteFile = new File(fileName);
                                    boolean deleted = deleteFile.delete();
                                    deleteFile.deleteOnExit();
                                    MediaScannerConnection.scanFile(mActivity.getApplicationContext(), new String[] { Environment.getExternalStorageDirectory().toString() }, null, new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(String path, Uri uri)
                                        {
                                            Log.i("ExternalStorage", "Scanned " + path + ":");
                                            Log.i("ExternalStorage", "-> uri=" + uri);
                                        }
                                    });
                                    if(!deleted) {
                                        Log.e("AppDisplay", "not deleted"+fileName);
                                    } else {
                                        Log.e("AppDisplay", "successfully deleted");
                                    }
                                    Toast.makeText(mActivity, "Successfully deletetd file", Toast.LENGTH_SHORT).show();
                                    mRecordedEvent.remove(etc1);
                                    mRecordListAdapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(mActivity, "failed to uninstalled app", Toast.LENGTH_SHORT).show();
                                }
                                myDialog.dismiss();
                            }
                        });

                        btn_no.setText("Cancel");
                        btn_no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myDialog.dismiss();
                            }
                        });
                    }
                    default:
                        Log.w("AppDisplay", "No Such case found");
                }
                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.show();
            }
        });
    }

    private class RecordedPlaybackReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("data")) {

            }
        }
    }

    private void nextPlayer() {
        mPath = mNextPath;
        mVideoView.setVideoPath(mPath);
        mVideoView.requestFocus();
        mVideoView.start();
        seekBarClick();

        Log.e(TAG, "Record next file path : " + mPath);
    }

    private View.OnTouchListener screenTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View arg0, MotionEvent arg1) {
            switch (arg1.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int button1IsVisible = mSeekBarLinearLayout.getVisibility();
                    touched = true;
                    if (touched == true) {
                        if (button1IsVisible == View.GONE) {
                            updateSeekBarData();
                            touched = false;
                        } else {
                            mSeekBarLinearLayout.setVisibility(View.GONE);
                            touched = false;
                        }
                    }
                    break;

            }
            return true;
        }
    };

}

