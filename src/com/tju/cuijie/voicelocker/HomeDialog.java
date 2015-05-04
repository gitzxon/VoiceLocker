package com.tju.cuijie.voicelocker;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class HomeDialog extends AlertDialog {

	public HomeDialog(Activity activity) {
        super(activity, R.style.HomeDialog);
//        super(activity);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.type = TYPE_SYSTEM_ALERT;
        params.dimAmount = 1.0F;
        params.width = 300;
        params.height = 300;
        params.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(params);
        getWindow().setFlags(FLAG_SHOW_WHEN_LOCKED | FLAG_NOT_TOUCH_MODAL, 0xffffff);
        setOwnerActivity(activity);
        setCancelable(false);
    }

    public final boolean dispatchTouchEvent(MotionEvent motionevent) {
        return true;
    }

    protected final void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        FrameLayout framelayout = new FrameLayout(getContext());
        framelayout.setBackgroundColor(Color.YELLOW);
        setContentView(framelayout);
    }
}