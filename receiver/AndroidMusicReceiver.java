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

import android.os.Bundle;
import android.util.Log;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

/**
 * PlayStatusReceiver listens to broadcasts sent by the android music player.
 * When a broadcast has been received, it is translated into a Track, and sent
 * to ScrobblingService.
 * 
 * @see ScrobblingService
 * @see <a
 *      href="http://code.google.com/p/a-simple-lastfm-scrobbler/wiki/Developers">Example
 *      code</a>
 * @author tgwizard
 * 
 */

public class AndroidMusicReceiver extends AbstractPlayStatusReceiver {

	private static final String TAG = "SLSAndroidMusicReceiver";
	
	public static final String ACTION_ANDROID_PLAYSTATECHANGED = "com.android.music.playstatechanged";
	public static final String ACTION_ANDROID_STOP = "com.android.music.playbackcomplete";
	public static final String ACTION_ANDROID_METACHANGED = "com.android.music.metachanged";

	public AndroidMusicReceiver() {
		super(MusicApp.ANDROID_MUSIC);
	}

	@Override
	protected void parseIntent(String action, Bundle bundle) {

		CharSequence ar = bundle.getCharSequence("artist");
		CharSequence al = bundle.getCharSequence("album");
		CharSequence tr = bundle.getCharSequence("track");
		
		if (ar == null || al == null || tr == null) {
			setTrack(null);
			Log.d(TAG, "Got null values");
			return;
		}

		// As of cupcake, it is not possible (feasible) to get the actual
		// duration of the playing track, so I default it to three minutes
		Track track = Track.createTrack(ar.toString(), al.toString(), tr
				.toString(), Track.DEFAULT_TRACK_LENGTH, Util
				.currentTimeSecsUTC());

		if (action.equals(ACTION_ANDROID_STOP)) {
			setStopped(true);
		}
		setTrack(track);
	}

}
