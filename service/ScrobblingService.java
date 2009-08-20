package com.adam.aslfms.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.adam.aslfms.AppSettings;
import com.adam.aslfms.AppTransaction;
import com.adam.aslfms.ScrobblesDbAdapter;
import com.adam.aslfms.Track;

public class ScrobblingService extends Service {

	private static final String TAG = "ScrobblingService";

	public static final String ACTION_AUTHENTICATE = "com.adam.aslfms.service.authenticate";
	public static final String ACTION_CLEARCREDS = "com.adam.aslfms.service.clearcreds";
	public static final String ACTION_PLAYSTATECHANGED = "com.adam.aslfms.service.playstatechanged";

	public static final String BROADCAST_ONAUTHCHANGED = "com.adam.aslfms.service.onauth";

	private static final int MINIMUM_SCROBBLE_TIME = 30;

	private AppSettings settings;
	private ScrobblesDbAdapter mDbHelper;

	NetworkLoop mNetworkLoop;

	private Track mCurrentPlayingTrack = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		settings = new AppSettings(this);
		mDbHelper = new ScrobblesDbAdapter(this);
		mDbHelper.open();

		mNetworkLoop = new NetworkLoop(this, mDbHelper);
		new Thread(mNetworkLoop).start();
	}

	@Override
	public void onDestroy() {
		mDbHelper.close();
	}

	@Override
	public void onStart(Intent i, int startId) {
		if (i.getAction().equals(ACTION_CLEARCREDS)) {
			mNetworkLoop.launchClearCreds();
		} else if (i.getAction().equals(ACTION_AUTHENTICATE)) {
			mNetworkLoop.launchHandshaker(true);
		} else if (i.getAction().equals(ACTION_PLAYSTATECHANGED)) {
			boolean stopped = false;
			if (i.getExtras() != null) {
				stopped = i.getExtras().getBoolean("stopped", false);
			}

			Track track = AppTransaction.popTrack();

			if (track == null) {
				Log.e(TAG, "Track was null when received in onStart!");
				return;
			}

			onPlayStateChanged(track, stopped);

		} else {
			Log.e(TAG, "Weird action in onStart: " + i.getAction());
		}
	}

	private synchronized void onPlayStateChanged(Track track, boolean stopped) {
		if (!stopped) {
			if (track.equals(mCurrentPlayingTrack)) // we have already been here
				return;

			mCurrentPlayingTrack = track;
			if (settings.isAuthenticated() && settings.isNowPlayingEnabled()) {
				mNetworkLoop.launchNPNotifier(mCurrentPlayingTrack);
			}
		} else {
			if (settings.isAuthenticated() && settings.isScrobblingEnabled()) {
				if (mCurrentPlayingTrack == null) {
					tryScrobble(track, false);
				} else {
					if (!track.equals(mCurrentPlayingTrack)) {
						Log.e(TAG, "Stopped track doesn't equal currentPlayingTrack!");
						Log.e(TAG, "t: " + track);
						Log.e(TAG, "c: " + mCurrentPlayingTrack);
					} else {
						// must scrobble mCurrentPlayingTrack, and not track,
						// because they have
						// different timestamps
						tryScrobble(mCurrentPlayingTrack, true);
					}
				}
			} else {
				Log.d(TAG, "Won't prepare scrobble, unauthed or disabled");
			}
			mCurrentPlayingTrack = null;
		}
	}

	private void tryScrobble(Track track, boolean careAboutTrackTimeStamp) {
		Log.d(TAG, "Might Scrobble");
		if (track == null) {
			Log.e(TAG, "Got null track in tryScrobble!");
			return;
		}
		if (checkTime(track, careAboutTrackTimeStamp)) {
			// TODO: should prepare scrobble earlier
			// But that will not be possible with the limited info available
			// from MusicService
			scrobblePrepare(track);
			settings.setLastScrobbleTime(AppTransaction.currentTimeUTC());
			mNetworkLoop.launchScrobbler();
		}
	}

	private boolean checkTime(Track track, boolean careAboutTrackTimeStamp) {
		long currentTime = AppTransaction.currentTimeUTC();
		long diff = currentTime - settings.getLastListenTime();
		if (diff < MINIMUM_SCROBBLE_TIME) {
			Log.i(TAG, "Tried to scrobble " + diff
					+ "s after last scrobble, which is less than the required "
					+ MINIMUM_SCROBBLE_TIME + "s");
			Log.i(TAG, track.toString());
			return false;
		}
		long len = -1;
		if (careAboutTrackTimeStamp) {
			len = currentTime - track.getWhen();
			if (len < MINIMUM_SCROBBLE_TIME) {
				Log.i(TAG, "Tried to scrobble " + len
						+ "s after track start, which is less than the required "
						+ MINIMUM_SCROBBLE_TIME + "s");
				return false;
			}
		}

		Log.d(TAG, "Scrobble will be prepared");
		Log.d(TAG, diff + "s since last scrobble and " + len
				+ "s since track start");
		return true;
	}

	private void scrobblePrepare(Track track) {
		mDbHelper.insertScrobble(track);
		Log.d(TAG, "Scrobble prepared");
		Log.d(TAG, track.toString());
	}
}
