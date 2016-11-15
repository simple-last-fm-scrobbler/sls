/**
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
 * A BroadcastReceiver for intents sent by the Spotify Music Player.
 *
 * @see AbstractPlayStatusReceiver
 *
 * @author HumbleBeeBumbleBee HumbleBeeBumbleBeeDebugs@gmail.com
 * @since 1.4.8
 * @helpers @metanota @inverse
 */

// Closing they Spotify UI can cause scrobbling to stop. Not our fault I think.
// (Lollipop 5.0 SSG S3) (Spotify 2.6.0.813) (SLS v1.4.9)

public class SpotifyReceiver extends AbstractPlayStatusReceiver {

    static final String APP_NAME = "Spotify";
    static final String TAG = "SpotifyReceiver";

    static final class BroadcastTypes {
        static final String APP_PACKAGE = "com.spotify.music";
    }

    @Override
    protected void parseIntent(Context ctx, String action, Bundle bundle) {

        MusicAPI musicAPI = MusicAPI.fromReceiver(ctx, APP_NAME,
                BroadcastTypes.APP_PACKAGE, null, false);
        setMusicAPI(musicAPI);
        if (bundle.containsKey("track")) {
            Track.Builder b = new Track.Builder();
            b.setMusicAPI(musicAPI);
            b.setWhen(Util.currentTimeSecsUTC());

            b.setArtist(bundle.getString("artist"));
            b.setAlbum(bundle.getString("album"));
            b.setTrack(bundle.getString("track"));
            long duration = bundle.getLong("duration");
            Log.e(TAG, Long.toString(duration));
            if (duration != 0) {
                b.setDuration((int) (long) duration / 1000);
            }
            Log.d(TAG,
                    bundle.getString("artist") + " - "
                            + bundle.getString("track") + " ("
                            + bundle.getInt("length", 0) + ")");
            setTrack(b.build());

            setState(Track.State.START);
        }
        if (bundle.containsKey("playing")) {
            boolean playing = bundle.getBoolean("playing");

            if (playing) {
                setState(Track.State.RESUME);
                Log.d(TAG, "Setting state to RESUME");
            } else {
                setState(Track.State.PAUSE);
                Log.d(TAG, "Setting state to PAUSE");
            }
        }
    }
}
