package com.adam.aslfms.service;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;

import com.adam.aslfms.ScrobblesDatabase;
import com.adam.aslfms.Track;
import com.adam.aslfms.service.Handshaker.HandshakeResult;

public class Networker {
	@SuppressWarnings("unused")
	private static final String TAG = "Networker";
	
	private final Context mCtx;
	private final ScrobblesDatabase mDbHelper;

	private final ThreadPoolExecutor mExecutor;

	private final NetRunnableComparator mComparator;

	private final NetworkWaiter mNetworkWaiter;
	private final Sleeper mSleeper;

	private HandshakeResult hInfo;

	public Networker(Context ctx, ScrobblesDatabase dbHelper) {
		mCtx = ctx;
		mDbHelper = dbHelper;

		mComparator = new NetRunnableComparator();

		// FIXME: what should the keepAliveTime/unit be?
		mExecutor = new ThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS,
				new PriorityBlockingQueue<Runnable>(1, mComparator));

		mSleeper = new Sleeper(ctx, this);
		mNetworkWaiter = new NetworkWaiter(ctx, this);
		hInfo = null;
	}

	public void launchHandshaker(boolean doAuth) {
		Handshaker h = new Handshaker(mCtx, this, doAuth);
		execute(h);
	}
	
	public void launchClearCreds() {
		launchHandshaker(false);
		// TODO: this will still show "Wrong username/password" if handshaker
		// already is retrying
	}

	public void launchScrobbler() {
		Scrobbler s = new Scrobbler(mCtx, this, mDbHelper);
		execute(s);
	}

	public void launchNPNotifier(Track track) {
		NPNotifier n = new NPNotifier(mCtx, this, track);
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
