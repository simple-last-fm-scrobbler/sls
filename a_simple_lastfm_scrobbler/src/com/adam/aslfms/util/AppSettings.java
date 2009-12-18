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

package com.adam.aslfms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.adam.aslfms.receiver.MusicApp;
import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.AppSettingsEnums.AdvancedOptions;
import com.adam.aslfms.util.AppSettingsEnums.AdvancedOptionsWhen;
import com.adam.aslfms.util.AppSettingsEnums.PowerOptions;
import com.adam.aslfms.util.AppSettingsEnums.SubmissionType;

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

	private static final String KEY_WHATSNEW_VIEWED_VERSION = "whatsnew_viewed_version";

	private static final String KEY_SCROBBLE_POINT = "scrobble_point";
	private static final String KEY_ADVANCED_OPTIONS = "advanced_options_type";
	private static final String KEY_ADVANCED_OPTIONS_WHEN = "advanced_options_when";
	private static final String KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE = "scrobbling_options_also_on_complete";

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

	public void setWhatsNewViewedVersion(int i) {
		Editor e = prefs.edit();
		e.putInt(KEY_WHATSNEW_VIEWED_VERSION, i);
		e.commit();
	}

	public int getWhatsNewViewedVersion() {
		return prefs.getInt(KEY_WHATSNEW_VIEWED_VERSION, 0);
	}

	// status stuff

	public void clearSubmissionStats(NetApp napp) {
		setLastSubmissionTime(napp, SubmissionType.SCROBBLE, -1);
		setLastSubmissionTime(napp, SubmissionType.NP, -1);

		setLastSubmissionSuccess(napp, SubmissionType.SCROBBLE, true);
		setLastSubmissionSuccess(napp, SubmissionType.NP, true);

		setLastSubmissionInfo(napp, SubmissionType.SCROBBLE, "");
		setLastSubmissionInfo(napp, SubmissionType.NP, "");

		setNumberOfSubmissions(napp, SubmissionType.SCROBBLE, 0);
		setNumberOfSubmissions(napp, SubmissionType.NP, 0);
	}

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

	// scrobbling options
	public boolean isSubmissionsEnabled(SubmissionType stype, PowerOptions pow) {
		if (stype == SubmissionType.SCROBBLE) {
			return isScrobblingEnabled(pow);
		} else {
			return isNowPlayingEnabled(pow);
		}
	}

	public void setScrobblingEnabled(PowerOptions pow, boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_SCROBBLING_ENABLE + pow.getSettingsPath(), b);
		e.commit();
	}

	public boolean isScrobblingEnabled(PowerOptions pow) {
		return prefs.getBoolean(KEY_SCROBBLING_ENABLE + pow.getSettingsPath(), getAdvancedOptions(pow).isScrobblingEnabled());
	}

	public void setNowPlayingEnabled(PowerOptions pow, boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_NOWPLAYING_ENABLE + pow.getSettingsPath(), b);
		e.commit();
	}

	public boolean isNowPlayingEnabled(PowerOptions pow) {
		return prefs.getBoolean(KEY_NOWPLAYING_ENABLE + pow.getSettingsPath(), getAdvancedOptions(pow).isNpEnabled());
	}
	
	public void setScrobblePoint(int sp) {
		Editor e = prefs.edit();
		e.putInt(KEY_SCROBBLE_POINT, sp);
		e.commit();
	}
	
	public int getScrobblePoint() {
		return prefs.getInt(KEY_SCROBBLE_POINT, 50);
	}

	public void setAdvancedOptions(PowerOptions pow, AdvancedOptions ao) {
		Editor e = prefs.edit();
		e.putString(KEY_ADVANCED_OPTIONS + pow.getSettingsPath(), ao
				.getSettingsVal());
		e.commit();
		if (ao != AdvancedOptions.CUSTOM) {
			setScrobblingEnabled(pow, ao.isScrobblingEnabled());
			setNowPlayingEnabled(pow, ao.isNpEnabled());
			setAdvancedOptionsWhen(pow, ao.getWhen());
			setAdvancedOptionsAlsoOnComplete(pow, ao.getAlsoOnComplete());
		}
	}

	public AdvancedOptions getAdvancedOptions(PowerOptions pow) {
		String s = prefs.getString(
				KEY_ADVANCED_OPTIONS + pow.getSettingsPath(), null);
		if (s == null) {
			return AdvancedOptions.STANDARD;
		} else {
			return AdvancedOptions.fromSettingsVal(s);
		}
	}

	public void setAdvancedOptionsWhen(PowerOptions pow, AdvancedOptionsWhen aow) {
		Editor e = prefs.edit();
		e.putString(KEY_ADVANCED_OPTIONS_WHEN + pow.getSettingsPath(), aow
				.getSettingsVal());
		e.commit();
	}

	public AdvancedOptionsWhen getAdvancedOptionsWhen(PowerOptions pow) {
		String s = prefs.getString(KEY_ADVANCED_OPTIONS_WHEN
				+ pow.getSettingsPath(), null);
		if (s == null) {
			return getAdvancedOptions(pow).getWhen();
		} else {
			return AdvancedOptionsWhen.fromSettingsVal(s);
		}
	}

	public void setAdvancedOptionsAlsoOnComplete(PowerOptions pow, boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE
				+ pow.getSettingsPath(), b);
		e.commit();
	}

	public boolean getAdvancedOptionsAlsoOnComplete(PowerOptions pow) {
		return prefs.getBoolean(KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE
				+ pow.getSettingsPath(), getAdvancedOptions(pow).getAlsoOnComplete());
	}
}
