package org.apache.cordova.pushlib.receivers;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.huawei.android.pushagent.api.PushEventReceiver;

import org.apache.cordova.pushlib.LOG;
import org.apache.cordova.pushlib.PushManager;

/**
 * Created by root on 2016/8/4.
 */
public class HuaweiReceiver extends PushEventReceiver
{

    private static final String TAG = "HuaweiReceiver";

    @Override
    public void onEvent(Context context, Event event, Bundle bundle) {
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            int notifyId = bundle.getInt(BOUND_KEY.pushNotifyId, 0);
            if (0 != notifyId) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(notifyId);
            }
            String content = bundle.getString(BOUND_KEY.pushMsgKey);
            LOG.d(TAG,"hwpush onEvent method:"+content);
        }
    }

    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        String message = new String(msg);
        LOG.d(TAG,"handle the hwpush message : " +message);
        PushManager.resolvePushData(context,PushManager.PUSH_CHANNEL_HWPUSH,message,null);
        return true;
    }

    @Override
    public void onToken(Context context, String token, Bundle extras) {
        LOG.d(TAG,"get the hwpush token");
        if(!TextUtils.isEmpty(token)){
            PushManager.sendPushID(new PushManager.Hwpush(token));
        }
        String belongId = extras.getString("belongId");
        String content = "获取token和belongId成功，token = " + token + ",belongId = " + belongId;
    }

}
