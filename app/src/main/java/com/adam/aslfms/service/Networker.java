/**
 * This file is part of Simple Last.fm Scrobbler.
 * 
 *     https://github.com/tgwizard/sls
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
 

package com.adam.aslfms.service;

import android.content.Context;

import com.adam.aslfms.service.Handshaker.HandshakeAction;
import com.adam.aslfms.service.Handshaker.HandshakeResult;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

	public void launchHeartTrack(Track track) {
		Iterator<Runnable> i = mExecutor.getQueue().iterator();
		while (i.hasNext()) {
			Runnable r = i.next();
			if (r.getClass() == Heart.class) {
				i.remove();
			}
		}

		Heart n = new Heart(mNetApp, mCtx, this, track, settings);
		mExecutor.execute(n);
	}

	public void launchUserInfo() {
		Iterator<Runnable> i = mExecutor.getQueue().iterator();
		while (i.hasNext()) {
			Runnable r = i.next();
			if (r.getClass() == UserInfo.class) {
				i.remove();
			}
		}

		UserInfo n = new UserInfo(mNetApp, mCtx, this, settings);
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
