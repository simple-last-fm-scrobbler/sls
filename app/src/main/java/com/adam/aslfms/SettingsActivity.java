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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.MyContextWrapper;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Util;
import com.example.android.supportv7.app.AppCompatPreferenceActivity;

/**
 * This is the activity that is shown when the user launches
 * "A Simple Scrobbler" from the app screen. It allows the user to set
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

    Context mCtx;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase));
    }

    @Override
    public Resources.Theme getTheme() {
        settings = new AppSettings(this);
        Resources.Theme theme = super.getTheme();
        theme.applyStyle(settings.getAppTheme(), true);
        //Log.d(TAG, getResources().getResourceName(settings.getAppTheme()));
        // you could also use a switch if you have many themes that could apply
        return theme;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_prefs);

        settings = new AppSettings(this);
        setTheme(settings.getAppTheme());

        mDb = new ScrobblesDatabase(this);
        mCtx = this;

        try {
            mDb.open();
        } catch (SQLException e) {
            Log.e(TAG, "Cannot open database!");
            Log.e(TAG, e.getMessage());
            mDb = null;
        }

        checkNetwork();

        mHeartCurrentTrack = findPreference(KEY_HEART_CURRENT_TRACK);
        mScrobbleAllNow = findPreference(KEY_SCROBBLE_ALL_NOW);
        mViewScrobbleCache = findPreference(KEY_VIEW_SCROBBLE_CACHE);
        mCopyCurrentTrack = findPreference(KEY_COPY_CURRENT_TRACK);

        // TODO: VERIFY EVERYTHING BELOW IS SAFE
        int v = Util.getAppVersionCode(this, getPackageName());
        if (settings.getWhatsNewViewedVersion() < v){
            Intent i = new Intent(this, PermissionsActivity.class);
            startActivity(i);
        }

        if (settings.getWhatsNewViewedVersion() < v && settings.getKeyBypassNewPermissions() != 2) {
            new WhatsNewDialog(this).show();
            settings.setWhatsNewViewedVersion(v);
            mDb.rebuildDataBaseOnce(); // keep as not all users have the newest database.
        }
        Util.runServices(this);        // Scrobbler, Controller
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
        Util.runServices(this);
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
            case R.id.menu_exit:
                handleAppExit();
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

    private void handleAppExit(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        boolean currentActiveState = settings.isActiveAppEnabled(Util.checkPower(mCtx));
                        settings.setActiveAppEnabled(Util.checkPower(mCtx),false);
                        settings.setTempExitAppEnabled(Util.checkPower(mCtx),true);
                        Util.runServices(mCtx);
                        Util.stopAllServices(mCtx);
                        settings.setActiveAppEnabled(Util.checkPower(mCtx),currentActiveState);
                        settings.setTempExitAppEnabled(Util.checkPower(mCtx), false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            finishAndRemoveTask();
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)  {
                            SettingsActivity.this.finishAffinity();
                        }
                        ActivityCompat.finishAffinity(SettingsActivity.this );
                        SettingsActivity.this.finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = mCtx.getResources().getString(R.string.warning) + "! " + mCtx.getResources().getString(R.string.are_you_sure) + " - " + mCtx.getResources().getString(R.string.warning_will_not_scrobble);
        builder.setMessage(message).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }
}
