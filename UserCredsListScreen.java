package com.adam.aslfms;

import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;

public class UserCredsListScreen extends PreferenceActivity {
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
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {

		if (pref == mClearAllCreds) {
			Intent service = new Intent(ScrobblingService.ACTION_CLEARCREDS);
			service.putExtra("clearall", true);
			startService(service);
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
			pref.setTitle(getString(R.string.log_in_to) + " " + napp.getName());
			mUserCredsPrefToAppMap.put(pref, napp);
			mUserCredsAppToPrefMap.put(napp, pref);
			mUserCredsList.addPreference(pref);
		}
	}

	private void setUserCredsSummaries() {
		for (NetApp napp : NetApp.values()) {
			mUserCredsAppToPrefMap.get(napp).setSummary(
					napp.getStatusSummary(this, settings));
		}
	}

	private BroadcastReceiver onAuth = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			UserCredsListScreen.this.update();
		}
	};
}
