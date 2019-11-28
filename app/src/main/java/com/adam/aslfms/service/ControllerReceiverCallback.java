/**
 * This file is part of Simple Scrobbler.
 * <p>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p>
 * Copyright 2011 Simple Scrobbler Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adam.aslfms.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Build;
import android.util.Log;

import com.adam.aslfms.MusicAppsActivity;
import com.adam.aslfms.R;
import com.adam.aslfms.UserCredActivity;
import com.adam.aslfms.receiver.MusicAPI;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.InternalTrackTransmitter;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

import java.math.BigDecimal;

/**
 * @author a93h
 * @since 1.5.8
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ControllerReceiverCallback extends MediaController.Callback {
    private final static String TAG = "CntrlrRcvrCallback";
    private Context mContext;
    private String mPlayer;
    private MusicAPI musicAPI = null;
    private Track mTrack = null;
    private Track.State trackState = null;
    private AppSettings mSettings;
    private MediaController mController;
    private Intent mService = null;

    public ControllerReceiverCallback(Context context, String player, MediaController controller) {
        super();
        mContext = context;
        mPlayer = player;
        mController = controller;
        mSettings = new AppSettings(mContext);
        Log.d(TAG, "callback instantiated " + player);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackState state) {
        Log.d(TAG, mPlayer + " playback state changed ");
        if (state != null) {
            // start/call the Scrobbling Service
            mService = new Intent(mContext, ScrobblingService.class);
            mService.setAction(ScrobblingService.ACTION_PLAYSTATECHANGED);
            int ps = state.getState();
            trackState = Track.State.UNKNOWN_NONPLAYING;
            switch (ps) {
                case PlaybackState.STATE_PLAYING:
                case PlaybackState.STATE_FAST_FORWARDING:
                case PlaybackState.STATE_REWINDING:
                    trackState = Track.State.RESUME;
                    break;
                case PlaybackState.STATE_BUFFERING:
                case PlaybackState.STATE_CONNECTING:
                case PlaybackState.STATE_ERROR:
                case PlaybackState.STATE_PAUSED:
                case PlaybackState.STATE_STOPPED:
                case PlaybackState.STATE_NONE:
                    trackState = Track.State.PAUSE;
                    break;
                case PlaybackState.STATE_SKIPPING_TO_NEXT:
                case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
                case PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM:
                    trackState = Track.State.COMPLETE;
                    break;
                default:
                    break;
            }
            mService.putExtra("state", trackState.name());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mSettings.isActiveAppEnabled(Util.checkPower(mContext))) {
                mContext.startForegroundService(mService);
            } else {
                mContext.startService(mService);
            }
            Log.d(TAG, "broadcast sent: controller play state");
        }
    }

    @Override
    public void onMetadataChanged(MediaMetadata metadata) {
        Log.d(TAG, mPlayer + " media metadata changed");
        mService = new Intent(mContext, ScrobblingService.class);
        mService.setAction(ScrobblingService.ACTION_PLAYSTATECHANGED);
        if (metadata != null) {
            String artist = null;
            String albumArtist = null;
            String track = null;
            String album = null;
            String playerName = null;
            PackageManager packageManager = mContext.getPackageManager();
            try {
                playerName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(mPlayer, PackageManager.GET_META_DATA)).toString();
            } catch (PackageManager.NameNotFoundException e) {
                Log.i(TAG, "Got a bad track from: "
                        + ((musicAPI == null) ? "null" : musicAPI.getName())
                        + ", ignoring it (" + e.getMessage() + ")");
            }
            if (playerName == null) {
                Log.e(TAG, "Metadata changed failed to find package info.");
                return;
            }

            MusicAPI musicAPI = MusicAPI.fromReceiver(mContext, playerName, mPlayer, "generic receiver", false);

            if (musicAPI == null) {
                Log.e(TAG, "Music API is null.");
                return;
            }

            // check if the user wants to scrobble music from this MusicAPI
            if (musicAPI.getEnabledValue() == 0) {
                Log.d(TAG, "App: " + musicAPI.getName()
                        + " has been disabled, won't propagate");
                return;
            } else if (musicAPI.getEnabledValue() == 2) {
                Util.myNotify(mContext, musicAPI.getName(), mContext.getString(R.string.new_music_app), 12473, new Intent(mContext, MusicAppsActivity.class));
                Log.d(TAG, "App: " + musicAPI.getName()
                        + " has been ignored, won't propagate");
                return;
            }
            double duration = -1;
            try {
                artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                if (artist == null)
                    artist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            } catch (Exception ignored) {
            }
            try {
                albumArtist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            } catch (Exception ignored) {
            }
            try {
                track = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            } catch (Exception ignored) {
            }
            try {
                album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
            } catch (Exception ignored) {
            }
            try {
                duration = (double) metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
            } catch (RuntimeException ignored) {
                duration = 0;
            }
            int length = new BigDecimal(Math.round(duration / 1000)).intValueExact();
            Track.Builder b = new Track.Builder();
            b.setMusicAPI(musicAPI);
            b.setWhen(Util.currentTimeSecsUTC());

            b.setArtist(artist);
            b.setAlbum(album);
            b.setTrack(track);
            b.setAlbumArtist(albumArtist);
            b.setDuration(length);

            try {
                mTrack = b.build();
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "Got a bad track from: "
                        + musicAPI
                        + ", ignoring it (" + e.getMessage() + ")");
                return;
            }
            // duration should be an Integer in seconds.
            Log.d(TAG, artist + " - "
                    + track + " ("
                    + length + ")");
            InternalTrackTransmitter.appendTrack(mTrack);
            Log.d(TAG, "broadcast sent: controller meta data");
            // we must be logged in to scrobble
            if (!mSettings.isAnyAuthenticated()) {
                Intent i = new Intent(mContext, UserCredActivity.class);
                i.putExtra("netapp", NetApp.LASTFM.getIntentExtraValue());
                Util.myNotify(mContext, mContext.getResources().getString(R.string.warning), mContext.getResources().getString(R.string.not_logged_in), 05233, i);
                Log
                        .d(TAG,
                                "The user has not authenticated, won't propagate the submission request");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mSettings.isActiveAppEnabled(Util.checkPower(mContext))) {
                mContext.startForegroundService(mService);
            } else {
                mContext.startService(mService);
            }
            Log.d(TAG, "broadcast sent: controller play state");
        }
    }
}
