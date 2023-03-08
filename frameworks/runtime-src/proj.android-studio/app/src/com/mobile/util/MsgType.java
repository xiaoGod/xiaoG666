//
//  MsgType.java
//

package com.mobile.util;

public class MsgType {
    public static final int MsgUnKnow = 0;
    public static final int MsgShake = 1;                     //H5-Native         震动，json格式: {type: 1, duration: 0.5}
    public static final int MsgExitApp = 2;                   //H5-Native         应用退出，json格式: {type: 2}
    public static final int MsgOpenWeChat = 3;                //H5-Native         打开微信, json格式: {type: 3, url: ""}

    public static final int MsgShowAlert = 4;                 //H5-Native-H5      打开提示框，json格式: {type: 4, title: "", msg: "", needCancel: false} 返回json格式: {"type": 4, "way": 1} way: 0 取消 1 确定
    public static final int MsgCopyToClipboard = 5;           //H5-Native         复制到剪贴板，json格式: {type: 5, text: ""}
    public static final int MsgOpenPhotoLibrary = 6;          //H5-Native-H5      打开相册，json格式: {type: 6, path: ""}
    public static final int MsgSaveImageToPhotoLibrary = 7;   //H5-Native         保存截图到相册, json格式: {type: 7, path: ""}

    public static final int MsgGetMachineId = 8;              //H5-Native-H5.     获取机器码, json格式: {type: 8, machineId: ""}
    public static final int MsgGetVersion = 9;                //H5-Native-H5.      获取Native版本号, json格式: {type: 9, version: "1.0.0", agent : "1_x_x", phone_os :"机型 型号", simulator : 0 };  simulator://0真机 1模拟器

    public static final int MsgGetPhoneNumber = 10;           //H5-Native-H5.     获取本机号码, json格式: {type: 10, phoneNum: ""}
    public static final int MsgGetAllContacts = 11;           //H5-Native-H5      获取手机联系人信息, json格式: {type: 11, dataList: [{name: "", phone: "", id: ""}]}

    public static final int MsgGetBatteryStatus = 12;         //H5-Native-H5      获取手机电量, json格式: {type: 12, data: {level: 0.78, state: 1}}  state: 0 未安装电池 1 没有充电 2 正在充电 3 已经充满
    public static final int MsgGetNetworkStatus = 13;         //H5-Native-H5      获取手机网络状态, json格式: (type: 13, netstatus: 1)   netstatus: 0 无网络 1 Wifi 2 4G

    public static final int MsgImageUploadForAgent = 14;      //H5-Native-H5      代理充值图片上传, json格式: {type: 14, data: {path: "", uploadstate: 1, msg: ""}, quality: 0.2} uploadstate: 1 开始上传 2 上传成功 3 上传失败

    public static final int MsgShareByType  = 15;             //H5-Native-H5      微信分享， json格式: { type: 15, result : 1 }

    public static final int MsgGetTextFromClipboard = 16;     //H5-Native-H5      获取剪贴板文本，json格式: {type: 16, text: ''};

    public static final int MsgJinChanWebView = 17;           //H5-Native-H5      改变屏幕方向: {type: 17, rotation : 1 };     //1横屏 2竖屏
    public static final int MsgOpenUrl    = 18;               //H5-Native         打开webview，并且显示显示顶部的功能按钮
    public static final int MsgCloseWebView = 20;             //顶号切服的时候关闭原生webview

    public static final int MsgAlipayRecharge = 21;           //H5-Native-H5  支付宝支付
    public static final int MsgChangeScreenRotation = 22;     //H5-Native-H5 选择屏幕

    public static final int MsgOpenApp = 23;                  //H5-Native-H5 打开APP, json格式: {type: 23, pkg_name: "com.tencent.mm", app_name: "腾讯QQ"} 返回json格式: {type: 23, installed: 0} // 0未安装 1已安装

    public static final int MsgTrackEvent = 24;               //H5-Native adjust上报, json格式: {type: 24, eventToken: "", revenue: value, currency: ""}

    public static final int MsgGetLocation = 25;              //H5-Native-H5 获取位置, json格式: {type: 25, permission: 1, location: "IN-印度-孟买", latitude: 0.0, longitude: 0.0}

    public static final int MsgCloseLaunchView = 201;         //H5-Native 关闭启动图
}
