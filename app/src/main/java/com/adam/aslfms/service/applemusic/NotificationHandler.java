package com.adam.aslfms.service.applemusic;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.adam.aslfms.util.AppSettings;

/**
 * Created by 4-Eyes on 16/3/2017.
 * This handles notifications which are parsed to it. Determining when to merge notifications and
 * when to broadcast the states.
 */
class NotificationHandler {

    private TrackData currentTrack;
    private long currentTrackDuration = NotificationService.DEFAULT_SONG_LENGTH;
    private AppleMusicBroadcaster broadcaster;
    private LfmApi api;
    private AsyncTask<TrackData, Void, Long> trackInfoTask = null;

    NotificationHandler(Context context, AppSettings settings) {
        broadcaster = new AppleMusicBroadcaster(context);
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
            if (currentTrack.sameTrack(data)) {
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

                // This attempts to verify that a track is properly completed
                Log.i("AppleNotification", "Total time was " + currentTrack.totalPlayTime());
                Log.i("AppleNotification", "Track duration was " + currentTrackDuration);
                if (currentTrack.isComplete(currentTrackDuration)) {
                    Log.i("AppleNotification", "Broadcasting track complete for track " + currentTrack.getTitle());
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
                    Log.i("AppleNotification", "Loading new data for song " + trackData.getTitle());
                    return api.getTrackDuration(trackData);
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                    currentTrackDuration = NotificationService.DEFAULT_SONG_LENGTH;
                }

                @Override
                protected void onPostExecute(Long result) {
                    currentTrackDuration = result;
                    Log.i("AppleNotification", "Broadcasting song Start for " + trackData.getTitle());
                    broadcaster.broadcast(trackData, BroadcastState.START, currentTrackDuration);
                }
            }.execute(currentTrack);
        }
    }
}

