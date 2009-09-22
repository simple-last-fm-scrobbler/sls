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

	public static final String PACKAGE_SCROBBLE_DROID = "net.jjc1138.android.scrobbler";

	private static final String KEY_SUPPORTED_MUSICAPPS_LIST = "supported_music_apps_list";

	private AppSettings settings;

	private PreferenceCategory mSupportedMusicAppsList;
	private HashMap<CheckBoxPreference, MusicApp> mPrefsToAppsMap;
	private HashMap<MusicApp, CheckBoxPreference> mAppsToPrefsMap;

	private boolean mScrobbleDroidInstalled;
	private String mScrobbleDroidLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.music_apps);

		settings = new AppSettings(this);

		mSupportedMusicAppsList = (PreferenceCategory) findPreference(KEY_SUPPORTED_MUSICAPPS_LIST);
		mPrefsToAppsMap = new HashMap<CheckBoxPreference, MusicApp>();
		mAppsToPrefsMap = new HashMap<MusicApp, CheckBoxPreference>();

		MusicApp[] apps = MusicApp.values();
		for (MusicApp app : apps) {
			boolean enabled = settings.isMusicAppEnabled(app);

			CheckBoxPreference appPref = new CheckBoxPreference(this, null);
			appPref.setTitle(app.getName());
			appPref.setPersistent(false); // TODO: what does this mean?
			appPref.setChecked(enabled);

			mSupportedMusicAppsList.addPreference(appPref);
			mPrefsToAppsMap.put(appPref, app);
			mAppsToPrefsMap.put(app, appPref);
		}

		mScrobbleDroidInstalled = Util.checkForInstalledApp(this,
				PACKAGE_SCROBBLE_DROID);
		mScrobbleDroidLabel = Util.getAppName(this, PACKAGE_SCROBBLE_DROID);
	}

	@Override
	protected void onResume() {
		super.onResume();
		update();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {

		// we clicked an "enable music app" checkbox
		MusicApp app = mPrefsToAppsMap.get(pref);
		if (app != null) {
			CheckBoxPreference cbp = (CheckBoxPreference) pref;
			boolean checked = cbp.isChecked();
			settings.setMusicAppEnabled(app, checked);
			setSMASummary(pref, app);

			if (checked && mScrobbleDroidInstalled
					&& app.clashesWithScrobbleDroid()) {
				Util.warningDialog(this, getString(
						R.string.incompatability_long).replaceAll("%1",
						mScrobbleDroidLabel));
			}
			return true;
		}

		return super.onPreferenceTreeClick(prefScreen, pref);
	}

	private void update() {
		MusicApp[] apps = MusicApp.values();
		for (MusicApp app : apps) {
			CheckBoxPreference appPref = mAppsToPrefsMap.get(app);
			setSMASummary(appPref, app);
		}
	}

	private void setSMASummary(Preference pref, MusicApp app) {
		boolean enabled = settings.isMusicAppEnabled(app);
		boolean installed = Util.checkForInstalledApp(this, app.getPackage());
		if (!enabled) {
			pref.setSummary(R.string.app_disabled);
		} else if (!installed) {
			pref.setSummary(R.string.not_installed);
		} else if (mScrobbleDroidInstalled && app.clashesWithScrobbleDroid()) {
			pref.setSummary(getString(R.string.incompatability_short)
					.replaceAll("%1", mScrobbleDroidLabel));
		} else {
			pref.setSummary(null);
		}
	}
}
