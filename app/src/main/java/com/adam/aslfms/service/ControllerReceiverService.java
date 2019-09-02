package com.adam.aslfms.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.adam.aslfms.receiver.GenericControllerReceiver;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.NotificationCreator;
import com.adam.aslfms.util.Util;

import java.util.List;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class ControllerReceiverService extends NotificationListenerService {
    
    private static final String TAG = "ControllerReceiverSrvc";
    private Handler handler = new Handler();
    private String mPlayer = null;

    private MediaSessionManager mediaSessionManager;
    private ComponentName componentName;
    private MediaController controller;
    private List<MediaController> controllers;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @SuppressWarnings("NewApi")
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"created");
        AppSettings settings = new AppSettings(this);
        
        //registerControllerReceiverCallback();
        Bundle extras = new Bundle();
        extras.putString("track", "");
        extras.putString("artist", "");
        extras.putString("album", "");
        extras.putString("app_name", "");
        this.startForeground(NotificationCreator.FOREGROUND_ID, NotificationCreator.prepareNotification(extras, this));
        initMediaListener();
        if (!settings.isActiveAppEnabled(Util.checkPower(this))) {
            this.stopForeground(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"started");
        AppSettings settings = new AppSettings(this);
        Bundle extras = new Bundle();
        extras.putString("track", "");
        extras.putString("artist", "");
        extras.putString("album", "");
        extras.putString("app_name", "");
        this.startForeground(NotificationCreator.FOREGROUND_ID, NotificationCreator.prepareNotification(extras, this));
        initMediaListener();
        if (!settings.isActiveAppEnabled(Util.checkPower(this))) {
            this.stopForeground(true);
            return Service.START_NOT_STICKY;
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"destroyed");
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


    public void initMediaListener(){
        componentName = new ComponentName(this, ControllerReceiverService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            mediaSessionManager.addOnActiveSessionsChangedListener(sessionsChangedListener, componentName);

            this.controllers = mediaSessionManager.getActiveSessions(componentName);
        }
    }

    MediaSessionManager.OnActiveSessionsChangedListener sessionsChangedListener = new MediaSessionManager.OnActiveSessionsChangedListener() {
        public void onActiveSessionsChanged(List<MediaController> controllers) {
            MediaController controller;
            MediaController.Callback controllerCallback;
            Log.d(TAG, "onActiveSessionsChanged");
            if (controllers.size() > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                controller = controllers.get(0);
                controllerCallback = new MediaController.Callback() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onPlaybackStateChanged(PlaybackState state) {
                        super.onPlaybackStateChanged(state);
                        if (state == null)
                            return;
                        boolean isPlaying = state.getState() == PlaybackState.STATE_PLAYING;
                        broadcastControllerState(controller, isPlaying);
                    }

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onMetadataChanged(MediaMetadata metadata) {
                        super.onMetadataChanged(metadata);
                        if (metadata == null)
                            return;
                        broadcastControllerState(controller, null);
                    }
                };
                controller.registerCallback(controllerCallback);
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void broadcastControllerState(MediaController mController, Boolean isPlaying) {
        final MediaController mediaController = mController;
        final Boolean playState = isPlaying;
        handler.postDelayed(() -> {
            mPlayer = mediaController.getPackageName();
            MediaMetadata metadata = mediaController.getMetadata();
            PlaybackState playbackState = mediaController.getPlaybackState();
            if (metadata == null)
                return;
            String artist = null;
            try {
                artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                if (artist == null)
                    artist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            } catch (Exception ignored) {
            }
            String albumArtist = null;
            try {
                albumArtist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            } catch (Exception ignored){
            }
            String track = null;
            try {
                track = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            } catch (Exception ignored) {
            }
            String album = null;
            try {
                album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
            } catch (Exception ignored) {
            }
            Bitmap artwork = null;
            try {
                artwork = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
                if (artwork == null)
                    artwork = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
            } catch (Exception ignored) {
            }

            double duration;
            try {
                duration =(double) metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
            } catch (RuntimeException ignored) {
                duration = 0;
            }
            long position = duration == 0 || playbackState == null ? -1 : playbackState.getPosition();

            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_filter_20min", true) && duration > 1200000)
                return;
            boolean tempPlaying = false;
            if (playState == null)
                tempPlaying = playbackState != null && playbackState.getState() == PlaybackState.STATE_PLAYING;

            String player = mediaController.getPackageName();
            if ("com.aimp.player".equals(player)) // Aimp is awful
                position = -1;
            broadcast(artist, track, album, tempPlaying, duration, position, albumArtist, player);
        }, 50);
        Log.d(TAG, "broadcast sent: controller state");
    }

    public void broadcast(String artist, String track, String album, boolean playing, double duration, long position, String albumArtist, String player) {
        Intent localIntent = new Intent(GenericControllerReceiver.ACTION_INTENT);
        localIntent.setComponent(new ComponentName(this.getPackageName(),"com.adam.aslfms.receiver.GenericControllerReceiver"));
        localIntent.putExtra("artist", artist);
        localIntent.putExtra("track", track);
        localIntent.putExtra("album", album);
        localIntent.putExtra("albumArtist", albumArtist);
        localIntent.putExtra("playing", playing);
        localIntent.putExtra("duration", duration);
        localIntent.putExtra("player", mPlayer);
        if (position != -1)
            localIntent.putExtra("position", position);
        this.sendBroadcast(localIntent);
        Log.d(TAG, "broadcast sent: double duration");
    }
}