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
import android.database.SQLException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.adam.aslfms.AppSettingsEnums.AdvancedOptions;
import com.adam.aslfms.AppSettingsEnums.AdvancedOptionsWhen;
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
 * FIXME: this class is way too big
 * 
 * @author tgwizard
 * 
 */
public class SettingsActivity extends PreferenceActivity {

	private static final String TAG = "SettingsActivity";

	// keys to Preference objects
	private static final String KEY_USER_CREDENTIALS = "user_credentials";
	private static final String KEY_USER_CREDENTIALS_LIST = "supported_netapps_list";
	private static final String KEY_CLEAR_ALL_CREDS = "clear_all_user_credentials";

	private static final String KEY_TOGGLE_SCROBBLING = "toggle_scrobbling";
	private static final String KEY_TOGGLE_NOWPLAYING = "toggle_nowplaying";

	private static final String KEY_SHOW_SUPPORTED_MUSICAPPS_LIST = "show_supported_musicapps_list";
	private static final String KEY_SUPPORTED_MUSICAPPS_LIST = "supported_musicapps_list";

	private static final String KEY_ADVANCED_OPTIONS_CHOOSER = "advanced_options_chooser";
	private static final String KEY_ADVANCED_OPTIONS_WHEN = "advanced_options_when";
	private static final String KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE = "advanced_options_also_on_complete";

	private static final String KEY_SCROBBLE_ALL_NOW = "scrobble_all_now";

	private static final String KEY_STATUS_SHOW = "status_show";

	private static final int MENU_ABOUT_ID = 0;

	private AppSettings settings;
	
	private ScrobblesDatabase mDb;

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

	private ListPreference mAOptionsChooser;
	private ListPreference mAOptionsWhen;
	private CheckBoxPreference mAOptionsAlsoOnComplete;

	private Preference mScrobbleAllNow;

	private Preference mShowStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_prefs);

		settings = new AppSettings(this);
		
		mDb = new ScrobblesDatabase(this);
		try {
			mDb.open();
		} catch (SQLException e) {
			Log.e(TAG, "Cannot open database!");
			Log.e(TAG, e.getMessage());
			mDb = null;
		}

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

		mAOptionsChooser = (ListPreference) findPreference(KEY_ADVANCED_OPTIONS_CHOOSER);
		mAOptionsWhen = (ListPreference) findPreference(KEY_ADVANCED_OPTIONS_WHEN);
		mAOptionsAlsoOnComplete = (CheckBoxPreference) findPreference(KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE);

		initAdvancedOptions();

		mScrobbleAllNow = findPreference(KEY_SCROBBLE_ALL_NOW);

		mShowStatus = findPreference(KEY_STATUS_SHOW);

		int v = Util.getAppVersionCode(this, getPackageName());
		if (settings.getWhatsNewViewedVersion() < v) {
			new WhatsNewDialog(this).show();
			settings.setWhatsNewViewedVersion(v);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDb.close();
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
		ifs.addAction(ScrobblingService.BROADCAST_ONSTATUSCHANGED);
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

	private void initAdvancedOptions() {
		// set the entries for mOptionsChooser
		AdvancedOptions[] scrobOpts = AdvancedOptions.values();
		CharSequence[] vals = new CharSequence[scrobOpts.length];
		for (int i = 0; i < scrobOpts.length; i++)
			vals[i] = scrobOpts[i].getName(this);
		mAOptionsChooser.setEntries(vals);

		// set the values for mOptionsChooser
		vals = new CharSequence[scrobOpts.length];
		for (int i = 0; i < scrobOpts.length; i++)
			vals[i] = scrobOpts[i].toString();
		mAOptionsChooser.setEntryValues(vals);

		AdvancedOptionsWhen[] scrobOptsWhen = AdvancedOptionsWhen.values();
		vals = new CharSequence[scrobOptsWhen.length];
		for (int i = 0; i < scrobOptsWhen.length; i++)
			vals[i] = scrobOptsWhen[i].getName(this);
		mAOptionsWhen.setEntries(vals);

		// set the values for mOptionsChooser
		vals = new CharSequence[scrobOptsWhen.length];
		for (int i = 0; i < scrobOptsWhen.length; i++)
			vals[i] = scrobOptsWhen[i].toString();
		mAOptionsWhen.setEntryValues(vals);

		mAOptionsChooser.setOnPreferenceChangeListener(mOnOptionsChange);
		mAOptionsWhen.setOnPreferenceChangeListener(mOnOptionsWhenChange);
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
		} else if (pref == mShowStatus) {
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
		} else if (pref == mAOptionsAlsoOnComplete) {
			settings.setAdvancedOptionsAlsoOnComplete(mAOptionsAlsoOnComplete.isChecked());
			updateAdvancedOptions();
			return true;
		} else if (pref == mScrobbleAllNow) {
			Intent service = new Intent(ScrobblingService.ACTION_JUSTSCROBBLE);
			service.putExtra("scrobbleall", true);
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
		updateAdvancedOptions();
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

	private void updateAdvancedOptions() {
		AdvancedOptions so = settings.getAdvancedOptions();
		setScrobblingOptionsRestEnabled(so == AdvancedOptions.CUSTOM);

		mAOptionsChooser.setSummary(so.getName(this));
		mAOptionsWhen.setSummary(settings.getAdvancedOptionsWhen()
				.getName(this));
		mAOptionsAlsoOnComplete.setChecked(settings
				.getAdvancedOptionsAlsoOnComplete());

		mAOptionsChooser.setValue(settings.getAdvancedOptions().toString());
		mAOptionsWhen.setValue(settings.getAdvancedOptionsWhen().toString());
		
		int numCache = mDb.queryNumberOfAllRows();
		mScrobbleAllNow.setSummary(getString(R.string.scrobbles_cache).replace("%1", Integer.toString(numCache)));
		mScrobbleAllNow.setEnabled(numCache > 0);
	}

	private OnPreferenceChangeListener mOnOptionsChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (!(newValue instanceof CharSequence)) {
				Log.e(TAG, "Got weird newValue on options change: " + newValue);
				return false;
			}
			CharSequence newcs = (CharSequence) newValue;
			AdvancedOptions so = AdvancedOptions.valueOf(newcs.toString());
			settings.setAdvancedOptions(so);

			if (so == AdvancedOptions.BATTERY_SAVING) {
				Toast.makeText(SettingsActivity.this,
						getString(R.string.should_disable_np),
						Toast.LENGTH_LONG).show();
			}
			updateAdvancedOptions();
			return true;
		}
	};
	private OnPreferenceChangeListener mOnOptionsWhenChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (!(newValue instanceof CharSequence)) {
				Log.e(TAG, "Got weird newValue on options when change: "
						+ newValue);
				return false;
			}
			CharSequence newcs = (CharSequence) newValue;
			AdvancedOptionsWhen sow = AdvancedOptionsWhen.valueOf(newcs
					.toString());
			settings.setAdvancedOptionsWhen(sow);
			updateAdvancedOptions();
			return true;
		}
	};

	private void setScrobblingOptionsRestEnabled(boolean enabled) {
		mAOptionsWhen.setEnabled(enabled);
		mAOptionsAlsoOnComplete.setEnabled(enabled);
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
