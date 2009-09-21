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

import com.adam.aslfms.AppSettings;
import com.adam.aslfms.R;
import com.adam.aslfms.Status;

public enum NetApp {
	LASTFM(0x01, "Last.fm", "http://post.audioscrobbler.com/?hs=true", "",
			"https://www.last.fm/join", R.drawable.lastfm_logo), //
	LIBREFM(0x02, "Libre.fm", "http://turtle.libre.fm/?hs=true", "librefm",
			"http://libre.fm/", R.drawable.librefm_logo);

	private final int val;
	private final String name;
	private final String handshakeUrl;
	private final String settingsPrefix;
	private final String signUpUrl;
	private final int logoRes;

	NetApp(int v, String n, String handshakeUrl, String settingsPrefix,
			String signUpUrl, int logoRes) {
		this.val = v;
		this.name = n;
		this.handshakeUrl = handshakeUrl;
		this.settingsPrefix = settingsPrefix;
		this.signUpUrl = signUpUrl;
		this.logoRes = logoRes;
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

	public String getSignUpUrl() {
		return signUpUrl;
	}

	public int getLogoRes() {
		return logoRes;
	}

	/**
	 * TODO: Should it be here?
	 * 
	 * @param ctx
	 * @param settings
	 * @return
	 */
	public String getStatusSummary(Context ctx, AppSettings settings) {
		if (settings.getAuthStatus(this) == Status.AUTHSTATUS_BADAUTH) {
			return ctx.getString(R.string.auth_bad_auth);
		} else if (settings.getAuthStatus(this) == Status.AUTHSTATUS_FAILED) {
			return ctx.getString(R.string.auth_internal_error);
		} else if (settings.getAuthStatus(this) == Status.AUTHSTATUS_RETRYLATER) {
			return ctx.getString(R.string.auth_network_error);
		} else if (settings.getAuthStatus(this) == Status.AUTHSTATUS_OK) {
			return ctx.getString(R.string.logged_in_as) + " "
					+ settings.getUsername(this);
		} else if (settings.getAuthStatus(this) == Status.AUTHSTATUS_NOAUTH) {
			return ctx.getString(R.string.user_credentials_summary).replace(
					"%1", this.getName());
		} else if (settings.getAuthStatus(this) == Status.AUTHSTATUS_UPDATING) {
			return ctx.getString(R.string.auth_updating);
		} else if (settings.getAuthStatus(this) == Status.AUTHSTATUS_CLIENTBANNED) {
			return ctx.getString(R.string.auth_client_banned);
		} else {
			return "";
		}
	}
}
