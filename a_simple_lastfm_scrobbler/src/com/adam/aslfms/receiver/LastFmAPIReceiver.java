/**
 *  This file is part of Simple Last.fm Scrobbler.
 *
 *  Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
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

	public static final String ACTION_LASTFMAPI_START = "fm.last.android.metachanged";
	public static final String ACTION_LASTFMAPI_PAUSERESUME = "fm.last.android.playbackpaused";
	public static final String ACTION_LASTFMAPI_STOP = "fm.last.android.playbackcomplete";

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle) throws IllegalArgumentException {
		MusicAPI musicAPI = MusicAPI.fromReceiver(ctx, "Last.fm Android App API", MusicAPI.NOT_AN_APPLICATION_PACKAGE,
				"Apps using the Last.fm Android App API", false);
		setMusicAPI(musicAPI);

		if (action.equals(ACTION_LASTFMAPI_START)) {
			setState(Track.State.START);
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
