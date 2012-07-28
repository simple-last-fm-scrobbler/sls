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
			duration = (int) ((Integer) obj);
		if (obj instanceof Double)
			duration = (int) ((double)((Double) obj));
		if (duration != -1) {
			// duration is in milliseconds
			duration /= 1000;
			b.setDuration(duration);
		}

		// throws on bad data
		setTrack(b.build());
	}

}
