package com.adam.aslfms.service.applemusic;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.adam.aslfms.util.AppSettings;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 4-Eyes on 16/3/2017.
 * This handles notifications which are parsed to it. Determining when to merge notifications and
 * when to broadcast the states.
 */
class NotificationHandler {

    private final AppSettings settings;
    private TrackData currentTrack;
    private long currentTrackDuration = NotificationService.DEFAULT_SONG_LENGTH;
    private AppleMusicBroadcaster broadcaster;
    private LfmApi api;
    private AsyncTask<TrackData, Void, Long> trackInfoTask = null;

    private static final long TRACK_TIMER_INTERVAL = 45000; // 45 seconds
    private static final long TRACK_TIMER_REPEAT_CUTOFF = 10; // How many times to call the timer before killing it
    private Timer trackTimer;

    NotificationHandler(Context context, AppSettings settings) {
        broadcaster = new AppleMusicBroadcaster(context);
        this.settings = settings;
        api = new LfmApi(settings);
    }

    /**
     * Pushes a new notification to the handler for processing.
     * @param data the new notification being parsed.
     */
    void push(TrackData data) {
        boolean newTrack = false;
        if (currentTrack == null) {
            currentTrack = data;
            newTrack = true;
        } else {
            if (currentTrack.sameTrack(data) && currentTrack.currentTotalPlayTime() < currentTrackDuration) {
                boolean stateChanged = currentTrack.mergeSame(data);
                if (stateChanged) {
                    Log.i("AppleNotification", "State has changed to: " + currentTrack.getCurrentState());
                    switch (currentTrack.getCurrentState()) {
                        case UNKNOWN:
                            break;
                        case PLAYING:
                            Log.i("AppleNotification", "Broadcasting track resumed for track " + currentTrack.getTitle());
                            broadcaster.broadcast(data, BroadcastState.RESUME, currentTrackDuration);
                            break;
                        case PAUSED:
                            Log.i("AppleNotification", "Broadcasting track paused for track " + currentTrack.getTitle());
                            broadcaster.broadcast(data, BroadcastState.PAUSE, currentTrackDuration);
                            break;
                    }
                }
            } else {
                Log.i("AppleNotification", "New track detected");
                // Check to see if there is overlap time with the next song
                // This is because if you're in the application the notifications don't always appear.
                long overlapTime = currentTrack.finalisePlayTime(currentTrackDuration);
                Log.i("AppleNotification", "Overlap time was " + overlapTime );
                data.addPlayTime(overlapTime);

                long recordedPlayTime = currentTrack.recordedTotalPlayTime();
                if (currentTrack.isRepeat() && !data.isRepeat() && recordedPlayTime < currentTrackDuration / 2) {
                    data.addPlayTime(recordedPlayTime);
                } else {
                    // This attempts to verify that a track is properly completed
                    Log.i("AppleNotification", "Total time was " + recordedPlayTime);
                    Log.i("AppleNotification", "Track duration was " + currentTrackDuration);
                    if (currentTrack.isComplete(currentTrackDuration)) {
                        Log.i("AppleNotification", "Broadcasting track complete for track " + currentTrack.getTitle());
                        broadcaster.broadcast(currentTrack, BroadcastState.COMPLETE, currentTrackDuration);
                    }
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
                    Log.i("AppleNotification", "Loading new data for song " + trackData.getTitle());
                    return api.getTrackDuration(trackData);
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                    currentTrackDuration = NotificationService.DEFAULT_SONG_LENGTH;
                }

                @Override
                protected void onPostExecute(final Long result) {
                    currentTrackDuration = result;
                    Log.i("AppleNotification", "Broadcasting song Start for " + trackData.getTitle());
                    broadcaster.broadcast(trackData, BroadcastState.START, currentTrackDuration);

                    if (!settings.getAppleRepeatEnabled()) {
                        return;
                    }
                    // Start timer
                    if (trackTimer != null) {
                        trackTimer.cancel();
                    }
                    trackTimer = new Timer();
                    trackTimer.schedule(new TimerTask() {
                        boolean isFinished = false;
                        int count = 0;
                        @Override
                        public void run() {
                            // Kill this timer if it has repeated too many times
                            if (++count >= TRACK_TIMER_REPEAT_CUTOFF) {
                                trackTimer.cancel();
                                trackTimer.purge();
                            }
                            long currPlayTime = currentTrack.currentTotalPlayTime();
                            Log.i("AppleNotification", String.format("Checking for potential repeated song. Current Play Time: %s, Current Track Duration: %s", currPlayTime, result));
                            if (currPlayTime > result && !isFinished) {
                                Log.i("AppleNotification", "Sending repeat track");
                                TrackData repeatTrack = new TrackData();
                                repeatTrack.setStartTime(new Date(System.currentTimeMillis()));
                                repeatTrack.setArtist(currentTrack.getArtist());
                                repeatTrack.setAlbum(currentTrack.getAlbum());
                                repeatTrack.setTitle(currentTrack.getTitle());
                                repeatTrack.setContentType(currentTrack.getContentType());
                                repeatTrack.setRepeat(true);
                                push(repeatTrack);
                                isFinished = true;
                            }
                        }
                    }, result - currentTrack.currentTotalPlayTime(), TRACK_TIMER_INTERVAL);
                }
            }.execute(currentTrack);
        }
    }
}

