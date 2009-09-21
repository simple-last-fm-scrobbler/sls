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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;

import com.adam.aslfms.util.AppSettings;
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
	private static final String KEY_TOGGLE_SCROBBLING = "toggle_scrobbling";
	private static final String KEY_TOGGLE_NOWPLAYING = "toggle_nowplaying";

	private static final int MENU_ABOUT_ID = 0;

	private AppSettings settings;

	private CheckBoxPreference mScrobblePref;
	private CheckBoxPreference mNowplayPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_prefs);

		settings = new AppSettings(this);

		mScrobblePref = (CheckBoxPreference) findPreference(KEY_TOGGLE_SCROBBLING);
		mNowplayPref = (CheckBoxPreference) findPreference(KEY_TOGGLE_NOWPLAYING);

		int v = Util.getAppVersionCode(this, getPackageName());
		if (settings.getWhatsNewViewedVersion() < v) {
			new WhatsNewDialog(this).show();
			settings.setWhatsNewViewedVersion(v);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		update();
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
			new AboutDialog(this).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
