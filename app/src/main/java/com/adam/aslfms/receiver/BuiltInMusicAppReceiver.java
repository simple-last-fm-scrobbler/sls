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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

import java.math.BigDecimal;

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

	private static final String TAG = "BuiltInMusicAppReceiver";

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
		} else if (action.equals("com.android.music.playbackcomplete")){
			setState(Track.State.COMPLETE);
		} else {
				setState(Track.State.RESUME);
		}
		if(bundle.containsKey("playing")){
			boolean playing = bundle.getBoolean("playing");
			if (!playing) {
				// if not playing, there is no guarantee the bundle will contain any
				// track info
				setTrack(Track.SAME_AS_CURRENT);
				setState(Track.State.PAUSE);
			} else {
				setTrack(Track.SAME_AS_CURRENT);
				setState(Track.State.RESUME);
			}
		}

		// throws on bad data
		setTrack(b.build());
	}

	MusicAPI getMusicAPI(Context ctx, Bundle bundle) {
		CharSequence bundleAppName;
		CharSequence bundleAppPackage = null;

		try {
			if (bundle.containsKey("app")) {
				bundleAppPackage = bundle.getCharSequence("app");
			}
			if (bundle.containsKey("app-package")) {
				bundleAppPackage = bundle.getCharSequence("app-package");
			}
			if (bundle.containsKey("scrobbling_source")){
				bundleAppPackage = bundle.getCharSequence("scrobbling_source");
			}
			if (bundle.containsKey("package") && !bundle.containsKey("player")){
				bundleAppPackage = bundle.getCharSequence("package");
			}
			if (bundle.containsKey("com.maxmpz.audioplayer.source")){
				bundleAppPackage = bundle.getCharSequence("com.maxmpz.audioplayer.source");
			}
			if (bundle.containsKey("gonemad.gmmp")){
				bundleAppPackage = "gonemad.gmmp";
			}
		} catch (Exception e){
			Log.d(TAG,"Improper package source: "+e);
		}
		if (bundleAppPackage != null)
		{
			if (bundleAppPackage.toString().contains("com.kabouzeid.gramophone")){
				bundleAppPackage = "com.kabouzeid.gramophone";
			}
			PackageManager packageManager = ctx.getPackageManager();
			try {
				bundleAppName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(bundleAppPackage.toString(), PackageManager.GET_META_DATA));
			} catch (PackageManager.NameNotFoundException e) {
				bundleAppName = bundle.getCharSequence("player");
				bundleAppPackage = bundle.getCharSequence("package");
			}
		} else {
			bundleAppName = bundle.getCharSequence("player");
			bundleAppPackage = bundle.getCharSequence("package");
		}

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
			else if (idBundle instanceof String && ((String) idBundle).contains(".")){
				id = Long.valueOf(((String) idBundle).replace(".","")).longValue();
			} else if (idBundle instanceof String) {
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
			Log.e(TAG, Integer.toString(duration));
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
		CharSequence tr = bundle.getCharSequence("track");

		// duration is needs as Integer in seconds.

		if(bundle.containsKey("duration")){
			Object tmp = bundle.get("duration");
			if (tmp != null) {
				if (tmp instanceof Long) {
					try {
						long du = bundle.getLong("duration");
						if (du < 30000){
							b.setDuration(new BigDecimal(bundle.getLong("duration")).intValueExact());
						} else {
							b.setDuration(new BigDecimal(du).intValueExact() / 1000);
						}
					} catch (Exception e) {
						Log.d(TAG, "duration: " + e);
					}
				} else if (tmp instanceof Integer){
					try {
						int du = bundle.getInt("duration");
						if (du < 30000){
							b.setDuration(bundle.getInt("duration"));
						} else {
							b.setDuration(du / 1000);
						}
						Log.d(TAG, "Integer: " + Integer.toString(du));
					} catch (Exception e) {
						Log.e(TAG, "duration: " + e);
					}
				}
			}
		}

		//if (ar == null || al == null || tr == null) {
		if (ar == null || tr == null) {
			throw new IllegalArgumentException("null track values");
		}
		if (bundle.containsKey("album")) {
			CharSequence al = bundle.getCharSequence("album");
			if (al == null || "Unknown album".equals(al.toString()) || "Unknown".equals(al.toString())) {
				b.setAlbum(""); // album is not required to scrobble.
			} else {
				b.setAlbum(al.toString());
			}
		} else {
			b.setAlbum("");
		}
		b.setArtist(ar.toString());
		b.setTrack(tr.toString());
	}
}
