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

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Track.State;
import com.adam.aslfms.util.TrackTestUtils;

public class AndroidMusicReceiverTest extends BuiltInMusicAppReceiverTest {

    @Override
    protected BroadcastReceiver createReceiver() {
        return new AndroidMusicReceiver();
    }

    @Override
    Scrobble assembleScrobbleIntent(State state) {
        Track t = TrackTestUtils.buildSimpleTrack(getMusicAPI());
        Intent i = new Intent();
        switch (state) {
            case PLAYLIST_FINISHED:
            case COMPLETE:
            case UNKNOWN_NONPLAYING:
                i.setAction(AndroidMusicReceiver.ACTION_ANDROID_STOP);
                break;
            case START:
            case RESUME:
            case PAUSE:
                i.setAction(AndroidMusicReceiver.ACTION_ANDROID_METACHANGED);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
        i.putExtra("artist", t.getArtist());
        i.putExtra("album", t.getAlbum());
        i.putExtra("track", t.getTrack());
        return new Scrobble(t, i);
    }

    MusicAPI getMusicAPI() {
        return MusicAPI.fromReceiver(ctx, AndroidMusicReceiver.NAME, AndroidMusicReceiver.PACKAGE_NAME, null, false);
    }

}
