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
 * A BroadcastReceiver for intents sent by the Huawei Music Player.
 *
 * @see AbstractPlayStatusReceiver
 *
 * @author HumbleBeeBumbleBee HumbleBeeBumbleBeeDebugs@gmail.com
 * @since 1.5.0
 */


public class HuaweiReceiver extends AbstractPlayStatusReceiver {

    static final String APP_NAME = "Huawei Music";
    static final String TAG = "HuaweiReceiver";

    static final class BroadcastTypes {
        static final String APP_PACKAGE = "com.android.mediacenter";
        static final String METADATA_CHANGED = APP_PACKAGE + ".metachanged";
        static final String PLAYSTATE_CHANGED = APP_PACKAGE + ".playstatechanged";
        static final String ACTION_STOP = APP_PACKAGE + ".playbackcomplete";
    }

    static private Track track = null;

    @Override
    protected void parseIntent(Context ctx, String action, Bundle bundle) {

        MusicAPI musicAPI = MusicAPI.fromReceiver(
                ctx, APP_NAME, BroadcastTypes.APP_PACKAGE, null, false);
        setMusicAPI(musicAPI);

        if (action.equals(BroadcastTypes.ACTION_STOP)) {
            setState(Track.State.COMPLETE);
        } else {
            boolean playing = bundle.getBoolean("isPlaying", false);
            if (playing) {
                setState(Track.State.RESUME);
                Log.d(TAG, "Setting state to RESUME");
            } else {
                setState(Track.State.PAUSE);
                Log.d(TAG, "Setting state to PAUSE");
            }

            Track.Builder b = new Track.Builder();
            b.setMusicAPI(musicAPI);
            b.setWhen(Util.currentTimeSecsUTC());

            b.setArtist(bundle.getString("artist"));
            b.setAlbum(bundle.getString("album"));
            b.setTrack(bundle.getString("track"));

            long duration = bundle.getLong(BroadcastTypes.APP_PACKAGE + "duration", 0);
            if (duration > 0) {
                b.setDuration((int) (duration / 1000));
            }

            track = b.build();
            setTrack(track);

            Log.d(TAG,
                    bundle.getString("artist") + " - "
                            + bundle.getString("track") + " ("
                            + bundle.getLong("duration", 0) + ")");
        }
    }
}
