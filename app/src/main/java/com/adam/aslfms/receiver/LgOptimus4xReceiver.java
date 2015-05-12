/**
 * This file is part of Simple Last.fm Scrobbler.
 * 
 *     http://code.google.com/p/a-simple-lastfm-scrobbler/
 * 
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
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
 * A BroadcastReceiver for intents sent by the LG Optimus 4X P880 music player
 *
 * @see AbstractPlayStatusReceiver
 *
 * @author kshahar <shahar.kosti@gmail.com>
 * @since 1.4.4
 */
public class LgOptimus4xReceiver extends AbstractPlayStatusReceiver {

	static final String APP_PACKAGE = "com.lge.music";
	static final String APP_NAME = "LG Music Player";

	static final String ACTION_LGE_METACHANGED = "com.lge.music.metachanged";
	static final String ACTION_LGE_PAUSERESUME = "com.lge.music.playstatechanged";
	static final String ACTION_LGE_STOP = "com.lge.music.endofplayback";

	static final String TAG = "SLSLgOptimus4xReceiver";

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle) {

		MusicAPI musicAPI = MusicAPI.fromReceiver(
			ctx, APP_NAME, APP_PACKAGE, null, false);
		setMusicAPI(musicAPI);

		if (action.equals(ACTION_LGE_STOP)) {
			setState(Track.State.COMPLETE);
			setTrack(Track.SAME_AS_CURRENT);
			Log.d(TAG,"Setting state to COMPLETE");
			return;
		}

		if (action.equals(ACTION_LGE_METACHANGED)) {
			setState(Track.State.START);
			Log.d(TAG,"Setting state to START");
		}
		else if (action.equals(ACTION_LGE_PAUSERESUME)) {
			boolean playing = bundle.getBoolean("playing");
			Track.State state =
				(playing) ? (Track.State.RESUME) : (Track.State.PAUSE);
			setState(state);
			Log.d(TAG,"Setting state to " + state.toString());
		}

		Track.Builder b = new Track.Builder();
		b.setMusicAPI(musicAPI);
		b.setWhen(Util.currentTimeSecsUTC());

		b.setArtist(bundle.getString("artist"));
		b.setAlbum(bundle.getString("album"));
		b.setTrack(bundle.getString("track"));

		// set duration
		int duration = -1;
		Object obj = bundle.get("duration");
		if (obj instanceof Long) {
			duration = ((Long)obj).intValue();
		}
		else if (obj instanceof Integer) {
			duration = ((Integer)obj).intValue();
		}
		if (duration != -1) {
			b.setDuration(duration / 1000);
		}

		// throws on bad data
		setTrack(b.build());

	}
}
