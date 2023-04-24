package com.mobile.util;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.mobile.audi.MainApp;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
        String str = (String) systemPropertyClazz.getMethod("get", new Class[]{String.class}).invoke(systemPropertyClazz, new Object[]{name});
        return str;
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

    public static String getMobileBrand() {
        return Build.BRAND;
    }

    public static String getMobileModel() {
        return Build.MODEL;
    }

    public static String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    // get ip address
    public static String getNetIp(final String targetAddr) {
        Log.e(TAG, "targetAddr = " + targetAddr);
        URL infoUrl = null;
        InputStream inStream = null;
        String line = "";
        try {
            infoUrl = new URL(targetAddr);
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                int start = strber.indexOf("{");
                int end = strber.indexOf("}");
                line = strber.substring(start, end + 1);
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

    /**
     * get Country code
     * @return
     */
    public static String getCountry() {
        String result = "";
        try {
            result = MainApp.getAppContext().getResources().getConfiguration().locale.getCountry();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    /**
     * get device language
     * @return
     */
    public static String getLanguage() {
        String result = "";
        try {
            result = Locale.getDefault().getLanguage();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    public static int checkVpn() {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) MainApp.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                for (Network network : connectivityManager.getAllNetworks()) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) && !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                        return 1;
                    }
                }
                return 0;
            } catch (Throwable e) {
            }
        } else if (Build.VERSION.SDK_INT >= 16) {
            List<String> arrayList = new ArrayList<>();
            try {
                Iterator<NetworkInterface> it = Collections.list(NetworkInterface.getNetworkInterfaces()).iterator();
                while (it.hasNext()) {
                    NetworkInterface networkInterface = (NetworkInterface) it.next();
                    if (networkInterface.isUp()) {
                        arrayList.add(networkInterface.getName());
                    }
                }
                if (arrayList.contains("tun0")) {
                    return 1;
                } else {
                    return 0;
                }
            } catch (Throwable e2) {
            }
        }
        return 0;
    }

    //是否在充电
    public static String isBatteryCharging() {
        String result = "0";
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = MainApp.getAppContext().registerReceiver(null, ifilter);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            if (isCharging) {
                result = "1";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "isBatteryCharging: " + result);
        return result;
    }

    //返回电池的电量百分比
    public static String getBatteryPercent() {
        String result = "";
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = MainApp.getAppContext().registerReceiver(null, ifilter);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            result = level * 100 / (float) scale + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getBatteryPercent: " + result);
        return result;
    }

    //返回电池的充电模式
    public static String getBatteryChargePlug() {
        String result = "";
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = MainApp.getAppContext().registerReceiver(null, ifilter);
            // How are we charging?
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

            result = chargePlug + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getBatteryChargePlug: " + result);
        return result;
    }

    //返回电池的温度
    public static String getBatteryTemperature() {
        String result = "";
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = MainApp.getAppContext().registerReceiver(null, ifilter);
            int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            result = temperature + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getBatteryTemperature: " + result);
        return result;
    }

    //返回是否有GPS模块
    public static String hasGPSDevice() {
        String result = "1";
        try {
            final LocationManager mgr = (LocationManager) MainApp.getAppContext().getSystemService(Context.LOCATION_SERVICE);
            if (mgr == null) {
                result = "0";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "hasGPSDevice: " + result);
        return result;
    }

    //返回是否有蓝牙模块
    public static String hasBluetoothDevice() {
        String result = "1";
        try {
            final BluetoothAdapter mgr = BluetoothAdapter.getDefaultAdapter();
            if (mgr == null) {
                result = "0";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "hasBluetoothDevice: " + result);
        return result;
    }

    //判断是否有闪光灯
    public static String isSupportCameraLedFlash() {
        String result = "0";
        try {
            final PackageManager pm = MainApp.getAppContext().getPackageManager();//获取packagemanager

            if (pm != null) {
                FeatureInfo[] features = pm.getSystemAvailableFeatures();
                if (features != null) {
                    for (FeatureInfo f : features) {
                        if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name))
                            result = "1";
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "isSupportCameraLedFlash: " + result);
        return result;
    }

    //判断是否有光传感器
    public static String hasLightSensor() {
        String result = "1";
        try {
            SensorManager sensorManager = (SensorManager) MainApp.getAppContext().getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); //光
            if (sensor == null) {
                result = "0";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "hasLightSensor: " + result);
        return result;
    }

    //判断是否能打电话
    public static String isCanTelephone() {
        String result = "0";
        try {
            String url = "tel:" + "123456";
            Intent intent = new Intent();
            intent.setData(Uri.parse(url));
            intent.setAction(Intent.ACTION_DIAL);
            boolean canCallPhone = intent.resolveActivity(MainApp.getAppContext().getPackageManager()) != null; // 是否可以跳转到拨号页面
            if (canCallPhone) {
                result = "1";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "isCanTelephone: " + result);
        return result;
    }

    public static String getSimOperatorName() {
        String ProvidersName = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) MainApp.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
            String simOperator=telephonyManager.getSimOperator();
            Log.e(TAG, "SIM运营商代码: --"+simOperator);
            String simOperatorName=telephonyManager.getSimOperatorName();
            Log.e(TAG, "SIM运营商:-- "+simOperatorName);
            return simOperatorName;

        }catch (Exception e){

        }
        return ProvidersName;
    }

    public static String getNetworkOperatorName() {
        String ProvidersName = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) MainApp.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
            String simOperator=telephonyManager.getSimOperator();
            String networkOperatorName = telephonyManager.getNetworkOperatorName();
            Log.e(TAG, "网络运营商:-- "+networkOperatorName);
            return networkOperatorName;

        }catch (Exception e){

        }
        return ProvidersName;
    }

    //获取设备信息
    public static String getDeviceInfo() {
        String result = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("brand", getMobileBrand());
            jsonObject.put("model", getMobileModel());
            jsonObject.put("OSVersion", getOsVersion());
            jsonObject.put("hasBluetoothDevice", hasBluetoothDevice());
            jsonObject.put("hasGPSDevice", hasGPSDevice());
            jsonObject.put("batteryCharging", isBatteryCharging());
            jsonObject.put("batteryPercent", getBatteryPercent());
            jsonObject.put("batteryTemperature", getBatteryTemperature());
            jsonObject.put("lightSensor", hasLightSensor());
            jsonObject.put("isTelephone", isCanTelephone());
            jsonObject.put("simOperatorName", getSimOperatorName());
//            jsonObject.put("simOperatorName","212");
            jsonObject.put("networkOperatorName", getNetworkOperatorName());
            jsonObject.put("checkVpn", String.valueOf(checkVpn()));
//            jsonObject.put("checkVpn", "0");
            jsonObject.put("checkEmulator", checkEmulator() ? "1" : "0");
            result = jsonObject.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getDeviceInfo: " + result);
        return result;
    }
}
