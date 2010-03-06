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
import android.util.Log;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

/**
 * A BroadcastReceiver for intents sent by music apps such as Android Music and
 * Hero Music. Specialized classes inherit from this class to deal with the
 * small differences.
 * 
 * @see AndroidMusicReceiver
 * @see HeroMusicReceiver
 * @author tgwizard
 * @since 1.2.7
 */
public class BuiltInMusicAppReceiver extends AbstractPlayStatusReceiver {

	private static final String TAG = "SLSBuiltInMusicAppReceiver";

	private final String stop_action;

	private final String app_package;
	private final String app_name;

	public BuiltInMusicAppReceiver(String stopAction, String appPackage,
			String appName) {
		super();
		stop_action = stopAction;
		app_package = appPackage;
		app_name = appName;
	}

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle)
			throws IllegalArgumentException {

		MusicAPI musicAPI = MusicAPI.fromReceiver(ctx, app_name, app_package,
				null, true);
		setMusicAPI(musicAPI);

		Track.Builder b = new Track.Builder();
		b.setMusicAPI(musicAPI);
		b.setWhen(Util.currentTimeSecsUTC());

		long audioid = -1;
		Object idBundle = bundle.get("id");
		if (idBundle != null) {
			if (idBundle instanceof Long)
				audioid = (Long) idBundle;
			else if (idBundle instanceof Integer)
				audioid = (Integer) idBundle;
		}

		if (audioid != -1) { // read from MediaStore

			Log.d(TAG, "Will read data from mediastore");

			final String[] columns = new String[] {
					MediaStore.Audio.AudioColumns.ARTIST,
					MediaStore.Audio.AudioColumns.TITLE,
					MediaStore.Audio.AudioColumns.DURATION,
					MediaStore.Audio.AudioColumns.ALBUM,
					MediaStore.Audio.AudioColumns.TRACK, };

			Cursor cur = ctx.getContentResolver().query(
					ContentUris.withAppendedId(
							MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							audioid), columns, null, null, null);

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

			Log.d(TAG, "Will read data from intent");

			CharSequence ar = bundle.getCharSequence("artist");
			CharSequence al = bundle.getCharSequence("album");
			CharSequence tr = bundle.getCharSequence("track");
			if (ar == null || al == null || tr == null) {
				throw new IllegalArgumentException("null track values");
			}

			b.setArtist(ar.toString());
			b.setAlbum(al.toString());
			b.setTrack(tr.toString());

		}

		if (action.equals(stop_action)) {
			setState(Track.State.PLAYLIST_FINISHED);
		} else {
			setState(Track.State.RESUME);
		}

		// throws on bad data
		setTrack(b.build());
	}
}
