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

package com.adam.aslfms;

import java.util.HashMap;

import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.adam.aslfms.receiver.MusicAPI;
import com.adam.aslfms.util.Util;
import com.example.android.supportv7.app.AppCompatPreferenceActivity;

public class MusicAppsActivity extends AppCompatPreferenceActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "MusicAppsScreen";

	public static final String PACKAGE_SCROBBLE_DROID = "net.jjc1138.android.scrobbler";

	private static final String KEY_SUPPORTED_MUSICAPPS_LIST = "supported_music_apps_list";

	private PreferenceCategory mSupportedMusicAppsList;
	private HashMap<CheckBoxPreference, MusicAPI> mPrefsToMapisMap;
	private HashMap<MusicAPI, CheckBoxPreference> mMapisToPrefsMap;

	private boolean mScrobbleDroidInstalled;
	private String mScrobbleDroidLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.music_apps);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		mSupportedMusicAppsList = (PreferenceCategory) findPreference(KEY_SUPPORTED_MUSICAPPS_LIST);
		mPrefsToMapisMap = new HashMap<CheckBoxPreference, MusicAPI>();
		mMapisToPrefsMap = new HashMap<MusicAPI, CheckBoxPreference>();

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
		MusicAPI mapi = mPrefsToMapisMap.get(pref);
		if (mapi != null) {
			CheckBoxPreference cbp = (CheckBoxPreference) pref;
			boolean checked = cbp.isChecked();
			mapi.setEnabled(this, checked);
			setSMASummary(pref, mapi);

			if (checked && mScrobbleDroidInstalled
					&& mapi.clashesWithScrobbleDroid()) {
				Util.warningDialog(this, getString(
						R.string.incompatability_long).replaceAll("%1",
						mScrobbleDroidLabel));
			}
			return true;
		}

		return super.onPreferenceTreeClick(prefScreen, pref);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void update() {
		mSupportedMusicAppsList.removeAll();
		mPrefsToMapisMap.clear();
		mMapisToPrefsMap.clear();

		MusicAPI[] mapis = MusicAPI.all(this);
		for (MusicAPI mapi : mapis) {
			CheckBoxPreference appPref = new CheckBoxPreference(this, null);
			appPref.setTitle(mapi.getName());
			appPref.setPersistent(false);
			appPref.setChecked(mapi.isEnabled());

			mSupportedMusicAppsList.addPreference(appPref);
			mPrefsToMapisMap.put(appPref, mapi);
			mMapisToPrefsMap.put(mapi, appPref);
			setSMASummary(appPref, mapi);
		}

		// explanation text, for what this screen does
		Preference detect = new Preference(this);
		if (mapis.length == 0)
			detect.setTitle(R.string.no_supported_mapis_title);
		else if (mapis.length == 1)
			detect.setTitle(R.string.find_supported_mapis_one_title);
		else
			detect.setTitle(getString(R.string.find_supported_mapis_many_title)
					.replace("%1", Integer.toString(mapis.length)));
		detect.setSummary(R.string.find_supported_mapis_summary);
		mSupportedMusicAppsList.addPreference(detect);
	}

	private void setSMASummary(Preference pref, MusicAPI mapi) {
		String pkg = mapi.getPackage();
		boolean installed;
		if ((pkg == null || pkg.startsWith(MusicAPI.NOT_AN_APPLICATION_PACKAGE)))
			installed = true; // i.e. it cannot be installed in this case
		else
			installed = Util.checkForInstalledApp(this, mapi.getPackage());

		if (!mapi.isEnabled()) {
			pref.setSummary(R.string.app_disabled);
		} else if (!installed) {
			pref.setSummary(R.string.not_installed);
		} else if (mScrobbleDroidInstalled && mapi.clashesWithScrobbleDroid()) {
			pref.setSummary(getString(R.string.incompatability_short)
					.replaceAll("%1", mScrobbleDroidLabel));
		} else {
			pref.setSummary(mapi.getMessage());
		}
	}
}
