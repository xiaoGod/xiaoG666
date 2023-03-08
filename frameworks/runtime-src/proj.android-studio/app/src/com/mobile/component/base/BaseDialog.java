package com.mobile.component.base;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobile.audi.MainApp;
import com.mobile.core.EventDispatcher;

import java.lang.ref.WeakReference;

public abstract class BaseDialog extends DialogFragment implements EventDispatcher.EventListener {

    protected Context mContext;
    protected View mRootView;

    public abstract int getLayoutID();

    public abstract int getStyle();

    public abstract void initView(View view, @Nullable Bundle savedInstanceState);

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mContext = new WeakReference<>(getActivity()).get();
        mRootView = LayoutInflater.from(mContext).inflate(getLayoutID(), null);
        Dialog dialog = new Dialog(mContext, getStyle());
        dialog.setContentView(mRootView);
        return dialog;
    }

//    @SuppressLint("JavascriptInterface")
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NO_FRAME, getStyle());
//    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        mContext = MainApp.getAppContext();
//        mRootView = inflater.inflate(getLayoutID(), container);
//        return mRootView;
//    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initBackPressedListener();
        initView(mRootView, savedInstanceState);
    }

    public void setCanceledOnTouchOutside(boolean cancel) {
        getDialog().setCanceledOnTouchOutside(cancel);
    }

    public void setBackgroundTransparent() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        getDialog().getWindow().setLayout(metrics.widthPixels, getDialog().getWindow().getAttributes().height);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void setGravityBottom() {
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width =  WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        getDialog().getWindow().setAttributes(params);
    }

    private void initBackPressedListener() {
        BaseActivity activity = (BaseActivity) MainApp.getActivity();
        activity.getEvent().addEventListener(BaseActivity.EVENT_BACK_PRESSED, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseActivity activity = (BaseActivity) MainApp.getActivity();
        activity.getEvent().removeEventListener(BaseActivity.EVENT_BACK_PRESSED, this);
    }

    @Override
    public void onDispatch(@Nullable Object... params) {
        onBackPressed();
    }

    protected void onBackPressed() {
        dismiss();
    }

    public String getFragmentTag() {
        return this.getClass().getSimpleName();
    }

    public void show() {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            MainApp.getFragmentManager().beginTransaction().remove(this).commit();
            show(MainApp.getFragmentManager(), getFragmentTag());
        } catch (IllegalStateException ignore) {
            ignore.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
