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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
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
import com.adam.aslfms.util.MyContextWrapper;
import com.adam.aslfms.util.Util;
import com.example.android.supportv7.app.AppCompatPreferenceActivity;

public class UserCredActivity extends AppCompatPreferenceActivity {

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
        addPreferencesFromResource(R.xml.user_cred_prefs);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            Log.e(TAG, "Got null snetapp");
            finish();
        }
        String snapp = bundle.getString("netapp");
        if (snapp == null) {
            Log.e(TAG, "Got null snetapp");
            finish();
        }
        mNetApp = NetApp.valueOf(snapp);

        settings = new AppSettings(this);
        setTheme(settings.getAppTheme());

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
    @SuppressWarnings("deprecation")
    public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
                                         Preference pref) {

        if (pref == mClearCreds) {
            if (settings.isAuthenticated(mNetApp)) {
                Util.confirmDialog(this,
                        getString(R.string.confirm_clear_creds).replaceAll(
                                "%1", mNetApp.getName()), R.string.clear_creds,
                        android.R.string.cancel, (dialog, which) -> sendClearCreds(pref.getContext()));
            } else {
                sendClearCreds(pref.getContext());
            }

            update();
            return true;
        } else if (pref == mCreateUser) {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(mNetApp
                    .getSignUpUrl(settings)));
            try {
                startActivity(browser);
            } catch (Exception e) {
                if (mNetApp == NetApp.LIBREFMCUSTOM) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://git.gnu.io/gnu/gnu-fm/blob/master/gnufm_install.txt")));
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/metabrainz/listenbrainz-server/blob/master/README.md")));
                }
            }
            return true;
        }

        return super.onPreferenceTreeClick(prefScreen, pref);
    }

    private void sendClearCreds(Context ctx) {
        Intent service = new Intent(this, ScrobblingService.class);
        service.setAction(ScrobblingService.ACTION_CLEARCREDS);
        service.putExtra("netapp", mNetApp.getIntentExtraValue());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && settings.isActiveAppEnabled(Util.checkPower(ctx))) {
            this.startForegroundService(service);
        } else {
            this.startService(service);
        }
    }

    private void update() {
        mEditCreds.setSummary(Util.getStatusSummary(this, settings, mNetApp));
        boolean hasCreds = settings.hasCreds(mNetApp);
        mClearCreds.setEnabled(hasCreds);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(onAuthChange);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter ifs = new IntentFilter();
        ifs.addAction(ScrobblingService.BROADCAST_ONAUTHCHANGED);
        registerReceiver(onAuthChange, ifs);

        update();
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

    private BroadcastReceiver onAuthChange = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            if (mNetApp == NetApp.valueOf(b.getString("netapp"))) {
                UserCredActivity.this.update();
            }
        }
    };
}
