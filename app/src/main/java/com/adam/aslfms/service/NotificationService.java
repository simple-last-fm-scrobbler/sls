package com.adam.aslfms.service;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 4-Eyes on 15/3/2017.
 *
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService {

    private NotificationHandler handler = new NotificationHandler();
    private AppSettings settings;

    private long defaultSongDuration = 3 * 60000; // Three minutes

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
        START(0),
        RESUME(1),
        PAUSE(2),
        COMPLETE(3);

        private int value;

        BroadcastState(int i) {
            value = i;
        }

        public int getValue() {
            return value;
        }
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

        long totalPlayTime() {
            long total = 0;
            for (long playtime : playTimes) {
                total += playtime;
            }
            return total;
        }

        long finalisePlayTime(long currentTrackDuration) {
            if (currentState.equals(PlayingState.PLAYING)) {
                long lastPlayTime = System.currentTimeMillis() - lastStateChangedTime;
                long totalPlayTimes = totalPlayTime();
                if (totalPlayTimes + lastPlayTime > currentTrackDuration) {
                    long overlapTime = (totalPlayTimes + lastPlayTime - currentTrackDuration);
                    playTimes.add(lastPlayTime - overlapTime);
                    return overlapTime;
                }
                playTimes.add(lastPlayTime);
            }
            return 0;
        }

        boolean sameTrack(TrackData other) {
            return this.title != null && this.artist != null && this.album != null &&
                    other.title != null && other.artist != null && other.album != null &&
                    this.title.equals(other.title) && this.artist.equals(other.artist) && this.album.equals(other.album);
        }

        public boolean isComplete(long trackDuration) {
            long playTime = totalPlayTime();
            return playTime >= (trackDuration - 5000); // Has been played for within 5 seconds of the actual song length.
        }
    }

    private class LfmApi {

        private String getTrackInfo(TrackData data) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(NetApp.LASTFM.getWebserviceUrl(settings));

                conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(7000);
                conn.setConnectTimeout(7000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                Map<String, Object> params = new LinkedHashMap<>();
                params.put("method", "track.getInfo");
                params.put("track", data.title);
                params.put("artist", data.artist);
                params.put("api_key", settings.rcnvK(settings.getAPIkey()));
                params.put("format", "json");

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

                conn.getOutputStream().write(postDataBytes);

                conn.connect();

                int resCode = conn.getResponseCode();
                BufferedReader r;
                if (resCode == -1) {
                    return "";
                } else if (resCode == 200) {
                    r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
                return stringBuilder.toString();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return "";
        }

        long getTrackDuration(TrackData data) {
            String response = getTrackInfo(data);

            try {
                JSONObject object = new JSONObject(response);
                if (object.has("error")) {
                    // TODO maybe do something with this
                    int code = object.getInt("error");
                    Log.e("LfmAPI", String.format("Failed to get track duration with error code %s", code));
                } else {
                    long duration = object.getJSONObject("track").getLong("duration");
                    Log.i("LfmAPI", String.format("Successfully got duration for song %s, by %s",
                            data.title, data.artist));
                    return duration;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return defaultSongDuration;

        }
    }

    private class AppleMusicBroadcaster {

        private String appName = "Apple Music";
        private String packageName = "com.apple.android.music";

        void broadcast(TrackData data, BroadcastState state, long duration) {
            Intent broadcastIntent = new Intent("com.adam.aslfms.notify.playstatechanged");
            broadcastIntent.putExtra("state", state.getValue());
            broadcastIntent.putExtra("app-name", appName);
            broadcastIntent.putExtra("app-package", packageName);
            broadcastIntent.putExtra("track", data.title);
            broadcastIntent.putExtra("artist", data.artist);
            broadcastIntent.putExtra("album", data.album);
            broadcastIntent.putExtra("duration", (int)(duration / 1000));

            sendBroadcast(broadcastIntent);
        }
    }

    private class NotificationHandler {

        private TrackData currentTrack;
        private long currentTrackDuration = defaultSongDuration;
        private AppleMusicBroadcaster broadcaster = new AppleMusicBroadcaster();
        private LfmApi api = new LfmApi();
        private AsyncTask<TrackData, Void, Long> trackInfoTask = null;

        void push(TrackData data) {
            boolean newTrack = false;
            if (currentTrack == null) {
                currentTrack = data;
                newTrack = true;
            } else {
                if (currentTrack.sameTrack(data)) {
                    boolean stateChanged = currentTrack.mergeSame(data);
                    if (stateChanged) {
                        Log.i("AppleNotification", "State has changed to: " + currentTrack.currentState);
                        switch (currentTrack.currentState) {
                            case UNKNOWN:
                                break;
                            case PLAYING:
                                Log.i("AppleNotification", "Broadcasting track resumed for track " + currentTrack.title);
                                broadcaster.broadcast(data, BroadcastState.RESUME, currentTrackDuration);
                                break;
                            case PAUSED:
                                Log.i("AppleNotification", "Broadcasting track paused for track " + currentTrack.title);
                                broadcaster.broadcast(data, BroadcastState.PAUSE, currentTrackDuration);
                                break;
                        }
                    }
                } else {
                    Log.i("AppleNotification", "New track detected");
                    // Check to see if there is overlap time with the next song
                    // This is because if you're in the application the notifications don't always appear.
                    long overlapTime = currentTrack.finalisePlayTime(currentTrackDuration);
                    data.playTimes.add(overlapTime);

                    // This attempts to verify that a track is properly completed
                    if (data.isComplete(currentTrackDuration)) {
                        Log.i("AppleNotification", "Broadcasting track complete for track " + currentTrack.title);
                        broadcaster.broadcast(currentTrack, BroadcastState.COMPLETE, currentTrackDuration);
                    }

                    currentTrack = data;
                    newTrack = true;
                }
            }

            if (newTrack) {
                if (trackInfoTask != null) {
                    trackInfoTask.cancel(true);
                }

                trackInfoTask = new AsyncTask<TrackData, Void, Long>() {
                    TrackData trackData;
                    @Override
                    protected Long doInBackground(TrackData... trackDatas) {
                        trackData = trackDatas[0];
                        Log.i("AppleNotification", "Loading new data for song " + trackData.title);
                        return api.getTrackDuration(trackData);
                    }

                    @Override
                    protected void onCancelled() {
                        super.onCancelled();
                        currentTrackDuration = defaultSongDuration;
                    }

                    @Override
                    protected void onPostExecute(Long result) {
                        currentTrackDuration = result;
                        Log.i("AppleNotification", "Broadcasting song Start for " + trackData.title);
                        broadcaster.broadcast(trackData, BroadcastState.START, currentTrackDuration);
                    }
                }.execute(currentTrack);
            }
        }
    }
}
