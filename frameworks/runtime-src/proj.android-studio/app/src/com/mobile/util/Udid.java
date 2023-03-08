package com.mobile.util;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class Udid {
    public static String getDevicesId(Context context) {
        //获取保存在sd中的 设备唯一标识符
        String readDeviceID = UdidUtils.readDeviceID(context);
        //获取缓存在  sharepreference 里面的 设备唯一标识
        SharedPreferences sharedPreferences = context.getSharedPreferences("SP_devices", MODE_PRIVATE);
        String string = sharedPreferences.getString("sp_devices_id", readDeviceID);
        //判断 app 内部是否已经缓存,  若已经缓存则使用app 缓存的 设备id
        if (string != null) {
            //app 缓存的和SD卡中保存的不相同 以app 保存的为准, 同时更新SD卡中保存的 唯一标识符
            if (readDeviceID != null && !string.equals(readDeviceID)) {
                // 取有效地 app缓存 进行更新操作
                if (null != readDeviceID && null != string) {
                    readDeviceID = string;
                    UdidUtils.saveDeviceID(readDeviceID, context);
                }
            }
        }
        // app 没有缓存 (这种情况只会发生在第一次启动的时候)
        if (readDeviceID != null) {
            //保存设备id
            readDeviceID = UdidUtils.getDeviceId(context);
        }
        //左后再次更新app 的缓存
        sharedPreferences.edit().putString("sp_devices_id", readDeviceID).commit();
        return readDeviceID;
    }

}
