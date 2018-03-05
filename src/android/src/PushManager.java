package org.apache.cordova.pushlib;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.LruCache;

import com.evideo.push.service.DdpushInterface;
import com.google.gson.Gson;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.pushlib.AppUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import android.provider.Settings;
import android.net.Uri;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by User on 2016/9/26.
 */
public final class PushManager extends CordovaPlugin {

    public static Map<Integer,JSONArray> REQ_ARGS= new HashMap();

    public static int REQ_CODE_INDEX= 0;

    public static final int PUSH_CHANNEL_JPUSH = 0x01;

    public static final int PUSH_CHANNEL_DDPUSH = 0x02;

    public static final int PUSH_CHANNEL_HWPUSH = 0x03;

    public static final int PUSH_CHANNEL_MIPUSH = 0x04;

    public static final int PUSH_DATA_TYPE = 0x01;

    private static final String TAG = "PushManager";


    public static String cid;

    private static CallbackContext callbackContext;

    private static String uuid;

    private static String brand;

    private static String sysVersion;

    private static Object romVersion;

    public static LruCache<String, PushBean> PUSH_DATA_LRU = new LruCache<String, PushBean>(20);//storage push data

    public static String EX_PUSH_DATA = null;//push data to handle by load page finish

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        LOG.d(TAG, "execute action :" + action);
        LOG.d(TAG, "execute args :" + args.toString());
        if (action.equals("initial")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    PushManager.callbackContext = callbackContext;
                    if (checkPermission(args)){
                        init(cordova.getActivity(), args, callbackContext);
                        handleExPushdata();
                    }
                }
            });
            return true;
        }
        else if ("installapk".equals(action)) {
            installApk(args.getString(0));
        }else if ("toAppDetail".equals(action))//to to app detail
        {
            Intent setting = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            setting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            setting.setData(Uri.fromParts("package", cordova.getActivity().getPackageName(), null));
            cordova.getActivity().startActivity(setting);
            System.exit(0);
        }
        return super.execute(action, args, callbackContext);
    }

    /***
     * 判断当前并申请所需权限
     * @return
     */
    private boolean checkPermission(JSONArray args){
        REQ_CODE_INDEX ++;
        if(!cordova.hasPermission(Manifest.permission.READ_PHONE_STATE)){
            LOG.d(TAG, "checkPermission missing READ_PHONE_STATE : ");
            REQ_ARGS.put(REQ_CODE_INDEX,args);
            cordova.requestPermission(PushManager.this,REQ_CODE_INDEX,Manifest.permission.READ_PHONE_STATE);
            return false;
        } else if (!cordova.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)){
            LOG.d(TAG, "checkPermission missing STORAGE : ");
            REQ_ARGS.put(REQ_CODE_INDEX,args);
            cordova.requestPermission(PushManager.this,REQ_CODE_INDEX,Manifest.permission.READ_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    private void handleExPushdata() {

        if (EX_PUSH_DATA != null)//update the ex push data
        {
            LOG.d(TAG, "handle the exPushdata,the data is : " + EX_PUSH_DATA);
            cordova.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    upDatePushData(EX_PUSH_DATA);
                    LOG.d(TAG, "the exPushdata had update to page,reset null");
                    EX_PUSH_DATA = null;
                }
            });
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        String pushData = intent.getStringExtra("pushData");
        upDatePushData(pushData);

    }

    private void upDatePushData(String pushData) {

        String format = "window.plugins.jPushPlugin.receiveMessage(%s);";
        final String js = String.format(format, pushData);
        LOG.d(TAG, "load js method : " + js);
        webView.loadUrl("javascript:" + js);
    }

    public static void initJustJPush(Context context, JSONArray args, CallbackContext callbackContext){

    }

    public static void init(Context context, JSONArray args, CallbackContext callbackContext) {
        LOG.d(TAG, "init  args: " + args);
        //init some files
        uuid = DeviceUtils.getDeviceId(context);
        brand = DeviceUtils.getCurSys();
        //        brand = "VIVO";//test jpush
        sysVersion = DeviceUtils.getSysVersion();
        romVersion = null;
        //register ddpush
        LOG.d(TAG, "init ddpush");
        registerDdPush(context, args);
        //change push by os
        if (DeviceUtils.isHuaWei()) {
            sendPushID(new Hwpush(null));//update ddpush client info to sever advance,in case other push register fail
            LOG.d(TAG, "init Huawei push");
            PushManager.registerHwPush(context);
        } else if (DeviceUtils.isXiaoMi()) {
            sendPushID(new Mipush(null));//update ddpush client info to sever advance,in case other push register fail
            LOG.d(TAG, "init xiao mi push");
            PushManager.registerMiPush(context);
        } else {
            sendPushID(new JPush(null));//update ddpush client info to sever advance,in case other push register fail
            LOG.d(TAG, "init j push");
            PushManager.registerJPush(context);
        }

    }
    private void installApk(String path) {
        
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse(path), "application/vnd.android.package-archive");
        cordova.getActivity().startActivity(i);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void registerJPush(Context context) {

        JPushInterface.init(context);
        LOG.e(TAG, "" + JPushInterface.isPushStopped(context));
        String rid = JPushInterface.getRegistrationID(context);
        sendPushID(new JPush(rid));
    }

    public static void unRegisterJPush(Context context) {

        JPushInterface.stopPush(context);
    }

    public static void registerDdPush(Context context, JSONArray args) {

        String serverAddr = getMateData(context).getString("ddpush_server_addr");

        int serverPort = getMateData(context).getInt("ddpush_server_prot");
        int appId = getMateData(context).getInt("ddpush_appid") ;
        try {
            cid = DeviceUtils.getUUID(context, args.getString(0));
        } catch (JSONException e) {
            LOG.e(TAG, "args is null,cant get phoneNum");
        }
        DdpushInterface.initService(context, serverAddr, serverPort, appId, cid);
    }

    public static void unRegisterDdPush(Context context) {

        DdpushInterface.stopService(context);
    }

    public static void registerHwPush(Context context) {

        com.huawei.android.pushagent.api.PushManager.requestToken(context);
    }

    public static void unRegisterHwPush(Context context) {
        //TODO
    }

    public static void registerMiPush(Context context) {

        String appId = getMateData(context).getString("xiaomi_appid").replace("mi","");
        String appKey = getMateData(context).getString("xiaomi_appkey").replace("mi","");
        MiPushClient.checkManifest(context);
        MiPushClient.registerPush(context, appId, appKey);
    }

    public static void unRegisterMiPush(Context context) {

        MiPushClient.unregisterPush(context);
    }

    public static void resolvePushData(Context context, int channel, String data, String title) {

        PushBean pushBean;
        try {
            pushBean = new Gson().fromJson(data, PushBean.class);
            if (PUSH_DATA_LRU.get(data) == null) {
                LOG.d(TAG, "this push is fresh,show the notification,the push data : " + data);
                PUSH_DATA_LRU.put(data, pushBean);
                showNotification(context, pushBean);
            } else {
                LOG.d(TAG, "this push had show to user,abort it");
            }

        } catch (Exception e) {
            LOG.d(TAG, "resolve Push erro,the exception is " + e.getMessage());
            return;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void showNotification(Context context, PushBean pushBean) {
        ApplicationInfo appInfo  =  AppUtils.getAppInfo(context);

        //custom notification
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence appName = getMateData(context).getString(AppUtils.getAppName(context));
        Bitmap appIcon = BitmapFactory.decodeResource(context.getResources(),appInfo.icon);
        Notification.Builder mBuilder = new Notification.Builder(context);
        mBuilder.setLargeIcon(appIcon).setSmallIcon(appInfo.icon).setContentTitle(pushBean.title).setContentText((pushBean.desc == null) ? appName : pushBean.desc).setTicker(appName).setAutoCancel(true).setWhen(System.currentTimeMillis()).setDefaults(Notification.DEFAULT_SOUND);

        Notification notification = mBuilder.build();
        Intent notificationIntent = new Intent("android.intent.action.MAIN");
        notificationIntent.putExtra("pushData", new Gson().toJson(pushBean));
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentIntent = contentIntent;
        mNotificationManager.notify(pushBean.type, notification);
    }

    public static void sendPushID(Object push) {

        PushClient client = new PushClient();
        client.uuid = uuid;
        client.brand = brand;
        client.sysVersion = sysVersion;
        client.romVersion = null;
        client.os = 0;
        if (push instanceof JPush) {
            client.androidSys = 0;
            client.jPush = ((JPush) push);
        } else if (push instanceof Hwpush) {
            client.androidSys = 2;
            client.hwPush = ((Hwpush) push);
        } else if (push instanceof Mipush) {
            client.androidSys = 1;
            client.miPush = ((Mipush) push);
        }
        client.ddPush = new DdPush(cid);
        String json = new Gson().toJson(client);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

    static class PushClient {

        String uuid;

        String brand;

        DdPush ddPush;

        JPush jPush;

        Hwpush hwPush;

        Mipush miPush;

        String sysVersion;

        int os;

        String romVersion;

        int androidSys;
    }

    static class DdPush {

        String cid;

        public DdPush(String cid) {

            this.cid = cid;
        }
    }

    static class JPush {

        String regId;

        public JPush(String regId) {

            this.regId = regId;
        }
    }

    public static class Hwpush {

        String token;

        public Hwpush(String token) {

            this.token = token;
        }
    }

    public static class Mipush {

        String regId;

        public Mipush(String regId) {

            this.regId = regId;
        }
    }

    public static class PushBean {

        int type;

        String id;

        String title;

        String time;

        String desc;

        Detail detail;

        static class Detail {

            String id;
        }
    }

    /**
     * 获取当前元数据
     * @param context
     * @return
     */
    public static Bundle getMateData(Context context){
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 请求权限后回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @throws JSONException
     */
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for(int r:grantResults)
        {
            if(r == PackageManager.PERMISSION_DENIED)
            {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                return;
            }
        }
        init(cordova.getActivity(), REQ_ARGS.get(requestCode), callbackContext);
        handleExPushdata();
    }

}
