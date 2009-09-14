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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class NetworkWaiter extends NetRunnable {

	private static final String TAG = "NetworkWaiter";
	boolean mWait;

	NetworkWaiter(Context ctx, Networker net) {
		super(ctx, net);
	}

	@Override
	public void run() {
		// register receiver
		IntentFilter ifs = new IntentFilter();
		ifs.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		getContext().registerReceiver(mConnReceiver, ifs);

		synchronized (this) {
			ConnectivityManager cMgr = (ConnectivityManager) getContext()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
			mWait = netInfo == null || !netInfo.isConnected();
			while (mWait) {
				try {
					Log.d(TAG, "waiting for network connection");
					this.wait();
					Log.d(TAG, "woke up, there's probably a network connection");
				} catch (InterruptedException e) {
					Log.i(TAG, "Got interrupted");
					Log.i(TAG, e.getMessage());
				}
			}
		}

		// unregister receiver
		getContext().unregisterReceiver(mConnReceiver);
	}

	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (NetworkWaiter.this) {
				Bundle extras = intent.getExtras();
				if (extras == null || !extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY)) {
					NetworkWaiter.this.mWait = false;
					NetworkWaiter.this.notifyAll();
				}
			}
		}
	};

}
