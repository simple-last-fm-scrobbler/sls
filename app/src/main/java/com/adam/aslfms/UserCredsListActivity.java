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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.Util;
import com.example.android.supportv7.app.AppCompatPreferenceActivity;

public class UserCredsListActivity extends AppCompatPreferenceActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "UserCredsListScreen";

	private static final String KEY_USER_CREDENTIALS_LIST = "supported_netapps_list";
	private static final String KEY_CLEAR_ALL_CREDS = "clear_all_user_credentials";

	private AppSettings settings;

	private PreferenceCategory mUserCredsList;
	private HashMap<Preference, NetApp> mUserCredsPrefToAppMap;
	private HashMap<NetApp, Preference> mUserCredsAppToPrefMap;
	private Preference mClearAllCreds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		addPreferencesFromResource(R.xml.user_creds_list);

		settings = new AppSettings(this);

		mUserCredsList = (PreferenceCategory) findPreference(KEY_USER_CREDENTIALS_LIST);
		mUserCredsPrefToAppMap = new HashMap<Preference, NetApp>();
		mUserCredsAppToPrefMap = new HashMap<NetApp, Preference>();
		mClearAllCreds = findPreference(KEY_CLEAR_ALL_CREDS);
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
		ifs.addAction(ScrobblingService.BROADCAST_ONAUTHCHANGED);

		registerReceiver(onStatusChange, ifs);
		update();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {

		if (pref == mClearAllCreds) {
			if (settings.isAnyAuthenticated()) {
				Util.confirmDialog(this,
						getString(R.string.confirm_clear_all_creds),
						R.string.clear_creds, android.R.string.cancel,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								sendClearCreds();
							}
						});
			} else {
				sendClearCreds();
			}

			return true;
		} else {
			// we clicked an "user creds" preference
			NetApp napp = mUserCredsPrefToAppMap.get(pref);
			if (napp != null) {
				Intent uca = new Intent(this, UserCredActivity.class);
				uca.putExtra("netapp", napp.getIntentExtraValue());
				startActivity(uca);
				return true;
			}
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

	private void sendClearCreds() {
		Intent service = new Intent(this, ScrobblingService.class);
		service.setAction(ScrobblingService.ACTION_AUTHENTICATE);
		service.putExtra("clearall", true);
		startService(service);
	}

	private void update() {
		mClearAllCreds.setEnabled(settings.hasAnyCreds());

		clearUserCredsList();
		loadUserCredsList();
		setUserCredsSummaries();
	}

	private void clearUserCredsList() {
		mUserCredsList.removeAll();
		mUserCredsPrefToAppMap.clear();
		mUserCredsAppToPrefMap.clear();
	}

	private void loadUserCredsList() {
		NetApp[] napps = NetApp.values();
		for (NetApp napp : napps) {
			Preference pref = new Preference(this, null);
			mUserCredsPrefToAppMap.put(pref, napp);
			mUserCredsAppToPrefMap.put(napp, pref);
			mUserCredsList.addPreference(pref);
		}
	}

	private void setUserCredsSummaries() {
		for (NetApp napp : NetApp.values()) {
			Preference pref = mUserCredsAppToPrefMap.get(napp);
			if (settings.isAuthenticated(napp)) {
				pref.setTitle(napp.getName());
			} else {
				pref.setTitle(getString(R.string.log_in_to) + " "
						+ napp.getName());
			}
			pref.setSummary(Util.getStatusSummary(this, settings, napp));
		}
	}

	private BroadcastReceiver onStatusChange = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			UserCredsListActivity.this.update();
		}
	};
}
