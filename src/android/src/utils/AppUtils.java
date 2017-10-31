package org.apache.cordova.pushlib;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Created by geek on 2017/10/29.
 */

public class AppUtils {

    public static ApplicationInfo  getAppInfo(Context context){
        ApplicationInfo applicationInfo =null;
        try {
             applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return applicationInfo ;

    }

    /**
     * 程序的图标
     * @param context
     * @return
     */
    public static Drawable getAppIcon(Context context){

        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            return info.loadIcon(context.getPackageManager());
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    /*
   * 获取程序的名字
   */
    public static String  getAppName(Context context){
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            return info.loadLabel(context.getPackageManager()).toString();
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
