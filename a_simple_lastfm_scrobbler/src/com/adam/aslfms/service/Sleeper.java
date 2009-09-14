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
