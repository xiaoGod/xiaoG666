/****************************************************************************
 Copyright (c) 2018 Xiamen Yaji Software Co., Ltd.

 http://www.cocos2d-x.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
package org.cocos2dx.lib;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Map;

public class Utils {
    private static Activity sActivity = null;

    public static void setActivity(final Activity activity) {
        Utils.sActivity = activity;
    }

    public static void hideVirtualButton() {
        if (Build.VERSION.SDK_INT >= 19 &&
                null != Utils.sActivity) {
            // use reflection to remove dependence of API level

            Class viewClass = View.class;
            final int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = Cocos2dxReflectionHelper.<Integer>getConstantValue(viewClass, "SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION");
            final int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = Cocos2dxReflectionHelper.<Integer>getConstantValue(viewClass, "SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN");
            final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = Cocos2dxReflectionHelper.<Integer>getConstantValue(viewClass, "SYSTEM_UI_FLAG_HIDE_NAVIGATION");
            final int SYSTEM_UI_FLAG_FULLSCREEN = Cocos2dxReflectionHelper.<Integer>getConstantValue(viewClass, "SYSTEM_UI_FLAG_FULLSCREEN");
            final int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = Cocos2dxReflectionHelper.<Integer>getConstantValue(viewClass, "SYSTEM_UI_FLAG_IMMERSIVE_STICKY");
            final int SYSTEM_UI_FLAG_LAYOUT_STABLE = Cocos2dxReflectionHelper.<Integer>getConstantValue(viewClass, "SYSTEM_UI_FLAG_LAYOUT_STABLE");

            // getWindow().getDecorView().setSystemUiVisibility();
            final Object[] parameters = new Object[]{SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | SYSTEM_UI_FLAG_IMMERSIVE_STICKY};
            Cocos2dxReflectionHelper.<Void>invokeInstanceMethod(Utils.sActivity.getWindow().getDecorView(),
                    "setSystemUiVisibility",
                    new Class[]{Integer.TYPE},
                    parameters);
        }
    }

    /**
     * dp转换成px
     */
    public static int dip2px(int dp) {
        float scale = getAppResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5);
    }

    /**
     * px转换成dp
     */
    public static int px2dip(float px) {
        final float scale = getAppResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * sp转换成px
     */
    public static int sp2px(float sp) {
        float scale = getAppResources().getDisplayMetrics().density;
        return (int) (sp * scale + 0.5f);
    }

    /**
     *   获取设备宽度(px)
     **/
    public static int getScreenWidth() {
        DisplayMetrics metrics = getAppResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    /**
     *   获取设备高度(px)
     **/
    public static int getScreenHeight() {
        DisplayMetrics metrics = getAppResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    /**
     *   获取设备宽度(dp)
     **/
    public static int getScreenWidthdp() {
        DisplayMetrics metrics = getAppResources().getDisplayMetrics();
        return px2dip(metrics.widthPixels);
    }

    /**
     *   获取设备高度(dp)
     **/
    public static int getScreenHeightdp() {
        DisplayMetrics metrics = getAppResources().getDisplayMetrics();
        return px2dip(metrics.heightPixels);
    }

    /*
     * 获取资源色值
     * */
    public static int getColor(@NonNull Context context, @ColorRes int id) {
        return ContextCompat.getColor(context, id);
    }

    public static int getColor(@ColorRes int id) {
        return ContextCompat.getColor(getAppContext(), id);
    }

    private static final int MIN_CLICK_DELA_TIME = 1000;
    private static long mLastClickTime = 0;
    public static boolean isFastClick() {
        boolean ret = true;
        long curClickTime = System.currentTimeMillis();
        if (curClickTime - mLastClickTime > MIN_CLICK_DELA_TIME) {
            mLastClickTime = curClickTime;
            ret = false;
        }

        return ret;
    }

    /**
     * map转换为url键值对字符串
     * @param treepMap
     * @return
     */
    public static String map2String(Map<String, Object> treepMap) {
        if (treepMap == null)
            return null;

        StringBuffer buff = new StringBuffer();
        for(Map.Entry<String, Object> entry : treepMap.entrySet()) {
            if( buff.length() > 0 ){
                buff.append("&");
            }
            buff.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return buff.toString();
    }

    public static String convertToSecretString(String str) {
        if (str == null || str.length() <= 6)
            return str;

        String ret = "";
        for (int i=0; i<str.length(); i++) {
            char ch = str.charAt(i);
            if (i > 3 && i < str.length() - 3)
                ret += '*';
            else
                ret += ch;
        }

        return ret;
    }

    public static Context getAppContext() {
        return sActivity.getApplicationContext();
    }

    public static Resources getAppResources() {
        return sActivity.getResources();
    }

    public static Activity getActivity() {
        return sActivity;
    }
}