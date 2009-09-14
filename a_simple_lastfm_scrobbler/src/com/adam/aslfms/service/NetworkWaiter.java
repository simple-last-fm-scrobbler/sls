package com.adam.aslfms.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkWaiter extends NetRunnable {

	private static final String TAG = "NetworkWaiter";
	boolean mWait;

	NetworkWaiter(Context ctx, Networker net) {
		super(ctx, net);
	}

	@Override
	public void run() {
		Log.d(TAG, "start waiting");
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
					Log.d(TAG, "go waiting");
					this.wait();
					Log.d(TAG, "ungo waiting");
				} catch (InterruptedException e) {
					Log.i(TAG, "Got interrupted");
					Log.i(TAG, e.getMessage());
				}
			}
		}

		// unregister receiver
		getContext().unregisterReceiver(mConnReceiver);
		Log.d(TAG, "stop waiting");
	}

	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (NetworkWaiter.this) {
				Log.d(TAG, "Received something");
				NetworkWaiter.this.mWait = false;
				NetworkWaiter.this.notifyAll();
			}
		}
	};

}
