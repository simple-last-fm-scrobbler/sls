/**
 * This file is part of Simple Scrobbler.
 * <p>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p>
 * Copyright 2011 Simple Scrobbler Team
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

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.adam.aslfms.receiver.MusicAPI;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.MyContextWrapper;
import com.adam.aslfms.util.Util;
import com.example.android.supportv7.app.AppCompatPreferenceActivity;

import java.util.HashMap;

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

    private AppSettings settings;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase));
    }

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

        settings = new AppSettings(this);
        setTheme(settings.getAppTheme());

        addPreferencesFromResource(R.xml.music_apps_prefs);

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
    @SuppressWarnings("deprecation")
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.clearapps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.MENU_CLEAR_APPS_ID:
                MusicAPI.clearDatabase(this);
                update();
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
