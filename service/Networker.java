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

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;

import com.adam.aslfms.AppSettings;
import com.adam.aslfms.ScrobblesDatabase;
import com.adam.aslfms.Track;
import com.adam.aslfms.service.Handshaker.HandshakeResult;

public class Networker {
	@SuppressWarnings("unused")
	private static final String TAG = "Networker";

	private final AppSettings settings;

	private final NetApp mNetApp;

	private final Context mCtx;
	private final ScrobblesDatabase mDbHelper;

	private final ThreadPoolExecutor mExecutor;

	private final NetRunnableComparator mComparator;

	private final NetworkWaiter mNetworkWaiter;
	private final Sleeper mSleeper;

	private HandshakeResult hInfo;

	public Networker(NetApp napp, Context ctx, ScrobblesDatabase dbHelper) {
		settings = new AppSettings(ctx);

		mNetApp = napp;
		mCtx = ctx;
		mDbHelper = dbHelper;

		mComparator = new NetRunnableComparator();

		// FIXME: what should the keepAliveTime/unit be?
		mExecutor = new ThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS,
				new PriorityBlockingQueue<Runnable>(1, mComparator));

		mSleeper = new Sleeper(mNetApp, ctx, this);
		mNetworkWaiter = new NetworkWaiter(mNetApp, ctx, this);
		hInfo = null;
	}

	public void launchHandshaker(boolean doAuth) {
		Handshaker h = new Handshaker(mNetApp, mCtx, this, doAuth);
		execute(h);
	}

	public void launchClearCreds() {
		settings.clearCreds(mNetApp);
		launchHandshaker(false);
		// TODO: this will still show "Wrong username/password" if handshaker
		// already is retrying
	}

	public void launchScrobbler() {
		Scrobbler s = new Scrobbler(mNetApp, mCtx, this, mDbHelper);
		execute(s);
	}

	public void launchNPNotifier(Track track) {
		NPNotifier n = new NPNotifier(mNetApp, mCtx, this, track);
		execute(n);
	}

	public void launchSleeper() {
		execute(mSleeper);
	}

	public void resetSleeper() {
		mSleeper.reset();
	}

	public void launchNetworkWaiter() {
		execute(mNetworkWaiter);
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

	private void execute(NetRunnable r) {
		mExecutor.execute(r);
	}
}
