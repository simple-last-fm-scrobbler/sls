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
public abstract class BuiltInMusicAppReceiver extends
	AbstractPlayStatusReceiver {

	private static final String TAG = "SLSBuiltInMusicAppReceiver";

	static final int NO_AUDIO_ID = -1;

	final String stop_action;

	final String app_package;
	final String app_name;

	public BuiltInMusicAppReceiver(String stopAction, String appPackage,
		String appName) {
		super();
		stop_action = stopAction;
		app_package = appPackage;
		app_name = appName;
	}
	
	/**
	 * Depending on the action received decide whether it should signal a stop or not.
	 * By default, it compares it to the unique `this.stop_action`, but there might be
	 * multiple actions that cause a stop signal.
	 * 
	 * @param action	the received action
	 * @return			true when the received action is a stop action, false otherwise
	 */
	protected boolean isStopAction(String action) {
		return action.equals(stop_action);
	}

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle)
		throws IllegalArgumentException {
		MusicAPI musicAPI = getMusicAPI(ctx, bundle);
		setMusicAPI(musicAPI);

		Track.Builder b = new Track.Builder();
		b.setMusicAPI(musicAPI);
		b.setWhen(Util.currentTimeSecsUTC());

		parseTrack(ctx, b, bundle);

		if (isStopAction(action)) {
			setState(Track.State.PLAYLIST_FINISHED);
		} else {
			setState(Track.State.RESUME);
		}

		// throws on bad data
		setTrack(b.build());
	}

	MusicAPI getMusicAPI(Context ctx, Bundle bundle) {
		CharSequence bundleAppName = bundle.getCharSequence("player");
		CharSequence bundleAppPackage = bundle.getCharSequence("package");

		MusicAPI musicAPI;
		if ((bundleAppName != null) && (bundleAppPackage != null)) {
			Log.d(
				TAG,
				String.format(
					"Will load MusicAPI from bundle: [appName: %s, appPackage: %s]",
					bundleAppName, bundleAppPackage));
			musicAPI = MusicAPI.fromReceiver(ctx, bundleAppName.toString(),
				bundleAppPackage.toString(), null, true);
		} else {
			musicAPI = MusicAPI.fromReceiver(ctx, app_name, app_package, null,
				true);
		}
		return musicAPI;
	}

	void parseTrack(Context ctx, Track.Builder b, Bundle bundle) {
		long audioid = getAudioId(bundle);

		if (shouldFetchFromMediaStore(ctx, audioid)) { // read from MediaStore
			readTrackFromMediaStore(ctx, b, audioid);
		} else {
			readTrackFromBundleData(b, bundle);
		}
	}

	long getAudioId(Bundle bundle) {
		long id = NO_AUDIO_ID;
		Object idBundle = bundle.get("id");
		if (idBundle != null) {
			if (idBundle instanceof Long)
				id = (Long) idBundle;
			else if (idBundle instanceof Integer)
				id = (Integer) idBundle;
			else if (idBundle instanceof String) {
				id = Long.valueOf((String) idBundle).longValue();
			} else {
				Log.w(TAG,
					"Got unsupported idBundle type: " + idBundle.getClass());
			}
		}
		return id;
	}

	boolean shouldFetchFromMediaStore(Context ctx, long audioid) {
		if (audioid > 0)
			return true;
		return false;
	}

	void readTrackFromMediaStore(Context ctx, Track.Builder b, long audioid) {
		Log.d(TAG, "Will read data from mediastore");

		final String[] columns = new String[] {
			MediaStore.Audio.AudioColumns.ARTIST,
			MediaStore.Audio.AudioColumns.TITLE,
			MediaStore.Audio.AudioColumns.DURATION,
			MediaStore.Audio.AudioColumns.ALBUM,
			MediaStore.Audio.AudioColumns.TRACK, };

		Cursor cur = ctx.getContentResolver().query(
			ContentUris.withAppendedId(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioid), columns,
			null, null, null);

		if (cur == null) {
			throw new IllegalArgumentException("could not open cursor to media in media store");
		}

		try {
			if (!cur.moveToFirst()) {
				throw new IllegalArgumentException("no such media in media store");
			}
			String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
			b.setArtist(artist);

			String track = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
			b.setTrack(track);

			String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
			b.setAlbum(album);

			int duration = (int) (cur.getLong(cur.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)) / 1000);
			if (duration != 0) {
				b.setDuration(duration);
			}

			int tracknr = cur.getInt(cur.getColumnIndex(MediaStore.Audio.AudioColumns.TRACK));
			// tracknumber is returned in the format DTTT where D is the
			// disc number and TTT is the track number
			tracknr %= 1000;
			b.setTrackNr(String.valueOf(tracknr));

		} finally {
			cur.close();
		}
	}

	void readTrackFromBundleData(Track.Builder b, Bundle bundle) {
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
}
