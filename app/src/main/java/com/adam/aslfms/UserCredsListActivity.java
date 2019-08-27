/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p>
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.Util;
import com.example.android.supportv7.app.AppCompatPreferenceActivity;

import java.util.HashMap;

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
    public Resources.Theme getTheme() {
        settings = new AppSettings(this);
        Resources.Theme theme = super.getTheme();
        theme.applyStyle(settings.getAppTheme(), true);
        Log.d(TAG, getResources().getResourceName(settings.getAppTheme()));
        // you could also use a switch if you have many themes that could apply
        return theme;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addPreferencesFromResource(R.xml.user_creds_list_prefs);

        settings = new AppSettings(this);
        setTheme(settings.getAppTheme());

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
    @SuppressWarnings("deprecation")
    public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
                                         Preference pref) {

        if (pref == mClearAllCreds) {
            if (settings.isAnyAuthenticated()) {
                Util.confirmDialog(this,
                        getString(R.string.confirm_clear_all_creds),
                        R.string.clear_creds, android.R.string.cancel,
                        (dialog, which) -> sendClearCreds());
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
        service.setAction(ScrobblingService.ACTION_CLEARCREDS);
        service.putExtra("clearall", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service);
        } else {
            startService(service);
        }
    }

    private void update() {
        mClearAllCreds.setEnabled(settings.hasAnyCreds());

        clearUserCredsList();
        setUserCredsSummaries();
    }

    private void clearUserCredsList() {
        mUserCredsList.removeAll();
        mUserCredsPrefToAppMap.clear();
        mUserCredsAppToPrefMap.clear();
    }

    @SuppressWarnings("deprecation")
    private void setUserCredsSummaries() {
        for (NetApp napp : NetApp.values()) {
            Preference pref = findPreference(getString(napp.getSettingsPrefix()));
            if (settings.isAuthenticated(napp)) {
                pref.setTitle(napp.getName());
            } else {
                pref.setTitle(getString(R.string.log_in_to) + " "
                        + napp.getName());
            }
            pref.setSummary(Util.getStatusSummary(this, settings, napp));
            mUserCredsPrefToAppMap.put(pref, napp);
            mUserCredsAppToPrefMap.put(napp, pref);
        }
    }

    private BroadcastReceiver onStatusChange = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            UserCredsListActivity.this.update();
        }
    };
}
