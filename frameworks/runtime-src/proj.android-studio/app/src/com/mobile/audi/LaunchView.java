package com.mobile.audi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.audiGame.teen_patti.R;

import org.cocos2dx.javascript.AppActivity;
import org.cocos2dx.lib.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class LaunchView extends FrameLayout implements Animation.AnimationListener {

    private LinearLayout mLoading;
    private ImageView mBackground;
    private boolean mRemoved = false;
    private boolean mAnimStarted = false;

    @SuppressLint("ResourceType")
    public LaunchView(@NonNull Context context) {
        super(context);
        inflate(context, R.layout.layout_launch, this);

        mLoading = findViewById(R.id.id_loading);
        mBackground = findViewById(R.id.id_img_bg);
        mBackground.setImageResource(R.drawable.launch);

        mLoading.setVisibility(GONE);

        delayShowLoading(1000);
        setEndHandler(5000);
    }

    private void delayShowLoading(int delay) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Utils.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.setVisibility(VISIBLE);
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, delay);
    }

    private void setEndHandler(int delay) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Utils.getActivity().runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        if (isAttachedToWindow() && !mRemoved)
                            removeWithAction();
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, delay);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void removeWithAction() {
        if (!isAttachedToWindow() && mRemoved || mAnimStarted)
            return;

        Animation animation = AnimationUtils.loadAnimation(Utils.getAppContext(), R.anim.anim_close_launch);
        animation.setFillAfter(true);
        animation.setRepeatCount(0);
        animation.setAnimationListener(this);
        startAnimation(animation);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        mAnimStarted = true;
        mLoading.setVisibility(GONE);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        mRemoved = true;
        AppActivity appActivity = (AppActivity) Utils.getActivity();
        appActivity.mFrameLayout.removeView(this);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
