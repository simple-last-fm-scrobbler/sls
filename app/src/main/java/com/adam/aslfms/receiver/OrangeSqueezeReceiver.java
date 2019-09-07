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

package com.adam.aslfms.receiver;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

/**
 * A BroadcastReceiver for intents sent by the Orange Squeeze music player
 *
 * @see AbstractPlayStatusReceiver
 *
 * @author a93h
 * @since 1.5.8
 */
public class OrangeSqueezeReceiver extends AbstractPlayStatusReceiver {

    static final String APP_PACKAGE = "com.orangebikelabs.orangesqueeze";
    static final String APP_NAME = "Orange Squeeze";

    static final String ACTION_ORANGE_METACHANGED = "com.orangebikelabs.orangesqueeze.metachanged";
    static final String ACTION_ORANGE_PLAYSTATECHANGED = "com.orangebikelabs.orangesqueeze.playstatechanged";

    static final String TAG = "OrangeSqzReceiver";
    static private Track track = null;

    @Override
    protected void parseIntent(Context ctx, String action, Bundle bundle) {

        MusicAPI musicAPI = MusicAPI.fromReceiver(
                ctx, APP_NAME, APP_PACKAGE, null, false);
        setMusicAPI(musicAPI);

        if (action.equals(ACTION_ORANGE_PLAYSTATECHANGED) || action.equals((ACTION_ORANGE_METACHANGED))){
            boolean isPlaying = bundle.getBoolean("isplaying");
            boolean isPaused = bundle.getBoolean("ispaused");
            if (isPaused) {
                setState(Track.State.PAUSE);
                Log.d(TAG, "Setting state to PAUSE");
            }
            if (isPlaying) {
                setState(Track.State.RESUME);
                Log.d(TAG, "Setting state to RESUME");
            }
            Track.Builder b = new Track.Builder();
            b.setMusicAPI(musicAPI);
            b.setWhen(Util.currentTimeSecsUTC());

            b.setArtist(bundle.getString("artist"));
            b.setAlbum(bundle.getString("album"));
            b.setTrack(bundle.getString("track"));
            b.setAlbumArtist(bundle.getString("albumartist"));
            b.setTrackArtist(bundle.getString("trackartist"));
            b.setTrackNr(bundle.getString("tracknr"));
            b.setDuration(bundle.getInt("duration")/1000); // convert ms to
            setTrack(b.build());
        }
    }
}
