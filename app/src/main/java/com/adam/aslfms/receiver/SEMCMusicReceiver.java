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

public class SEMCMusicReceiver extends BuiltInMusicAppReceiver {

    private static final String TAG = "SEMCMusicReceiver";

    static final String APP_PACKAGE = "com.sonyericsson.music";
    static final String ACTION_SEMC_STOP_LEGACY = "com.sonyericsson.music.playbackcontrol.ACTION_PLAYBACK_PAUSE";
    static final String ACTION_SEMC_STOP = "com.sonyericsson.music.playbackcontrol.ACTION_PAUSED";
    static final String ACTION_SEMC_METACHANGED = "com.sonyericsson.music.metachanged";


    public SEMCMusicReceiver() {
        super(ACTION_SEMC_STOP, APP_PACKAGE, "Sony Ericsson Music Player");
    }

    /**
     * @param action the received action
     * @Override /**
     * Checks that the action received is either the one used in the
     * newer Sony Xperia devices or that of the previous versions
     * of the app.
     * @return true when the received action is a stop action, false otherwise
     * <p>
     * protected boolean isStopAction(String action) {
     * return ;
     * }
     */

    @Override
    protected void parseIntent(Context ctx, String action, Bundle bundle) throws IllegalArgumentException {
        super.parseIntent(ctx, action, bundle);
        if (action.equals(ACTION_SEMC_STOP) || action.equals(ACTION_SEMC_STOP_LEGACY)) { // Should stop double scrobbles.
            setState(Track.State.PAUSE);
        }
    }

    @Override
    void readTrackFromBundleData(Track.Builder b, Bundle bundle) {
        Log.d(TAG, "Will read data from SEMC intent");

        CharSequence ar = bundle.getCharSequence("ARTIST_NAME");
        CharSequence al = bundle.getCharSequence("ALBUM_NAME");
        CharSequence tr = bundle.getCharSequence("TRACK_NAME");

        if (ar == null || al == null || tr == null) {
            throw new IllegalArgumentException("null track values");
        }

        b.setArtist(ar.toString());
        b.setAlbum(al.toString());
        b.setTrack(tr.toString());
    }

}
