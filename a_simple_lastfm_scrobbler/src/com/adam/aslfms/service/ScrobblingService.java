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

package com.adam.aslfms.service;

import android.app.Service;
import android.content.Intent;
import android.database.SQLException;
import android.os.IBinder;
import android.util.Log;

import com.adam.aslfms.AppSettings;
import com.adam.aslfms.InternalTrackTransmitter;
import com.adam.aslfms.ScrobblesDatabase;
import com.adam.aslfms.Track;

/**
 * 
 * @author tgwizard
 * 
 */
public class ScrobblingService extends Service {

	private static final String TAG = "ScrobblingService";

	public static final String ACTION_AUTHENTICATE = "com.adam.aslfms.service.authenticate";
	public static final String ACTION_CLEARCREDS = "com.adam.aslfms.service.clearcreds";
	public static final String ACTION_PLAYSTATECHANGED = "com.adam.aslfms.service.playstatechanged";

	public static final String BROADCAST_ONAUTHCHANGED = "com.adam.aslfms.service.onauth";

	private static final int MIN_SCROBBLE_TIME = 30;

	private AppSettings settings;
	private ScrobblesDatabase mDbHelper;

	NetworkLoop mNetworkLoop;

	private Track mCurrentPlayingTrack = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		settings = new AppSettings(this);
		mDbHelper = new ScrobblesDatabase(this);
		try {
			mDbHelper.open();
		} catch(SQLException e) {
			Log.e(TAG, "Cannot open database!");
			Log.e(TAG, e.getMessage());
			Log.e(TAG, "Will terminate");
			stopSelf();
		}
		

		mNetworkLoop = new NetworkLoop(this, mDbHelper);
		new Thread(mNetworkLoop).start();
	}

	@Override
	public void onDestroy() {
		mDbHelper.close();
	}

	@Override
	public void onStart(Intent i, int startId) {
		Log.d(TAG, "ScrobblingService started");
		if (i.getAction().equals(ACTION_CLEARCREDS)) {
			Log.d(TAG, "Will launch clear creds");
			mNetworkLoop.launchClearCreds();
		} else if (i.getAction().equals(ACTION_AUTHENTICATE)) {
			mNetworkLoop.launchHandshaker(true);
		} else if (i.getAction().equals(ACTION_PLAYSTATECHANGED)) {
			boolean stopped = false;
			if (i.getExtras() != null) {
				stopped = i.getExtras().getBoolean("stopped", false);
			}

			Track track = InternalTrackTransmitter.popTrack();

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

			if (mCurrentPlayingTrack != null) {
				tryScrobble(mCurrentPlayingTrack, true);
			}

			mCurrentPlayingTrack = track;
			tryNotifyNP(mCurrentPlayingTrack);
		} else {
			if (mCurrentPlayingTrack == null) {
				tryScrobble(track, false);
			} else {
				if (!track.equals(mCurrentPlayingTrack)) {
					Log.e(TAG,
							"Stopped track doesn't equal currentPlayingTrack!");
					Log.e(TAG, "t: " + track);
					Log.e(TAG, "c: " + mCurrentPlayingTrack);
				} else {
					// must scrobble mCurrentPlayingTrack, and not track,
					// because they have
					// different timestamps
					tryScrobble(mCurrentPlayingTrack, true);
				}
			}
			mCurrentPlayingTrack = null;
		}
	}

	/**
	 * Launches a Now Playing notification of <code>track</code>, if we're authenticated and Now Playing is enabled.
	 * @param track the currently playing track
	 */
	private void tryNotifyNP(Track track) {
		if (settings.isAuthenticated() && settings.isNowPlayingEnabled()) {
			mNetworkLoop.launchNPNotifier(track);
		} else {
			Log.d(TAG, "Won't notify NP, unauthed or disabled");
		}
	}

	private void tryScrobble(Track track, boolean careAboutTrackTimeStamp) {

		if (!settings.isAuthenticated() || !settings.isScrobblingEnabled()) {
			Log.d(TAG, "Won't prepare scrobble, unauthed or disabled");
			return;
		}

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
			settings.setLastScrobbleTime(InternalTrackTransmitter.currentTimeUTC());
			mNetworkLoop.launchScrobbler();
		}
	}

	private boolean checkTime(Track track, boolean careAboutTrackTimeStamp) {
		long currentTime = InternalTrackTransmitter.currentTimeUTC();
		long diff = currentTime - settings.getLastListenTime();
		if (diff < MIN_SCROBBLE_TIME) {
			Log.i(TAG, "Tried to scrobble " + diff
					+ "s after last scrobble, which is less than the required "
					+ MIN_SCROBBLE_TIME + "s");
			Log.i(TAG, track.toString());
			return false;
		}
		long len = -1;
		if (careAboutTrackTimeStamp) {
			len = currentTime - track.getWhen();
			if (len < MIN_SCROBBLE_TIME) {
				Log.i(TAG, "Tried to scrobble "
							+ len
							+ "s after track start, which is less than the required "
							+ MIN_SCROBBLE_TIME + "s");
				return false;
			}
		}

		Log.d(TAG, "Scrobble will be prepared");
		Log.d(TAG, diff + "s since last scrobble and " + len
				+ "s since track start");
		return true;
	}

	private void scrobblePrepare(Track track) {
		if (mDbHelper.insertScrobble(track) != -1) {
			Log.d(TAG, "Scrobble prepared");
			Log.d(TAG, track.toString());
		} else {
			Log.d(TAG, "Could not insert scrobble into the db");
			Log.d(TAG, track.toString());
		}
		
	}
}
