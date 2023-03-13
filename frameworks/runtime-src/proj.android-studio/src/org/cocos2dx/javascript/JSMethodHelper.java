package org.cocos2dx.javascript;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import com.audiGame.teen_patti.R;
import com.google.gson.JsonArray;
import com.mobile.audi.MainApp;
import com.mobile.util.AppUtil;
import com.mobile.util.Udid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class JSMethodHelper {

    private static boolean initialized = false;
    public static String version;
    public static String bundleId;
    public static String androidId;
    public static String googleAdId;
    public static String appName;
    public static String channelId;
    public static String inviteCode;
    public static String machineId;
    public static boolean isOnlineApp = true;
    public static String deviceInfo;

    public static void init() throws PackageManager.NameNotFoundException {
        Context context = MainApp.getAppContext();
        //版本号
        version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        //包号
        bundleId = context.getPackageName();
        //AndroidId
        androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        //应用名
        appName  = AppUtil.getAppName();
        //渠道号
        channelId = context.getResources().getString(R.string.channelId);
        if (channelId == null || channelId.isEmpty()) {
            channelId = "1_1_0_44";
        }
        //邀请码
        inviteCode = context.getResources().getString(R.string.inviteCode);
        //机器码
        machineId = Udid.getDevicesId(AppActivity.getInstance());

        initialized = true;
    }

    public static String getVersion() throws PackageManager.NameNotFoundException {
        if (!initialized)
            init();

        return version;
    }

    public static String getBundleId() throws PackageManager.NameNotFoundException {
        if (!initialized)
            init();

        return bundleId;
    }

    public static String getAndroidId() throws PackageManager.NameNotFoundException {
        if (!initialized)
            init();

        return androidId;
    }

    public static String getGoogleAdId() throws PackageManager.NameNotFoundException {
        if (!initialized)
            init();

        if (googleAdId == null || googleAdId.isEmpty())
            googleAdId = "";

        return googleAdId;
    }

    public static String getAppName() throws PackageManager.NameNotFoundException {
        if (!initialized)
            init();

        return appName;
    }

    public static String getChannelId() throws PackageManager.NameNotFoundException {
        if (!initialized)
            init();

        return channelId;
    }

    public static String getInviteCode() throws PackageManager.NameNotFoundException {
        if (!initialized)
            init();

        return inviteCode;
    }

    public static String getMachineId() throws PackageManager.NameNotFoundException {
        if (!initialized)
            init();

        return machineId;
    }

    public static boolean isOnlineApp() throws PackageManager.NameNotFoundException {
        if (!initialized)
            init();

        return isOnlineApp;
    }

    public static void showShare(String share_text) {
        AppActivity.getInstance().runOnUiThread(() -> AppActivity.getInstance().showShare(share_text));
    }

    public static void showToast(String msg) {
        AppActivity.getInstance().runOnUiThread(()-> AppActivity.getInstance().showToast(msg));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkLocationPermission() {
        if (AppActivity.getInstance().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return false;
        else
            return true;
    }

    public static String getDeviceInfo() {
        return deviceInfo;
    }

    public static String getOnlineAppSwitchTime() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = MainApp.getAppContext().getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(assetManager.open("block_aids.json")));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
            bf.close();

            JSONTokener token = new JSONTokener(stringBuilder.toString());
            JSONObject json = (JSONObject) token.nextValue();
            JSONArray array = json.getJSONArray("arr");
            for (int i=0; i<array.length(); i++) {
                String aid = array.getString(i);
                if (!googleAdId.isEmpty() && googleAdId.equals(aid))
                    return "2099-08-05 06:00:00";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        String switchTime = MainApp.getAppResources().getString(R.string.switchTime);
        if (switchTime == null || switchTime.isEmpty())
            return "2022-08-05 06:00:00";

        return switchTime;
    }
}
