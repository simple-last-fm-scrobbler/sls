package com.adam.aslfms.service;

import android.content.Context;

import com.adam.aslfms.service.Handshaker.HandshakeResult;

public abstract class AbstractSubmitter extends NetRunnable {

	@SuppressWarnings("unused")
	private static final String TAG = "ASubmitter";
	private HandshakeResult hInfo;

	public AbstractSubmitter(Context ctx, Networker net) {
		super(ctx, net);
	}

	@Override
	public final void run() {
		// TODO Auto-generated method stub
		hInfo = getNetworker().getHandshakeResult();
		if (hInfo == null) {
			getNetworker().launchHandshaker(false);
			relaunchThis();
			return;
		} else {
			int rCount = 0;
			boolean retry = false;
			do {
				retry = !doRun(hInfo);
				rCount++;
			} while (retry && rCount < 3);

			if (rCount >= 3) {
				getNetworker().launchHandshaker(false);
				relaunchThis();
			}
		}
	}

	protected abstract boolean doRun(HandshakeResult hInfo);

	protected abstract void relaunchThis();
}
