package com.tju.cuijie.voicelocker;

import android.app.Activity;

public class HomeLocker {
	private HomeDialog mHomeDialog;
	public void lock(Activity activity) {
        if (mHomeDialog == null) {
            mHomeDialog = new HomeDialog(activity);
            mHomeDialog.show();
        }
    }

    public void unlock() {
        if (mHomeDialog != null) {
            mHomeDialog.dismiss();
            mHomeDialog = null;
        }
    }
}