package com.mobile.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;

import org.cocos2dx.javascript.AppActivity;

/**
 * Created by Administrator on 2018/4/17.
 * 获取用户的地理位置
 */
public class GPSUtils {

    private static GPSUtils instance;
    private LocationManager locationManager;
    private Location location = null;

    public static GPSUtils getInstance() {
        if (instance == null) {
            instance = new GPSUtils();
            instance.init();
        }
        return instance;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void init() {
        locationManager = (LocationManager) AppActivity.instance.getSystemService(Context.LOCATION_SERVICE); // 默认Android GPS定位实例
        // 是否已经授权
        if (AppActivity.instance.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //判断GPS是否开启，没有开启，则开启
//            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//                //跳转到手机打开GPS页面
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                //设置完成后返回原来的界面
//                AppActivity.instance.startActivityForResult(intent,OPEN_GPS_CODE);
//            }
//
//            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);      // GPS芯片定位 需要开启GPS
//            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);      // 利用网络定位 需要开启GPS
//            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            List<String> allProviders = locationManager.getProviders(true);
            if (allProviders.contains(LocationManager.NETWORK_PROVIDER))
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // 利用网络定位 需要开启GPS
        }
    }

    public String getLocation() {
        String p = "";
        if(location != null) {
            Log.i("GPS: ","经度：" + location.getLatitude());
            Log.i("GPS: ","纬度：" + location.getLongitude());

            // 获取地址信息
            p = getAddress(location.getLatitude(), location.getLongitude());
//            p = getAddress(19.069948, 72.830578);
        }
        else {
            Log.i("GPS: ", "获取位置信息失败，请检查是够开启GPS,是否授权");
        }

        return p;
    }

    /*
     * 根据经度纬度 获取国家，省份
     * */
    public String getAddress(double latitude, double longitude) {
        String cityName = "";
        List<Address> addList = null;
        Geocoder ge = new Geocoder(AppActivity.instance);

        try {
            addList = ge.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addList != null && addList.size() > 0) {
            for (int i = 0; i < addList.size(); i++) {
                Address ad = addList.get(i);
                cityName += ad.getCountryCode() + "-" + ad.getCountryName() + "-" + ad.getLocality();
            }
        }
        return cityName;
    }

    public double getLatitude() {
        if (location == null)
            return 0;

        return location.getLatitude();
    }

    public double getLongitude() {
        if (location == null)
            return 0;

        return location.getLongitude();
    }
}
