package com.adam.aslfms.receiver;

import com.adam.aslfms.util.Track;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class SEMCMusicReceiver extends BuiltInMusicAppReceiver {

	private static final String TAG = "SEMCMusicReceiver";

	static final String APP_PACKAGE = "com.sonyericsson.music";
	static final String ACTION_SEMC_STOP_LEGACY = "com.sonyericsson.music.playbackcontrol.ACTION_PLAYBACK_PAUSE";
	static final String ACTION_SEMC_STOP = "com.sonyericsson.music.playbackcontrol.ACTION_PAUSED";
	static final String ACTION_SEMC_METACHANGED = "com.sonyericsson.music.metachanged";

	public SEMCMusicReceiver() {
		super(ACTION_SEMC_STOP, APP_PACKAGE, "Sony Ericsson Music Player");
	}
	
	@Override
	/**
	 * Checks that the action received is either the one used in the
	 * newer Sony Xperia devices or that of the previous versions
	 * of the app.
	 * 
	 * @param action	the received action
	 * @return			true when the received action is a stop action, false otherwise
	 */
	protected boolean isStopAction(String action) {
		return action.equals(ACTION_SEMC_STOP) || action.equals(ACTION_SEMC_STOP_LEGACY);
	}

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle) throws IllegalArgumentException {
		super.parseIntent(ctx, action, bundle);
	}

	@Override
	void readTrackFromBundleData(Track.Builder b, Bundle bundle) {
		Log.d(TAG, "Will read data from SEMC intent");

		CharSequence ar = bundle.getCharSequence("ARTIST_NAME");
		CharSequence al = bundle.getCharSequence("ALBUM_NAME");
		CharSequence tr = bundle.getCharSequence("TRACK_NAME");

		if (ar == null || al == null || tr == null) {
			throw new IllegalArgumentException("null track values");
		}

		b.setArtist(ar.toString());
		b.setAlbum(al.toString());
		b.setTrack(tr.toString());
	}

}
