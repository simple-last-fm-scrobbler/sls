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

package com.adam.aslfms.adc2.receiver;

import com.adam.aslfms.adc2.Track;
import com.adam.aslfms.adc2.util.Util;

import android.os.Bundle;

public class HeroMusicReceiver extends AbstractPlayStatusReceiver {
	
	public static final String ACTION_HTC_PLAYSTATECHANGED = "com.htc.music.playstatechanged";
	public static final String ACTION_HTC_STOP = "com.htc.music.playbackcomplete";
	public static final String ACTION_HTC_METACHANGED = "com.htc.music.metachanged";

	public HeroMusicReceiver() {
		super(MusicApp.HERO_MUSIC);
	}

	@Override
	protected void parseIntent(String action, Bundle bundle) {
		CharSequence ar = bundle.getCharSequence("artist");
		CharSequence al = bundle.getCharSequence("album");
		CharSequence tr = bundle.getCharSequence("track");

		// As of cupcake, it is not possible (feasible) to get the actual
		// duration of the playing track, so I default it to three minutes
		Track track = Track.createTrack(ar, al, tr, Track.DEFAULT_TRACK_LENGTH,
				Util.currentTimeSecsUTC());

		if (action.equals(ACTION_HTC_STOP)) {
			setStopped(true);
		}
		setTrack(track);
	}

}
