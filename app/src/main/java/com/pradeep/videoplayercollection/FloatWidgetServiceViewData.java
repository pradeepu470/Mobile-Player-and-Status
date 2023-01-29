package com.pradeep.videoplayercollection;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

public class FloatWidgetServiceViewData extends Service {
    private WindowManager mWindowManager;
    private View mFloatingWidget;
    private FloatingActionButton fab;
    private boolean isFabMenuOpen = false;
    private Animation fabOpenAnimation;
    private Animation fabCloseAnimation;
    private LinearLayout mSchedulefabLayout, mAdminControlfabLayout, mReminderLayout, mRecordedLayout;
    public FloatWidgetServiceViewData() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        fab = (FloatingActionButton) mFloatingWidget.findViewById(R.id.fab);
    }

    private void getAnimations() {
        fabOpenAnimation = AnimationUtils.loadAnimation(mFloatingWidget.getContext(), R.anim.fab_open);
        fabCloseAnimation = AnimationUtils.loadAnimation(mFloatingWidget.getContext(), R.anim.fab_close);
    }

    private void expandFabMenu() {
        ViewCompat.animate(fab).rotation(45.0F).withLayer().setDuration(300).setInterpolator(new OvershootInterpolator(10.0F)).start();
        fab.setImageDrawable(ContextCompat.getDrawable(mFloatingWidget.getContext(), R.drawable.ic_add_black_24dp));
        isFabMenuOpen = true;
        mRecordedLayout.startAnimation(fabOpenAnimation);
        mReminderLayout.startAnimation(fabOpenAnimation);
        mAdminControlfabLayout.startAnimation(fabOpenAnimation);
        mSchedulefabLayout.startAnimation(fabOpenAnimation);
        mSchedulefabLayout.setClickable(true);
        mAdminControlfabLayout.setClickable(true);
        mReminderLayout.setClickable(true);
        mRecordedLayout.setClickable(true);
    }

    private void collapseFabMenu() {
        ViewCompat.animate(fab).rotation(0.0F).withLayer().setDuration(300).setInterpolator(new OvershootInterpolator(10.0F)).start();
        fab.setImageDrawable(ContextCompat.getDrawable(mFloatingWidget.getContext(), R.drawable.internet_explore));
        mRecordedLayout.startAnimation(fabCloseAnimation);
        mReminderLayout.startAnimation(fabCloseAnimation);
        mAdminControlfabLayout.startAnimation(fabCloseAnimation);
        mSchedulefabLayout.startAnimation(fabCloseAnimation);
        mSchedulefabLayout.setClickable(false);
        mAdminControlfabLayout.setClickable(false);
        mReminderLayout.setClickable(false);
        mRecordedLayout.setClickable(false);
        isFabMenuOpen = false;
    }

    private void getall() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingWidget, params);
        mFloatingWidget.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {

                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingWidget, params);
                        return true;
                }
                return false;
            }
        });
    }

    private boolean isViewCollapsed() {
        return mFloatingWidget == null || mFloatingWidget.findViewById(R.id.root_container).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingWidget != null) mWindowManager.removeView(mFloatingWidget);
    }
}