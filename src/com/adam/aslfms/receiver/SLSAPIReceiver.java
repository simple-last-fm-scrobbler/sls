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

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

/**
 * A BroadcastReceiver for the Simple Last.fm Scrobbler API. More info available
 * at the SLS <a
 * href="http://code.google.com/p/a-simple-lastfm-scrobbler/wiki/Developers">
 * dev page</a>.
 * 
 * @see AbstractPlayStatusReceiver
 * @see MusicAPI
 * 
 * @author tgwizard
 * @since 1.2.3
 */
public class SLSAPIReceiver extends AbstractPlayStatusReceiver {
	@SuppressWarnings("unused")
	private static final String TAG = "SLSAPIReceiver";

	public static final String SLS_API_BROADCAST_INTENT = "com.adam.aslfms.notify.playstatechanged";

	public static final int STATE_START = 0;
	public static final int STATE_RESUME = 1;
	public static final int STATE_PAUSE = 2;
	public static final int STATE_COMPLETE = 3;

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle)
			throws IllegalArgumentException {

		// music api stuff
		// app-name, required
		String appname = bundle.getString("app-name");
		// app-package, required
		String apppkg = bundle.getString("app-package");

		// throws on bad appname / apppkg
		MusicAPI musicAPI = MusicAPI.fromReceiver(ctx, appname, apppkg, null,
				false);
		setMusicAPI(musicAPI);

		// state, required
		int state = bundle.getInt("state", -1);

		if (state == -1)
			throw new IllegalArgumentException("no state");

		if (state == STATE_START)
			setState(Track.State.START);
		else if (state == STATE_RESUME)
			setState(Track.State.RESUME);
		else if (state == STATE_PAUSE)
			setState(Track.State.PAUSE);
		else if (state == STATE_COMPLETE)
			setState(Track.State.COMPLETE);
		else
			throw new IllegalArgumentException("bad state: " + state);

		Track.Builder b = new Track.Builder();
		b.setMusicAPI(musicAPI);
		b.setWhen(Util.currentTimeSecsUTC());
		// artist name, required
		b.setArtist(bundle.getString("artist"));
		// album name, optional (recommended)
		b.setAlbum(bundle.getString("album"));
		// track name, required
		b.setTrack(bundle.getString("track"));

		// duration, required
		int duration = bundle.getInt("duration", -1);
		if (duration == -1)
			throw new IllegalArgumentException("no duration");
		b.setDuration(duration);

		// tracknr, optional
		int tracknr = bundle.getInt("track-number", -1);
		if (tracknr != -1)
			b.setTrackNr(Integer.toString(tracknr));

		// music-brainz id, optional
		String mbid = bundle.getString("mbid");
		b.setMbid(mbid);

		// source, optional (defaults to "P")
		String source = bundle.getString("source");
		source = (source == null) ? "P" : source;
		b.setSource(source);

		// throws on bad data
		setTrack(b.build());
	}
}
