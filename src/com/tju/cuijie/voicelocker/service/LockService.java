package com.tju.cuijie.voicelocker.service;

import com.tju.cuijie.voicelocker.LogUtil;
import com.tju.cuijie.voicelocker.activity.LockActivity;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.WindowManager;

public class LockService extends Service {
	
	public static boolean ifListenToScreenOff = false;

    private static Intent mIntent;

    public static Context mContext;
    public static WindowManager mWindowManager;

    private KeyguardManager.KeyguardLock mKeyguardLock;
	
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d("mReceiver onReceive" + intent.getAction());
//            addHomeLockView();
            startActivity(new Intent(LockService.this, LockActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

    };
    
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
    public void onCreate(){
        super.onCreate();
        this.startForeground(0, null);

        mContext = getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        if (ifListenToScreenOff == false) {
            ifListenToScreenOff = true;
            addScreenEventListener();
        }
        LogUtil.d("LockService onCreate");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        LogUtil.d("LockService onStartCommand");

        if (mContext == null) {
            mContext = this;
        }

        mIntent = intent;

        if (ifListenToScreenOff == false) {
            ifListenToScreenOff = true;
            addScreenEventListener();
        }
        
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onDestroy(){
        LogUtil.d("LockService onDestroy");
        ifListenToScreenOff = false;
        unregisterReceiver(mReceiver);

        startService(mIntent);

        super.onDestroy();
    }
    
    public void addScreenEventListener(){
        mKeyguardLock = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock("AngLocker");
        mKeyguardLock.disableKeyguard();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
//        intentFilter.addAction(Intent.ACTION_SCREEN_ON);

        intentFilter.setPriority(1000);
        registerReceiver(mReceiver, intentFilter);//把广播接收器BroadcastReceiver注册到ActivityManagerService中去
    }
}