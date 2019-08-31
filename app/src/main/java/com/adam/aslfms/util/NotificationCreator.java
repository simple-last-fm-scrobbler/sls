package com.adam.aslfms.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.adam.aslfms.R;
import com.adam.aslfms.SettingsActivity;
import com.adam.aslfms.service.NetworkerManager;
import com.adam.aslfms.service.ScrobblingService;

public class NotificationCreator {

    private static final String TAG = "NotificationCreator";
    public final static int FOREGROUND_ID = 1098733;
    public final static String FOREGROUND_CHANNEL_ID = "com.adam.aslfms";
    private static AppSettings settings;

    private NetworkerManager mNetManager;

    private NotificationManager mNotificationManager;

    public static void initChannels(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
         if (!settings.isActiveAppEnabled(Util.checkPower(context))) {
            notificationManager.cancel(FOREGROUND_ID);
        }
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID,
                context.getString(R.string.app_name_short),
                Util.notificationStringToInt(context));
        channel.setDescription(context.getString(R.string.app_name));
        channel.setSound(null, null);
        notificationManager.createNotificationChannel(channel);
    }

    public static Notification prepareNotification(Bundle extras, Context context) {
        settings = new AppSettings(context);
        // handle build version above android oreo
        initChannels(context);
        ////
        Log.d(TAG, "normal update for notification bar!");
        String album = null;
        String artist = null;
        String track = null;
        String app_name = null;
        if (extras != null) {
            track = extras.getString("track");
            artist = extras.getString("artist");
            album = extras.getString("album");
            app_name = extras.getString("app_name");
        }
        track = (track == null ? "" : track);
        artist = (artist == null ? "" : artist);
        album = (album == null ? "" : album);
        app_name = (app_name == null ? "" : app_name);

        ////

        // if min sdk goes below honeycomb
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }*/

        Intent targetIntent = new Intent(context, SettingsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Heart intent
        Intent heartIntent = new Intent(context, ScrobblingService.class);
        heartIntent.setAction(ScrobblingService.ACTION_HEART);
        PendingIntent heartPendingIntent = PendingIntent.getService(context, 0, heartIntent, 0);
        NotificationCompat.Action heartAction = new NotificationCompat.Action.Builder(R.drawable.ic_heart, context.getString(R.string.heart_title), heartPendingIntent).build();

        // Copy intent
        Intent copyIntent = new Intent(context, ScrobblingService.class);
        copyIntent.setAction(ScrobblingService.ACTION_COPY);
        PendingIntent copyPendingIntent = PendingIntent.getService(context, 0, copyIntent, 0);
        NotificationCompat.Action copyAction = new NotificationCompat.Action.Builder(R.drawable.ic_content_copy, context.getString(R.string.copy_title), copyPendingIntent).build();

        // notification builder
        NotificationCompat.Builder notificationBuilder = null;
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID);
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        } else {
            notificationBuilder = new NotificationCompat.Builder(context);
        }
        if (track.equals("")){
            notificationBuilder
                    .setContentTitle("");
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP){
                notificationBuilder
                        .setSmallIcon(R.drawable.ic_icon_white);
            } else {
                notificationBuilder
                        .setSmallIcon(R.drawable.ic_icon);
            }
            notificationBuilder
                    .setContentText("");
        } else {
            notificationBuilder
                    .setContentTitle(track + " " + context.getString(R.string.by) + " " + artist);
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP){
                notificationBuilder
                        .setSmallIcon(R.drawable.ic_icon_white);
            } else {
                notificationBuilder
                        .setSmallIcon(R.drawable.ic_icon);
            }
            notificationBuilder
                    .setContentText(album + " : " + app_name);
        }
        if (settings.isActiveAppEnabled(Util.checkPower(context))){
            notificationBuilder
                            .setAutoCancel(false)
                            .setOngoing(true);
        } else {
            notificationBuilder
                    .setAutoCancel(true)
                    .setOngoing(false);
        }
        notificationBuilder
                .setContentIntent(contentIntent)
                .setColor(Color.RED)
                .setPriority(Util.oldNotificationStringToInt(context))
                .addAction(heartAction)
                .addAction(copyAction)
                .setChannelId(FOREGROUND_CHANNEL_ID);

        notification = notificationBuilder.build();

        return notification;
    }
}
