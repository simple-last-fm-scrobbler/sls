package com.adam.aslfms.service;

import android.content.Context;
import android.content.Intent;

public abstract class NetRunnable implements Runnable {

	private Context mContext;
	private Networker mNetworker;

	public NetRunnable(Context ctx, Networker net) {
		super();
		this.mContext = ctx;
		this.mNetworker = net;
	}

	public Context getContext() {
		return mContext;
	}

	public Networker getNetworker() {
		return mNetworker;
	}
	
	protected void notifyStatusUpdate() {
		Intent i = new Intent(ScrobblingService.BROADCAST_ONSTATUSCHANGED);
		mContext.sendBroadcast(i);
	}

	@Override
	public abstract void run();

}
