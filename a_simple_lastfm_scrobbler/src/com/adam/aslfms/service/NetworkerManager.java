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

import java.util.EnumMap;
import java.util.Map;

import android.content.Context;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;

public class NetworkerManager {

	@SuppressWarnings("unused")
	private static final String TAG = "SLSNetManager";

	private final AppSettings settings;

	private Map<NetApp, Networker> mSupportedNetApps;

	public NetworkerManager(Context ctx, ScrobblesDatabase db) {
		this.settings = new AppSettings(ctx);
		mSupportedNetApps = new EnumMap<NetApp, Networker>(NetApp.class);
		for (NetApp napp : NetApp.values())
			mSupportedNetApps.put(napp, new Networker(napp, ctx, db));
	}

	public void launchAuthenticator(NetApp napp) {
		mSupportedNetApps.get(napp).launchAuthenticator();
	}

	public void launchClearCreds(NetApp napp) {
		mSupportedNetApps.get(napp).launchClearCreds();
	}

	public void launchHandshaker(NetApp napp) {
		mSupportedNetApps.get(napp).launchHandshaker();
	}

	public void launchClearAllCreds() {
		for (Networker nw : mSupportedNetApps.values())
			nw.launchClearCreds();
	}

	public void launchNPNotifier(Track track) {
		for (NetApp napp : NetApp.values()) {
			if (settings.isAuthenticated(napp)) {
				mSupportedNetApps.get(napp).launchNPNotifier(track);
			}
		}
	}

	public void launchScrobbler(NetApp napp) {
		if (settings.isAuthenticated(napp)) {
			mSupportedNetApps.get(napp).launchScrobbler();
		}
	}

	public void launchAllScrobblers() {
		for (NetApp napp : NetApp.values()) {
			launchScrobbler(napp);
		}
	}

}
