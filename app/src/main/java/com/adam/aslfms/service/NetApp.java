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

import java.util.HashMap;
import java.util.Map;

import com.adam.aslfms.util.AppSettings;

public enum NetApp {
	LASTFM(
		0x01, "Last.fm", "http://post.audioscrobbler.com/?hs=true", "",
		"https://www.last.fm/join", "https://www.last.fm/user/%1"), //
	LIBREFM(
		0x02, "Libre.fm", "http://turtle.libre.fm/?hs=true", "librefm",
		"https://libre.fm/", "https://libre.fm/user/%1");

	private final int val;
	private final String name;
	private final String handshakeUrl;
	private final String settingsPrefix;
	private final String signUpUrl;
	private final String profileUrl;

	private NetApp(int val, String name, String handshakeUrl,
		String settingsPrefix, String signUpUrl, String profileUrl) {
		this.val = val;
		this.name = name;
		this.handshakeUrl = handshakeUrl;
		this.settingsPrefix = settingsPrefix;
		this.signUpUrl = signUpUrl;
		this.profileUrl = profileUrl;
	}

	public String getIntentExtraValue() {
		return toString();
	}

	public int getValue() {
		return this.val;
	}

	public String getName() {
		return this.name;
	}

	public String getHandshakeUrl() {
		return this.handshakeUrl;
	}

	public String getSettingsPrefix() {
		return settingsPrefix;
	}

	public String getProfileUrl(AppSettings settings) {
		return profileUrl.replaceAll("%1", settings.getUsername(this));
	}

	public String getSignUpUrl() {
		return signUpUrl;
	}

	private static Map<Integer, NetApp> mValNetAppMap;

	static {
		mValNetAppMap = new HashMap<Integer, NetApp>();
		for (NetApp napp : NetApp.values())
			mValNetAppMap.put(napp.getValue(), napp);
	}

	public static NetApp fromValue(int value) {
		NetApp napp = mValNetAppMap.get(value);
		if (napp == null) {
			throw new IllegalArgumentException("Got null netapp in fromValue: "
				+ value);
		}
		return napp;
	}

}
