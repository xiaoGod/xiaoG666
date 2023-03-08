package com.mobile.component.base;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import org.cocos2dx.javascript.AppActivity;
import com.mobile.audi.MainApp;
import com.mobile.component.dialog.CommonAlertDialog;
import com.mobile.component.dialog.WebDialog;
import com.mobile.core.EventDispatcher;

import org.cocos2dx.javascript.SDKWrapper;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;

public class BaseActivity extends Cocos2dxActivity {

    private final String TAG = getClass().getName();

    private final int MIN_CLICK_DELA_TIME = 2000;
    private long mLastClickTime = 0;

    public static final String EVENT_BACK_PRESSED = "EVENT_BACK_PRESSED";
    private EventDispatcher mEvent = new EventDispatcher();

    @Override
    public void onResume() {
        super.onResume();
        SDKWrapper.getInstance().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        SDKWrapper.getInstance().onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isTaskRoot()) {
            return;
        }
        SDKWrapper.getInstance().onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SDKWrapper.getInstance().onNewIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SDKWrapper.getInstance().onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SDKWrapper.getInstance().onStop();
    }

    @Override
    public void onBackPressed() {
        SDKWrapper.getInstance().onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SDKWrapper.getInstance().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        SDKWrapper.getInstance().onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SDKWrapper.getInstance().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        SDKWrapper.getInstance().onStart();
        getGLSurfaceView().setListener((keyCode, event) -> {
            Log.d(TAG, "key back pressed in GLSurfaceView!");
            //如果WebView存在，则关闭WebView
            boolean result = WebDialog.getInstance().closeWebAndRotation();
            if (result)
                return;

            if (!mEvent.containsListener(EVENT_BACK_PRESSED)) {
                if (MainApp.getActivityList().size() > 1)
                    return;

                CommonAlertDialog alertDialog = CommonAlertDialog.newInstance("Are you sure to exit Game?");
                alertDialog.setOkListener(()-> ((AppActivity)MainApp.getActivity()).exitApp());
                alertDialog.show();
                return;
            }
            else {
                mEvent.dispatchPop(EVENT_BACK_PRESSED);
            }
        });
        super.onStart();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "key back pressed in BaseActivity!");
            //如果WebView存在，则关闭WebView
            boolean result = WebDialog.getInstance().closeWebAndRotation();
            if (result)
                return true;

            if (!mEvent.containsListener(EVENT_BACK_PRESSED)) {
                if (MainApp.getActivityList().size() > 1)
                    return super.onKeyDown(keyCode, event);

//                long curClickTime = System.currentTimeMillis();
//                if (curClickTime - mLastClickTime > MIN_CLICK_DELA_TIME) {
//                    mLastClickTime = curClickTime;
//                    showToast("Press again to exit.");
//                }
//                else {
//                    finish();
//                    System.exit(0);
//                }
                CommonAlertDialog alertDialog = CommonAlertDialog.newInstance("Are you sure to exit Game?");
                alertDialog.setOkListener(()-> ((AppActivity)MainApp.getActivity()).exitApp());
                alertDialog.show();
                return true;
            }
            else {
                mEvent.dispatchPop(EVENT_BACK_PRESSED);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void startIntent(@NonNull Class<?> cls) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void openUrl(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void showToast(String msg) {
//        ToastUtils.showShort(msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public EventDispatcher getEvent() {
        return mEvent;
    }

    public void sendMessageToJS(String msg) {
        final String jsCallStr = String.format("messageFromNative(%s);", msg);
        this.runOnGLThread(() -> {
            int ret = Cocos2dxJavascriptJavaBridge.evalString(jsCallStr);
            Log.d(TAG, String.format("ret = %d", ret));
        });
    }

    public void showShare(String share_text) {
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
        intent.putExtra(Intent.EXTRA_TEXT, share_text);
        startActivity(Intent.createChooser(intent, getTitle()));
    }
}
