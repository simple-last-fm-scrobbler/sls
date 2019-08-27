package com.adam.aslfms.service;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.adam.aslfms.util.NotificationCreator;

import java.lang.ref.WeakReference;
import java.util.Set;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class ControllerReceiverService extends android.service.notification.NotificationListenerService implements RemoteController.OnClientUpdateListener {

    private static final String TAG = "ControllerReceiverSrvc";
    private static WeakReference<RemoteController> mRemoteController = new WeakReference<>(null);
    private ControllerReceiverCallback controllerReceiverCallback;
    private String track;
    private String artist;
    private String album;
    private String albumArtist;
    private Object durationObject;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @SuppressWarnings("NewApi")
    public void onCreate() {
        super.onCreate();
        if (NotificationManagerCompat.getEnabledListenerPackages (getApplicationContext()).contains(getApplicationContext().getPackageName())) {
            mRemoteController = new WeakReference<>(new RemoteController(this, this));
            mRemoteController.get().setArtworkConfiguration(3000, 3000);
            if (!((AudioManager) getSystemService(Context.AUDIO_SERVICE)).registerRemoteController(mRemoteController.get())) {
                throw new RuntimeException("Error while registering RemoteController!");
            }
            controllerReceiverCallback = new ControllerReceiverCallback();
        }
        Bundle extras = new Bundle();
        extras.putString("track", track);
        extras.putString("artist", artist);
        extras.putString("album", album);
        extras.putString("app_name", albumArtist);
        this.startForeground(NotificationCreator.FOREGROUND_ID, NotificationCreator.prepareNotification(extras, this));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = new Bundle();
        extras.putString("track", track);
        extras.putString("artist", artist);
        extras.putString("album", album);
        extras.putString("app_name", albumArtist);
        this.startForeground(NotificationCreator.FOREGROUND_ID, NotificationCreator.prepareNotification(extras, this));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (mRemoteController != null && mRemoteController.get() != null)
                ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).unregisterRemoteController(mRemoteController.get());
        }
    }

    public static boolean isControllerReceiverServiceEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        return packageNames.contains(context.getPackageName());
    }

    private void disableControllerReceiverService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), ControllerReceiverService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    /* KitKat stuff */

    private boolean isRemoteControllerPlaying;
    private boolean mHasBug = true;

    @Override
    public void onClientChange(boolean clearing) {
        // isListeningAuthorized(ControllerReceiverService.this);
    }

    @Override
    public void onClientPlaybackStateUpdate(int state) {
        this.isRemoteControllerPlaying = state == RemoteControlClient.PLAYSTATE_PLAYING;
    }


    public static long getRemotePlayerPosition() {
        return mRemoteController.get() != null ?
                Math.min(3600000,mRemoteController.get().getEstimatedMediaPosition()) : -1L;
    }

    @Override
    public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
        this.isRemoteControllerPlaying = state == RemoteControlClient.PLAYSTATE_PLAYING;
        mHasBug = false;
        if (currentPosMs > 3600000)
            currentPosMs = -1L;
        SharedPreferences current = getSharedPreferences("current_music", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = current.edit();
        editor.putLong("position", currentPosMs);

        if (isRemoteControllerPlaying) {
            long currentTime = System.currentTimeMillis();
            editor.putLong("startTime", currentTime);
        }
        editor.putBoolean("playing", isRemoteControllerPlaying);
        editor.apply();

        Log.d(TAG, "PlaybackStateUpdate - position stored: " + currentPosMs);

        long position = getRemotePlayerPosition();

        if (durationObject instanceof Double) {
            Log.d(TAG,"duration is Double");
            if (artist != null && !artist.isEmpty())
                controllerReceiverCallback.broadcast(this, artist, track, album, isRemoteControllerPlaying, (Double) durationObject, position, albumArtist, null);
        } else if (durationObject instanceof Long) {
            Log.d(TAG,"duration is Long");
            if (artist != null && !artist.isEmpty())
                controllerReceiverCallback.broadcast(this, artist, track, album, isRemoteControllerPlaying, (Long) durationObject, position, albumArtist, null);
        } else if (durationObject instanceof Integer) {
            Log.d(TAG, "duration is Integer");
            if (artist != null && !artist.isEmpty())
                controllerReceiverCallback.broadcast(this, artist, track, album, isRemoteControllerPlaying, (Integer) durationObject, position, albumArtist, null);
        }
    }

    @Override
    public void onClientTransportControlUpdate(int transportControlFlags) {
        if (mHasBug && mRemoteController.get() != null) {
            long position = mRemoteController.get().getEstimatedMediaPosition();
            if (position > 3600000)
                position = -1L;
            SharedPreferences current = getSharedPreferences("current_music", Context.MODE_PRIVATE);
            current.edit().putLong("position", position).apply();
            if (isRemoteControllerPlaying) {
                long currentTime = System.currentTimeMillis();
                current.edit().putLong("startTime", currentTime).apply();
            }
            Log.d(TAG, "TransportControlUpdate - position stored: " + position);
        }
    }

    @Override
    public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
        // isRemoteControllerPlaying = true;

        durationObject = metadataEditor.getObject(MediaMetadataRetriever.METADATA_KEY_DURATION, 1200); //allow it to pass if not present
        artist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "");
        albumArtist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, "");
        track = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "");
        album = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "");
        Bitmap artwork = metadataEditor.getBitmap(MediaMetadataEditor.BITMAP_KEY_ARTWORK, null);

        controllerReceiverCallback.saveArtwork(this, artwork, artist, track, album);
    }

    // BEGIN listener stuff

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
    }

    @Override
    @TargetApi(24)
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        requestRebind(new ComponentName(getApplicationContext(), ControllerReceiverService.class));
    }

    public static boolean isListeningAuthorized(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = context.getPackageName();

        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

    public static boolean isNotificationListenerServiceEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        return packageNames.contains(context.getPackageName());
    }

    // END listener stuff
}