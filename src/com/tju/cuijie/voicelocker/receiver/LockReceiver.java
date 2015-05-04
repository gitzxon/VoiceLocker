package com.tju.cuijie.voicelocker.receiver;

import com.tju.cuijie.voicelocker.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LockReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent intent) {
		LogUtil.d("mReceiver onReceive" + intent.getAction());
	}
}