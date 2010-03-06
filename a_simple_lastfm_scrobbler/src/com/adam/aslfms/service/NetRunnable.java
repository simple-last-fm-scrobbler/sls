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
import android.content.Intent;

public abstract class NetRunnable implements Runnable {

	private final NetApp mNetApp;
	private final Context mContext;
	private final Networker mNetworker;

	public NetRunnable(NetApp napp, Context ctx, Networker net) {
		super();
		this.mNetApp = napp;
		this.mContext = ctx;
		this.mNetworker = net;
	}

	public NetApp getNetApp() {
		return mNetApp;
	}

	public Context getContext() {
		return mContext;
	}

	public Networker getNetworker() {
		return mNetworker;
	}

	protected void notifyStatusUpdate() {
		Intent i = new Intent(ScrobblingService.BROADCAST_ONSTATUSCHANGED);
		i.putExtra("netapp", mNetApp.getIntentExtraValue());
		mContext.sendBroadcast(i);
	}

	@Override
	public abstract void run();

}
