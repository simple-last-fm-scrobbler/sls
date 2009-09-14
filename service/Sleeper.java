package com.adam.aslfms.service;

import android.content.Context;
import android.util.Log;

public class Sleeper extends NetRunnable {

	private static final String TAG = "Sleeper";

	// TODO: correct value
	private static final long START_TIME = 5000; //60 * 1000; // 1 min
	private static final long MAX_TIME = 120 * 60 * 1000; // 120 min

	private long mSleepTime;

	public Sleeper(Context ctx, Networker net) {
		super(ctx, net);
		reset();
	}

	public void reset() {
		synchronized (this) {
			mSleepTime = START_TIME;
			this.notifyAll(); // if we were waiting, which we probably wasn't
		}
	}

	private void incSleepTime() {
		synchronized (this) {
			mSleepTime *= 2;
			if (mSleepTime > MAX_TIME) {
				mSleepTime = MAX_TIME;
			}
		}
	}

	@Override
	public void run() {
		Log.d(TAG, "start sleeping");
		synchronized (this) {
			try {
				Log.d(TAG, "go sleeping: " + mSleepTime);
				this.wait(mSleepTime);
				Log.d(TAG, "un sleeping");
			} catch (InterruptedException e) {
				Log.i(TAG, "Got interrupted");
				Log.i(TAG, e.getMessage());
			}
			incSleepTime();
		}
		
		//getNetworker().launchHandshaker(false);
		Log.d(TAG, "stop sleeping");
	}

}
