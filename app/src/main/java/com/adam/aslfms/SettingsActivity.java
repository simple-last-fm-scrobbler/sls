/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/tgwizard/sls
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

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.applemusic.NotificationService;
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
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final String TAG = "SettingsActivity";

    private static final String KEY_SCROBBLE_ALL_NOW = "scrobble_all_now";
    private static final String KEY_VIEW_SCROBBLE_CACHE = "view_scrobble_cache";
    private static final String KEY_HEART_CURRENT_TRACK = "my_heart_button";
    private static final String KEY_COPY_CURRENT_TRACK = "my_copy_button";

    private AppSettings settings;

    private ScrobblesDatabase mDb;

    private Preference mScrobbleAllNow;
    private Preference mViewScrobbleCache;
    private Preference mHeartCurrentTrack;
    private Preference mCopyCurrentTrack;

    int REQUEST_READ_STORAGE;
    int REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

    @Override
    @SuppressWarnings("deprecation")
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

        mHeartCurrentTrack = findPreference(KEY_HEART_CURRENT_TRACK);
        mScrobbleAllNow = findPreference(KEY_SCROBBLE_ALL_NOW);
        mViewScrobbleCache = findPreference(KEY_VIEW_SCROBBLE_CACHE);
        mCopyCurrentTrack = findPreference(KEY_COPY_CURRENT_TRACK);

        checkNetwork();
        permsCheck();
        credsCheck();

        int v = Util.getAppVersionCode(this, getPackageName());
        if (settings.getWhatsNewViewedVersion() < v) {
            new WhatsNewDialog(this).show();
            settings.setWhatsNewViewedVersion(v);
        }

        // Start Apple listening service if applicabble
        if (settings.getAppleListenerEnabled()) {
            Intent intent = new Intent(this, NotificationService.class);
            startService(intent);
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

        checkNetwork();
        credsCheck();

        IntentFilter ifs = new IntentFilter();
        ifs.addAction(ScrobblingService.BROADCAST_ONSTATUSCHANGED);
        registerReceiver(onStatusChange, ifs);
        update();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
                                         Preference pref) {
        if (pref == mScrobbleAllNow) {

            checkNetwork();


            int numInCache = mDb.queryNumberOfTracks();
            Util.scrobbleAllIfPossible(this, numInCache);
            return true;
        } else if (pref == mViewScrobbleCache) {
            Intent i = new Intent(this, ViewScrobbleCacheActivity.class);
            i.putExtra("viewall", true);
            startActivity(i);
            return true;
        } else if (pref == mHeartCurrentTrack) {
            Util.heartIfPossible(this);
            return true;
        } else if (pref == mCopyCurrentTrack) {
            Util.copyIfPossible(this);
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

    private void checkNetwork() {
        this.sendBroadcast(new Intent(AppSettings.ACTION_NETWORK_OPTIONS_CHANGED));
        if (Util.checkForOkNetwork(this) != Util.NetworkStatus.OK) {
            Snackbar.make(getListView(), getString(R.string.limited_network), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void credsCheck() {
        //Credentials Check
        for (NetApp napp : NetApp.values()) {
            if (!settings.getUsername(napp).equals("")) {
                return;
            }
        }
        Snackbar.make(getListView(), this.getString(R.string.creds_required), Snackbar.LENGTH_LONG).show();
    }

    private void permsCheck() {
        //PERMISSION CHECK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception, READ_EXTERNAL_STORAGE. " + e);
            }
        }

        try {
            if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS}, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS. " + e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            if (requestCode == REQUEST_READ_STORAGE) {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //PERMISSION GRANTED
                } else {
                    //PERMISSION DENIED permission denied
                    Toast.makeText(SettingsActivity.this, "App will not function correctly.", Toast.LENGTH_LONG).show(); //TODO string
                }
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        } catch (Exception e) {
            Log.e(TAG, "READ_EXTERNAL_STORAGE. " + e);
        }
    }
}
