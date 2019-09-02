package com.adam.aslfms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.adam.aslfms.util.Util;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    public static final String SYSTEM_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "launching_on_boot");
            if (intent != null || intent.getAction() == SYSTEM_ACTION) {
                Util.runServices(context);
            }
        }
    }
}
