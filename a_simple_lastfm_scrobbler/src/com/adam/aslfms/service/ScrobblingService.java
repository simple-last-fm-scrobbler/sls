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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.InternalTrackTransmitter;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.AppSettingsEnums.AdvancedOptionsWhen;

/**
 * 
 * @author tgwizard
 * 
 */
public class ScrobblingService extends Service {

	private static final String TAG = "ScrobblingService";

	public static final String ACTION_AUTHENTICATE = "com.adam.aslfms.service.authenticate";
	public static final String ACTION_CLEARCREDS = "com.adam.aslfms.service.clearcreds";
	public static final String ACTION_JUSTSCROBBLE = "com.adam.aslfms.service.justscrobble";
	public static final String ACTION_PLAYSTATECHANGED = "com.adam.aslfms.service.playstatechanged";

	public static final String BROADCAST_ONAUTHCHANGED = "com.adam.aslfms.service.bcast.onauth";
	public static final String BROADCAST_ONSTATUSCHANGED = "com.adam.aslfms.service.bcast.onstatus";

	private static final int MIN_SCROBBLE_TIME = 30;

	private AppSettings settings;
	private ScrobblesDatabase mDb;

	private NetworkerManager mNetManager;

	private Track mCurrentPlayingTrack = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		settings = new AppSettings(this);
		mDb = new ScrobblesDatabase(this);
		mDb.open();
		mNetManager = new NetworkerManager(this, mDb);
	}

	@Override
	public void onDestroy() {
		mDb.close();
	}

	@Override
	public void onStart(Intent i, int startId) {
		String action = i.getAction();
		Bundle extras = i.getExtras();
		if (action.equals(ACTION_CLEARCREDS)) {
			if (extras.getBoolean("clearall", false)) {
				mNetManager.launchClearAllCreds();
			} else {
				String snapp = extras.getString("netapp");
				if (snapp != null) {
					mNetManager.launchClearCreds(NetApp.valueOf(snapp));
				} else
					Log.e(TAG, "launchClearCreds got null napp");
			}
		} else if (action.equals(ACTION_AUTHENTICATE)) {
			String snapp = extras.getString("netapp");
			if (snapp != null)
				mNetManager.launchAuthenticator(NetApp.valueOf(snapp));
			else
				Log.e(TAG, "launchHandshaker got null napp");
		} else if (action.equals(ACTION_JUSTSCROBBLE)) {
			if (extras.getBoolean("scrobbleall", false)) {
				mNetManager.launchAllScrobblers();
			} else {
				String snapp = extras.getString("netapp");
				if (snapp != null) {
					mNetManager.launchScrobbler(NetApp.valueOf(snapp));
				} else
					Log.e(TAG, "launchScrobbler got null napp");
			}
		} else if (action.equals(ACTION_PLAYSTATECHANGED)) {
			boolean stopped = false;
			if (extras != null) {
				stopped = extras.getBoolean("stopped", false);
			}

			Track track = InternalTrackTransmitter.popTrack();

			if (track == null) {
				Log.e(TAG, "A null track got through!! (Ignoring it)");
				return;
			}

			onPlayStateChanged(track, stopped);

		} else {
			Log.e(TAG, "Weird action in onStart: " + action);
		}
	}

	private synchronized void onPlayStateChanged(Track track, boolean stopped) {
		if (!stopped) {
			if (track.equals(mCurrentPlayingTrack)) // we have already been here
				return;

			if (mCurrentPlayingTrack != null) {
				tryScrobble(mCurrentPlayingTrack, true, false);
			}

			mCurrentPlayingTrack = track;
			tryNotifyNP(mCurrentPlayingTrack);
		} else {
			if (mCurrentPlayingTrack == null) {
				tryScrobble(track, false, true);
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
					tryScrobble(mCurrentPlayingTrack, true, true);
				}
			}
			mCurrentPlayingTrack = null;
		}
	}

	/**
	 * Launches a Now Playing notification of <code>track</code>, if we're
	 * authenticated and Now Playing is enabled.
	 * 
	 * @param track
	 *            the currently playing track
	 */
	private void tryNotifyNP(Track track) {
		if (settings.isAnyAuthenticated() && settings.isNowPlayingEnabled()) {
			mNetManager.launchNPNotifier(track);
		} else {
			Log.d(TAG, "Won't notify NP, unauthed or disabled");
		}
	}

	private void tryScrobble(Track track, boolean careAboutTrackTimeStamp,
			boolean playbackComplete) {

		if (!settings.isAnyAuthenticated() || !settings.isScrobblingEnabled()) {
			Log.d(TAG, "Won't prepare scrobble, unauthed or disabled");
			return;
		}

		if (track == null) {
			Log.e(TAG, "Got null track in tryScrobble!");
			return;
		}
		if (checkTime(track, careAboutTrackTimeStamp)) {
			// TODO: should prepare scrobble earlier
			// But that will not be possible with the limited info available
			// from MusicPlaybackService
			queueTrack(track);
			settings.setLastListenTime(Util.currentTimeSecsUTC());

			scrobble(playbackComplete);
		}
	}

	private void scrobble(boolean playbackComplete) {
		boolean aoc = settings.getAdvancedOptionsAlsoOnComplete();
		if (aoc && playbackComplete) {
			mNetManager.launchAllScrobblers();
			return;
		}

		AdvancedOptionsWhen aow = settings.getAdvancedOptionsWhen();
		for (NetApp napp: NetApp.values()) {
			int numInCache = mDb.queryNumberOfScrobbles(napp);
			if (numInCache >= aow.getTracksToWaitFor()) {
				mNetManager.launchScrobbler(napp);
			}
		}
	}

	private boolean checkTime(Track track, boolean careAboutTrackTimeStamp) {
		long currentTime = Util.currentTimeSecsUTC();
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
				Log
						.i(
								TAG,
								"Tried to scrobble "
										+ len
										+ "s after track start, which is less than the required "
										+ MIN_SCROBBLE_TIME + "s");
				return false;
			}
		}

		/*
		 * Log.d(TAG, "Scrobble will be prepared"); Log.d(TAG, diff +
		 * "s since last scrobble and " + len + "s since track start");
		 */
		return true;
	}

	private void queueTrack(Track track) {
		long rowId = mDb.insertTrack(track);
		if (rowId != -1) {
			Log.d(TAG, "queued track: " + track.toString());

			// now set up scrobbling rels
			for (NetApp napp : NetApp.values()) {
				if (settings.isAuthenticated(napp)) {
					Log.d(TAG, "inserting scrobble: " + napp.getName());
					mDb.insertScrobble(napp, rowId);

					// tell interested parties
					Intent i = new Intent(
							ScrobblingService.BROADCAST_ONSTATUSCHANGED);
					i.putExtra("netapp", napp.getIntentExtraValue());
					sendBroadcast(i);
				}
			}
		} else {
			Log.d(TAG, "Could not insert scrobble into the db");
			Log.d(TAG, track.toString());
		}

	}
}
