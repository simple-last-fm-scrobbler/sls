/**
 * This file is part of Simple Last.fm Scrobbler.
 * 
 *     https://github.com/tgwizard/sls
 * 
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 

package com.adam.aslfms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Util;
import com.example.android.supportv7.app.AppCompatPreferenceActivity;

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
public class SettingsActivity extends AppCompatPreferenceActivity {
	private static final String TAG = "SettingsActivity";

	private static final String KEY_SCROBBLE_ALL_NOW = "scrobble_all_now";
	private static final String KEY_VIEW_SCROBBLE_CACHE = "view_scrobble_cache";

	private AppSettings settings;

	private ScrobblesDatabase mDb;

	private Preference mScrobbleAllNow;
	private Preference mViewScrobbleCache;

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

		mScrobbleAllNow = findPreference(KEY_SCROBBLE_ALL_NOW);
		mViewScrobbleCache = findPreference(KEY_VIEW_SCROBBLE_CACHE);

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
		unregisterReceiver(onStatusChange);
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter ifs = new IntentFilter();
		ifs.addAction(ScrobblingService.BROADCAST_ONSTATUSCHANGED);
		registerReceiver(onStatusChange, ifs);
		update();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {
		if (pref == mScrobbleAllNow) {
			int numInCache = mDb.queryNumberOfTracks();
			Util.scrobbleAllIfPossible(this, numInCache);
			return true;
		} else if (pref == mViewScrobbleCache) {
			Intent i = new Intent(this, ViewScrobbleCacheActivity.class);
			i.putExtra("viewall", true);
			startActivity(i);
			return true;
		}
		return super.onPreferenceTreeClick(prefScreen, pref);
	}

	/**
	 * Updates what is shown to the user - preference titles and summaries, and
	 * whether stuff is enabled or checked, etc.
	 */
	private void update() {
		int numCache = mDb.queryNumberOfTracks();
		mScrobbleAllNow.setSummary(getString(R.string.scrobbles_cache).replace(
				"%1", Integer.toString(numCache)));
		mScrobbleAllNow.setEnabled(numCache > 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			new AboutDialog(this).show();
			return true;
		case R.id.menu_whats_new:
			new WhatsNewDialog(this).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private BroadcastReceiver onStatusChange = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SettingsActivity.this.update();
		}
	};
}
