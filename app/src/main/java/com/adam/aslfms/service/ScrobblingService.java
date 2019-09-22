/**
 * This file is part of Simple Scrobbler.
 * <p/>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p/>
 * Copyright 2011 Simple Scrobbler Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.adam.aslfms.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.adam.aslfms.PermissionsActivity;
import com.adam.aslfms.R;
import com.adam.aslfms.SettingsActivity;
import com.adam.aslfms.UserCredActivity;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.InternalTrackTransmitter;
import com.adam.aslfms.util.NotificationCreator;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.enums.AdvancedOptionsWhen;
import com.adam.aslfms.util.enums.PowerOptions;

/**
 * @author tgwizard
 */
public class ScrobblingService extends Service {

    private static final String TAG = "ScrobblingService";

    public static final String ACTION_START_SCROBBLER_SERVICE = "com.adam.aslfms.service.startscrobbler";
    public static final String ACTION_AUTHENTICATE = "com.adam.aslfms.service.authenticate";
    public static final String ACTION_CLEARCREDS = "com.adam.aslfms.service.clearcreds";
    public static final String ACTION_JUSTSCROBBLE = "com.adam.aslfms.service.justscrobble";
    public static final String ACTION_PLAYSTATECHANGED = "com.adam.aslfms.service.playstatechanged";
    public static final String ACTION_HEART = "com.adam.aslfms.service.heart";
    public static final String ACTION_COPY = "com.adam.aslfms.service.copy";

    public static final String BROADCAST_ONAUTHCHANGED = "com.adam.aslfms.service.bcast.onauth";
    public static final String BROADCAST_ONSTATUSCHANGED = "com.adam.aslfms.service.bcast.onstatus";

    private static final long MIN_LISTENING_TIME = 30 * 1000;
    private static final long UPPER_SCROBBLE_MIN_LIMIT = 240 * 1000;
    private static final long MAX_PLAYTIME_DIFF_TO_SCROBBLE = 3000;

    private AppSettings settings;
    private ScrobblesDatabase mDb;

    private NetworkerManager mNetManager;

    private Track mCurrentTrack = null;
    private Intent mNotificationService = null;

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

        foreGroundService();
    }

    @Override
    public void onDestroy() {
        mDb.close();
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        handleCommand(i, startId);

        foreGroundService();

        if (settings.isTempExitAppEnabled(Util.checkPower(mCtx))) {
            return Service.START_NOT_STICKY;
        }
        return Service.START_STICKY;
    }

    private void handleCommand(Intent i, int startId) {
        if (i == null) {
            Log.e(TAG, "got null intent");
            return;
        }
        String action = i.getAction();
        Bundle extras = i.getExtras();
        if (action == null){
            // weird null action
        } else if (action.equals(ACTION_START_SCROBBLER_SERVICE )) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !Util.isMyServiceRunning(this, ControllerReceiverService.class)) {
                if (!Util.checkNotificationListenerPermission(this)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Util.myNotify(this, this.getResources().getString(R.string.warning), this.getResources().getString(R.string.permission_notification_listener_notice), 72135, new Intent(mCtx, PermissionsActivity.class));
                    }
                } else {
                    Intent ii = new Intent(this, ControllerReceiverService.class);
                    ii.putExtras(bundleTrack());
                    Log.d(TAG, "(re)starting controllerreceiver");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && settings.isActiveAppEnabled(Util.checkPower(this))) {
                        this.startForegroundService(ii);
                    } else {
                        this.startService(ii);
                    }
                }
            }
        } else if (action.equals(ACTION_CLEARCREDS)) {
            if (extras.getBoolean("clearall", false)) {
                mNetManager.launchClearAllCreds();
            } else {
                String snapp = extras.getString("netapp");
                if (snapp != null) {
                    mNetManager.launchClearCreds(NetApp.valueOf(snapp));
                } else
                    Log.e(TAG, "launchClearCreds got null napp");
            }
        } else if (action.equals(ACTION_AUTHENTICATE)) {
            String snapp = extras.getString("netapp");
            if (snapp != null)
                mNetManager.launchAuthenticator(NetApp.valueOf(snapp));
            else {
                Log.e(TAG, "launchHandshaker got null napp");
                mNetManager.launchHandshakers();
            }
        } else if (action.equals(ACTION_JUSTSCROBBLE)) {
            if (extras.getBoolean("scrobbleall", false)) {
                Log.d(TAG, "Scrobble All TRUE");
                mNetManager.launchAllScrobblers();
            } else {
                Log.e(TAG, "Scrobble All False");
                String snapp = extras.getString("netapp");
                if (snapp != null) {
                    mNetManager.launchScrobbler(NetApp.valueOf(snapp));
                } else
                    Log.e(TAG, "launchScrobbler got null napp");
            }
        } else if (action.equals(ACTION_PLAYSTATECHANGED)) {
            if (extras == null) {
                Log.e(TAG, "Got null extras on playstatechange");
                return;
            }
            Track.State state = Track.State.valueOf(extras.getString("state"));

            Track track = InternalTrackTransmitter.popTrack();

            if (track == null) {
                Log.e(TAG, "A null track got through!! (Ignoring it)");
                return;
            }

            onPlayStateChanged(track, state);

        } else if (action.equals(ACTION_HEART)) {
            if (mCurrentTrack != null && mCurrentTrack.hasBeenQueued()) {
                try {
                    if (mDb.fetchRecentTrack() == null) {
                        Toast.makeText(this, this.getString(R.string.no_heart_track),
                                Toast.LENGTH_LONG).show();
                    } else {
                        for (NetApp napp  : NetApp.values()){
                            if (napp != NetApp.LISTENBRAINZCUSTOM && napp != NetApp.LISTENBRAINZ) mDb.insertHeart(mCurrentTrack, napp);
                        }
                        mNetManager.launchAllHearts();
                        Toast.makeText(this, this.getString(R.string.song_is_ready), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Love track insert");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "CAN'T HEART TRACK" + e);
                }
            } else if (mCurrentTrack != null) {
                for (NetApp napp  : NetApp.values()){
                    if (napp != NetApp.LISTENBRAINZCUSTOM && napp != NetApp.LISTENBRAINZ) mDb.insertHeart(mCurrentTrack, napp);
                }
                mNetManager.launchAllHearts();
                Toast.makeText(this, this.getString(R.string.song_is_ready), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Love Track Rating!");
            } else {
                Toast.makeText(this, this.getString(R.string.no_current_track),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (action.equals(ACTION_COPY)) {
            if (mCurrentTrack != null && mCurrentTrack.hasBeenQueued()) {
                try {
                    Log.e(TAG, mDb.fetchRecentTrack().toString());
                    Track tempTrack = mDb.fetchRecentTrack();
                    int sdk = Build.VERSION.SDK_INT;
                    if (sdk < Build.VERSION_CODES.HONEYCOMB) {
                        @SuppressWarnings("deprecation")
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(tempTrack.getTrack() + R.string.by + tempTrack.getArtist() + ", " + tempTrack.getAlbum() + "; " + tempTrack.getMusicAPI().getName());
                    } else {
                        @SuppressWarnings("deprecation")
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Track", tempTrack.getTrack() + R.string.by + tempTrack.getArtist() + ", " + tempTrack.getAlbum() + "; " + tempTrack.getMusicAPI().getName());
                        clipboard.setPrimaryClip(clip);
                    }
                    Log.d(TAG, "Copy Track!");
                } catch (Exception e) {
                    Toast.makeText(this, this.getString(R.string.no_copy_track),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "CAN'T COPY TRACK" + e);
                }
            } else if (mCurrentTrack != null) {
                try {
                    int sdk = Build.VERSION.SDK_INT;
                    if (sdk < Build.VERSION_CODES.HONEYCOMB) {
                        @SuppressWarnings("deprecation")
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(mCurrentTrack.getTrack() + R.string.by + mCurrentTrack.getArtist() + ", " + mCurrentTrack.getAlbum() + "; " + mCurrentTrack.getMusicAPI().getName());
                    } else {
                        @SuppressWarnings("deprecation")
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Track", mCurrentTrack.getTrack() + R.string.by + mCurrentTrack.getArtist() + ", " + mCurrentTrack.getAlbum() + "; " + mCurrentTrack.getMusicAPI().getName());
                        clipboard.setPrimaryClip(clip);
                    }
                    Log.d(TAG, "Copy Track!");
                } catch (Exception e) {
                    Toast.makeText(this, this.getString(R.string.no_copy_track),
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "CAN'T COPY TRACK" + e);
                }
            } else {
                Toast.makeText(this, this.getString(R.string.no_current_track),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Weird action in onStart: " + action);
        }
    }

    private synchronized void onPlayStateChanged(Track track, Track.State state) {
        Log.d(TAG, "State: " + state.name());
        if (track == Track.SAME_AS_CURRENT) {
            // this only happens for apps implementing Scrobble Droid's API
            Log.d(TAG, "Got a SAME_AS_CURRENT track");
            if (mCurrentTrack != null) {
                track = mCurrentTrack;
            } else {
                Log.e(TAG, "Got a SAME_AS_CURRENT track, but current was null!");
                return;
            }
        }

        if (state == Track.State.START || state == Track.State.RESUME) { // start/resume
            if (mCurrentTrack != null) {
                mCurrentTrack.updateTimePlayed();
                tryQueue(mCurrentTrack);
                if (track.equals(mCurrentTrack)) {
                    return;
                } else {
                    tryScrobble();
                }
            }

            mCurrentTrack = track;
            mCurrentTrack.updateTimePlayed();
            tryNotifyNP(mCurrentTrack);

            foreGroundService();
        } else if (state == Track.State.PAUSE) { // pause
            // TODO: test this state
            if (mCurrentTrack == null) {
                // just ignore the track
            } else {
                if (!track.equals(mCurrentTrack)) {
                    Log.e(TAG, "PStopped track doesn't equal currentTrack!");
                    Log.e(TAG, "t: " + track);
                    Log.e(TAG, "c: " + mCurrentTrack);
                } else {
                    mCurrentTrack.updateTimePlayed();
                    // below: to be set on RESUME
                    mCurrentTrack.stopCountingTime();

                    tryQueue(mCurrentTrack);
                }
            }
        } else if (state == Track.State.COMPLETE) { // "complete"
            // TODO test this state
            if (mCurrentTrack == null) {
                // just ignore the track
            } else {
                if (!track.equals(mCurrentTrack)) {
                    Log.e(TAG, "CStopped track doesn't equal currentTrack!");
                    Log.e(TAG, "t: " + track);
                    Log.e(TAG, "c: " + mCurrentTrack);
                } else {
                    mCurrentTrack.updateTimePlayed();
                    tryQueue(mCurrentTrack);
                    tryScrobble();
                    mCurrentTrack = null;
                }
            }
        } else if (state == Track.State.PLAYLIST_FINISHED) { // playlist end
            if (mCurrentTrack == null) {
                tryQueue(track); // TODO: this can't succeed (time played = 0)
                tryScrobble(true);
            } else {
                if (!track.equals(mCurrentTrack)) {
                    Log.e(TAG, "PFStopped track doesn't equal currentTrack!");
                    Log.e(TAG, "t: " + track);
                    Log.e(TAG, "c: " + mCurrentTrack);
                } else {
                    mCurrentTrack.updateTimePlayed();
                    tryQueue(mCurrentTrack);
                    tryScrobble(true);
                }
            }

            mCurrentTrack = null;
        } else if (state == Track.State.UNKNOWN_NONPLAYING) {
            // similar to PAUSE, but might scrobble if close enough
            if (mCurrentTrack == null) {
                // just ignore the track
            } else {
                mCurrentTrack.updateTimePlayed();
                // below: to be set on RESUME
                mCurrentTrack.stopCountingTime();

                tryQueue(mCurrentTrack);
                if (!mCurrentTrack.hasUnknownDuration()) {
                    long diff = Math.abs(mCurrentTrack.getDuration() * 1000
                            - mCurrentTrack.getTimePlayed());
                    if (diff < MAX_PLAYTIME_DIFF_TO_SCROBBLE) {
                        tryScrobble();
                    }
                }
            }
        } else {
            Log.e(TAG, "Unknown track state: " + state.toString());
        }
    }

    /**
     * Launches a Now Playing notification of <code>track</code>, if we're
     * authenticated and Now Playing is enabled.
     *
     * @param track the currently playing track
     */
    private void tryNotifyNP(Track track) {
        PowerOptions pow = Util.checkPower(this);

        if (!settings.isAnyAuthenticated()
                || !settings.isNowPlayingEnabled(pow)) {
            Log.d(TAG, "Won't notify NP, unauthed or disabled");
            return;
        }

        mNetManager.launchNPNotifier(track);
    }

    private void tryQueue(Track track) {
        if (!settings.isAnyAuthenticated()
                || !settings.isScrobblingEnabled(Util.checkPower(this))) {
            Log.d(TAG, "Won't prepare scrobble, unauthed or disabled");
            return;
        }

        double sp = settings.getScrobblePoint() / (double) 100;
        sp -= 0.01; // to be safe
        long mintime = (long) (sp * 1000 * track.getDuration());
        //Log.e(TAG,"mintime:" +Long.toString(mintime));

        if (track.hasBeenQueued()) {
            Log.d(TAG, "Trying to queue a track that already has been queued");
            // Log.d(TAG, track.toString());
            return;
        }
        if (track.hasUnknownDuration() || mintime < MIN_LISTENING_TIME) {
            mintime = MIN_LISTENING_TIME;
        } else if (mintime > UPPER_SCROBBLE_MIN_LIMIT) {
            mintime = UPPER_SCROBBLE_MIN_LIMIT;
        }
        if (track.getTimePlayed() >= mintime) {
            Log.d(TAG, "Will try to queue track, played: "
                    + track.getTimePlayed() + " vs " + mintime);
            queue(mCurrentTrack);
        } else {
            Log.d(TAG, "Won't queue track, not played long enough: "
                    + track.getTimePlayed() + " vs " + mintime);
            Log.d(TAG, track.toString());
        }
    }

    /**
     * Only to be called by tryQueue(Track track).
     *
     * @param track
     */
    private void queue(Track track) {
        long rowId = mDb.insertTrack(track);
        if (rowId != -1) {
            track.setQueued();
            Log.d(TAG, "queued track after playtime: " + track.getTimePlayed());
            Log.d(TAG, track.toString());

            // now set up scrobbling rels
            for (NetApp napp : NetApp.values()) {
                Log.d(TAG, "inserting scrobble: " + napp.getName());
                if (settings.isAuthenticated(napp)) {
                    if (mDb.insertScrobble(napp, rowId)) {
                        Log.d(TAG, "inserting scrobble successful");
                    } else {
                        Log.d(TAG, "inserting scrobble failure");
                    }
                }
                // tell interested parties
                Intent i = new Intent(
                        ScrobblingService.BROADCAST_ONSTATUSCHANGED);
                i.putExtra("netapp", napp.toString());
                sendBroadcast(i);
            }
        } else {
            Log.e(TAG, "Could not insert scrobble into the db");
            Log.e(TAG, track.toString());
        }
    }

    private void tryScrobble() {
        tryScrobble(false);
    }

    private void tryScrobble(boolean playbackComplete) {

        if (!settings.isAnyAuthenticated()
                || !settings.isScrobblingEnabled(Util.checkPower(this))) {
            Log.d(TAG, "Won't prepare scrobble, unauthed or disabled");
            return;
        }

        scrobble(playbackComplete);
    }

    /**
     * Only to be called by tryScrobble(...).
     *
     * @param playbackComplete
     */
    private void scrobble(boolean playbackComplete) {

        PowerOptions pow = Util.checkPower(this);

        boolean aoc = settings.getAdvancedOptionsAlsoOnComplete(pow);
        if (aoc && playbackComplete) {
            Log.d(TAG, "Launching scrobbler because playlist is finished");
            mNetManager.launchAllScrobblers();
            return;
        }

        AdvancedOptionsWhen aow = settings.getAdvancedOptionsWhen(pow);
        for (NetApp napp : NetApp.values()) {
            int numInCache = mDb.queryNumberOfScrobbles(napp);
            if (numInCache >= aow.getTracksToWaitFor()) {
                mNetManager.launchScrobbler(napp);
            }
        }
    }

    private Bundle bundleTrack(){
        Bundle extras = new Bundle();
        if (mCurrentTrack != null) {
            extras.putString("track", mCurrentTrack.getTrack());
            extras.putString("artist", mCurrentTrack.getArtist());
            extras.putString("album", mCurrentTrack.getAlbum());
            extras.putString("app_name", mCurrentTrack.getMusicAPI().getName());
        } else {
            extras.putString("track", "");
            extras.putString("artist", "");
            extras.putString("album", "");
            extras.putString("app_name", "");
        }
        return extras;
    }

    private void foreGroundService(){
        if (settings.isActiveAppEnabled(Util.checkPower(mCtx))) {
            this.startForeground(NotificationCreator.FOREGROUND_ID, NotificationCreator.prepareNotification(bundleTrack(), mCtx));
        } else {
            this.stopForeground(true);
        }
    }
}