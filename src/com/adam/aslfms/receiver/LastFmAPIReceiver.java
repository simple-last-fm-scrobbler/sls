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

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

import android.content.Context;
import android.os.Bundle;

/**
 * A BroadcastReceiver for the Last.fm Android API. More info at <a
 * href="https://github.com/c99koder/lastfm-android/wiki/scrobbler-interface"
 * >their dev page</a>
 * 
 * @see AbstractPlayStatusReceiver
 * @see MusicAPI
 * 
 * @author tgwizard
 * @since 1.3.2
 */
public class LastFmAPIReceiver extends AbstractPlayStatusReceiver {

	public static final String ACTION_LASTFMAPI_METACHANGED = "fm.last.android.metachanged";
	public static final String ACTION_LASTFMAPI_PAUSERESUME = "fm.last.android.playbackpaused";
	public static final String ACTION_LASTFMAPI_STOP = "fm.last.android.playbackcomplete";

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle) throws IllegalArgumentException {
		MusicAPI musicAPI = MusicAPI.fromReceiver(ctx, "Last.fm Android App API", MusicAPI.NOT_AN_APPLICATION_PACKAGE,
				"Apps using the Last.fm Android App API", false);
		setMusicAPI(musicAPI);

		if (action.equals(ACTION_LASTFMAPI_METACHANGED)) {
			setState(Track.State.CHANGED);
			Track.Builder b = new Track.Builder();
			b.setMusicAPI(musicAPI);
			b.setWhen(Util.currentTimeSecsUTC());

			b.setArtist(bundle.getString("artist"));
			b.setAlbum(bundle.getString("album"));
			b.setTrack(bundle.getString("track"));
			b.setDuration(bundle.getInt("duration") / 1000);
			// throws on bad data
			setTrack(b.build());
		} else if (action.equals(ACTION_LASTFMAPI_PAUSERESUME)) {
			if (bundle.containsKey("position")) {
				setState(Track.State.RESUME);
			} else {
				setState(Track.State.PAUSE);
			}
			setTrack(Track.SAME_AS_CURRENT);
		} else if (action.equals(ACTION_LASTFMAPI_STOP)) {
			setState(Track.State.COMPLETE);
			setTrack(Track.SAME_AS_CURRENT);
		}
	}

}
