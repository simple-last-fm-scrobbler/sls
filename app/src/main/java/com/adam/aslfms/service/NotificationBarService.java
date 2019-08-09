package com.adam.aslfms.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.util.Log;

import com.adam.aslfms.R;
import com.adam.aslfms.SettingsActivity;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

public class NotificationBarService extends Service {

    private static final String TAG = "NotificationBarService";

    public static final String ACTION_NOTIFICATION_BAR_UPDATE = "com.adam.aslfms.service.notificationbarupdate";

    private AppSettings settings;
    private ScrobblesDatabase mDb;

    private NetworkerManager mNetManager;

    public static final String SLS_CHANNEL_ID = "sls_channel";

    private String album = null;
    private String artist = null;
    private String track = null;
    private String app_name = null;

    Context mCtx = this;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        settings = new AppSettings(this);
        mDb = new ScrobblesDatabase(this);
        mDb.open();
        mNetManager = new NetworkerManager(this, mDb);

        int sdk = Build.VERSION.SDK_INT;
        if (sdk == Build.VERSION_CODES.GINGERBREAD || sdk == Build.VERSION_CODES.GINGERBREAD_MR1) {
            if (settings.isActiveAppEnabled(Util.checkPower(mCtx))) {
                if (track != null) {
                    String ar = artist;
                    String tr = track;
                    String al = album;
                    String api = app_name;

                    // Heart intent
                    Intent heartIntent = new Intent(mCtx, ScrobblingService.class);
                    heartIntent.setAction(ScrobblingService.ACTION_HEART);
                    PendingIntent  heartPendingIntent =  PendingIntent.getService(mCtx, 0, heartIntent, 0);
                    NotificationCompat.Action heartAction = new NotificationCompat.Action.Builder(R.drawable.ic_heart, getString(R.string.heart_title), heartPendingIntent).build();

                    // Copy intent
                    Intent copyIntent = new Intent(mCtx, ScrobblingService.class);
                    copyIntent.setAction(ScrobblingService.ACTION_COPY);
                    PendingIntent copyPendingIntent =  PendingIntent.getService(mCtx, 0, copyIntent, 0);
                    NotificationCompat.Action copyAction = new NotificationCompat.Action.Builder(R.drawable.ic_content_copy, getString(R.string.copy_title), copyPendingIntent).build();

                    Intent targetIntent = new Intent(mCtx, SettingsActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(mCtx)
                                    .setContentTitle(tr + " by " + ar )
                                    .setSmallIcon(R.drawable.ic_icon)
                                    .setColor(Color.RED)
                                    .setContentText(al + " : " + api)
                                    .setPriority(NotificationCompat.PRIORITY_MIN)
                                    .addAction(heartAction)
                                    .addAction(copyAction)
                                    .setContentIntent(contentIntent);

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
                        builder.setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(),
                                R.drawable.ic_icon));
                    }

                    this.startForeground(24689, builder.build());
                }
            } else {
                this.stopForeground(true); // TODO: test if this conflicts/stops scrobbles
            }
        }
    }

    @Override
    public void onDestroy() {
        mDb.close();
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        handleCommand(i, startId);
        if (settings.isActiveAppEnabled(Util.checkPower(mCtx))) {
            if (track != null) {
                String ar = artist;
                String tr = track;
                String al = album;
                String api = app_name;

                // Heart intent
                Intent heartIntent = new Intent(mCtx, ScrobblingService.class);
                heartIntent.setAction(ScrobblingService.ACTION_HEART);
                PendingIntent  heartPendingIntent =  PendingIntent.getService(mCtx, 0, heartIntent, 0);
                NotificationCompat.Action heartAction = new NotificationCompat.Action.Builder(R.drawable.ic_heart, getString(R.string.heart_title), heartPendingIntent).build();

                // Copy intent
                Intent copyIntent = new Intent(mCtx, ScrobblingService.class);
                copyIntent.setAction(ScrobblingService.ACTION_COPY);
                PendingIntent copyPendingIntent =  PendingIntent.getService(mCtx, 0, copyIntent, 0);
                NotificationCompat.Action copyAction = new NotificationCompat.Action.Builder(R.drawable.ic_content_copy, getString(R.string.copy_title), copyPendingIntent).build();

                Intent targetIntent = new Intent(mCtx, SettingsActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(mCtx)
                                .setContentTitle(tr + " by " + ar )
                                .setSmallIcon(R.drawable.ic_icon)
                                .setColor(Color.RED)
                                .setContentText(al + " : " + api)
                                .setPriority(NotificationCompat.PRIORITY_MIN)
                                .addAction(heartAction)
                                .addAction(copyAction)
                                .setContentIntent(contentIntent);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
                    builder.setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(),
                            R.drawable.ic_icon));
                }

                this.startForeground(24689, builder.build());
            }
        } else {
            this.stopForeground(true); // TODO: test if this conflicts/stops scrobbles
        }
        return Service.START_STICKY;
    }

    private void handleCommand(Intent i, int startId) {
        if (i == null) {
            Log.e(TAG, "got null intent");
            return;
        }
        Log.e(TAG, "got intent");
        String action = i.getAction();
        Bundle extras = i.getExtras();
        if (action.equals(ACTION_NOTIFICATION_BAR_UPDATE)) {
            track = extras.getString("track");
            artist = extras.getString("artist");
            album = extras.getString("album");
            app_name = extras.getString("app_name");
        }
    }
}
