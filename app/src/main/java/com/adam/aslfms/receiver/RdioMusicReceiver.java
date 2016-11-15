/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/tgwizard/sls
 * <p>
 * Copyright 2011 Simple Last.fm Scrobbler Team
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

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

/**
 * A BroadcastReceiver for intents sent by the Rdio Music Player.
 *
 * @see BuiltInMusicAppReceiver
 *
 * @author tgwizard
 * @since 1.3.7
 */
public class RdioMusicReceiver extends AbstractPlayStatusReceiver {
    @SuppressWarnings("unused")
    private static final String TAG = "SLSRdioReceiver";

    static final String APP_PACKAGE = "com.rdio.android.ui";
    static final String APP_NAME = "Rdio";

    @Override
    protected void parseIntent(Context ctx, String action, Bundle bundle)
            throws IllegalArgumentException {

        MusicAPI musicAPI = MusicAPI.fromReceiver(ctx, APP_NAME, APP_PACKAGE, null,
                false);
        setMusicAPI(musicAPI);

        // state, required
        boolean isPaused = bundle.getBoolean("isPaused");
        boolean isPlaying = bundle.getBoolean("isPlaying");

        if (isPlaying)
            setState(Track.State.RESUME);
        else if (isPaused)
            setState(Track.State.PAUSE);
        else
            setState(Track.State.COMPLETE);

        Track.Builder b = new Track.Builder();

        b.setMusicAPI(musicAPI);
        b.setWhen(Util.currentTimeSecsUTC());

        b.setArtist(bundle.getString("artist"));
        b.setAlbum(bundle.getString("album"));
        b.setTrack(bundle.getString("track"));

        // TODO: cleanup this
        int duration = -1;
        Object obj = bundle.get("duration");
        if (obj instanceof Integer)
            duration = ((Integer) obj);
        if (obj instanceof Double)
            duration = (int) ((double) ((Double) obj));
        if (duration != -1) {
            // duration is in milliseconds
            duration /= 1000;
            b.setDuration(duration);
        }

        // throws on bad data
        setTrack(b.build());
    }

}
