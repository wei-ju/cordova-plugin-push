package org.apache.cordova.pushlib.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evideo.push.service.DdpushInterface;

import org.apache.cordova.pushlib.LOG;
import org.apache.cordova.pushlib.PushManager;
import org.json.JSONException;
import org.json.JSONObject;


public class DdpushReceiver extends BroadcastReceiver {

	private static final String TAG = "DdpushReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (DdpushInterface.ACTION_RECEIVED_MESSAGE.equals(action)) {
			resolveMessage(context, intent.getByteArrayExtra(DdpushInterface.EXTRA_MESSAGE));
		}
	}
	
	private void resolveMessage(Context context, byte[] data) {
		if (data == null || data.length < 8) {
			return;
		}
		if (data[0] != 0x78 || data[1] != 0x56 || data[2] != 0x34 || data[3] != 0x12) {
			return;
		}
		int dataOffset = (data[7]&0xff) << 24 | (data[6]&0xff) << 16 | (data[5]&0xff) << 8 | (data[4]&0xff);
		if (dataOffset < 8 || dataOffset > data.length) {
			return;
		}
		
		String headJson = new String(data, 8, dataOffset - 8);

		String bodyJson = new String(data, dataOffset, data.length - dataOffset);

		LOG.d(TAG,"handle the ddpush message : "+headJson+bodyJson);

		try {
			JSONObject titleJson= new JSONObject(headJson);
//			headJson=titleJson.getString(PushEventSets.TRANSMISSION_KEY_TITLE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		PushManager.resolvePushData(context, PushManager.PUSH_CHANNEL_DDPUSH,bodyJson,headJson);
	}
}
