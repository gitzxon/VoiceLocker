package com.tju.cuijie.voicelocker;

import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class MyApplication extends Application {
	private static Context mContext;
	private MyContacts myContacts;
    public static boolean STATE = false;
    public static String homePkgName = null;
    public static String homeActName = null;
    
    @Override
    public void onCreate() {
    	LogUtil.d("my application");
        mContext = getApplicationContext();
        myContacts = new MyContacts(mContext);
        myContacts.getPhoneContacts();
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setAction(Intent.ACTION_MAIN);

        List<ResolveInfo> resolveInfos = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (int i = 0; i < resolveInfos.size(); i++) {
            String packageName = resolveInfos.get(i).activityInfo.packageName;
            String activityName = resolveInfos.get(i).activityInfo.name;
            if (!packageName.equals(mContext.getPackageName())) {//排除自己的包名　　　　　　
                homePkgName = packageName;
                homeActName = activityName;
            }
        }
    }

    public static Context getmContext() {
        return mContext;
    }
}
