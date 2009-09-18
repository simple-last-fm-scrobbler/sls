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

import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;

import com.adam.aslfms.receiver.MusicApp;
import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.Util;

/**
 * This is the activity that is shown when the user launches
 * "A Simple Last.fm Scrobbler" from the app screen. It allows the user to set
 * preferences regarding his/her scrobbling, whether to enable now playing
 * notifications or not. It also allows the user to enter and clear user
 * credentials.
 * 
 * @author tgwizard
 * 
 */
public class SettingsActivity extends PreferenceActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "SettingsActivity";

	// keys to Preference objects
	private static final String KEY_USER_CREDENTIALS = "user_credentials";
	private static final String KEY_USER_CREDENTIALS_LIST = "supported_netapps_list";
	private static final String KEY_CLEAR_ALL_CREDS = "clear_all_user_credentials";

	private static final String KEY_TOGGLE_SCROBBLING = "toggle_scrobbling";
	private static final String KEY_TOGGLE_NOWPLAYING = "toggle_nowplaying";

	private static final String KEY_SHOW_SUPPORTED_MUSICAPPS_LIST = "show_supported_musicapps_list";
	private static final String KEY_SUPPORTED_MUSICAPPS_LIST = "supported_musicapps_list";

	private static final String KEY_STATUS_SHOW = "status_show";

	private static final int MENU_ABOUT_ID = 0;

	private AppSettings settings;

	private Preference mUserCreds;
	private PreferenceCategory mUserCredsList;
	private HashMap<Preference, NetApp> mUserCredsPrefToAppMap;
	private HashMap<NetApp, Preference> mUserCredsAppToPrefMap;
	private Preference mClearAllCreds;

	private CheckBoxPreference mScrobblePref;
	private CheckBoxPreference mNowplayPref;

	private Preference mShowSupportedMusicAppsList;
	private PreferenceCategory mSupportedMusicAppsList;
	private HashMap<CheckBoxPreference, MusicApp> mSupportedMusicAppsMap;

	private Preference mStatusShow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_prefs);

		settings = new AppSettings(this);

		mUserCreds = findPreference(KEY_USER_CREDENTIALS);
		mUserCredsList = (PreferenceCategory) findPreference(KEY_USER_CREDENTIALS_LIST);
		mUserCredsPrefToAppMap = new HashMap<Preference, NetApp>();
		mUserCredsAppToPrefMap = new HashMap<NetApp, Preference>();
		mClearAllCreds = findPreference(KEY_CLEAR_ALL_CREDS);

		mScrobblePref = (CheckBoxPreference) findPreference(KEY_TOGGLE_SCROBBLING);
		mNowplayPref = (CheckBoxPreference) findPreference(KEY_TOGGLE_NOWPLAYING);

		mShowSupportedMusicAppsList = findPreference(KEY_SHOW_SUPPORTED_MUSICAPPS_LIST);
		mSupportedMusicAppsList = (PreferenceCategory) findPreference(KEY_SUPPORTED_MUSICAPPS_LIST);
		mSupportedMusicAppsMap = new HashMap<CheckBoxPreference, MusicApp>();

		mStatusShow = findPreference(KEY_STATUS_SHOW);

		int v = Util.getAppVersionCode(this, getPackageName());
		if (settings.getWhatsNewViewedVersion() < v) {
			new WhatsNewDialog(this).show();
			settings.setWhatsNewViewedVersion(v);
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {

		if (pref == mScrobblePref) {
			toggleScrobbling(mScrobblePref.isChecked());
			return true;
		} else if (pref == mNowplayPref) {
			toggleNowplaying(mNowplayPref.isChecked());
			return true;
		} else if (pref == mStatusShow) {
			Intent st = new Intent(this, StatusActivity.class);
			startActivity(st);
			return true;
		} else if (pref == mShowSupportedMusicAppsList) {
			loadSupportedMusicAppsList();
			return true;
		} else if (pref == mUserCreds) {
			loadUserCredsList();
			return true;
		} else if (pref == mClearAllCreds) {
			Intent service = new Intent(ScrobblingService.ACTION_CLEARCREDS);
			service.putExtra("clearall", true);
			startService(service);
			return true;
		} else {
			// we clicked an "enable music app" checkbox
			MusicApp app = mSupportedMusicAppsMap.get(pref);
			if (app != null) {
				CheckBoxPreference cbp = (CheckBoxPreference) pref;
				boolean checked = cbp.isChecked();
				settings.setMusicAppEnabled(app, checked);
				setSMASummary(pref, app);
				return true;
			} else { // or we clicked an "user creds" preference
				NetApp napp = mUserCredsPrefToAppMap.get(pref);
				if (napp != null) {
					Intent uca = new Intent(this, UserCredActivity.class);
					uca.putExtra("netapp", napp.getIntentExtraValue());
					startActivity(uca);
					return true;
				}
			}
		}

		return super.onPreferenceTreeClick(prefScreen, pref);
	}

	private void toggleScrobbling(boolean toggle) {
		settings.setScrobblingEnabled(toggle);
	}

	private void toggleNowplaying(boolean toggle) {
		settings.setNowPlayingEnabled(toggle);
	}

	/**
	 * Updates what is shown to the user - preference titles and summaries, and
	 * whether stuff is enabled or checked, etc.
	 */
	private void update() {
		mScrobblePref.setChecked(settings.isScrobblingEnabled());
		mNowplayPref.setChecked(settings.isNowPlayingEnabled());
		
		mClearAllCreds.setEnabled(settings.hasAnyCreds());

		updateUserCredsList();
	}

	private void loadSupportedMusicAppsList() {
		clearSupportedMusicAppsList();
		MusicApp[] apps = MusicApp.values();
		for (MusicApp app : apps) {
			boolean enabled = settings.isMusicAppEnabled(app);
			// Log.d(TAG, "App: " + app.getName() + " : " + enabled);
			CheckBoxPreference appPref = new CheckBoxPreference(this, null);
			appPref.setTitle(app.getName());
			appPref.setPersistent(false); // TODO: what does this mean?
			appPref.setChecked(enabled);
			setSMASummary(appPref, app);
			mSupportedMusicAppsList.addPreference(appPref);
			mSupportedMusicAppsMap.put(appPref, app);
		}
	}

	private void clearSupportedMusicAppsList() {
		for (CheckBoxPreference p : mSupportedMusicAppsMap.keySet()) {
			mSupportedMusicAppsList.removePreference(p);
		}
		mSupportedMusicAppsMap.clear();
	}

	private void setSMASummary(Preference pref, MusicApp app) {
		boolean enabled = settings.isMusicAppEnabled(app);
		boolean installed = Util.checkForInstalledApp(this, app.getPackage());
		if (!enabled) {
			pref.setSummary(R.string.app_disabled);
		} else if (!installed) {
			pref.setSummary(R.string.not_installed);
		} else {
			pref.setSummary(null);
		}
	}

	private void loadUserCredsList() {
		clearUserCredsList();
		NetApp[] napps = NetApp.values();
		for (NetApp napp : napps) {
			Preference pref = new Preference(this, null);
			pref.setTitle(getString(R.string.log_in_to) + " " + napp.getName());
			mUserCredsPrefToAppMap.put(pref, napp);
			mUserCredsAppToPrefMap.put(napp, pref);
			mUserCredsList.addPreference(pref);

			
		}
		updateUserCredsList();
	}

	private void clearUserCredsList() {
		mUserCredsList.removeAll();
		mUserCredsPrefToAppMap.clear();
		mUserCredsAppToPrefMap.clear();
	}

	private void updateUserCredsList() {
		if (mUserCredsPrefToAppMap.isEmpty())
			return;

		for (NetApp napp : NetApp.values()) {
			mUserCredsAppToPrefMap.get(napp).setSummary(
					napp.getStatusSummary(this, settings));
		}

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

		IntentFilter ifs = new IntentFilter();
		ifs.addAction(ScrobblingService.BROADCAST_ONAUTHCHANGED);

		registerReceiver(onAuth, ifs);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ABOUT_ID, 0, R.string.about);
		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT_ID:
			showAboutDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showAboutDialog() {
		new AboutDialog(this).show();
	}

	private BroadcastReceiver onAuth = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			SettingsActivity.this.update();
		}
	};

}
