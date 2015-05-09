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

package com.adam.aslfms.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.adam.aslfms.UserCredActivity;
import com.adam.aslfms.service.Handshaker;
import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.enums.AdvancedOptions;
import com.adam.aslfms.util.enums.AdvancedOptionsWhen;
import com.adam.aslfms.util.enums.NetworkOptions;
import com.adam.aslfms.util.enums.PowerOptions;
import com.adam.aslfms.util.enums.SortField;
import com.adam.aslfms.util.enums.SubmissionType;

/**
 * 
 * @author tgwizard
 * 
 */
public class AppSettings {

	private static final String TAG = "SLSAppSettings";

	public static final String ACTION_NETWORK_OPTIONS_CHANGED = "com.adam.aslfms.service.bcast.onnetoptions";

	private static final String SETTINGS_NAME = "settings";

	private static final String KEY_USERNAME = "username";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_PWDMD5 = "pwdMd5";

	private static final String KEY_SCROBBLING_ENABLE = "enable_scrobbling";
	private static final String KEY_NOWPLAYING_ENABLE = "enable_nowplaying";

	private static final String KEY_AUTH_STATUS = "authstatus";

	private static final String KEY_WHATSNEW_VIEWED_VERSION = "whatsnew_viewed_version";

	private static final String KEY_VIEW_CACHE_SORTFIELD = "view_cache_sortfield";

	private static final String KEY_SCROBBLE_POINT = "scrobble_point";
	private static final String KEY_ADVANCED_OPTIONS = "advanced_options_type";
	private static final String KEY_ADVANCED_OPTIONS_WHEN = "advanced_options_when";
	private static final String KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE = "scrobbling_options_also_on_complete";
	private static final String KEY_ADVANCED_OPTIONS_NETWORK = "advanced_options_network";
	private static final String KEY_ADVANCED_OPTIONS_ROAMING = "advanced_options_roaming";

	// Widget stuff
	private static final String KEY_WIDGET_ALSO_DISABLE_NP = "widget_also_disable_np";

	private final Context mCtx;
	private final SharedPreferences prefs;

	public AppSettings(Context ctx) {
		super();
		mCtx = ctx;
		prefs = ctx.getSharedPreferences(SETTINGS_NAME, 0);
	}

	public void clearCreds(NetApp napp) {
		setUsername(napp, "");
		setPassword(napp, "");
		setPwdMd5(napp, "");
		setAuthStatus(napp, AuthStatus.AUTHSTATUS_NOAUTH);
	}

	public boolean hasCreds(NetApp napp) {
		return getAuthStatus(napp) != AuthStatus.AUTHSTATUS_NOAUTH
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

	/**
	 * Saves the password in plain-text for a user account at the {@link NetApp}
	 * {@code napp}. This is only used as an intermediary step, and is removed
	 * when the authentication is successful in {@link Handshaker#run()}
	 * 
	 * @see #setPwdMd5(NetApp)
	 * 
	 * @param napp
	 *            the {@code NetApp} for which a user account has this password
	 * @param s
	 *            the password, in plain-text
	 */
	public void setPassword(NetApp napp, String s) {
		Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + KEY_PASSWORD, s);
		e.commit();
	}

	/**
	 * Returns the password in plain-text for a user account at the
	 * {@link NetApp} {@code napp}. This is only used as an intermediary step,
	 * and is removed when the authentication is successful in
	 * {@link Handshaker#run()}
	 * 
	 * @see #getPwdMd5(NetApp)
	 * 
	 * @param napp
	 *            the {@code NetApp} for which a user account has this password
	 * @return the password, in plain-text
	 */
	public String getPassword(NetApp napp) {
		return prefs.getString(napp.getSettingsPrefix() + KEY_PASSWORD, "");
	}

	/**
	 * Saves an MD5 hash of the password for a user account at the
	 * {@link NetApp} {@code napp}. This is stored in the settings file until it
	 * is cleared by the user through {@link UserCredActivity}. It is "safe" to
	 * store a password this way, as it would take a very, very long time to
	 * extract the original password from the MD5 hash.
	 * 
	 * @param napp
	 *            the {@code NetApp} for which a user account has this password
	 * @param s
	 *            the password, as an MD5 hash
	 */
	public void setPwdMd5(NetApp napp, String s) {
		Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + KEY_PWDMD5, s);
		e.commit();
	}

	/**
	 * Returns the password as an MD5 hash for a user account at the
	 * {@link NetApp} {@code napp}.
	 * 
	 * @see #getPwdMd5(NetApp)
	 * 
	 * @param napp
	 *            the {@code NetApp} for which a user account has this password
	 * @return the password, as an MD5 hash
	 */
	public String getPwdMd5(NetApp napp) {
		return prefs.getString(napp.getSettingsPrefix() + KEY_PWDMD5, "");
	}

	public void setAuthStatus(NetApp napp, int i) {
		Editor e = prefs.edit();
		e.putInt(napp.getSettingsPrefix() + KEY_AUTH_STATUS, i);
		e.commit();
	}

	public int getAuthStatus(NetApp napp) {
		return prefs.getInt(napp.getSettingsPrefix() + KEY_AUTH_STATUS,
				AuthStatus.AUTHSTATUS_NOAUTH);
	}

	public boolean isAnyAuthenticated() {
		for (NetApp napp : NetApp.values())
			if (isAuthenticated(napp))
				return true;
		return false;
	}

	public boolean isAuthenticated(NetApp napp) {
		return getAuthStatus(napp) == AuthStatus.AUTHSTATUS_OK;
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
		for (SubmissionType st : SubmissionType.values()) {
			setLastSubmissionTime(napp, st, -1);
			setLastSubmissionSuccess(napp, st, true);
			setLastSubmissionInfo(napp, st, "");
			setNumberOfSubmissions(napp, st, 0);
		}
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

	// view cache options
	public void setCacheSortField(SortField sf) {
		Editor e = prefs.edit();
		e.putString(KEY_VIEW_CACHE_SORTFIELD, sf.name());
		e.commit();
	}

	public SortField getCacheSortField() {
		String s = prefs.getString(KEY_VIEW_CACHE_SORTFIELD,
				SortField.WHEN_DESC.name());

		SortField sf = SortField.WHEN_DESC;
		try {
			sf = SortField.valueOf(s);
		} catch (Exception e) {
			Log.e(TAG, "Got exception when trying to convert to sort-order: "
					+ s);
			Log.e(TAG, e.getMessage());
		}

		return sf;
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
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		return prefs.getBoolean(KEY_SCROBBLING_ENABLE + pow.getSettingsPath(),
				getAdvancedOptions(pow).isScrobblingEnabled());
	}

	public void setNowPlayingEnabled(PowerOptions pow, boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_NOWPLAYING_ENABLE + pow.getSettingsPath(), b);
		e.commit();
	}

	public boolean isNowPlayingEnabled(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		return prefs.getBoolean(KEY_NOWPLAYING_ENABLE + pow.getSettingsPath(),
				getAdvancedOptions(pow).isNpEnabled());
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
		boolean found = false;
		for (AdvancedOptions aof : pow.getApplicableOptions()) {
			if (aof == ao) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new IllegalArgumentException(
					"Bad option for this power setting: " + ao + ", " + pow);
		}

		Editor e = prefs.edit();
		e.putString(KEY_ADVANCED_OPTIONS + pow.getSettingsPath(), ao
				.getSettingsVal());
		e.commit();
		if (ao != AdvancedOptions.CUSTOM
				&& ao != AdvancedOptions.SAME_AS_BATTERY) {
			setScrobblingEnabled(pow, ao.isScrobblingEnabled());
			setNowPlayingEnabled(pow, ao.isNpEnabled());
			setAdvancedOptionsWhen(pow, ao.getWhen());
			setAdvancedOptionsAlsoOnComplete(pow, ao.getAlsoOnComplete());
			setNetworkOptions(pow, ao.getNetworkOptions());
			setSubmitOnRoaming(pow, ao.getRoaming());
		}
	}

	/**
	 * Wow, I apologize for this mess. I'll clean it up when I figure out how.
	 * 
	 * @param pow
	 * @return
	 */
	public AdvancedOptions getAdvancedOptions_raw(PowerOptions pow) {
		String s = prefs.getString(
				KEY_ADVANCED_OPTIONS + pow.getSettingsPath(), null);
		if (s == null) {
			if (pow == PowerOptions.PLUGGED_IN)
				return AdvancedOptions.SAME_AS_BATTERY;
			return AdvancedOptions.STANDARD;
		} else {
			return AdvancedOptions.fromSettingsVal(s);
		}
	}

	public AdvancedOptions getAdvancedOptions(PowerOptions pow) {
		AdvancedOptions ao = getAdvancedOptions_raw(pow);
		// if we have said that we don't want custom settings for plugged in
		if (pow == PowerOptions.PLUGGED_IN
				&& ao == AdvancedOptions.SAME_AS_BATTERY) {
			// return the advanced settings used for battery
			return getAdvancedOptions_raw(PowerOptions.BATTERY);
		}
		return ao;
	}

	public void setAdvancedOptionsWhen(PowerOptions pow, AdvancedOptionsWhen aow) {
		Editor e = prefs.edit();
		e.putString(KEY_ADVANCED_OPTIONS_WHEN + pow.getSettingsPath(), aow
				.getSettingsVal());
		e.commit();
	}

	public AdvancedOptionsWhen getAdvancedOptionsWhen(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

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
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		return prefs.getBoolean(KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE
				+ pow.getSettingsPath(), getAdvancedOptions(pow)
				.getAlsoOnComplete());
	}

	public void setNetworkOptions(PowerOptions pow, NetworkOptions no) {
		Editor e = prefs.edit();
		e.putString(KEY_ADVANCED_OPTIONS_NETWORK + pow.getSettingsPath(), no
				.getSettingsVal());
		e.commit();

		mCtx.sendBroadcast(new Intent(ACTION_NETWORK_OPTIONS_CHANGED));
	}

	public NetworkOptions getNetworkOptions(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		String s = prefs.getString(KEY_ADVANCED_OPTIONS_NETWORK
				+ pow.getSettingsPath(), null);
		if (s == null) {
			return NetworkOptions.ANY;
		} else {
			return NetworkOptions.fromSettingsVal(s);
		}
	}

	public void setSubmitOnRoaming(PowerOptions pow, boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_ADVANCED_OPTIONS_ROAMING + pow.getSettingsPath(), b);
		e.commit();

		mCtx.sendBroadcast(new Intent(ACTION_NETWORK_OPTIONS_CHANGED));
	}

	public boolean getSubmitOnRoaming(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		return prefs.getBoolean(KEY_ADVANCED_OPTIONS_ROAMING
				+ pow.getSettingsPath(), getAdvancedOptions(pow).getRoaming());
	}

	// Widget stuff
	public void setWidgetAlsoDisableNP(boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_WIDGET_ALSO_DISABLE_NP, b);
		e.commit();
	}

	public boolean getWidgetAlsoDisableNP() {
		return prefs.getBoolean(KEY_WIDGET_ALSO_DISABLE_NP, false);
	}
}
