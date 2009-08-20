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

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adam.aslfms.AppSettings;
import com.adam.aslfms.ScrobblesDbAdapter;
import com.adam.aslfms.Status;
import com.adam.aslfms.Track;
import com.adam.aslfms.Status.BadAuthException;
import com.adam.aslfms.Status.BadSessionException;
import com.adam.aslfms.Status.FailureException;
import com.adam.aslfms.Status.TemporaryFailureException;
import com.adam.aslfms.service.Handshaker.HandshakeInfo;

/**
 * 
 * @author tgwizard
 *
 */
public class NetworkLoop implements Runnable {

	private static final String TAG = "NetLoop";

	private final Context mCtx;
	private final AppSettings settings;
	private final ScrobblesDbAdapter mDbHelper;

	private static final Object waitNotifyObject = new Object();
	private boolean mWait;

	private Scrobbler scrobbler;
	private NPNotifier npNotifier;
	private Handshaker handshaker;

	private int retryCount = 0;

	// handshake-requests
	private boolean mHandshakeRequests = false;
	private boolean mDoAuth = false;

	// scrobble-requests
	private int mScrobbleRequests = 0;

	// notifynp-requests
	private Track mNotifyNPTrack = null;

	public NetworkLoop(Context ctx, ScrobblesDbAdapter dbHelper) {
		super();
		this.mCtx = ctx;
		this.settings = new AppSettings(ctx);
		this.mDbHelper = dbHelper;
	}

	@Override
	public void run() {
		handshaker = new Handshaker(mCtx);
		scrobbler = null;
		npNotifier = null;
		while (true) {
			synchronized (waitNotifyObject) {

				mWait = true;

				if (wannaHandshake() || wannaScrobble() || wannaNotifyNP()) {
					mWait = false;
				}

				while (mWait) {
					try {
						waitNotifyObject.wait();
					} catch (InterruptedException e) {
						Log.i(TAG, "Got interrupted while waiting");
						Log.i(TAG, e.getMessage());
					}
				}
			}

			// handshaking
			boolean doHand = false;
			boolean doAuth = false;
			synchronized (this) {
				if (wannaHandshake()) {
					doHand = true;
					doAuth = mDoAuth;
					// reset
					mHandshakeRequests = false;
					mDoAuth = false;
				}
			}

			if (doHand) {
				doHandshake(doAuth);
			}

			// scrobbling
			boolean doScrobble = false;
			int sCount = 0;
			synchronized (this) {
				if (wannaScrobble()) {
					if (scrobbler == null) {
						launchHandshaker(false);
					} else {
						doScrobble = true;
						sCount = getNScrobbleReqs();
					}
				}
			}

			if (doScrobble) {
				doScrobble(sCount);
			}

			// np-notifying
			boolean doNotifyNP = false;
			Track track = null;
			synchronized (this) {
				if (wannaNotifyNP()) {
					if (npNotifier == null) {
						launchHandshaker(false);
					} else {
						doNotifyNP = true;
						track = popAndResetNPTrack();
					}
				}
			}

			if (doNotifyNP) {
				doNotifyNP(track);
			}
		}
	}

	private boolean doHandshake(boolean doAuth) {
		boolean ret = false;

		scrobbler = null;
		npNotifier = null;

		if (doAuth)
			updateAuthStatus(Status.AUTHSTATUS_UPDATING);

		try {
			HandshakeInfo hi = handshaker.handshake();

			scrobbler = new Scrobbler(mCtx, hi, mDbHelper);
			npNotifier = new NPNotifier(mCtx, hi);

			resetRetry();

			settings.setPassword("");
			updateAuthStatus(Status.AUTHSTATUS_OK);

			// won't do anything if there aren't any scrobbles,
			// but will submit those tracks that were prepared
			// but interrupted by a badauth
			launchScrobbler();

			ret = true;
		} catch (BadAuthException e) {
			if (doAuth)
				updateAuthStatus(Status.AUTHSTATUS_BADAUTH);
			else
				updateAuthStatus(Status.AUTHSTATUS_NOAUTH);

			// badauth means we cant do any scrobbling/notifying, so clear them
			// the scrobbles already prepared will be sent at a later time
			unsetScrobblingAndNPNotifying();
		} catch (TemporaryFailureException e) {
			// TODO: retry, with sleeps between
			if (doAuth)
				updateAuthStatus(Status.AUTHSTATUS_RETRYLATER);
		} catch (FailureException e) {
			Log.e(TAG, "Serious failure while handshaking");
			Log.e(TAG, e.getMessage());
			updateAuthStatus(Status.AUTHSTATUS_FAILED);
			// TODO: what??
			// this is a _serious_ failure
		}
		return ret;
	}

	private boolean doScrobble(int sCount) {
		boolean ret = false;
		if (scrobbler == null) {
			Log.e(TAG, "Scrobbler is null when we want to scrobble!!");
		} else {
			try {
				scrobbler.scrobbleCommit();
				decScrobbleReqs(sCount);
				ret = true;
			} catch (BadSessionException e) {
				Log.i(TAG, e.getMessage());
				launchHandshaker(false);
			} catch (TemporaryFailureException e) {
				Log.i(TAG, e.getMessage());
				retry();
			} catch (FailureException e) {
				Log.e(TAG, "Serious failure while scrobbling");
				Log.e(TAG, e.getMessage());
				// TODO: what??
				// this is a _serious_ failure
			}
		}
		return ret;
	}

	private boolean doNotifyNP(Track t) {
		boolean ret = false;
		if (npNotifier == null) {
			Log.e(TAG, "npNotifier is null when we want to notify-np!!");
		} else {
			try {
				npNotifier.notifyNowPlaying(t);
			} catch (BadSessionException e) {
				Log.i(TAG, e.getMessage());
				launchHandshaker(false);
			} catch (TemporaryFailureException e) {
				Log.i(TAG, e.getMessage());
				retry();
			} catch (FailureException e) {
				Log.e(TAG, "Serious failure while notifying np");
				Log.e(TAG, e.getMessage());
				// TODO: what??
				// this is a _serious_ failure
			}
		}
		return ret;
	}

	private void retry() {
		retryCount++;
		if (retryCount > 3) {
			launchHandshaker(false);
		}
	}

	private void resetRetry() {
		retryCount = 0;
	}

	private void requestResume() {
		synchronized (waitNotifyObject) {
			mWait = false;
			waitNotifyObject.notifyAll();
		}
	}

	public synchronized void launchHandshaker(boolean auth) {
		mHandshakeRequests = true;
		mDoAuth = mDoAuth || auth;
		requestResume();
	}

	public synchronized void launchClearCreds() {
		launchHandshaker(false);
		// TODO: this will still show "Wrong username/password" if handshaker
		// already is retrying
	}

	public synchronized void launchScrobbler() {
		mScrobbleRequests++;
		requestResume();
	}

	public synchronized void launchNPNotifier(Track t) {
		if (mNotifyNPTrack == null || t.getWhen() > mNotifyNPTrack.getWhen()) {
			mNotifyNPTrack = t;
		}
		requestResume();
	}

	private synchronized boolean wannaHandshake() {
		return mHandshakeRequests;
	}

	private synchronized boolean wannaScrobble() {
		return mScrobbleRequests > 0;
	}

	private synchronized boolean wannaNotifyNP() {
		return mNotifyNPTrack != null;
	}

	private synchronized Track popAndResetNPTrack() {
		Track ret = mNotifyNPTrack;
		mNotifyNPTrack = null;
		return ret;
	}

	private synchronized int getNScrobbleReqs() {
		return mScrobbleRequests;
	}

	private synchronized void decScrobbleReqs(int a) {
		mScrobbleRequests -= a;
	}

	private synchronized void unsetScrobblingAndNPNotifying() {
		mScrobbleRequests = 0;
		mNotifyNPTrack = null;
	}

	private void updateAuthStatus(int st) {
		Log.d(TAG, "updateAS: " + st);
		settings.setAuthStatus(st);
		Intent i = new Intent(ScrobblingService.BROADCAST_ONAUTHCHANGED);
		mCtx.sendBroadcast(i);
	}

}
