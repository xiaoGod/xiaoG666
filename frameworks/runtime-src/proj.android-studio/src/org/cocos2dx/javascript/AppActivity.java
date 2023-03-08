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

import com.audiGame.teen_patti.BuildConfig;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
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
    private String picSavePath;          //图片的保存路径
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //保持窗口常亮
        SDKWrapper.getInstance().init(this);
        AppActivity.instance = this;
        if (!BuildConfig.DEBUG){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }


        if (mLaunch == null) {
            mLaunch = new LaunchView(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mFrameLayout.addView(mLaunch, params);
        }
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
    //是否需要申请权限,目前使用sdk=27发布，需要代码检测权限
    public static boolean shouldRequestPermission() {
        boolean ret = false;
        //Build.VERSION_CODES.M为23，即android 6.0
        Log.d("operateImg", "build version is " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int state = ContextCompat.checkSelfPermission(AppActivity.instance, Manifest.permission.READ_EXTERNAL_STORAGE);
            ret = state != PackageManager.PERMISSION_GRANTED;
        }
        return ret;
    }

    //动态申请存储权限,相关代码先不要删除，等待版本多次迭代。
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
                            //打开相册,类型为0
                            AppActivity.getInstance().openPhotoLibrary();
                        } else if (requestCode == MsgType.MsgImageUploadForAgent) {
                            //打开相册,类型为1
                            AppActivity.getInstance().handleImageUpload();
                        } else if (requestCode == MsgType.MsgSaveImageToPhotoLibrary) {
                            //存储图片到相册
                            this.saveImageToPhotoLibrary(picSavePath);
                        } else if (requestCode == MsgType.MsgGetMachineId) {
                            //获取机器码
                            AppActivity.getInstance().getMachineId();
                        }
                        else if (requestCode == MsgType.MsgGetLocation) {
                            //获取位置
                            getLocation(true);
                        }
                    } else {
                        if (requestCode == MsgType.MsgSaveImageToPhotoLibrary) {
                            this.saveImageState("0");
                        }
                        else if (requestCode == MsgType.MsgGetLocation) {
                            getLocation(false);
                        }

                        //引导用户设置开启权限
                        boolean shouldShow = shouldShowRequestPermissionRationale(permissions[0]);
                        if (shouldShow) {
                            Toast.makeText(AppActivity.this, "Request for authorisation rejected.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            //当用户选择不再询问，引导开启
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
                    //rotation：1竖屏 2横屏 11全民推广 12视讯横屏 13竖向的全屏
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
                    //旋转屏幕 1横屏 2竖屏
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
                    // {type: 23, pkg_name: "com.tencent.mm", app_name: "腾讯QQ"}
                    String pkg_name = jsonobj.getString("pkg_name");
                    String tip_msg = jsonobj.getString("app_name") + " not installed!";
                    Boolean installed = AppUtil.openApp(pkg_name);
                    if (!installed)
                        Toast.makeText(AppActivity.getContext(), tip_msg, Toast.LENGTH_LONG);

                    // 返回json格式: {type: 23, installed: 0}
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

    //调整屏幕方向
    public void changeScreenRotation(int direction) {
        if (direction == 1) {
            changeScreenLandScape();
        } else if (direction == 2) {
            changeScreenPortrait();
        }

        String str = String.format("{\"type\": %d, \"rotation\": %d}", MsgType.MsgChangeScreenRotation, direction);
        sendMessageToJS(str);
    }

    //从JS层发消息，主动关闭WebView
    public void closeWebViewFromJS() {
        Runnable runnable = () -> {
            WebDialog.getInstance().closeWebView(false);
            String str = String.format("{\"type\": %d}", MsgType.MsgCloseWebView);
            sendMessageToJS(str);
            //强制横屏
            changeScreenLandScape();
        };
        runOnUiThread(runnable);
    }

    //震动
    private void shake(final double duration) {
        //时间单位是毫秒
        new Thread(() -> {
            final double time = duration < 10 ? duration * 1000 : duration;
            Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate((long) time);
        }).start();
    }

    //退出App
    public void exitApp() {
        finish();
        System.exit(0);
    }

    private void showAlert(final String title, final String msg, final boolean needCancel) {
        this.runOnUiThread(() -> {
            Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            //返回json格式: {"type": 4, "way": 1} way: 0 取消 1 确定
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

    //剪贴板
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

        if (requestCode == 0) {  //获取相册照片
            //获取相册照片完成，json格式: {"type": 6, "path": ""}
            String str = String.format("{\"type\": %d, \"path\": \"%s\"}", MsgType.MsgOpenPhotoLibrary, jpgPath);
            sendMessageToJS(str);
        } else if (requestCode == 1) {   //代理图片上传
            Log.d(TAG, String.format("cipherText %s,cipherKey %s,playerId %s", cipherText, cipherKey, playerId));
            ImageUtil.uploadFile(jpgPath, quality, url, cipherText, cipherKey, playerId);
        }
    }

    //打开相册
    private void openPhotoLibrary() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 0);
    }

    //保存图片至相册
    private void saveImageToPhotoLibrary(String path) {
        int index = path.lastIndexOf(".png");
        String jpg_file = path.substring(0, index) + ".jpg";
        Bitmap bitmap_png = BitmapFactory.decodeFile(path);
        if (bitmap_png == null)
            return;

        ImageUtil.SaveImageToJPG(bitmap_png, jpg_file);
        //复制jpg至相册
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
            //使用use可以自动关闭流
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

    //保存图片至相册结果
    private void saveImageState(String result) {
        Log.d(TAG, "saveImageState is " + result);
        String str = String.format("{\"type\": %d, \"result\": \"%s\"}", MsgType.MsgSaveImageToPhotoLibrary, result);
        sendMessageToJS(str);
    }

    //获取机器ID
    private void getMachineId() {
        //获取设备id,需要权限
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

    //关闭启动图
    private void closeLaunchView(int screenshot) throws PackageManager.NameNotFoundException {
        int online = JSMethodHelper.isOnlineApp() ? 1 : 0; //渠道包: 0, 商店包: 1
        Log.d(TAG, "online is: " + online);
        //获取版本号
        Context context = getApplicationContext();
        String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        //获取渠道号
        String channelId = getResources().getString(R.string.channelId);
        if (channelId == null || channelId == "") {
            channelId = "1_1_0_44";
        }
        //包号
        String bundleId = context.getPackageName();
        //应用名
        String appName  = AppUtil.getAppName();

        String str = String.format("{\"type\": %d, \"online\": %d, \"version\": \"%s\", \"channelId\": \"%s\", \"bundleId\": \"%s\", \"appName\": \"%s\"}",
                MsgType.MsgCloseLaunchView, online, version, channelId, bundleId, appName);
        sendMessageToJS(str);

        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                if (screenshot == 1&& !BuildConfig.DEBUG) {
                    // 禁止截屏
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }

                if (mLaunch != null && mLaunch.isAttachedToWindow())
                    mLaunch.removeWithAction();
            }
        });
    }

    //获取App版本号
    private void getVersion() {
        try {
            //获取版本号
            Context context = getApplicationContext();
            String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            //获取渠道号
            String channelId = getResources().getString(R.string.channelId);
            if (channelId == null || channelId == "") {
                channelId = "1_1_0_44";
            }
            Log.d(CHANNEL_TAG, "default channelId is " + channelId);

//            String comment = readApkComment();
//            //新的数据格式  推广类型_渠道号_推广者,例如1_1_0_1
//            if (comment != null && comment != "") {
//                Log.d(CHANNEL_TAG, "new comment id is " + comment);
//                String[] cmtArray = comment.split("_");
//                if (cmtArray.length == 4) {
//                    //新的格式，包信息全来自PHP
//                    channelId = comment;
//                }
//            }
//            Log.d(CHANNEL_TAG, "new channelId is " + channelId);

            //获取是否模拟器
            int simulator = 0;   //0真机 1模拟器  //coolpad 5370
            boolean emu = AppUtil.checkEmulator();
            simulator = emu ? 1 : 0;

            String phone_os = SystemUtil.getSystemModel();
            //品牌型号
            String brandName = SystemUtil.getDeviceBrand();
            //刘海屏的高度
            int notchHeight = ScreenNotchUtil.getNotchSize();
            //machineId
            String machineId = Udid.getDevicesId(AppActivity.getInstance());
            //包号
            String bundleId = context.getPackageName();
            //androidId
            String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            //googleAdId
            String googleAdId = JSMethodHelper.getGoogleAdId();
            //应用名
            String appName  = AppUtil.getAppName();
            //邀请码
            String inviteCode = getResources().getString(R.string.inviteCode);

            String formatStr = "{\"type\":%d,\"version\":\"%s\",\"agent\":\"%s\",\"phone_os\":\"%s\",\"simulator\":%d,\"brandName\":\"%s\",\"height\":%d,\"machineId\":\"%s\",\"bundleId\":\"%s\",\"androidId\":\"%s\",\"googleAdId\":\"%s\",\"appName\":\"%s\", \"inviteCode\": \"%s\"}";
            String str = String.format(formatStr,MsgType.MsgGetVersion, version, channelId, phone_os, simulator,brandName,notchHeight,machineId,bundleId,androidId,googleAdId,appName,inviteCode);
            sendMessageToJS(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取电池状态
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
            //没有安装电池
            state = 0;
        }
        else if(status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
            //没有充电
            state = 1;
        }
        else if(status == BatteryManager.BATTERY_STATUS_CHARGING) {
            //正在充电
            state = 2;
        }
        else if(status == BatteryManager.BATTERY_STATUS_FULL) {
            //已经充满
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

    //获取网络状态
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

    //代理充值图片上传
    private void handleImageUpload() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 1);
    }

    //获取剪贴板文本
    private void getTextFromClipboard()
    {
        String  text = "";
        //避免android 7.0系统报错
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

    //打开webView
    public void openWebView( String url,int msgType,int rotation) {
        if (rotation == 1 || rotation == 13) //显示竖屏提示
            changeScreenPortrait();

        Bundle bd = new Bundle();
        bd.putString("url", url);
        bd.putInt("msgType",msgType);
        bd.putInt("rotation",rotation);

        WebDialog.getInstance().init(bd);
        WebDialog.getInstance().show(getFragmentManager(),"web_dialog");
    }

    //关闭webview
    public void closeWebView(int msgType,int rotation) {
        if (rotation == 1 || rotation == 13) //旋转成横屏
            changeScreenLandScape();

        String str = String.format("{\"type\": %d, \"rotation\": %d}", msgType, rotation);
        sendMessageToJS( str );
    }

    //切换横屏
    private void changeScreenLandScape(  ) {
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == mConfiguration.ORIENTATION_PORTRAIT) { //竖屏
            Log.i("cocosxx: ", "当前为横屏");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        } else {
            Log.i( "cocosx: ", "当前为竖屏");
        }
    }

    //切换竖屏
    public void changeScreenPortrait(  ) {
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) { //横屏
            Log.i("cocosxx: ", "当前为竖屏");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制为竖屏
        }
    }

    //获取代理
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
