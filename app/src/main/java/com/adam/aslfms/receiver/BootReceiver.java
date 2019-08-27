package com.adam.aslfms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.adam.aslfms.service.ControllerReceiverCallback;
import com.adam.aslfms.service.ControllerReceiverService;
import com.adam.aslfms.service.NotificationBarService;

public class BootReceiver extends BroadcastReceiver {

    private ControllerReceiverCallback controllerCallback = null;
    private static final String TAG = "BootReceiver";

    public static final String NOTIFICATION_RECEIVER = "com.adam.aslfms.notificationreceiver";
    public static final String NOTIFICATION_RECEIVER_WAKE = "com.adam.aslfms.notificationreceiverwake";

    @Override
    public void onReceive(Context context, Intent intent) {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent i = new Intent(context, NotificationBarService.class);
            if (intent == null || intent.getAction() == NOTIFICATION_RECEIVER) {
                i.setAction(NotificationBarService.ACTION_NOTIFICATION_BAR_UPDATE);
                i.putExtra("track", "");
                i.putExtra("artist", "");
                i.putExtra("album", "");
                i.putExtra("app_name", "");
            } else if (intent.getAction() == NOTIFICATION_RECEIVER_WAKE) {
                i.setAction(NotificationBarService.ACTION_NOTIFICATION_BAR_WAKE);
            }
            context.startService(i);
            context.startService(new Intent(context, ControllerReceiverService.class));
            if (controllerCallback == null)
                controllerCallback = new ControllerReceiverCallback();
            if (ControllerReceiverService.isListeningAuthorized(context))
                ControllerReceiverCallback.registerFallbackControllerCallback(context, controllerCallback);
        }
    }
}
