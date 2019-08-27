package com.adam.aslfms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.adam.aslfms.service.ControllerReceiverCallback;
import com.adam.aslfms.service.ControllerReceiverService;
import com.adam.aslfms.service.NotificationBarService;
import com.adam.aslfms.service.ScrobblingService;

public class BootReceiver extends BroadcastReceiver {

    private ControllerReceiverCallback controllerCallback = null;
    private static final String TAG = "BootReceiver";

    public static final String SYSTEM_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "launching");
            Intent i = new Intent(context, NotificationBarService.class);
            if (intent != null || intent.getAction() == SYSTEM_ACTION) {
                i.setAction(NotificationBarService.ACTION_NOTIFICATION_BAR_UPDATE);
                i.putExtra("track", "");
                i.putExtra("artist", "");
                i.putExtra("album", "");
                i.putExtra("app_name", "");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(i);
                    context.startForegroundService(new Intent(context, ScrobblingService.class));
                    context.startForegroundService(new Intent(context, ControllerReceiverService.class));
                } else {
                    context.startService(i);
                    context.startService(new Intent(context, ScrobblingService.class));
                    context.startService(new Intent(context, ControllerReceiverService.class));
                }
                if (controllerCallback == null)
                    controllerCallback = new ControllerReceiverCallback();
                if (ControllerReceiverService.isListeningAuthorized(context))
                    ControllerReceiverCallback.registerFallbackControllerCallback(context, controllerCallback);
            }
        }
    }
}
