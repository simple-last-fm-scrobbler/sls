/**
 *  This file is part of A Simple Last.fm Scrobbler.
 *
 *  A Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  A Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with A Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms.receiver;

import android.content.Context;
import android.os.Bundle;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;


/**
 * A BroadcastReceiver for intents sent by the Android Music Player.
 * 
 * @see AbstractPlayStatusReceiver
 * 
 * @author tgwizard
 * @since 1.0.1
 */
public class AndroidMusicReceiver extends AbstractPlayStatusReceiver {

	@SuppressWarnings("unused")
	private static final String TAG = "SLSAndroidMusicReceiver";

	public static final String ACTION_ANDROID_PLAYSTATECHANGED = "com.android.music.playstatechanged";
	public static final String ACTION_ANDROID_STOP = "com.android.music.playbackcomplete";
	public static final String ACTION_ANDROID_METACHANGED = "com.android.music.metachanged";

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle)
			throws IllegalArgumentException {

		MusicAPI musicAPI = MusicAPI.fromReceiver(ctx, "Android Music Player",
				"com.android.music", null, true);
		setMusicAPI(musicAPI);

		CharSequence ar = bundle.getCharSequence("artist");
		CharSequence al = bundle.getCharSequence("album");
		CharSequence tr = bundle.getCharSequence("track");
		if (ar == null || al == null || tr == null) {
			throw new IllegalArgumentException("null track values");
		}

		Track.Builder b = new Track.Builder();
		b.setMusicAPI(musicAPI);
		b.setWhen(Util.currentTimeSecsUTC());
		b.setArtist(ar.toString());
		b.setAlbum(al.toString());
		b.setTrack(tr.toString());

		if (action.equals(ACTION_ANDROID_STOP)) {
			setState(Track.State.PLAYLIST_FINISHED);
		} else {
			setState(Track.State.RESUME);
		}

		// throws on bad data
		setTrack(b.build());
	}

}
