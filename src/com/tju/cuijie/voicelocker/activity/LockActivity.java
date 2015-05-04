package com.tju.cuijie.voicelocker.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.tju.cuijie.voicelocker.HomeLocker;
import com.tju.cuijie.voicelocker.LogUtil;
import com.tju.cuijie.voicelocker.MyApplication;
import com.tju.cuijie.voicelocker.MyContacts;
import com.tju.cuijie.voicelocker.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LockActivity extends Activity implements OnClickListener {

	HomeLocker mHomeLocker;
	KeyguardManager keyguardManager;

	public static RelativeLayout lockScreenRoot;
	public static boolean STATE = false;

	//语音
	private static final String MYAPPID = "=551d0c86";
	private SpeechRecognizer mIat;// 语音听写对象,无讯飞UI
	private RecognizerDialog mIatDialog;// 语音听写,有UI

	//在锁屏界面显示系统时间
	static TextView Date, Time;
	private static Handler mainhandler;
	Calendar c = Calendar.getInstance();
	SimpleDateFormat sdf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d("display lock activity");

		SpeechUtility.createUtility(this, SpeechConstant.APPID + MYAPPID);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		setContentView(R.layout.activity_lock);//my lock layout

		MyApplication.STATE = true;

		Date = (TextView) findViewById(R.id.date);
		Time = (TextView) findViewById(R.id.time);

		try {
			fullScreenCall();
		} catch (Exception e) {
			LogUtil.d("full_screen_call failed");
		}

		mHomeLocker = new HomeLocker();
		mHomeLocker.lock(this);

		lockScreenRoot = (RelativeLayout) findViewById(R.id.look_screen_root);
		lockScreenRoot.setDrawingCacheEnabled(true);
		lockScreenRoot.buildDrawingCache();

		mIatDialog = new RecognizerDialog(LockActivity.this, mInitListener);
		mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
		mIat.setParameter(SpeechConstant.DOMAIN, "iat");
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		mIat.setParameter(SpeechConstant.ACCENT, "mandarin");


		Button btnRecognize = (Button) findViewById(R.id.startRecognizer);
		btnRecognize.setOnClickListener(LockActivity.this);

		mainhandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				c = Calendar.getInstance();

				sdf = new SimpleDateFormat("hh:mm a");
				String time = sdf.format(c.getTime());
				Time.setText(time);

				sdf = new SimpleDateFormat("yyyy年M月d日 EE");
				String date = sdf.format(c.getTime());
				Date.setText(date);
			}
		};

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						mainhandler.sendEmptyMessage(0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		LogUtil.d("view id:"+v.getId());
		switch (v.getId()) {  
		case R.id.startRecognizer:
			LogUtil.d("start recognizer");
			mIatDialog.setListener(recognizerDialogListener);
			mIatDialog.show();
			//				mIat.startListening(recognizerListener); //no UI
			break;
		}
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				LogUtil.d("初始化失败，错误码：" + code);
			}
		}
	};

	/**
	 * 听写监听器。
	 */
	//	private RecognizerListener recognizerListener = new RecognizerListener() {
	//
	//		@Override
	//		public void onBeginOfSpeech() {
	//			LogUtil.d("开始说话");
	//		}
	//
	//		@Override
	//		public void onError(SpeechError error) {
	//			LogUtil.d(error.getPlainDescription(true));
	//		}
	//
	//		@Override
	//		public void onEndOfSpeech() {
	//			LogUtil.d("结束说话");
	//		}
	//
	//		@Override
	//		public void onResult(RecognizerResult results, boolean isLast) {
	//			LogUtil.d("on result");
	//			LogUtil.d(results.getResultString());
	//
	//			if (isLast) {
	//				// TODO 最后的结果
	//			}
	//		}
	//		
	//		@Override
	//		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
	//		}
	//
	//		@Override
	//		public void onVolumeChanged(int volume) {
	//			//LogUtil.d("当前正在说话，音量大小：" + volume);
	//		}
	//	};

	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			//			LogUtil.d(results.getResultString());

			try {
				JSONObject jsonResult = new JSONObject(results.getResultString());
				//				LogUtil.d("length:" + jsonResult.length());
				JSONArray ws = jsonResult.getJSONArray("ws");
				StringBuffer resultStr = new StringBuffer();
				for(int i = 0;i < ws.length();i++){
					String word = null;
					word = ws.getJSONObject(i).getJSONArray("cw").getJSONObject(0).getString("w");
					if(word != null){
						resultStr.append(word);
					}
				}
				LogUtil.d("result is:" + resultStr.toString());
				recognizerCommand(resultStr.toString());
			} catch (JSONException e) {
				LogUtil.d("json error");
				e.printStackTrace();
			}	
		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			LogUtil.d(error.getPlainDescription(true));
		}

	};
	private PackageManager mPackageManager;  
	private Context mContext;
	private void recognizerCommand(String resultStr){
		if(resultStr.contains("短信")){
			Intent resultIntent2 = new Intent();
			ComponentName cmp = new ComponentName("com.android.mms","com.android.mms.ui.ConversationList");
			resultIntent2.setAction(Intent.ACTION_MAIN);
			resultIntent2.addCategory(Intent.CATEGORY_LAUNCHER);
			resultIntent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resultIntent2.setComponent(cmp);
			startActivityForResult(resultIntent2, 0);

			//			给。。。发短信
			//			Uri smsToUri = Uri.parse("smsto:10086");
			//			Intent resultIntent1 = new Intent(Intent.ACTION_SENDTO, smsToUri);
			//			resultIntent1.putExtra("sms_body", "测试发送短信");
			//			startActivity(resultIntent1);
		} else if (resultStr.contains("电话")){
			if(resultStr.contains("给")){
				String name = null;
				for(int i = resultStr.indexOf("给") + 1;i <= resultStr.length();i++){
					name += resultStr.indexOf(i);
				}
				LogUtil.d("name is "+name);
				
				String phoneno = MyContacts.CONTACT_LIST.get(name);
				LogUtil.d("number is "+phoneno);
				if(phoneno == null||"".equals(phoneno.trim())) {
					Toast.makeText(getApplicationContext(), "没有电话号码", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "有电话号码"+phoneno, Toast.LENGTH_SHORT).show();
					Intent intent=new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+phoneno));
					LogUtil.d("call "+phoneno);
					startActivity(intent);
				}
			}
		} else if (resultStr.contains("微信")){
			Intent resultIntent2 = new Intent();
			ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
			resultIntent2.setAction(Intent.ACTION_MAIN);
			resultIntent2.addCategory(Intent.CATEGORY_LAUNCHER);
			resultIntent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resultIntent2.setComponent(cmp);
			startActivityForResult(resultIntent2, 0);
		} else if (resultStr.contains("腾讯微博")){
			Intent resultIntent3 = new Intent();
			ComponentName cmp = new ComponentName("com.tencent.WBlog","com.tencent.WBlog.activity.MicroblogInput");
			resultIntent3.setAction(Intent.ACTION_MAIN);
			resultIntent3.addCategory(Intent.CATEGORY_LAUNCHER);
			resultIntent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resultIntent3.setComponent(cmp);
			startActivityForResult(resultIntent3, 0);
		} else if (resultStr.contains("微博")){
			Intent resultIntent4 = new Intent();
			ComponentName cmp = new ComponentName("com.sina.weibo","com.sina.weibo.EditActivity");
			resultIntent4.setAction(Intent.ACTION_MAIN);
			resultIntent4.addCategory(Intent.CATEGORY_LAUNCHER);
			resultIntent4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resultIntent4.setComponent(cmp);
			startActivityForResult(resultIntent4, 0);
		} else if (resultStr.contains("QQ")){
			Intent resultIntent5 = new Intent();
			ComponentName cmp = new ComponentName("com.tencent.mobileqq","com.tencent.mobileqq.activity.SplashActivity");
			resultIntent5.setAction(Intent.ACTION_MAIN);
			resultIntent5.addCategory(Intent.CATEGORY_LAUNCHER);
			resultIntent5.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resultIntent5.setComponent(cmp);
			startActivityForResult(resultIntent5, 0);
		} else if (resultStr.contains("照相") || resultStr.contains("相机")){
			Intent resultIntent6 = new Intent();
			ComponentName comp = new ComponentName("com.android.camera","com.android.camera.Camera");
			resultIntent6.setComponent(comp);
			resultIntent6.setAction("android.intent.action.VIEW");
			startActivity(resultIntent6);
		} else if (resultStr.contains("联系人")){
			Intent resultIntent7 = new Intent();
			ComponentName cmp = new ComponentName("com.android.contacts","com.android.contacts.activities.PeopleActivity");
			resultIntent7.setAction(Intent.ACTION_MAIN);
			resultIntent7.addCategory(Intent.CATEGORY_LAUNCHER);
			resultIntent7.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resultIntent7.setComponent(cmp);
			startActivityForResult(resultIntent7, 0);
		} else if (resultStr.contains("啦啦啦")){
			Intent resultIntent7 = new Intent();
			ComponentName cmp = new ComponentName("","");
			resultIntent7.setAction(Intent.ACTION_MAIN);
			resultIntent7.addCategory(Intent.CATEGORY_LAUNCHER);
			resultIntent7.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resultIntent7.setComponent(cmp);
			startActivityForResult(resultIntent7, 0);
		} 
	}

	@Override
	public void onResume(){
		super.onResume();
	}

	@SuppressLint("NewApi")
	public void fullScreenCall() {
		if (Build.VERSION.SDK_INT >= 19) { // 19 or above api
			View decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

		} else {
			// for lower api versions.
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN;

			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	@Override
	protected void onDestroy(){
		if (mHomeLocker != null) {
			mHomeLocker.unlock();
			mHomeLocker = null;
		}
		super.onDestroy();
	}

	public void unlock(View view){
		LogUtil.d("unlock event");
		mHomeLocker.unlock();
		MyApplication.STATE = false;
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogUtil.d("key code is: " + keyCode);
		return keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event){
		LogUtil.d(event.getKeyCode() + "");
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		return;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		return;
	}

	@Override
	public void onStop() {
		super.onStop();
		return;
	}
}
