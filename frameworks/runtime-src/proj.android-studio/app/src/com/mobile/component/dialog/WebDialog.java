package com.mobile.component.dialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.cocos2dx.javascript.AppActivity;
import com.mobile.audi.MainApp;
import com.mobile.util.MsgType;
import com.mobile.util.ScreenNotchUtil;
import com.mobile.util.ScreenUtil;
import com.audiGame.teen_patti.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

public class WebDialog extends DialogFragment implements View.OnClickListener, View.OnTouchListener {
    private static final String TAG = WebDialog.class.getName();
    private WebView mWebView;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private Uri contentUri;

    //JS ????????????????????????
    int msgType;
    // 1??????(???????????????) 13???????????????(??????????????????)
    // 2??????(???????????????) 11????????????(??????) 12????????????(???????????????)
    public int rotation;
    //???????????????
    private ImageButton btnHome = null;
    //????????????????????????
    private RelativeLayout fullScreen;
    // progressbar
    private ProgressBar progressBar;
    //??????????????????
    private float touchX,touchY,viewTouchPosX,viewTouchPosY;
    //??????????????????????????????????????????,
    private float screenWidth,screenHeight,statusHeight;
    // ????????????
    private float noTouchWidth = 0;
    //?????????????????????
    private long lastClickTime = 0;
    //webview??????
    private static WebDialog instance = null;
    //url
    String url;

    public static WebDialog getInstance() {
        if (instance == null)
            instance = new WebDialog();

        return instance;
    }

    //???????????????
    public void init(Bundle bundle) {
        instance.url        = bundle.getString("url");
        instance.msgType    = bundle.getInt("msgType");
        instance.rotation   = bundle.getInt("rotation");
        Log.d(TAG,"url is " + instance.url);
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (rotation == 1 || rotation == 13) // ??????
            setStyle(DialogFragment.STYLE_NO_FRAME, R.style.web_dialog_portrait);
        else
            setStyle(DialogFragment.STYLE_NO_FRAME, R.style.web_dialog_landscape);

        Activity activity = getActivity();
        screenWidth   = ScreenUtil.getInstance().getScreenWidth(activity);
        String branchName = ScreenNotchUtil.getBrandName();

        noTouchWidth = ScreenNotchUtil.getNotchSize();
        Log.d(TAG,"notouchsize is " + noTouchWidth + "screenWidth is " + screenWidth);
        screenWidth = screenWidth - noTouchWidth;

        screenHeight  = ScreenUtil.getInstance().getScreenHeight(activity);
        statusHeight  = ScreenUtil.getInstance().getStatusBarHeight(activity);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        //?????????webview
        if (rotation == 2 || rotation == 12) {
            view         = inflater.inflate(R.layout.dialog_web2, container);
            mWebView = view.findViewById(R.id.webview);
            btnHome     = view.findViewById(R.id.btnBackHome);
            btnHome.setOnTouchListener(this);
            progressBar  = view.findViewById(R.id.id_progressBar2);
            progressBar.setVisibility(View.GONE);

            if (rotation == 2) //??????????????????
                btnHome.setVisibility(View.GONE);

            //???????????????????????????????????????
            final View decorView = getDialog().getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        //???????????????????????????
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        //??????
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        //???????????????
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                uiOptions |= 0x00001000;
                decorView.setSystemUiVisibility(uiOptions);
            });

            getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getDialog().getWindow().getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // ???????????????????????????
                WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                getDialog().getWindow().setAttributes(lp);
                // ????????????????????????
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        //??????
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        //???????????????
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            }
            getDialog().getWindow().setAttributes(layoutParams);
        }
        else {
            //?????????webview
            view    = inflater.inflate(R.layout.dialog_web, container);
            mWebView = view.findViewById(R.id.webview);
            progressBar  = view.findViewById(R.id.id_progressBar);
            progressBar.setVisibility(View.GONE);

            //?????????????????????
            View topView = view.findViewById(R.id.topLayout);
            if(rotation == 13) //??????????????????????????????
                topView.setVisibility(View.GONE);
            else {
                topView.setVisibility(View.VISIBLE);
                ImageButton btnClose = view.findViewById(R.id.btnClose);
                btnClose.setOnClickListener(this);
                ImageButton btnOpenBrowser = view.findViewById(R.id.btnOpenBrowser);
                btnOpenBrowser.setOnClickListener(this);
            }

            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getDialog().getWindow().setStatusBarColor(Color.WHITE);
            getDialog().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_VISIBLE);

            //?????????APPName
            TextView txtTitle = view.findViewById(R.id.textView);
            ApplicationInfo aInfo = getActivity().getApplicationInfo();
            String appName = getString(aInfo.labelRes);
            txtTitle.setText(appName);
        }

        getDialog().setOnKeyListener((DialogInterface dialog, int keyCode, KeyEvent event)->{
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                closeWebAndRotation();

            return true;
        });

        return view;
    }

    //????????????????????????????????????
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                viewTouchPosX = view.getX();
                viewTouchPosY = view.getY();
                //?????????layout??????
                ViewGroup.LayoutParams params = view.getLayoutParams();
                view.setLayoutParams(new RelativeLayout.LayoutParams(params.width, params.height));
                view.setTranslationX(viewTouchPosX);
                view.setTranslationY(viewTouchPosY);

                touchX = event.getX();
                touchY = event.getY();
                //Log.d(TAG,"???????????????" + event.getX() + "," + event.getY());
            } break;
            case MotionEvent.ACTION_MOVE: {
                float curPosX = view.getX() + event.getX() - touchX;
                if (curPosX >= 0 && curPosX < screenWidth)
                    view.setTranslationX(curPosX);

                float curPosY = view.getY() + event.getY() - touchY;
                if (curPosY >= 0 && curPosY < screenHeight - statusHeight)
                    view.setTranslationY(curPosY);
                //Log.d(TAG,"???????????????" + view.getX() + "," + view.getY() + " screenWidth " + screenWidth + " screenHeigth " + screenHeight);
            } break;
            case MotionEvent.ACTION_UP: {
                float viewWidth = view.getWidth();
                float endPosX = view.getX();
                float endPosY = view.getY();

                Log.d(TAG, "viewwidth is " + viewWidth + " screenWidth is " + screenWidth);
                //??????????????????3?????????????????????
                if (Math.abs(endPosX - viewTouchPosX) >= 2 || Math.abs(endPosY - viewTouchPosY) >= 2) {
                    Log.d(TAG, "ACTION_UP move ");
                    if (endPosX <= screenWidth / 2)
                        view.setTranslationX(0 + noTouchWidth);
                    else {
                        float changDisX = viewWidth / 2;
                        view.setTranslationX(screenWidth - changDisX);
                        //Log.d(TAG,"viewwidth is " + (screenWidth - changDisX));
                    }
                } else {
                    //????????????2????????????????????????
                    Log.d(TAG, "ACTION_UP click");
                    onClick(btnHome);
                }
            } break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - lastClickTime <= 1000)
            return;

        lastClickTime = nowTime;
        if (view.getId() == R.id.btnClose) {
            Log.d(this.TAG,"btnClick close");
            closeWebAndRotation();
        }
        else if (view.getId() == R.id.btnOpenBrowser) {
            Log.d(this.TAG,"btnClick openBrowser");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
        else if (view.getId() == R.id.btnBackHome) {
            Log.d(this.TAG,"btnClick home back");
            closeWebAndRotation();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(msgType == MsgType.MsgOpenUrl) {
            //???webview??????????????????
            Log.d(TAG," MsgType.MsgOpenUrl open url  is  " + url);
            WebSettings settings= mWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setSupportMultipleWindows(true);
            settings.setDomStorageEnabled(true);
            settings.setLoadWithOverviewMode(true);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);

            // ?????????js?????????????????????js??????
            mWebView.addJavascriptInterface(this, "messageHandler");

            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view,String url) {
                    //???????????????????????????
                    if (!TextUtils.isEmpty(url) && !url.startsWith("http")) {
                        try {
                            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return true;
                        }
                    }
                    //??????return true?????????webview????????????return super...?????????????????????
                    view.loadUrl(url);
                    return super.shouldOverrideUrlLoading(view,url);
                };
            });

            //return true??????????????????url??????
            mWebView.setWebChromeClient(new WebChromeClient() {
                // For Android < 3.0
                public void openFileChooser(ValueCallback<Uri> valueCallback) {
                    uploadMessage = valueCallback;
                    openImageChooserActivity();
                }

                // For Android  >= 3.0
                public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                    uploadMessage = valueCallback;
                    openImageChooserActivity();
                }

                //For Android  >= 4.1
                public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                    uploadMessage = valueCallback;
                    openImageChooserActivity();
                }

                //For Android >= 5.0
                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    uploadMessageAboveL = filePathCallback;
                    openImageChooserActivity();
                    return true;
                }

                @Override
                public void onCloseWindow(WebView window) {
                    super.onCloseWindow(window);
                }

                //@Override
                public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setPositiveButton("Confirm", (dialog, which) -> result.confirm());
                    builder.setMessage(message);
                    builder.setCancelable(false);
                    builder.create().show();
                    return true;
                }

                @Override
                public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                    WebView newWebView = new WebView(view.getContext());
                    newWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view,String url) {
                            //????????????????????????????????????????????????
                            if (!TextUtils.isEmpty(url) && !url.startsWith("http")) {
                                try {
                                    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    return true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return true;
                                }
                            }

                            view.loadUrl(url);
                            return super.shouldOverrideUrlLoading(view,url);
                        };
                    });
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(newWebView);
                    resultMsg.sendToTarget();
                    return true;
                }

                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    String msg = String.format("==============> %d", newProgress);
                    Log.d(TAG, msg);
                    if (rotation == 2)
                        return;

                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                    if (newProgress >= 100)
                        progressBar.setVisibility(View.GONE);
                }
            });

            mWebView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                    //???????????????????????????????????????
                    Log.d(TAG,"downfile start");
                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
                    startActivity(intent);
                }
            });

            mWebView.loadUrl(url);
        }
    }

    //??????js?????????????????????
    @JavascriptInterface
    public void closeWebViewPay(String params) {
        Log.d(TAG,"closeWebViewPay");
        MainApp.getActivity().runOnUiThread(()->closeWebAndRotation());
    }

    //??????WebView????????????????????????
    public boolean closeWebAndRotation() {
        if (mWebView == null)
            return false;

        if (rotation == 2 || rotation == 12) {
            CommonAlertDialog alertDialog = CommonAlertDialog.newInstance("Are you sure to exit?");
            alertDialog.setOkListener(()->closeWebView(true));
            alertDialog.show();
            return true;
        }

        closeWebView(true);
        return true;
    }

    //??????WebView?????????
    public void closeWebView(boolean tellCocos) {
        //??????cocos WebView?????????
        if (tellCocos)
            AppActivity.getInstance().closeWebView(msgType, rotation);

        mWebView.destroy();
        dismiss();
        mWebView = null;
    }

    Bitmap bmp = null;
    @JavascriptInterface
    public void jcSaveQrcode(String data) throws JSONException {
        JSONObject json=new JSONObject(data);
        if(!json.isNull("qrcode")){
            Log.e("dddd",""+data);
            try {
                byte[] bits = Base64.decode( json.getString("qrcode"), Base64.DEFAULT);
                bmp = BitmapFactory.decodeByteArray(bits, 0, bits.length);
            } catch (NullPointerException | IllegalArgumentException | ClassCastException ex) {
                Log.d(TAG, "Broken Image", ex);
            }

            //???????????????????????????????????????
            if (Build.VERSION.SDK_INT >= 24) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1005);
            } else {
                saveBitmap(getActivity(),bmp);
            }
        }
    }

    // ?????????????????????
    private void openImageChooserActivity() {
//        final CharSequence[] items = {"Album", "Take a photo", "Cancel"};
//        AlertDialog alert = new AlertDialog.Builder(getActivity())
//                .setTitle("Choose a picture").setCancelable(false)
//                .setItems(items, (dialog, which) -> {
//                    if (which == 0) {
//                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                        i.addCategory(Intent.CATEGORY_OPENABLE);
//                        i.setType("image/*");
//                        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
//                    } else if (which == 1) {
//                        openCamera();
//                    } else if (which == 2) {
//                        if (uploadMessageAboveL != null) {
//                            uploadMessageAboveL.onReceiveValue(null);
//                            uploadMessageAboveL = null;
//                        }
//                        if (uploadMessage != null) {
//                            uploadMessage.onReceiveValue(null);
//                            uploadMessage = null;
//                        }
//                    }
//                }).create();
//        alert.show();

        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    public void openCamera() {
        //???????????????????????????????????????
        if (Build.VERSION.SDK_INT >= 24) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
        } else {
            startActivityForResult(Intent.createChooser(getCameraIntent(), "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
        }
    }

    //????????????????????????
    //@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(Intent.createChooser(getCameraIntent(), "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
            } else {
                Toast.makeText(getActivity(),"Request for authorisation rejected.",Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == 1005){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(bmp==null){
                    Toast.makeText(getActivity(),"File is empty.",Toast.LENGTH_LONG).show();
                    return;
                }
                saveBitmap(getActivity(),bmp);
            } else {
                Toast.makeText(getActivity(),"Request for authorisation rejected.",Toast.LENGTH_LONG).show();
            }
        }
    }

    // Android7????????????
    private Intent getCameraIntent() {
        File imagePath = new File(getActivity().getExternalCacheDir(), "Pictures");
        if (!imagePath.exists())
            imagePath.mkdirs();

        //??????provider?????????
        String packageName = getActivity().getPackageName();
        String strProvider = packageName + ".fileProvider";

        File newFile = new File(imagePath, "mycamera.jpg");
        //contentUri = FileProvider.getUriForFile(getActivity(), "com.example.jch5demo.fileprovider1", newFile);
        contentUri = FileProvider.getUriForFile(getActivity(), strProvider, newFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        return intent;
    }

    // ???????????????????????????
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) { //???????????????????????????????????????
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }

    // ??????????????????????????????
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            } else {
                results = new Uri[]{contentUri};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    public static void saveBitmap(final Context activity, final Bitmap bitmap) {
        if (bitmap == null)
            return;

        new AsyncTask<Void, Integer, File>() {
            File destFile;
            @Override
            protected File doInBackground(Void... params) {
                try {
                    // ??????????????????
                    File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();

                    File appDir = new File(pictureFolder, "Beauty");
                    if (!appDir.exists())
                        appDir.mkdirs();

                    String fileName = System.currentTimeMillis() + ".jpg";
                    destFile = new File(appDir, fileName);

                    if (destFile.exists())
                        destFile.delete();

                    FileOutputStream out;
                    try {
                        out = new FileOutputStream(destFile);
                        if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                            out.flush();
                            out.close();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // ????????????????????????
                    activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(destFile.getPath()))));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                return destFile;
            }

            @Override
            protected void onPostExecute(File file) {
                Toast.makeText(activity,"Saved.",Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }
        }.execute();
    }
}
