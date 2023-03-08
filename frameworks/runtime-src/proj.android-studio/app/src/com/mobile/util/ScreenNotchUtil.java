package com.mobile.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import org.cocos2dx.javascript.AppActivity;

import java.lang.reflect.Method;

public class ScreenNotchUtil
{
    private final static int DEVICE_BRAND_OPPO = 0;
    private final static int DEVICE_BRAND_HUAWEI = 1;
    private final static int DEVICE_BRAND_VIVO = 2;
    private final static int DEVICE_BRAND_XIAOMI = 3;

    private static String[] brandNames= {"oppo","huaWei","vivo","xiaomi"};

    private static AppActivity context = AppActivity.getInstance();

    private static final String TAG = ScreenNotchUtil.class.getName();

    /**
     * 获取手机的品牌名
     * @return  手机厂商
     */
    public static String getBrandName()
    {
        String brandName = "";
        int brandIdx = ScreenNotchUtil.getDeivceIdx();
        if (brandIdx != -1)
        {
            brandName = brandNames[brandIdx];
        }
        return  brandName;
    }


    /**
     * 获取各种手机的刘海高度
     *
     * @return  手机厂商
     */
    public static int getNotchSize()
    {
        int ret = 0;
        //先处理android 9的手机系统
        if (Build.VERSION.SDK_INT >= 28)
        {
            WindowManager.LayoutParams lp = context.getWindow().getAttributes();
            Log.d(TAG,"lp is " + lp.layoutInDisplayCutoutMode);
            if (lp.layoutInDisplayCutoutMode == 0)
            {
                //非全屏模式,直接返回0
                return ret;
            }
        }

        //android 9以前的手机系统
        String brandName = "";
        int brand = getDeivceIdx();
        switch (brand)
        {
            case DEVICE_BRAND_OPPO:
                ret = getOppoNotch(context);
                break;
            case DEVICE_BRAND_HUAWEI:
                //0表示不隐藏刘海开关，即使用刘海区域。
                int forceBlack = Settings.Secure.getInt(context.getContentResolver(),"display_notch_status",0);
                boolean isUseNotch = forceBlack == 0;
                Log.d(TAG,String.format("forceBlack %d,isOpen %b",forceBlack,isUseNotch));
                if (isUseNotch)
                {
                    ret = getHuaWeiNotch(context);
                }
                break;
            case DEVICE_BRAND_VIVO:
                ret = getVivoNotch(context);
                break;
            case DEVICE_BRAND_XIAOMI:
                //0表示不隐藏刘海开关，即使用刘海区域。
                int forceBlack2 = Settings.Global.getInt(context.getContentResolver(),"force_black",0);
                boolean isUseNotch2 = forceBlack2 == 0;
                Log.d(TAG,String.format("forceBlack %d,isOpen %b",forceBlack2,isUseNotch2));
                if (isUseNotch2)
                {
                    ret = getXiaomiNotch(context);
                }
                break;
            default:
                break;
        }

        return ret;
    }


    /**
     * 获取OPPO手机刘海高度,OPPO目前刘海统一为80
     *
     * @return  刘海高度
     */
    public static int getOppoNotch(Context context)
    {
        int ret = 0;
        boolean hasNotch = context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        Log.d("NotchScreenUtil", "this OPPO device has notch in screen？"+hasNotch);
        if (hasNotch)
        {
            //oppo 刘海高度和状态栏一致
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0)
            {
                ret = resources.getDimensionPixelSize(resourceId);
                Log.d("ScreenNotchUtil", "this OPPO device has notch size is " + ret);
            }
        }
        return ret;
    }

    /**
     * 获取HUAWEI手机刘海高度
     *
     * @return  刘海高度
     */
    public static int getHuaWeiNotch(Context context)
    {
        int[] ret = new int[]{0,0};
        try
        {
            ClassLoader cld = context.getClassLoader();
            Class notchSizeUtil = cld.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method hasNotchInScreen = notchSizeUtil.getMethod("hasNotchInScreen");
            if(hasNotchInScreen != null)
            {
                boolean hasNotch = (boolean) hasNotchInScreen.invoke(notchSizeUtil);
                if (hasNotch)
                {
                    Method getFun = notchSizeUtil.getMethod("getNotchSize");
                    ret = (int[])getFun.invoke(notchSizeUtil);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret[1];
    }

    /**
     * 获取VIVO手机刘海高度
     *
     * @return  刘海高度
     */
    public static int getVivoNotch(Context context)
    {
        int ret = 0;
        try
        {
            ClassLoader cld = context.getClassLoader();
            Class<?> ftFeature = cld.loadClass("android.util.FtFeature");
            boolean hasNotch = false;
            Method[] methods = ftFeature.getDeclaredMethods();
            for (Method method : methods)
            {
                if (method.getName().equalsIgnoreCase("isFeatureSupport"))
                {
                    hasNotch = (boolean) method.invoke(ftFeature, 0x00000020);
                    break;
                }
            }

            if(hasNotch)
            {
                Resources resources = context.getResources();
                int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");

                if (resourceId > 0)
                {
                    ret = resources.getDimensionPixelSize(resourceId);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 获取XIAOMI手机刘海高度
     *
     * @return  刘海高度
     */
    public static int getXiaomiNotch(Context context)
    {
        int ret = 0;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;
            Method getIntFun = SystemProperties.getMethod("getInt", paramTypes);
            if (getIntFun != null)
            {
                boolean hasNotch = (Integer) getIntFun.invoke(SystemProperties,"ro.miui.notch", 0) == 1;
                Log.d("NotchScreenUtil", "this device has notch in screen？" + hasNotch);
                if (hasNotch)
                {
                    Resources resources = context.getResources();
                    int resourceId =  resources.getIdentifier("notch_height", "dimen", "android");
                    if (resourceId > 0)
                    {
                        ret = resources.getDimensionPixelSize(resourceId);
                        Log.d("ScreenNotchUtil", "this device has notch size is " + ret);
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    private static int getDeivceIdx()
    {
        int ret = -1;
        //品牌
        String brand = Build.BRAND.trim().toUpperCase();
        //制造商
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (brand.contains("OPPO"))
        {
            ret = DEVICE_BRAND_OPPO;
        }
        else if(brand.contains("HUAWEI") || manufacturer.contains("HUAWEI"))
        {
            ret = DEVICE_BRAND_HUAWEI;
        }
        else if(brand.contains("VIVO"))
        {
            ret = DEVICE_BRAND_VIVO;
        }
        else if(brand.contains("XIAOMI") || manufacturer.contains("XIAOMI"))
        {
            ret = DEVICE_BRAND_XIAOMI;
        }
        Log.d("screenNotchUtil ", String.format("brand is %s,Brand2 is %s,model is %s",brand,manufacturer,model));
        return ret;
    }



}
