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

import android.content.Context;
import android.util.Log;

public class Sleeper extends NetRunnable {

	private static final String TAG = "Sleeper";

	// TODO: correct value
	private static final long START_TIME = 60 * 1000; // 1 min
	private static final long MAX_TIME = 120 * 60 * 1000; // 120 min

	private long mSleepTime;

	public Sleeper(NetApp napp, Context ctx, Networker net) {
		super(napp, ctx, net);
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
		synchronized (this) {
			try {
				Log.d(TAG, "start sleeping: " + mSleepTime + ": "
						+ getNetApp().getName());
				this.wait(mSleepTime);
				Log.d(TAG, "woke up sleeping: " + getNetApp().getName());
			} catch (InterruptedException e) {
				Log.i(TAG, "Got interrupted: " + getNetApp().getName());
				Log.i(TAG, e.getMessage());
			}
			incSleepTime();
		}
	}

}
