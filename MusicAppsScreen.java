package com.adam.aslfms;

import java.util.HashMap;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.adam.aslfms.receiver.MusicApp;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.Util;

public class MusicAppsScreen extends PreferenceActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "MusicAppsScreen";

	private static final String KEY_SUPPORTED_MUSICAPPS_LIST = "supported_music_apps_list";

	private AppSettings settings;

	private PreferenceCategory mSupportedMusicAppsList;
	private HashMap<CheckBoxPreference, MusicApp> mSupportedMusicAppsMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.music_apps);

		settings = new AppSettings(this);

		mSupportedMusicAppsList = (PreferenceCategory) findPreference(KEY_SUPPORTED_MUSICAPPS_LIST);
		mSupportedMusicAppsMap = new HashMap<CheckBoxPreference, MusicApp>();
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadSupportedMusicAppsList();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {

		// we clicked an "enable music app" checkbox
		MusicApp app = mSupportedMusicAppsMap.get(pref);
		if (app != null) {
			CheckBoxPreference cbp = (CheckBoxPreference) pref;
			boolean checked = cbp.isChecked();
			settings.setMusicAppEnabled(app, checked);
			setSMASummary(pref, app);
			return true;
		}

		return super.onPreferenceTreeClick(prefScreen, pref);
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
}
