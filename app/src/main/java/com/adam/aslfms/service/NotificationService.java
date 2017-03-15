package com.adam.aslfms.service;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.adam.aslfms.util.AppSettings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 4-Eyes on 15/3/2017.
 *
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService {

    private NotificationHandler handler = new NotificationHandler();
    private AppSettings settings;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        settings = new AppSettings(this);
        Log.i("AppleNotification", "Notification listener created");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        super.onNotificationPosted(notification);

        // Filter out all notifications that do not come from Apple Music
        if (!notification.getPackageName().equals("com.apple.android.music")) return;

        // Attempt to retrieve the
        RemoteViews views = notification.getNotification().bigContentView;
        if (views == null) return;

        Log.i("AppleNotification", "New notification being processed");

        TrackData data = new TrackData();
        int dataCount = 0;
        try {
            Field field = views.getClass().getDeclaredField("mActions");
            field.setAccessible(true);

            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);

            for (Parcelable p : actions) {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);

                int tag = parcel.readInt();
                if (tag != 2 && tag != 12) continue;
                parcel.readInt();
                String methodName = parcel.readString();
                if (methodName == null) continue;

                if (tag == 2) {
                    // This is for ReflectionAction objects
                    switch (methodName) {
                        case "setText": {
                            parcel.readInt();

                            String text = TextUtils.CHAR_SEQUENCE_CREATOR
                                    .createFromParcel(parcel).toString().trim();
                            switch (dataCount) {
                                case 0:
                                    data.title = text;
                                    break;
                                case 1:
                                    data.album = text;
                                    break;
                                case 2:
                                    data.artist = text;
                                    break;
                            }
                            dataCount++;
                            break;
                        }
                        case "setContentDescription": {
                            parcel.readInt();

                            String text = TextUtils.CHAR_SEQUENCE_CREATOR
                                    .createFromParcel(parcel).toString().trim();
                            data.setContentType(text);
                            break;
                        }
                        case "setEnabled":
                            parcel.readInt();

                            boolean enabled = parcel.readByte() != 0;
                            // TODO see if this can be used in help determine when a song ends
                            break;
                        default:
                            break;
                    }
                } else {
                    // This is for BitmapReflectionAction objects (tag 12)
                    parcel.readInt();
                    // TODO work out if you want this or not
                    Bitmap albumArtwork = Bitmap.CREATOR.createFromParcel(parcel);

                }
            }
        } catch (Exception e) {
            Log.e("AppleNotification", "Failed to parse Apple Notification");
            e.printStackTrace();
        }

        data.startTime = new Date(System.currentTimeMillis());

        handler.push(data);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {
        super.onNotificationRemoved(notification);
    }

    private enum PlayingState {
        UNKNOWN,
        PLAYING,
        PAUSED,
    }

    private enum BroadcastState {
        START,
        RESUME,
        PAUSE,
        COMPLETE
    }

    private class TrackData {

        private String artist;
        private String title;
        private String album;
        private Date startTime;
        private PlayingState currentState = PlayingState.UNKNOWN;
        private ArrayList<Long> playTimes = new ArrayList<>();
        private long lastStateChangedTime;

        void setContentType(String contentType) {
            switch (contentType) {
                case "Pause":
                    currentState = PlayingState.PLAYING;
                    break;
                case "Play":
                    currentState = PlayingState.PAUSED;
                    break;
                default:
                    currentState = PlayingState.UNKNOWN;
                    break;
            }
            this.lastStateChangedTime = System.currentTimeMillis();
        }

        boolean mergeSame(TrackData data) {
            if (this.currentState.equals(data.currentState)) return false;
            if (this.currentState.equals(PlayingState.PLAYING)
                    && data.currentState.equals(PlayingState.PAUSED)) {
                playTimes.add(data.startTime.getTime() - this.lastStateChangedTime);
            }
            this.currentState = data.currentState;
            lastStateChangedTime = System.currentTimeMillis();
            return true;
        }

        void finalisePlayTime() {
            if (currentState.equals(PlayingState.PLAYING)) {
                playTimes.add(System.currentTimeMillis() - lastStateChangedTime);
            }
        }

        boolean sameTrack(TrackData other) {
            return this.title != null && this.artist != null && this.album != null &&
                    other.title != null && other.artist != null && other.album != null &&
                    this.title.equals(other.title) && this.artist.equals(other.artist) && this.album.equals(other.album);
        }
    }

    private class LfmApi {

    }

    private class AppleMusicBroadcaster {

        private String appName = "Apple Music";
        private String packageName = "com.apple.android.music";


    }

    private class NotificationHandler {

        private TrackData currentTrack;
        private long currentTrackDuration;
        private long defaultSongDuration = 3 * 60000; // Three minutes

        void push(TrackData data) {
            boolean newTrack = false;
            if (currentTrack == null) {
                currentTrack = data;
                newTrack = true;
            } else {
                if (currentTrack.sameTrack(data)) {
                    boolean stateChanged = currentTrack.mergeSame(data);
                    if (stateChanged) {
                        // TODO add broadcasting
                    }
                } else {
                    Log.i("AppleNotification", "New track detected");
                    currentTrack.finalisePlayTime();

                    // TODO add broadcasting
                    currentTrack = data;
                    newTrack = true;
                }
            }

            if (newTrack) {
                Log.i("AppleNotification", "Loading new data for song " + data.title);
                // TODO add broadcasting
            }
        }
    }
}
