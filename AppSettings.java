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

package com.adam.aslfms;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 
 * @author tgwizard
 *
 */
public class AppSettings {

	private final String SETTINGS_NAME = "settings";

	private final String KEY_USERNAME = "username";
	private final String KEY_PASSWORD = "password";
	private final String KEY_PWDMD5 = "pwdMd5";

	private final String KEY_SCROBBLING_ENABLE = "enable_scrobbling";
	private final String KEY_NOWPLAYING_ENABLE = "enable_nowplaying";

	private final String KEY_AUTH_STATUS = "authstatus";

	private final String KEY_LAST_LISTEN_TIME = "last_listen_time";
	
	// status stuff
	private final String KEY_STATUS_LAST_SCROBBLE_TIME = "status_last_scrobble_time";
	private final String KEY_STATUS_LAST_SCROBBLE_SUCCESS = "status_last_scrobble_success";
	private final String KEY_STATUS_LAST_SCROBBLE_INFO = "status_last_scrobble_info";
	
	private final String KEY_STATUS_LAST_NP_TIME = "status_last_np_time";
	private final String KEY_STATUS_LAST_NP_SUCCESS = "status_last_np_success";
	private final String KEY_STATUS_LAST_NP_INFO = "status_last_np_info";
	
	private final String KEY_STATUS_NSCROBBLES = "status_nscrobbles";
	private final String KEY_STATUS_NNPS = "status_nnps";

	private SharedPreferences prefs;

	public AppSettings(Context ctx) {
		super();
		prefs = ctx.getSharedPreferences(SETTINGS_NAME, 0);
	}

	public void clearSettings() {
		setUsername("");
		setPassword("");
		setPwdMd5("");
		setAuthStatus(Status.AUTHSTATUS_NOAUTH);
		setScrobblingEnabled(false);
		setNowPlayingEnabled(false);
	}

	public void setUsername(String s) {
		Editor e = prefs.edit();
		e.putString(KEY_USERNAME, s);
		e.commit();
	}

	public String getUsername() {
		return prefs.getString(KEY_USERNAME, "");
	}

	public void setPassword(String s) {
		Editor e = prefs.edit();
		e.putString(KEY_PASSWORD, s);
		e.commit();
	}

	public String getPassword() {
		return prefs.getString(KEY_PASSWORD, "");
	}

	public void setPwdMd5(String s) {
		Editor e = prefs.edit();
		e.putString(KEY_PWDMD5, s);
		e.commit();
	}

	public String getPwdMd5() {
		return prefs.getString(KEY_PWDMD5, "");
	}

	public void setScrobblingEnabled(boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_SCROBBLING_ENABLE, b);
		e.commit();
	}

	public boolean isScrobblingEnabled() {
		return prefs.getBoolean(KEY_SCROBBLING_ENABLE, false);
	}

	public void setNowPlayingEnabled(boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_NOWPLAYING_ENABLE, b);
		e.commit();
	}

	public boolean isNowPlayingEnabled() {
		return prefs.getBoolean(KEY_NOWPLAYING_ENABLE, false);
	}

	public void setAuthStatus(int i) {
		Editor e = prefs.edit();
		e.putInt(KEY_AUTH_STATUS, i);
		e.commit();
	}

	public int getAuthStatus() {
		return prefs.getInt(KEY_AUTH_STATUS, Status.AUTHSTATUS_NOAUTH);
	}

	public boolean isAuthenticated() {
		return getAuthStatus() == Status.AUTHSTATUS_OK;
	}

	public void setLastListenTime(long time) {
		Editor e = prefs.edit();
		e.putLong(KEY_LAST_LISTEN_TIME, time);
		e.commit();
	}

	public long getLastListenTime() {
		return prefs.getLong(KEY_LAST_LISTEN_TIME, 0);
	}
	
	
	// status stuff
	// scrobbling
	public void setLastScrobbleTime(long time) {
		Editor e = prefs.edit();
		e.putLong(KEY_STATUS_LAST_SCROBBLE_TIME, time);
		e.commit();
	}

	public long getLastScrobbleTime() {
		return prefs.getLong(KEY_STATUS_LAST_SCROBBLE_TIME, 0);
	}
	
	public void setLastScrobbleSuccess(boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_STATUS_LAST_SCROBBLE_SUCCESS, b);
		e.commit();
	}

	public boolean wasLastScrobbleSuccessful() {
		return prefs.getBoolean(KEY_STATUS_LAST_SCROBBLE_SUCCESS, false);
	}
	
	public void setLastScrobbleInfo(String s) {
		Editor e = prefs.edit();
		e.putString(KEY_STATUS_LAST_SCROBBLE_INFO, s);
		e.commit();
	}

	public String getLastScrobbleInfo() {
		return prefs.getString(KEY_STATUS_LAST_SCROBBLE_INFO, "");
	}
	
	// np-notifying
	public void setLastNPTime(long time) {
		Editor e = prefs.edit();
		e.putLong(KEY_STATUS_LAST_NP_TIME, time);
		e.commit();
	}

	public long getLastNPTime() {
		return prefs.getLong(KEY_STATUS_LAST_NP_TIME, 0);
	}
	
	public void setLastNPSuccess(boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_STATUS_LAST_NP_SUCCESS, b);
		e.commit();
	}

	public boolean wasLastNPSuccessful() {
		return prefs.getBoolean(KEY_STATUS_LAST_NP_SUCCESS, false);
	}
	
	public void setLastNPInfo(String s) {
		Editor e = prefs.edit();
		e.putString(KEY_STATUS_LAST_NP_INFO, s);
		e.commit();
	}

	public String getLastNPInfo() {
		return prefs.getString(KEY_STATUS_LAST_NP_INFO, "");
	}
	
	// number of scrobbles
	public void setNumberOfScrobbles(int i) {
		Editor e = prefs.edit();
		e.putInt(KEY_STATUS_NSCROBBLES, i);
		e.commit();
	}
	
	public int getNumberOfScrobbles() {
		return prefs.getInt(KEY_STATUS_NSCROBBLES, 0);
	}
	
	// number of np-notifications
	public void setNumberOfNPs(int i) {
		Editor e = prefs.edit();
		e.putInt(KEY_STATUS_NNPS, i);
		e.commit();
	}
	
	public int getNumberOfNPs() {
		return prefs.getInt(KEY_STATUS_NNPS, 0);
	}
}
