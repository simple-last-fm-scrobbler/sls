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

import com.adam.aslfms.service.ScrobblingService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * 
 * @author tgwizard
 *
 */
public class SettingsActivity extends PreferenceActivity {

	private static final String TAG = "SettingsActivity";

	private static final String SIGNUP_LINK = "https://www.last.fm/join";

	private final String KEY_USER_CREDENTIALS = "user_credentials";
	private final String KEY_EDIT_USER_CREDENTIALS = "edit_user_credentials";
	private final String KEY_CLEAR_USER_CREDENTIALS = "clear_user_credentials";
	private final String KEY_CREATE_USER = "create_user";
	private final String KEY_TOGGLE_SCROBBLING = "toggle_scrobbling";
	private final String KEY_TOGGLE_NOWPLAYING = "toggle_nowplaying";

	private final String KEY_TEST_NOWPLAYING = "test_nowplaying";
	private final String KEY_TEST_SCROBBLING = "test_scrobbling";

	private AppSettings settings;

	private Preference mUserCreds;
	private Preference mEditCreds;
	private Preference mClearCreds;
	private Preference mCreateUser;

	private CheckBoxPreference mScrobblePref;
	private CheckBoxPreference mNowplayPref;

	private Preference mTestNowplaying;
	private Preference mTestScrobbling;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_prefs);

		settings = new AppSettings(this);

		mUserCreds = findPreference(KEY_USER_CREDENTIALS);
		mEditCreds = findPreference(KEY_EDIT_USER_CREDENTIALS);
		mClearCreds = findPreference(KEY_CLEAR_USER_CREDENTIALS);
		mCreateUser = findPreference(KEY_CREATE_USER);
		mScrobblePref = (CheckBoxPreference) findPreference(KEY_TOGGLE_SCROBBLING);
		mNowplayPref = (CheckBoxPreference) findPreference(KEY_TOGGLE_NOWPLAYING);

		mTestNowplaying = findPreference(KEY_TEST_NOWPLAYING);
		mTestScrobbling = findPreference(KEY_TEST_SCROBBLING);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		if (preference == mScrobblePref) {
			toggleScrobbling(mScrobblePref.isChecked());
			return true;
		} else if (preference == mNowplayPref) {
			toggleNowplaying(mNowplayPref.isChecked());
			return true;
		} else if (preference == mClearCreds) {
			settings.clearSettings();
			update();
			Intent service = new Intent(ScrobblingService.ACTION_CLEARCREDS);
			startService(service);
			return true;
		} else if (preference == mCreateUser) {
			Log.d(TAG, "Clicked \"create user\"");
			Log.d(TAG, "Link: " + SIGNUP_LINK);
			Intent browser = new Intent(Intent.ACTION_VIEW, Uri
					.parse(SIGNUP_LINK));
			startActivity(browser);
			return true;
		} else if (preference == mTestNowplaying) {
			Intent service = new Intent(
					ScrobblingService.ACTION_PLAYSTATECHANGED);

			Track t = new Track("Chris Cornell", "Casino Royale",
					"You Know My Name", 180, AppTransaction.currentTimeUTC());
			AppTransaction.pushTrack(t);

			startService(service);
			return true;
		} else if (preference == mTestScrobbling) {
			Intent service = new Intent(
					ScrobblingService.ACTION_PLAYSTATECHANGED);

			Track t = new Track("Chris Cornell", "Casino Royale",
					"You Know My Name", 180, AppTransaction.currentTimeUTC());
			AppTransaction.pushTrack(t);

			service.putExtra("stopped", true);
			startService(service);
			return true;
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	protected void toggleScrobbling(boolean toggle) {
		settings.setScrobblingEnabled(toggle);
		// TODO: notify service perhaps
	}

	protected void toggleNowplaying(boolean toggle) {
		settings.setNowPlayingEnabled(toggle);
		// TODO: notify service perhaps
	}

	protected void update() {
		Log.d(TAG, "update, authstat: " + settings.getAuthStatus());
		mNowplayPref.setEnabled(false);
		mScrobblePref.setEnabled(false);
		if (settings.getAuthStatus() == Status.AUTHSTATUS_BADAUTH) {
			mUserCreds.setSummary(R.string.user_credentials_summary);
			mEditCreds.setSummary(R.string.auth_bad_auth);
		} else if (settings.getAuthStatus() == Status.AUTHSTATUS_FAILED) {
			mUserCreds.setSummary(R.string.user_credentials_summary);
			mEditCreds.setSummary(R.string.auth_internal_error);
		} else if (settings.getAuthStatus() == Status.AUTHSTATUS_RETRYLATER) {
			mUserCreds.setSummary(R.string.user_credentials_summary);
			mEditCreds.setSummary(R.string.auth_network_error);
		} else if (settings.getAuthStatus() == Status.AUTHSTATUS_OK) {
			mUserCreds.setSummary(getString(R.string.logged_in_as) + " "
					+ settings.getUsername());
			mEditCreds.setSummary(getString(R.string.logged_in_as) + " "
					+ settings.getUsername());
			mNowplayPref.setEnabled(true);
			mScrobblePref.setEnabled(true);
		} else if (settings.getAuthStatus() == Status.AUTHSTATUS_NOAUTH) {
			mUserCreds.setSummary(R.string.user_credentials_summary);
			mEditCreds.setSummary(R.string.user_credentials_summary);
		} else if (settings.getAuthStatus() == Status.AUTHSTATUS_UPDATING) {
			mUserCreds.setSummary(R.string.user_credentials_summary);
			mEditCreds.setSummary(R.string.auth_updating);
		}

		mScrobblePref.setChecked(settings.isScrobblingEnabled());
		mNowplayPref.setChecked(settings.isNowPlayingEnabled());

		boolean hasCreds = settings.getAuthStatus() != Status.AUTHSTATUS_NOAUTH
				|| settings.getUsername().length() != 0
				|| settings.getPassword().length() != 0
				|| settings.getPwdMd5().length() != 0;

		mClearCreds.setEnabled(hasCreds);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(onAuth);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(onAuth, onAuthIntents);
		update();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public final IntentFilter onAuthIntents = new IntentFilter(
			ScrobblingService.BROADCAST_ONAUTHCHANGED);
	private BroadcastReceiver onAuth = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			SettingsActivity.this.update();
		}
	};

}
