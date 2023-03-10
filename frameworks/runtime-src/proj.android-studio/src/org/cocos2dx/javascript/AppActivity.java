/****************************************************************************
 Copyright (c) 2015-2016 Chukong Technologies Inc.
 Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

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
package org.cocos2dx.javascript;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mobile.audi.LaunchView;
import com.mobile.audi.MainApp;
import com.mobile.component.base.BaseActivity;
import com.mobile.component.dialog.CommonAlertDialog;
import com.mobile.component.dialog.TipsAlertDialog;
import com.mobile.component.dialog.WebDialog;
import com.mobile.util.AppUtil;
import com.mobile.util.GPSUtils;
import com.mobile.util.ImageUtil;
import com.mobile.util.MediaScanner;
import com.mobile.util.MsgType;
import com.mobile.util.ScreenNotchUtil;
import com.mobile.util.SystemUtil;
import com.mobile.util.Udid;

import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import com.audiGame.teen_patti.R;

public class AppActivity extends BaseActivity {
    public static AppActivity instance = null;

    private double quality;
    private String cipherText;
    private String cipherKey;
    private String playerId;
    private String url;
    private String picSavePath;          //?????????????????????
    private ClipboardManager clipboard;

    private LaunchView mLaunch = null;
    private static final String TAG = AppActivity.class.getName();
    private static final String CHANNEL_TAG = "CHANNEL_TAG";

    public static synchronized AppActivity getInstance() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot())
            return;

        // DO OTHER INITIALIZATION BELOW
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //??????????????????
        SDKWrapper.getInstance().init(this);
        AppActivity.instance = this;

        if (mLaunch == null) {
            mLaunch = new LaunchView(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mFrameLayout.addView(mLaunch, params);
        }

        JSMethodHelper.deviceInfo = AppUtil.getDeviceInfo();
    }

    @Override
    public Cocos2dxGLSurfaceView onCreateView() {
        Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
        // TestCpp should create stencil buffer
        glSurfaceView.setEGLConfigChooser(5, 6, 5, 0, 16, 8);
        SDKWrapper.getInstance().setGLSurfaceView(glSurfaceView, this);

        return glSurfaceView;
    }

    /*********************************************/
    //????????????????????????,????????????sdk=27?????????????????????????????????
    public static boolean shouldRequestPermission() {
        boolean ret = false;
        //Build.VERSION_CODES.M???23??????android 6.0
        Log.d("operateImg", "build version is " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int state = ContextCompat.checkSelfPermission(AppActivity.instance, Manifest.permission.READ_EXTERNAL_STORAGE);
            ret = state != PackageManager.PERMISSION_GRANTED;
        }
        return ret;
    }

    //????????????????????????,?????????????????????????????????????????????????????????
    public static void requestStoragePermission(int code) {
        Log.d("operateImg", "need request permission");
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(AppActivity.instance, perms, code);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MsgType.MsgOpenPhotoLibrary:
            case MsgType.MsgImageUploadForAgent:
            case MsgType.MsgSaveImageToPhotoLibrary:
            case MsgType.MsgGetLocation:
            case MsgType.MsgGetMachineId: {
                if (grantResults.length > 0) {
                    boolean isOk = true;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                            isOk = false;
                    }

                    if (isOk) {
                        if (requestCode == MsgType.MsgOpenPhotoLibrary) {
                            //????????????,?????????0
                            AppActivity.getInstance().openPhotoLibrary();
                        } else if (requestCode == MsgType.MsgImageUploadForAgent) {
                            //????????????,?????????1
                            AppActivity.getInstance().handleImageUpload();
                        } else if (requestCode == MsgType.MsgSaveImageToPhotoLibrary) {
                            //?????????????????????
                            this.saveImageToPhotoLibrary(picSavePath);
                        } else if (requestCode == MsgType.MsgGetMachineId) {
                            //???????????????
                            AppActivity.getInstance().getMachineId();
                        }
                        else if (requestCode == MsgType.MsgGetLocation) {
                            //????????????
                            getLocation(true);
                        }
                    } else {
                        if (requestCode == MsgType.MsgSaveImageToPhotoLibrary) {
                            this.saveImageState("0");
                        }
                        else if (requestCode == MsgType.MsgGetLocation) {
                            getLocation(false);
                        }

                        //??????????????????????????????
                        boolean shouldShow = shouldShowRequestPermissionRationale(permissions[0]);
                        if (shouldShow) {
                            Toast.makeText(AppActivity.this, "Request for authorisation rejected.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            //??????????????????????????????????????????
                            String msg = requestCode == MsgType.MsgGetLocation ? "App want to access your location." : "You are not authorised to use this function.";
                            CommonAlertDialog alertDialog = CommonAlertDialog.newInstance(msg);
                            alertDialog.setOkListener(()-> {
                                Intent intent = new Intent();
                                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            });
                            alertDialog.show();
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void callRuntimeMethod(String message) {
        Log.d("TAG", message);
        JSONTokener jsonParser = new JSONTokener(message);
        try {
            JSONObject jsonobj = (JSONObject) jsonParser.nextValue();
            if (jsonobj.isNull("type"))
                return;

            int type = jsonobj.getInt("type");
            switch (type) {
                case MsgType.MsgShake: {
                    final double duration = jsonobj.getDouble("duration");
                    AppActivity.getInstance().shake(duration);
                    break;
                }
                case MsgType.MsgExitApp: {
                    AppActivity.getInstance().exitApp();
                    break;
                }
                case MsgType.MsgOpenWeChat: {
                    String url = jsonobj.getString("url");
                    //AppActivity.getInstance().openWeChat(url);
                    break;
                }
                case MsgType.MsgShowAlert: {
                    String title = jsonobj.getString("title");
                    String msg = jsonobj.getString("msg");
                    boolean needCancel = jsonobj.getBoolean("needCancel");
                    AppActivity.getInstance().showAlert(title, msg, needCancel);
                    break;
                }
                case MsgType.MsgCopyToClipboard: {
                    String text = jsonobj.getString("text");
                    AppActivity.getInstance().copyToClipboard(text);
                    break;
                }
                case MsgType.MsgOpenPhotoLibrary: {
                    if (shouldRequestPermission()) {
                        requestStoragePermission(MsgType.MsgOpenPhotoLibrary);
                    } else {
                        AppActivity.getInstance().openPhotoLibrary();
                    }
                    break;
                }
                case MsgType.MsgSaveImageToPhotoLibrary: {
                    String path = jsonobj.getString("path");
                    AppActivity.getInstance().picSavePath = path;
                    if (shouldRequestPermission()) {
                        requestStoragePermission(MsgType.MsgSaveImageToPhotoLibrary);
                    } else {
                        AppActivity.getInstance().saveImageToPhotoLibrary(path);
                    }
                    break;
                }
                case MsgType.MsgGetMachineId: {
                    AppActivity.getInstance().getMachineId();
                    break;
                }
                case MsgType.MsgGetVersion: {
                    float[] arr = Cocos2dxHelper.getSafeArea();
                    float f1 = arr[0];
                    AppActivity.getInstance().getVersion();
                    break;
                }
                case MsgType.MsgGetPhoneNumber:
                case MsgType.MsgGetAllContacts:
                case MsgType.MsgShareByType:
                case MsgType.MsgAlipayRecharge: {
                    break;
                }
                case MsgType.MsgGetBatteryStatus: {
                    AppActivity.getInstance().getBatteryStatus();
                    break;
                }
                case MsgType.MsgGetNetworkStatus: {
                    AppActivity.getInstance().getNetworkStatus();
                    break;
                }
                case MsgType.MsgJinChanWebView:
                case MsgType.MsgOpenUrl: {
                    String web_url = jsonobj.getString("web_url");
                    //rotation???1?????? 2?????? 11???????????? 12???????????? 13???????????????
                    int rotation = jsonobj.getInt("rotation");
                    AppActivity.getInstance().openWebView(web_url, type, rotation);
                    break;
                }
                case MsgType.MsgGetTextFromClipboard: {
                    AppActivity.getInstance().getTextFromClipboard();
                    break;
                }

                case MsgType.MsgImageUploadForAgent: {
                    AppActivity.getInstance().url = jsonobj.getString("data");
                    AppActivity.getInstance().quality = jsonobj.getDouble("quality");
                    AppActivity.getInstance().cipherText = jsonobj.getString("cipherText");
                    AppActivity.getInstance().cipherKey = jsonobj.getString("cipherKey");
                    AppActivity.getInstance().playerId = jsonobj.getString("playerId");

                    if (shouldRequestPermission()) {
                        requestStoragePermission(MsgType.MsgImageUploadForAgent);
                    } else {
                        AppActivity.getInstance().handleImageUpload();
                    }
                    break;
                }
                case MsgType.MsgCloseWebView: {
                    AppActivity.getInstance().closeWebViewFromJS();
                    break;
                }
                case MsgType.MsgChangeScreenRotation: {
                    //???????????? 1?????? 2??????
                    int direction = jsonobj.getInt("rotation");
                    AppActivity.getInstance().changeScreenRotation(direction);
                    break;
                }
                case MsgType.MsgCloseLaunchView: {
                    int screenshot = 0;
                    if (jsonobj.has("screenshot"))
                        screenshot = jsonobj.getInt("screenshot");

                    AppActivity.getInstance().closeLaunchView(screenshot);
                    break;

                }
                case MsgType.MsgOpenApp: {
                    // {type: 23, pkg_name: "com.tencent.mm", app_name: "??????QQ"}
                    String pkg_name = jsonobj.getString("pkg_name");
                    String tip_msg = jsonobj.getString("app_name") + " not installed!";
                    Boolean installed = AppUtil.openApp(pkg_name);
                    if (!installed)
                        Toast.makeText(AppActivity.getContext(), tip_msg, Toast.LENGTH_LONG);

                    // ??????json??????: {type: 23, installed: 0}
                    int result = installed ? 1 : 0;
                    String str = String.format("{\"type\": %d, \"installed\": %d}", MsgType.MsgOpenApp, result);
                    AppActivity.getInstance().sendMessageToJS(str);
                    break;
                }
                case MsgType.MsgTrackEvent: {
                    String eventToken = jsonobj.getString("eventToken");
                    double revenue = jsonobj.has("revenue") ? jsonobj.getDouble("revenue") : .0f;
                    String currency = jsonobj.has("currency") ? jsonobj.getString("currency") : "";
                    MainApp.trackEvent(eventToken, revenue, currency);
                    break;
                }
                case MsgType.MsgGetLocation: {
                    if (AppActivity.getInstance().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        AppActivity.getInstance().requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MsgType.MsgGetLocation);
                    else
                        AppActivity.getInstance().getLocation(true);
                    break;
                }
                default:
                    break;
            }
        } catch (JSONException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    //??????????????????
    public void changeScreenRotation(int direction) {
        if (direction == 1) {
            changeScreenLandScape();
        } else if (direction == 2) {
            changeScreenPortrait();
        }

        String str = String.format("{\"type\": %d, \"rotation\": %d}", MsgType.MsgChangeScreenRotation, direction);
        sendMessageToJS(str);
    }

    //???JS???????????????????????????WebView
    public void closeWebViewFromJS() {
        Runnable runnable = () -> {
            WebDialog.getInstance().closeWebView(false);
            String str = String.format("{\"type\": %d}", MsgType.MsgCloseWebView);
            sendMessageToJS(str);
            //????????????
            changeScreenLandScape();
        };
        runOnUiThread(runnable);
    }

    //??????
    private void shake(final double duration) {
        //?????????????????????
        new Thread(() -> {
            final double time = duration < 10 ? duration * 1000 : duration;
            Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate((long) time);
        }).start();
    }

    //??????App
    public void exitApp() {
        finish();
        System.exit(0);
    }

    private void showAlert(final String title, final String msg, final boolean needCancel) {
        this.runOnUiThread(() -> {
            Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            //??????json??????: {"type": 4, "way": 1} way: 0 ?????? 1 ??????
            if (needCancel) {
                CommonAlertDialog alertDialog = CommonAlertDialog.newInstance(msg);
                alertDialog.setTitle(title);
                alertDialog.setCancelListener(()->{
                    int way = 0;
                    String str = String.format("{\"type\": %d, \"way\": %d}", MsgType.MsgShowAlert, way);
                    sendMessageToJS(str);
                });
                alertDialog.setOkListener(()->{
                    int way = 1;
                    String str = String.format("{\"type\": %d, \"way\": %d}", MsgType.MsgShowAlert, way);
                    sendMessageToJS(str);
                });
                alertDialog.show();
            }
            else {
                TipsAlertDialog tipsAlertDialog = TipsAlertDialog.newInstance(msg);
                tipsAlertDialog.setTitle(title);
                tipsAlertDialog.setOkListener(()->{
                    int way = 1;
                    String str = String.format("{\"type\": %d, \"way\": %d}", MsgType.MsgShowAlert, way);
                    sendMessageToJS(str);
                });
                tipsAlertDialog.show();
            }
        });
    }

    //?????????
    private void copyToClipboard(String msg) {
        clipboard.setPrimaryClip(ClipData.newPlainText(null, msg));
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null)
            return;

        Uri uri = data.getData();
        String[] filePathColumns = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, filePathColumns, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumns[0]);
        String imagePath = cursor.getString(columnIndex);
        Log.d(TAG, "img path is " + imagePath);

        String temp = imagePath.toLowerCase();
        if (temp.indexOf(".png") < 0 && temp.indexOf(".jpg") < 0 && temp.indexOf(".jpeg") < 0)
            return;

        File jpgFile = getApplicationContext().getFilesDir();
        try {
            jpgFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String jpgPath = jpgFile.toString() + "/image2H5.jpg";
        ImageUtil.CopyImageToJPG(imagePath, jpgPath);

        if (requestCode == 0) {  //??????????????????
            //???????????????????????????json??????: {"type": 6, "path": ""}
            String str = String.format("{\"type\": %d, \"path\": \"%s\"}", MsgType.MsgOpenPhotoLibrary, jpgPath);
            sendMessageToJS(str);
        } else if (requestCode == 1) {   //??????????????????
            Log.d(TAG, String.format("cipherText %s,cipherKey %s,playerId %s", cipherText, cipherKey, playerId));
            ImageUtil.uploadFile(jpgPath, quality, url, cipherText, cipherKey, playerId);
        }
    }

    //????????????
    private void openPhotoLibrary() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 0);
    }

    //?????????????????????
    private void saveImageToPhotoLibrary(String path) {
        int index = path.lastIndexOf(".png");
        String jpg_file = path.substring(0, index) + ".jpg";
        Bitmap bitmap_png = BitmapFactory.decodeFile(path);
        if (bitmap_png == null)
            return;

        ImageUtil.SaveImageToJPG(bitmap_png, jpg_file);
        //??????jpg?????????
        Bitmap bitmap_jpg = BitmapFactory.decodeFile(jpg_file);

        if (Build.VERSION.SDK_INT < 29) {
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap_jpg, "", "imageSaveToPhotoLibrary");
                MediaScanner msc = new MediaScanner(getApplicationContext());
                msc.scanFile(new File(jpg_file), "image/jpeg");
                this.saveImageState("1");
            } catch (Exception e) {
                e.printStackTrace();
                this.saveImageState("0");
            }
        }
        else {
            Uri insertUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            //??????use?????????????????????
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(insertUri, "rw");
                if (bitmap_jpg.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)) {
                    this.saveImageState("1");
                } else {
                    this.saveImageState("0");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                this.saveImageState("0");
            }
        }
    }

    //???????????????????????????
    private void saveImageState(String result) {
        Log.d(TAG, "saveImageState is " + result);
        String str = String.format("{\"type\": %d, \"result\": \"%s\"}", MsgType.MsgSaveImageToPhotoLibrary, result);
        sendMessageToJS(str);
    }

    //????????????ID
    private void getMachineId() {
        //????????????id,????????????
        String machineId = Udid.getDevicesId(AppActivity.getInstance());
        Log.d(TAG, "machineId is " + machineId);
        String str = String.format("{\"type\": %d, \"machineId\": \"%s\"}", MsgType.MsgGetMachineId, machineId);
        sendMessageToJS(str);
    }

    private void getLocation(boolean permissionAllowed) {
        if (permissionAllowed) {
            String location = GPSUtils.getInstance().getLocation();
            Log.d(TAG, "location is " + location);
            double latitude = GPSUtils.getInstance().getLatitude();
            double longitude = GPSUtils.getInstance().getLongitude();
            int permission = 1;
            String str = String.format("{\"type\": %d, \"permission\": %d, \"location\": \"%s\", \"latitude\": %f, \"longitude\": %f}", MsgType.MsgGetLocation,
                    permission, location, latitude, longitude);
            sendMessageToJS(str);
        }
        else {
            Log.d(TAG, "location permission not allowed");
            int permission = 0;
            String str = String.format("{\"type\": %d, \"permission\": %d}", MsgType.MsgGetLocation, permission);
            sendMessageToJS(str);
        }
    }

    //???????????????
    private void closeLaunchView(int screenshot) throws PackageManager.NameNotFoundException {
        int online = JSMethodHelper.isOnlineApp() ? 1 : 0; //?????????: 0, ?????????: 1
        Log.d(TAG, "online is: " + online);
        //???????????????
        Context context = getApplicationContext();
        String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        //???????????????
        String channelId = getResources().getString(R.string.channelId);
        if (channelId == null || channelId == "") {
            channelId = "1_1_0_44";
        }
        //??????
        String bundleId = context.getPackageName();
        //?????????
        String appName  = AppUtil.getAppName();

        String str = String.format("{\"type\": %d, \"online\": %d, \"version\": \"%s\", \"channelId\": \"%s\", \"bundleId\": \"%s\", \"appName\": \"%s\"}",
                MsgType.MsgCloseLaunchView, online, version, channelId, bundleId, appName);
        sendMessageToJS(str);

        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                if (screenshot == 1) {
                    // ????????????
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }

                if (mLaunch != null && mLaunch.isAttachedToWindow())
                    mLaunch.removeWithAction();
            }
        });
    }

    //??????App?????????
    private void getVersion() {
        try {
            //???????????????
            Context context = getApplicationContext();
            String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            //???????????????
            String channelId = getResources().getString(R.string.channelId);
            if (channelId == null || channelId == "") {
                channelId = "1_1_0_44";
            }
            Log.d(CHANNEL_TAG, "default channelId is " + channelId);

//            String comment = readApkComment();
//            //??????????????????  ????????????_?????????_?????????,??????1_1_0_1
//            if (comment != null && comment != "") {
//                Log.d(CHANNEL_TAG, "new comment id is " + comment);
//                String[] cmtArray = comment.split("_");
//                if (cmtArray.length == 4) {
//                    //?????????????????????????????????PHP
//                    channelId = comment;
//                }
//            }
//            Log.d(CHANNEL_TAG, "new channelId is " + channelId);

            //?????????????????????
            int simulator = 0;   //0?????? 1?????????  //coolpad 5370
            boolean emu = AppUtil.checkEmulator();
            simulator = emu ? 1 : 0;

            String phone_os = SystemUtil.getSystemModel();
            //????????????
            String brandName = SystemUtil.getDeviceBrand();
            //??????????????????
            int notchHeight = ScreenNotchUtil.getNotchSize();
            //machineId
            String machineId = Udid.getDevicesId(AppActivity.getInstance());
            //??????
            String bundleId = context.getPackageName();
            //androidId
            String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            //googleAdId
            String googleAdId = JSMethodHelper.getGoogleAdId();
            //?????????
            String appName  = AppUtil.getAppName();
            //?????????
            String inviteCode = getResources().getString(R.string.inviteCode);

            String formatStr = "{\"type\":%d,\"version\":\"%s\",\"agent\":\"%s\",\"phone_os\":\"%s\",\"simulator\":%d,\"brandName\":\"%s\",\"height\":%d,\"machineId\":\"%s\",\"bundleId\":\"%s\",\"androidId\":\"%s\",\"googleAdId\":\"%s\",\"appName\":\"%s\", \"inviteCode\": \"%s\"}";
            String str = String.format(formatStr,MsgType.MsgGetVersion, version, channelId, phone_os, simulator,brandName,notchHeight,machineId,bundleId,androidId,googleAdId,appName,inviteCode);
            sendMessageToJS(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //??????????????????
    private void getBatteryStatus() {
        Intent intent = registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int rawlevel = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", -1);
        int status = intent.getIntExtra("status", -1);
        double level = -1;
        if(rawlevel >= 0 && scale > 0)
            level = (rawlevel * 1.0) / scale;

        int state = 1;
        if(status == BatteryManager.BATTERY_STATUS_UNKNOWN) {
            //??????????????????
            state = 0;
        }
        else if(status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
            //????????????
            state = 1;
        }
        else if(status == BatteryManager.BATTERY_STATUS_CHARGING) {
            //????????????
            state = 2;
        }
        else if(status == BatteryManager.BATTERY_STATUS_FULL) {
            //????????????
            state = 3;
        }

        JSONObject obj = new JSONObject();
        try {
            obj.put("level", level);
            obj.put("state", state);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String str = String.format("{\"type\": %d, \"data\": %s}", MsgType.MsgGetBatteryStatus, obj.toString());
        sendMessageToJS(str);
    }

    //??????????????????
    private void getNetworkStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        int netStatus;
        if(info == null || !info.isConnected())
            netStatus = 0;
        else if(info != null && info.getType() == ConnectivityManager.TYPE_WIFI)
            netStatus = 1;
        else
            netStatus = 2;

        String str = String.format("{\"type\": %d, \"netstatus\": %d}", MsgType.MsgGetNetworkStatus, netStatus);
        sendMessageToJS(str);
    }

    //????????????????????????
    private void handleImageUpload() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 1);
    }

    //?????????????????????
    private void getTextFromClipboard()
    {
        String  text = "";
        //??????android 7.0????????????
//        Looper.prepare();
//        ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData mClipData = clipboard.getPrimaryClip();
        if ( mClipData != null  ) {
            if ( mClipData.getItemAt(0) != null ) {
                text = mClipData.getItemAt(0).getText().toString();
            }
        }
        String str = String.format("{\"type\": %d, \"text\": \"%s\"}", MsgType.MsgGetTextFromClipboard,text);
        sendMessageToJS(str);
    }

    //??????webView
    public void openWebView( String url,int msgType,int rotation) {
        if (rotation == 1 || rotation == 13) //??????????????????
            changeScreenPortrait();

        Bundle bd = new Bundle();
        bd.putString("url", url);
        bd.putInt("msgType",msgType);
        bd.putInt("rotation",rotation);

        WebDialog.getInstance().init(bd);
        WebDialog.getInstance().show(getFragmentManager(),"web_dialog");
    }

    //??????webview
    public void closeWebView(int msgType,int rotation) {
        if (rotation == 1 || rotation == 13) //???????????????
            changeScreenLandScape();

        String str = String.format("{\"type\": %d, \"rotation\": %d}", msgType, rotation);
        sendMessageToJS( str );
    }

    //????????????
    private void changeScreenLandScape(  ) {
        Configuration mConfiguration = this.getResources().getConfiguration(); //???????????????????????????
        int ori = mConfiguration.orientation; //??????????????????
        if (ori == mConfiguration.ORIENTATION_PORTRAIT) { //??????
            Log.i("cocosxx: ", "???????????????");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//???????????????
        } else {
            Log.i( "cocosx: ", "???????????????");
        }
    }

    //????????????
    public void changeScreenPortrait(  ) {
        Configuration mConfiguration = this.getResources().getConfiguration(); //???????????????????????????
        int ori = mConfiguration.orientation; //??????????????????
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) { //??????
            Log.i("cocosxx: ", "???????????????");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//???????????????
        }
    }

    //????????????
    public String readApkComment(){
        File file = new File(instance.getPackageCodePath());
        RandomAccessFile accessFile = null;
        byte[] bytes = null;
        try {
            accessFile = new RandomAccessFile(file, "r");
            long index = accessFile.length();

            bytes = new byte[4];
            index = index - bytes.length;
            accessFile.seek(index);
            accessFile.readFully(bytes);

            int contentLen = Integer.parseInt(new String(bytes, "utf-8"));

            index = index - contentLen;
            accessFile.seek(index);
            bytes = new byte[contentLen];
            accessFile.readFully(bytes);
            return new String(bytes, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(CHANNEL_TAG, "apk not write Comment!!!!!");
        }finally{
            if(accessFile!=null){
                try{
                    accessFile.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
}
