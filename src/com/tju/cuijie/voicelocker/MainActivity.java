package com.tju.cuijie.voicelocker;

import com.tju.cuijie.voicelocker.service.LockService;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        LogUtil.d("MainActivity onCreate");
        
        //全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        Intent i = new Intent(this, LockService.class);
        startService(i);
        
        //home键处理
        if (MyApplication.STATE == false) {
        	LogUtil.d("not in our locker, call the system's home");
            if(MyApplication.homePkgName != null && MyApplication.homePkgName != "") {
                ComponentName componentName = new ComponentName(MyApplication.homePkgName, MyApplication.homeActName);
                Intent intent = new Intent();
                intent.setComponent(componentName);

                if(intent != null) {
                    startActivity(intent);
                } else {
                    LogUtil.d("intent is null");
                }
            }
            finish();
        } else { //锁屏中，不做任何处理
        	LogUtil.d("in my locker, home key is not used");
        	
        }
	}
	
	@Override
    protected void onResume() {
        LogUtil.d("MainActivity onResume");
        super.onResume();
    }

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}