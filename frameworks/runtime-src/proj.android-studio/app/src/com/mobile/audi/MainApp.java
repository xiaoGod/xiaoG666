package com.mobile.audi;

import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnDeviceIdsRead;
import com.audiGame.teen_patti.R;
import com.blankj.utilcode.util.ToastUtils;

import org.cocos2dx.javascript.JSMethodHelper;

import java.util.ArrayList;

public class MainApp extends Application {
    private static MainApp mApp;
    private static Activity sActivity;
    private String TAG = MainApp.class.getName();

    private static ArrayList<Activity> mActivityList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        mApp = this;
        // 初始化adjust
        String appToken = getAppResources().getString(R.string.adjustToken);
        String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(this, appToken, environment);
        config.setLogLevel(LogLevel.VERBOSE);
        Adjust.onCreate(config);

        // 上报打开app
        String launchAppEvent = getAppResources().getString(R.string.launchApp);
        AdjustEvent adjustEvent = new AdjustEvent(launchAppEvent);
        Adjust.trackEvent(adjustEvent);
        // 获取GoogleAdId
        Adjust.getGoogleAdId(this, googleAdId -> JSMethodHelper.googleAdId = googleAdId);

        ToastUtils.make().setGravity(Gravity.BOTTOM, 0, 0);

        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.d(TAG, "onActivityCreated: " + activity);
                mActivityList.add(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.d(TAG, "onActivityCreated: " + activity);
                sActivity = activity;
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Adjust.onResume();
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Adjust.onPause();
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(TAG, "onActivityDestroyed: " + activity);
                mActivityList.remove(activity);
            }
        });
    }

    public static void trackEvent(String eventToken, double revenue, String currency) {
        Log.d("UploadAdjust", eventToken);
        AdjustEvent adjustEvent = new AdjustEvent(eventToken);
        if (revenue > 0)
            adjustEvent.setRevenue(revenue, currency);
        Adjust.trackEvent(adjustEvent);
    }

    public static Application getApp() {
        return mApp;
    }

    public static Context getAppContext() {
        return mApp.getApplicationContext();
    }

    public static Resources getAppResources() {
        return mApp.getResources();
    }

    public static Activity getActivity() {
        return sActivity;
    }

    public static ArrayList<Activity> getActivityList() {
        return mActivityList;
    }

//    public static MainFragment getMainFragment() {
//        FragmentManager fManager = getFragmentManager();
//        MainFragment f = (MainFragment) fManager.findFragmentByTag(MainFragment.class.getName());
//        return f;
//    }

    public static FragmentManager getFragmentManager() {
        return getActivity().getFragmentManager();
    }
}
