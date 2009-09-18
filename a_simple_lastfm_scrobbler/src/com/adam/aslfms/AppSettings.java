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

import com.adam.aslfms.receiver.MusicApp;
import com.adam.aslfms.service.NetApp;

/**
 * 
 * @author tgwizard
 * 
 */
public class AppSettings {

	private static final String SETTINGS_NAME = "settings";

	private static final String KEY_USERNAME = "username";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_PWDMD5 = "pwdMd5";

	private static final String KEY_SCROBBLING_ENABLE = "enable_scrobbling";
	private static final String KEY_NOWPLAYING_ENABLE = "enable_nowplaying";

	private static final String KEY_MUSIC_APP_ENABLE_PREFIX = "appenable_";

	private static final String KEY_AUTH_STATUS = "authstatus";

	private static final String KEY_LAST_LISTEN_TIME = "last_listen_time";

	private static final String KEY_WHATSNEW_VIEWED_VERSION = "whatsnew_viewed_version";

	// status stuff
	/*
	 * private static final String KEY_STATUS_LAST_SCROBBLE_TIME =
	 * "status_last_scrobble_time"; private static final String
	 * KEY_STATUS_LAST_SCROBBLE_SUCCESS = "status_last_scrobble_success";
	 * private static final String KEY_STATUS_LAST_SCROBBLE_INFO =
	 * "status_last_scrobble_info";
	 * 
	 * private static final String KEY_STATUS_LAST_NP_TIME =
	 * "status_last_np_time"; private static final String
	 * KEY_STATUS_LAST_NP_SUCCESS = "status_last_np_success"; private static
	 * final String KEY_STATUS_LAST_NP_INFO = "status_last_np_info";
	 */

	// private static final String KEY_STATUS_NSCROBBLES = "status_nscrobbles";
	// private static final String KEY_STATUS_NNPS = "status_nnps";

	public enum SubmissionType {
		SCROBBLE("status_last_scrobble", "status_nscrobbles"), NP(
				"status_last_np", "status_nnps");

		private final String lastPrefix;
		private final String numberOfPrefix;

		SubmissionType(String lastPrefix, String numberOfPrefix) {
			this.lastPrefix = lastPrefix;
			this.numberOfPrefix = numberOfPrefix;
		}

		public String getLastPrefix() {
			return lastPrefix;
		}

		public String getNumberOfPrefix() {
			return numberOfPrefix;
		}
	}

	private final SharedPreferences prefs;

	public AppSettings(Context ctx) {
		super();
		prefs = ctx.getSharedPreferences(SETTINGS_NAME, 0);
	}

	public void clearCreds(NetApp napp) {
		setUsername(napp, "");
		setPassword(napp, "");
		setPwdMd5(napp, "");
		setAuthStatus(napp, Status.AUTHSTATUS_NOAUTH);
	}

	public boolean hasCreds(NetApp napp) {
		return getAuthStatus(napp) != Status.AUTHSTATUS_NOAUTH
				|| getUsername(napp).length() != 0
				|| getPassword(napp).length() != 0
				|| getPwdMd5(napp).length() != 0;
	}
	
	public boolean hasAnyCreds() {
		for (NetApp napp : NetApp.values())
			if (hasCreds(napp))
				return true;
		return false;
	}

	public void setUsername(NetApp napp, String s) {
		Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + KEY_USERNAME, s);
		e.commit();
	}

	public String getUsername(NetApp napp) {
		return prefs.getString(napp.getSettingsPrefix() + KEY_USERNAME, "");
	}

	public void setPassword(NetApp napp, String s) {
		Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + KEY_PASSWORD, s);
		e.commit();
	}

	public String getPassword(NetApp napp) {
		return prefs.getString(napp.getSettingsPrefix() + KEY_PASSWORD, "");
	}

	public void setPwdMd5(NetApp napp, String s) {
		Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + KEY_PWDMD5, s);
		e.commit();
	}

	public String getPwdMd5(NetApp napp) {
		return prefs.getString(napp.getSettingsPrefix() + KEY_PWDMD5, "");
	}

	public boolean isSubmissionsEnabled(SubmissionType stype) {
		if (stype == SubmissionType.SCROBBLE) {
			return isScrobblingEnabled();
		} else {
			return isNowPlayingEnabled();
		}
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

	public void setMusicAppEnabled(MusicApp app, boolean enabled) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_MUSIC_APP_ENABLE_PREFIX + app.toString(), enabled);
		e.commit();
	}

	public boolean isMusicAppEnabled(MusicApp app) {
		return prefs.getBoolean(KEY_MUSIC_APP_ENABLE_PREFIX + app.toString(),
				true);
	}

	public void setAuthStatus(NetApp napp, int i) {
		Editor e = prefs.edit();
		e.putInt(napp.getSettingsPrefix() + KEY_AUTH_STATUS, i);
		e.commit();
	}

	public int getAuthStatus(NetApp napp) {
		return prefs.getInt(napp.getSettingsPrefix() + KEY_AUTH_STATUS,
				Status.AUTHSTATUS_NOAUTH);
	}

	public boolean isAnyAuthenticated() {
		for (NetApp napp : NetApp.values())
			if (isAuthenticated(napp))
				return true;
		return false;
	}

	public boolean isAuthenticated(NetApp napp) {
		return getAuthStatus(napp) == Status.AUTHSTATUS_OK;
	}

	public void setLastListenTime(long time) {
		Editor e = prefs.edit();
		e.putLong(KEY_LAST_LISTEN_TIME, time);
		e.commit();
	}

	public long getLastListenTime() {
		return prefs.getLong(KEY_LAST_LISTEN_TIME, 0);
	}

	public void setWhatsNewViewedVersion(int i) {
		Editor e = prefs.edit();
		e.putInt(KEY_WHATSNEW_VIEWED_VERSION, i);
		e.commit();
	}

	public int getWhatsNewViewedVersion() {
		return prefs.getInt(KEY_WHATSNEW_VIEWED_VERSION, 0);
	}

	// status stuff
	// submission notifying
	public void setLastSubmissionTime(NetApp napp, SubmissionType stype,
			long time) {
		Editor e = prefs.edit();
		e.putLong(napp.getSettingsPrefix() + stype.getLastPrefix() + "_time",
				time);
		e.commit();
	}

	public long getLastSubmissionTime(NetApp napp, SubmissionType stype) {
		return prefs.getLong(napp.getSettingsPrefix() + stype.getLastPrefix()
				+ "_time", -1);
	}

	public void setLastSubmissionSuccess(NetApp napp, SubmissionType stype,
			boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(napp.getSettingsPrefix() + stype.getLastPrefix()
				+ "_success", b);
		e.commit();
	}

	public boolean wasLastSubmissionSuccessful(NetApp napp, SubmissionType stype) {
		return prefs.getBoolean(napp.getSettingsPrefix()
				+ stype.getLastPrefix() + "_success", true);
	}

	public void setLastSubmissionInfo(NetApp napp, SubmissionType stype,
			String s) {
		Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + stype.getLastPrefix() + "_info",
				s);
		e.commit();
	}

	public String getLastSubmissionInfo(NetApp napp, SubmissionType stype) {
		return prefs.getString(napp.getSettingsPrefix() + stype.getLastPrefix()
				+ "_info", "");
	}

	// number of submissions (scrobbles/nps)
	public void setNumberOfSubmissions(NetApp napp, SubmissionType stype, int i) {
		Editor e = prefs.edit();
		e.putInt(napp.getSettingsPrefix() + stype.getNumberOfPrefix(), i);
		e.commit();
	}

	public int getNumberOfSubmissions(NetApp napp, SubmissionType stype) {
		return prefs.getInt(napp.getSettingsPrefix()
				+ stype.getNumberOfPrefix(), 0);
	}
}
