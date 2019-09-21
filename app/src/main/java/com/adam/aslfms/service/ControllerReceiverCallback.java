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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Build;
import android.util.Log;

import com.adam.aslfms.receiver.GenericControllerReceiver;

/**
 * @author a93h
 * @since 1.5.8
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ControllerReceiverCallback extends MediaController.Callback {
    private final static String TAG = "CntrlrRcvrCallback";
    private Context mContext;
    private String mPlayer;
    private MediaController mController;

    public ControllerReceiverCallback(Context context, String player, MediaController controller) {
        super();
        mContext = context;
        mPlayer = player;
        mController = controller;
        Log.d(TAG,"callback instantiated " + player);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackState state) {
        Log.d(TAG, mPlayer + " playback state changed ");
        if (state != null){
            Intent localIntent = new Intent(GenericControllerReceiver.ACTION_INTENT_PLAYSTATE);
            localIntent.setComponent(new ComponentName(mContext.getPackageName(), "com.adam.aslfms.receiver.GenericControllerReceiver"));
            int ps = state.getState();
            int playing = -1;
            switch (ps){
                case PlaybackState.STATE_PLAYING:
                case PlaybackState.STATE_FAST_FORWARDING:
                case PlaybackState.STATE_REWINDING:
                    playing = 2; // resume
                    break;
                case PlaybackState.STATE_BUFFERING:
                case PlaybackState.STATE_CONNECTING:
                case PlaybackState.STATE_ERROR:
                case PlaybackState.STATE_PAUSED:
                case PlaybackState.STATE_STOPPED:
                case PlaybackState.STATE_NONE:
                    playing = 3; // pause
                    break;
                case PlaybackState.STATE_SKIPPING_TO_NEXT:
                case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
                case PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM:
                    playing = 4; // complete
                    break;
                default:
                    break;
            }
            localIntent.putExtra("playing", playing);
            mContext.sendBroadcast(localIntent);
            Log.d(TAG, "broadcast sent: controller play state");
        }
    }

    @Override
    public void onMetadataChanged(MediaMetadata metadata) {
        Log.d(TAG, mPlayer + " media metadata changed");
        if (metadata != null) {
            Intent localIntent = new Intent(GenericControllerReceiver.ACTION_INTENT_METADATA);;
            String artist = null;
            String albumArtist = null;
            String track = null;
            String album = null;
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
            localIntent.setComponent(new ComponentName(mContext.getPackageName(), "com.adam.aslfms.receiver.GenericControllerReceiver"));
            localIntent.putExtra("player", mPlayer);
            localIntent.putExtra("artist", artist);
            localIntent.putExtra("track", track);
            localIntent.putExtra("album", album);
            localIntent.putExtra("albumArtist", albumArtist);
            localIntent.putExtra("duration", duration);
            localIntent.putExtra("playing", 1);
            mContext.sendBroadcast(localIntent);
            Log.d(TAG, "broadcast sent: controller meta data");
        }
    }
}
