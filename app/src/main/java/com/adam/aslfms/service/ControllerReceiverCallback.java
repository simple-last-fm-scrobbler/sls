package com.adam.aslfms.service;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.adam.aslfms.receiver.GenericControllerReceiver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class ControllerReceiverCallback {

    private static final String TAG = "ControllerReceiverCall";
    private static MediaSessionManager.OnActiveSessionsChangedListener sessionListener;
    private MediaController controller;
    private String mPlayer = null;
    private static WeakReference<MediaController> sController = new WeakReference<>(null);
    private MediaController.Callback controllerCallback;
    private Handler handler = new Handler();
    private Bitmap lastBitmap;

    public ControllerReceiverCallback() {

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static void registerFallbackControllerCallback(Context context, ControllerReceiverCallback controllerReceiverCallback) {
        MediaSessionManager mediaSessionManager = ((MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE));
        ComponentName className = new ComponentName(context.getApplicationContext(), ControllerReceiverService.class);
        if (sessionListener != null)
            mediaSessionManager.removeOnActiveSessionsChangedListener(sessionListener);
        sessionListener = list -> controllerReceiverCallback.registerActiveSessionCallback(context, list);
        mediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, className);
        controllerReceiverCallback.registerActiveSessionCallback(context, mediaSessionManager.getActiveSessions(className));
    }

    public void registerActiveSessionCallback(Context context, List<MediaController> controllers) {
        if (controllers.size() > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            controller = controllers.get(0);
            sController = new WeakReference<>(controller);
            if (controllerCallback != null) {
                for (MediaController ctlr : controllers)
                    ctlr.unregisterCallback(controllerCallback);
            } else {
                controllerCallback = new MediaController.Callback() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onPlaybackStateChanged(PlaybackState state) {
                        super.onPlaybackStateChanged(state);
                        if (state == null)
                            return;
                        boolean isPlaying = state.getState() == PlaybackState.STATE_PLAYING;
                        if (!isPlaying) {
                            NotificationManager notificationManager =
                                    ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
                            notificationManager.cancel(0);
                            notificationManager.cancel(8);
                        }
                        if (controller != controller)
                            return; //ignore inactive sessions
                        broadcastControllerState(context, controller, isPlaying);
                    }

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onMetadataChanged(MediaMetadata metadata) {
                        super.onMetadataChanged(metadata);
                        if (controller != controller)
                            return;
                        if (metadata == null)
                            return;
                        broadcastControllerState(context, controller, null);
                    }
                };
            }
            controller.registerCallback(controllerCallback);
            broadcastControllerState(context, controller, null);
        }
    }

    public static long getActiveControllerPosition(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && sController.get() != null) {
            PlaybackState state = sController.get().getPlaybackState();
            if (state != null)
                return state.getPosition();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            long kitkatPosition = ControllerReceiverService.getRemotePlayerPosition();
            if (kitkatPosition >= 0)
                return kitkatPosition;
        }
        SharedPreferences preferences = context.getSharedPreferences("current_music", Context.MODE_PRIVATE);
        long startTime = preferences.getLong("startTime", System.currentTimeMillis());
        long distance = System.currentTimeMillis() - startTime;
        long position = preferences.getLong("position", -1L);
        if (preferences.getBoolean("playing", true) && position >= 0L)
            position += distance;
        return position;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void removeControllerCallback() {
        if (controllerCallback != null && controller != null) {
            controller.unregisterCallback(controllerCallback);
        }
        controllerCallback = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void broadcastControllerState(Context context, MediaController controller, Boolean isPlaying) {
        final MediaController[] controllers = new MediaController[]{controller};
        final Boolean[] playing = new Boolean[]{isPlaying};
        handler.postDelayed(() -> {
            mPlayer = controllers[0].getPackageName();
            MediaMetadata metadata = controllers[0].getMetadata();
            PlaybackState playbackState = controllers[0].getPlaybackState();
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

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_filter_20min", true) && duration > 1200000)
                return;
            if (playing[0] == null)
                playing[0] = playbackState != null && playbackState.getState() == PlaybackState.STATE_PLAYING;

            saveArtwork(context, artwork, artist, track, album);

            String player = controllers[0].getPackageName();
            if ("com.aimp.player".equals(player)) // Aimp is awful
                position = -1;
            broadcast(context, artist, track, album, playing[0], duration, position, albumArtist, player);
        }, 100);
    }

    public void broadcast(Context context, String artist, String track, String album, boolean playing, int duration, long position, String albumArtist, String player) {
        Intent localIntent = new Intent(GenericControllerReceiver.ACTION_INTENT);
        localIntent.setComponent(new ComponentName(context.getPackageName(),"com.adam.aslfms.receiver.GenericControllerReceiver"));
        localIntent.putExtra("artist", artist);
        localIntent.putExtra("track", track);
        localIntent.putExtra("album", album);
        localIntent.putExtra("albumArtist", albumArtist);
        localIntent.putExtra("playing", playing);
        localIntent.putExtra("duration", duration);
        if (mPlayer == null)
            mPlayer = player;
        localIntent.putExtra("player", mPlayer);
        Log.d("title", track);
        if (position != -1)
            localIntent.putExtra("position", position);
        Log.d(TAG,"title "+track);
    }

    public void broadcast(Context context, String artist, String track, String album, boolean playing, long duration, long position, String albumArtist, String player) {
        Intent localIntent = new Intent(GenericControllerReceiver.ACTION_INTENT);
        localIntent.setComponent(new ComponentName(context.getPackageName(),"com.adam.aslfms.receiver.GenericControllerReceiver"));
        localIntent.putExtra("artist", artist);
        localIntent.putExtra("track", track);
        localIntent.putExtra("album", album);
        localIntent.putExtra("albumArtist", albumArtist);
        localIntent.putExtra("playing", playing);
        localIntent.putExtra("duration", duration);
        if (mPlayer == null)
            mPlayer = player;
        localIntent.putExtra("player", mPlayer);
        Log.d("title", track);
        if (position != -1)
            localIntent.putExtra("position", position);
        context.sendBroadcast(localIntent);
        Log.d(TAG,"title "+track);
    }

    public void broadcast(Context context, String artist, String track, String album, boolean playing, double duration, long position, String albumArtist, String player) {
        Intent localIntent = new Intent(GenericControllerReceiver.ACTION_INTENT);
        localIntent.setComponent(new ComponentName(context.getPackageName(),"com.adam.aslfms.receiver.GenericControllerReceiver"));
        localIntent.putExtra("artist", artist);
        localIntent.putExtra("track", track);
        localIntent.putExtra("album", album);
        localIntent.putExtra("albumArtist", albumArtist);
        localIntent.putExtra("playing", playing);
        localIntent.putExtra("duration", duration);
        if (mPlayer == null)
            mPlayer = player;
        localIntent.putExtra("player", mPlayer);
        Log.d("title", track);
        if (position != -1)
            localIntent.putExtra("position", position);
        context.sendBroadcast(localIntent);
        Log.d(TAG,"title "+track);
    }

    public void saveArtwork(Context context, Bitmap artwork, String artist, String track, String album) {
        File artworksDir = new File(context.getCacheDir(), "artworks");
        if (artwork != null && (artworksDir.exists() || artworksDir.mkdir())) {
            File artworkFile = new File(artworksDir, artist + track + ".png");
            if (!artworkFile.exists())
                try {
                    //noinspection ResultOfMethodCallIgnored
                    artworkFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            if (artworkFile.length() == 0) {
                FileOutputStream fos = null;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                artwork.compress(Bitmap.CompressFormat.PNG, 100, stream);
                try {
                    fos = new FileOutputStream(artworkFile);
                    stream.writeTo(fos);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null)
                        {
                            fos.flush();
                            fos.getFD().sync();
                            fos.close();
                        }
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (lastBitmap != null) {
                lastBitmap.recycle();
            }
            lastBitmap = artwork;
        }
    }
}
