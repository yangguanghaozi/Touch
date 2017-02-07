package com.carl.touch;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    public static final String TYPE = "type";
    private static final String TAG = "Notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int type = intent.getIntExtra(TYPE, -1);

        if (type != -1) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(type);
        }

        if (action.equals("notification_clicked")) {
            Log.i(TAG, "click");
            FloatButtonService.resume();
        }

        if (action.equals("notification_cancelled")) {
            Log.i(TAG, "cancle");
            FloatButtonService.resume();
        }
    }
}