package org.apache.cordova.pushlib;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * Created by User on 2016/9/26.
 */
public class DeviceUtils
{

    public static final String XiaoMi = "xiaomi";
    public static final String HuaWei = "huawei";
    public static String getUUID(Context context,String phoneNum) {
        final TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString
                (context.getContentResolver()
                        , android.provider.Settings.Secure.ANDROID_ID);
        if(phoneNum==null)
            return null;
        return Md5Utility.encodeMd5("danbaofan" + tmDevice + tmSerial + androidId+phoneNum);
    }

    public static String getCurSys() {
        return Build.MANUFACTURER;
    }

    public static boolean isXiaoMi() {
        String curSys = getCurSys().toLowerCase();
        if (curSys.contains(XiaoMi))
            return true;
        return false;
    }

    public static boolean isHuaWei() {
        String curSys = getCurSys().toLowerCase();
        if (curSys.contains(HuaWei))
            return true;
        return false;
    }

    public static String getDeviceId(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    public static String getSysVersion()
    {
        return Build.VERSION.RELEASE;
    }
}
