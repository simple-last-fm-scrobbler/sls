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

package com.adam.aslfms.service;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;

import com.adam.aslfms.service.Handshaker.HandshakeAction;
import com.adam.aslfms.service.Handshaker.HandshakeResult;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;

public class Networker {
	@SuppressWarnings("unused")
	private static final String TAG = "Networker";

	private final AppSettings settings;

	private final NetApp mNetApp;

	private final Context mCtx;
	private final ScrobblesDatabase mDb;

	private final ThreadPoolExecutor mExecutor;

	private final NetRunnableComparator mComparator;

	private final NetworkWaiter mNetworkWaiter;
	private final Sleeper mSleeper;

	private HandshakeResult hInfo;

	public Networker(NetApp napp, Context ctx, ScrobblesDatabase db) {
		settings = new AppSettings(ctx);

		mNetApp = napp;
		mCtx = ctx;
		mDb = db;

		mComparator = new NetRunnableComparator();

		// TODO: what should the keepAliveTime/unit be?
		mExecutor = new ThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS,
				new PriorityBlockingQueue<Runnable>(1, mComparator));

		mSleeper = new Sleeper(mNetApp, ctx, this);
		mNetworkWaiter = new NetworkWaiter(mNetApp, ctx, this);
		hInfo = null;
	}

	public void launchAuthenticator() {
		launchHandshaker(HandshakeAction.AUTH);
	}

	public void launchClearCreds() {
		settings.clearCreds(mNetApp);

		mDb.deleteAllScrobbles(mNetApp);
		mDb.cleanUpTracks();

		launchHandshaker(HandshakeAction.CLEAR_CREDS);
	}

	public void launchHandshaker() {
		launchHandshaker(HandshakeAction.HANDSHAKE);
	}

	public void launchHandshaker(HandshakeAction hsAction) {
		Handshaker h = new Handshaker(mNetApp, mCtx, this, hsAction);
		mExecutor.execute(h);
	}

	public void launchScrobbler() {
		Iterator<Runnable> i = mExecutor.getQueue().iterator();
		while (i.hasNext()) {
			Runnable r = i.next();
			if (r.getClass() == Scrobbler.class) {
				i.remove();
			}
		}

		Scrobbler s = new Scrobbler(mNetApp, mCtx, this, mDb);
		mExecutor.execute(s);
	}

	public void launchNPNotifier(Track track) {
		Iterator<Runnable> i = mExecutor.getQueue().iterator();
		while (i.hasNext()) {
			Runnable r = i.next();
			if (r.getClass() == NPNotifier.class) {
				i.remove();
			}
		}

		NPNotifier n = new NPNotifier(mNetApp, mCtx, this, track);
		mExecutor.execute(n);
	}

	public void launchSleeper() {
		mExecutor.execute(mSleeper);
	}

	public void resetSleeper() {
		mSleeper.reset();
	}

	public void launchNetworkWaiter() {
		mExecutor.execute(mNetworkWaiter);
	}

	public void unlaunchScrobblingAndNPNotifying() {
		Iterator<Runnable> i = mExecutor.getQueue().iterator();
		while (i.hasNext()) {
			Runnable r = i.next();
			if (r.getClass() == Scrobbler.class
					|| r.getClass() == NPNotifier.class) {
				i.remove();
			}
		}
	}

	public void setHandshakeResult(HandshakeResult h) {
		hInfo = h;
	}

	public HandshakeResult getHandshakeResult() {
		return hInfo;
	}

}
