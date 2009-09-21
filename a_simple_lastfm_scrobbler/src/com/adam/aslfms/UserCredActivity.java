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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.adam.aslfms.R;
import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.Status;

public class UserCredActivity extends PreferenceActivity {

	private static final String TAG = "UserCredActivity";

	// keys to preferences
	private static final String KEY_USER_CREDS_HEADER = "user_creds_header";
	private static final String KEY_EDIT_USER_CREDENTIALS = "edit_user_credentials";
	private static final String KEY_CLEAR_USER_CREDENTIALS = "clear_user_credentials";
	private static final String KEY_CREATE_USER = "create_user";

	private NetApp mNetApp;

	private AppSettings settings;

	private PreferenceCategory mHeader;
	private EditUserCredentials mEditCreds;
	private Preference mClearCreds;
	private Preference mCreateUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.user_cred_prefs);

		String snapp = getIntent().getExtras().getString("netapp");
		if (snapp == null) {
			Log.e(TAG, "Got null snetapp");
			finish();
		}
		mNetApp = NetApp.valueOf(snapp);

		settings = new AppSettings(this);

		mHeader = (PreferenceCategory) findPreference(KEY_USER_CREDS_HEADER);
		mHeader.setTitle(mNetApp.getName());
		mEditCreds = (EditUserCredentials) findPreference(KEY_EDIT_USER_CREDENTIALS);
		mEditCreds.setNetApp(mNetApp);
		mClearCreds = findPreference(KEY_CLEAR_USER_CREDENTIALS);
		mCreateUser = findPreference(KEY_CREATE_USER);
		mCreateUser.setSummary(getString(R.string.create_user_summary).replace(
				"%1", mNetApp.getName()));
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {

		if (pref == mClearCreds) {
			update();
			Intent service = new Intent(ScrobblingService.ACTION_CLEARCREDS);
			service.putExtra("netapp", mNetApp.getIntentExtraValue());
			startService(service);
			return true;
		} else if (pref == mCreateUser) {
			Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(mNetApp
					.getSignUpUrl()));
			startActivity(browser);
			return true;
		}

		return super.onPreferenceTreeClick(prefScreen, pref);
	}

	private void update() {
		mEditCreds.setSummary(mNetApp.getStatusSummary(this, settings));

		boolean hasCreds = settings.getAuthStatus(mNetApp) != Status.AUTHSTATUS_NOAUTH
				|| settings.getUsername(mNetApp).length() != 0
				|| settings.getPassword(mNetApp).length() != 0
				|| settings.getPwdMd5(mNetApp).length() != 0;

		mClearCreds.setEnabled(hasCreds);
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

	private BroadcastReceiver onAuth = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			if (mNetApp == NetApp.valueOf(b.getString("netapp"))) {
				UserCredActivity.this.update();
			}
		}
	};

}
