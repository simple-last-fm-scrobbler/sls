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

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

/**
 * A BroadcastReceiver for the <a
 * href="http://code.google.com/p/scrobbledroid/wiki/DeveloperAPI"> Scrobbler
 * Droid API</a>. New music apps are recommended to use the <a
 * href="http://code.google.com/p/a-simple-lastfm-scrobbler/wiki/Developers">
 * SLS API</a> instead.
 * 
 * @see AbstractPlayStatusReceiver
 * 
 * @author tgwizard
 * @since 1.2
 */
public class ScrobbleDroidMusicReceiver extends AbstractPlayStatusReceiver {

	@SuppressWarnings("unused")
	private static final String TAG = "SLSSDMusicReceiver";

	public static final String SCROBBLE_DROID_MUSIC_STATUS = "net.jjc1138.android.scrobbler.action.MUSIC_STATUS";

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle)
			throws IllegalArgumentException {

		MusicAPI musicAPI = MusicAPI.fromReceiver(ctx,
				"\"Scrobble Droid Apps\"", MusicAPI.NOT_AN_APPLICATION_PACKAGE
						+ "scrobbledroidapi",
				"Apps supported by Scrobble Droid", true);
		setMusicAPI(musicAPI);

		/*
		 * Intent i = new
		 * Intent("net.jjc1138.android.scrobbler.action.MUSIC_STATUS");
		 * 
		 * i.putExtra("playing", true); i.putExtra("id", songCursor.getInt(
		 * songCursor.getColumnIndexOrThrow( MediaStore.Audio.Media._ID)));
		 * 
		 * sendBroadcast(i);
		 */

		boolean playing = bundle.getBoolean("playing", false);

		if (!playing) {
			// if not playing, there is no guarantee the bundle will contain any
			// track info
			setTrack(Track.SAME_AS_CURRENT);
			setState(Track.State.UNKNOWN_NONPLAYING);
			return;
		}

		String source = bundle.getString("source");
		if (source == null || source.length() > 1) {
			source = "P";
		}
		int msid = bundle.getInt("id", -1);

		Track.Builder b = new Track.Builder();
		b.setMusicAPI(musicAPI);
		b.setWhen(Util.currentTimeSecsUTC());

		if (msid != -1) { // read from MediaStore
			final String[] columns = new String[] {
					MediaStore.Audio.AudioColumns.ARTIST,
					MediaStore.Audio.AudioColumns.TITLE,
					MediaStore.Audio.AudioColumns.DURATION,
					MediaStore.Audio.AudioColumns.ALBUM,
					MediaStore.Audio.AudioColumns.TRACK, };

			Cursor cur = ctx.getContentResolver().query(
					ContentUris.withAppendedId(
							MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, msid),
					columns, null, null, null);

			if (cur == null) {
				throw new IllegalArgumentException(
						"could not open cursor to media in media store");
			}

			try {
				if (!cur.moveToFirst()) {
					throw new IllegalArgumentException(
							"no such media in media store");
				}
				String artist = cur.getString(cur
						.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
				b.setArtist(artist);

				String track = cur.getString(cur
						.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
				b.setTrack(track);

				String album = cur.getString(cur
						.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
				b.setAlbum(album);

				int duration = (int) (cur
						.getLong(cur
								.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)) / 1000);
				if (duration != 0) {
					b.setDuration(duration);
				}

				int tracknr = cur.getInt(cur
						.getColumnIndex(MediaStore.Audio.AudioColumns.TRACK));
				// tracknumber is returned in the format DTTT where D is the
				// disc number and TTT is the track number
				tracknr %= 1000;
				b.setTrackNr(String.valueOf(tracknr));

			} finally {
				cur.close();
			}

		} else { // read from intent
			String artist = bundle.getString("artist"); // required
			b.setArtist(artist);
			String track = bundle.getString("track"); // required
			b.setTrack(track);

			int duration = bundle.getInt("secs", -1); // optional unless source
			// is P, but we don't
			// care
			if (duration != -1)
				b.setDuration(duration);

			String album = bundle.getString("album"); // optional
			b.setAlbum(album);

			String tracknr;
			int tnr = bundle.getInt("tracknumber", -1); // optional
			if (tnr != -1) {
				tracknr = String.valueOf(tnr);
			} else {
				tracknr = "";
			}
			b.setTrackNr(tracknr);

			String mbid = bundle.getString("mb-trackid"); // optional
			b.setMbid(mbid);
		}

		// we've handled stopping/pausing at the top
		setState(Track.State.RESUME);

		setTrack(b.build());
	}
}
