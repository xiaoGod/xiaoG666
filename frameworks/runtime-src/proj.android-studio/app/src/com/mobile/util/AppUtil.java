package com.mobile.util;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.mobile.audi.MainApp;

import java.util.Collections;
import java.util.List;

public class AppUtil {
    private static final String TAG = AppUtil.class.getName();

    public static String getAppName() {
        String appName = "";
        try
        {
            PackageManager pManager = MainApp.getAppContext().getPackageManager();
            ApplicationInfo pInfo   = pManager.getApplicationInfo(MainApp.getAppContext().getPackageName(),0);
            appName  = (String)pManager.getApplicationLabel(pInfo);
            Log.d(TAG,"getAppName is " + appName);
        }
        catch (Exception e)
        {
            return "";
        }

        return  appName;
    }

    private static String getSystemProperty(String name) throws Exception {
        Class systemPropertyClazz = Class.forName("android.os.SystemProperties");
        return (String) systemPropertyClazz.getMethod("get", new Class[]{String.class})
                .invoke(systemPropertyClazz, new Object[]{name});
    }

    public static boolean checkEmulator() {
        try {
            String hardware = getSystemProperty("ro.hardware");
            String abi = getSystemProperty("ro.product.cpu.abi");
            String qemu = getSystemProperty("ro.kernel.qemu");

            if (hardware.contains("x86") || abi.contains("x86") || qemu.contains("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 判断某个包是否可用
     * @param pack_name
     */
    //wx:com.tencent.mm   qq:com.tencent.mobileqq alipay:com.eg.android.AlipayGphone
    public static boolean isApplicationAvailable( String pack_name ) {
        PackageManager packageManager = MainApp.getAppContext().getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if(pinfo != null) {
            for(int i=0; i<pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if(pn.equals( pack_name )) {
                    return true;
                }
            }
        }
        return  false;
    }

    public static boolean openApp(String pkg_name ) {
        Intent mainIntent = new Intent( Intent.ACTION_MAIN, null );
        mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
        PackageManager mPackageManager = MainApp.getAppContext().getPackageManager();
        List<ResolveInfo> mAllApps = mPackageManager.queryIntentActivities( mainIntent, 0 );
        Collections.sort( mAllApps, new ResolveInfo.DisplayNameComparator( mPackageManager ));

        for (ResolveInfo res : mAllApps) {
            //该应用的包名和主Activity
            String pkg = res.activityInfo.packageName;
            String cls = res.activityInfo.name;
            if (pkg.contains(pkg_name))
            {
                ComponentName component = new ComponentName( pkg, cls );
                Intent intent = new Intent();
                intent.setComponent( component );
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                MainApp.getActivity().startActivity( intent );
                return true;
            }
        }

        return false;
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
}
