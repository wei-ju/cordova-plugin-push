package org.apache.cordova.pushlib.receivers;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.apache.cordova.pushlib.LOG;
import org.apache.cordova.pushlib.PushManager;

import java.util.List;

/**
 * Created by root on 2016/8/8.
 */
public class XiaoMiReceiver extends PushMessageReceiver
{

    private static final String TAG = "XiaoMiReceiver";

    private String mRegId;
    private long mResultCode = -1;
    private String mReason;
    private String mCommand;
    private String mMessage;
    private String mTopic;
    private String mAlias;
    private String mStartTime;
    private String mEndTime;
    @Override
    public void onReceiveMessage(Context context, MiPushMessage message) {
        mMessage = message.getContent();
        LOG.d(TAG,"handle the mipush message : "+message);
//        PushManager.resolvePushData(context,PushManager.PUSH_CHANNEL_MIPUSH,mMessage,message.getTitle());
    }
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        LOG.d(TAG,"receive command"+command);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                    mRegId = cmdArg1;
                if(!TextUtils.isEmpty(mRegId)){
                    PushManager.sendPushID(new PushManager.Mipush(mRegId));
                }
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mStartTime = cmdArg1;
                mEndTime = cmdArg2;
            }
        }
    }

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        LOG.d(TAG,"Receive PassThroughMessage "+message);
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        LOG.d(TAG,"Click mi Notification "+message);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("pushData", message.getContent());
        context.startActivity(intent);
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        LOG.d(TAG,"mi on NotificationMessage Arrived "+message);
        if (PushManager.PUSH_DATA_LRU.get(message.getContent()) == null)
        {
            LOG.d(TAG, "this push is fresh,show the notification,the push data : " + message.getContent());
            PushManager.PUSH_DATA_LRU.put(message.getContent(), new Gson().fromJson(message.getContent(), PushManager.PushBean.class));
        }
        else
        {
            LOG.d(TAG, "this push had show to user,abort it");
            MiPushClient.clearNotification(context);
        }
    }


    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
    }
}