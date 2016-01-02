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
