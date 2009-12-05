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

import com.adam.aslfms.service.ScrobblingService;
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

	@SuppressWarnings("unused")
	private static final String TAG = "SLSAndroidMusicReceiver";

	public static final String ACTION_ANDROID_PLAYSTATECHANGED = "com.android.music.playstatechanged";
	public static final String ACTION_ANDROID_STOP = "com.android.music.playbackcomplete";
	public static final String ACTION_ANDROID_METACHANGED = "com.android.music.metachanged";

	public AndroidMusicReceiver() {
		super(MusicApp.ANDROID_MUSIC);
	}

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle)
			throws IllegalArgumentException {
		CharSequence ar = bundle.getCharSequence("artist");
		CharSequence al = bundle.getCharSequence("album");
		CharSequence tr = bundle.getCharSequence("track");
		if (ar == null || al == null || tr == null) {
			throw new IllegalArgumentException("null values");
		}

		Track.Builder b = new Track.Builder();
		b.setMusicApp(getApp());
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
