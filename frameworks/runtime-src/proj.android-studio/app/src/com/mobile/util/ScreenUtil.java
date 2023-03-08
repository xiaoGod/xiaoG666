package com.mobile.util;

import android.content.Context;
import android.view.Window;

import java.lang.reflect.Field;

public class ScreenUtil {
    private static ScreenUtil screenUtil;
    public  static ScreenUtil getInstance() {
        if (screenUtil == null)
            screenUtil = new ScreenUtil();

        return screenUtil;
    }

    //获得屏幕的高度
    public float getScreenWidth(Context context) {
        //替用代码
        /**
        WindowManager manager =  (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        **/

        int width = context.getResources().getDisplayMetrics().widthPixels;
        return width;
    }

    //获得屏幕的高度
    public float getScreenHeight(Context context) {
        int height = context.getResources().getDisplayMetrics().heightPixels;
        return height;
    }

    //获得状态栏的高度
    public float getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return statusBarHeight;
    }

    public void setScreenStyle(Window window, boolean withStatusBar) {

    }
}
