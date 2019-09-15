/**
 * This file is part of Simple Scrobbler.
 * <p/>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p/>
 * Copyright 2011 Simple Scrobbler Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.adam.aslfms;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.MyContextWrapper;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.enums.AdvancedOptions;
import com.adam.aslfms.util.enums.AdvancedOptionsWhen;
import com.adam.aslfms.util.enums.NetworkOptions;
import com.adam.aslfms.util.enums.PowerOptions;
import com.example.android.supportv7.app.AppCompatPreferenceActivity;

import java.util.Arrays;

public class OptionsActivity extends AppCompatPreferenceActivity {
    private static final String TAG = "OptionsGeneralScreen";

    private static final String KEY_SCROBBLE_POINT = "scrobble_pointer";

    private static final String KEY_BATTERY = "ao_battery";
    private static final String KEY_PLUGGED = "ao_plugged";
    private static final String KEY_EXPORT_DB = "export_database";
    private static final String KEY_NOTIFICATION_PRIORITY = "notification_priority";
    private static final String KEY_LANGUAGES_LIST = "languages_list";
    private static final String KEY_PERMISSION_SHOW = "permission_activity_show";
    private static final String KEY_THEME = "my_theme";

    private AppSettings settings;

    private SeekBarPreference mScrobblePoint;
    private PowerSpecificPrefs mBatteryOptions;
    private PowerSpecificPrefs mPluggedOptions;

    Context ctx = this;


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

        addPreferencesFromResource(R.xml.options_prefs);

        settings = new AppSettings(this);
        setTheme(settings.getAppTheme());

        mScrobblePoint = (SeekBarPreference) findPreference(KEY_SCROBBLE_POINT);
        mScrobblePoint.setDefaults(settings.getScrobblePoint() - 50, 50);
        mScrobblePoint.setSaver(new SeekBarPreference.Saver() {
            @Override
            public void save(int value) {
                settings.setScrobblePoint(value + 50);
                mScrobblePoint
                        .setDefaults(settings.getScrobblePoint() - 50, 50);
                OptionsActivity.this.update();
            }

        });

        mBatteryOptions = new PowerSpecificPrefs(PowerOptions.BATTERY,
                (PreferenceCategory) findPreference(KEY_BATTERY));
        mBatteryOptions.create();

        mPluggedOptions = new PowerSpecificPrefs(PowerOptions.PLUGGED_IN,
                (PreferenceCategory) findPreference(KEY_PLUGGED));
        mPluggedOptions.create();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
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

        if (mBatteryOptions.onClick(pref)) {
            update();
            return true;
        } else if (mPluggedOptions.onClick(pref)) {
            update();
            return true;
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

    private void update() {
        Log.d(TAG, "updating...");
        String sp = Integer.toString(settings.getScrobblePoint());
        mScrobblePoint.setSummary(getString(R.string.scrobble_point_summary)
                .replaceAll("%1", sp));
        mBatteryOptions.update();
        mPluggedOptions.update();
    }

    private class PowerSpecificPrefs {

        public PowerSpecificPrefs(PowerOptions power,
                                  PreferenceCategory category) {
            super();
            this.power = power;
            this.category = category;
        }

        private PowerOptions power;
        private PreferenceCategory category;

        private Preference showPermissionActivity;
        private ListPreference chooser;
        private CheckBoxPreference active_app;
        private CheckBoxPreference scrobble;
        private CheckBoxPreference np;
        private ListPreference when;
        private CheckBoxPreference also_on_complete;
        private ListPreference net;
        private CheckBoxPreference roaming;
        private Preference exportdatabase;
        private ListPreference notification_priority;
        private ListPreference languages_list;
        private Preference mChangeTheme;

        public void create() {
            createChooserPreference();
            createActiveAppPreference();
            createScrobbleEnablePreference();
            createNPEnablePreference();
            createWhenPreference();
            createAOCPreference();
            createNetPreference();
            createRoamingPreference();
            mChangeTheme = findPreference(KEY_THEME);
            exportdatabase = findPreference(KEY_EXPORT_DB);
            showPermissionActivity = findPreference(KEY_PERMISSION_SHOW);
            notification_priority = (ListPreference) findPreference(KEY_NOTIFICATION_PRIORITY);
            notification_priority.setDefaultValue(Util.notificationStringToInt(getApplicationContext()));
            languages_list = (ListPreference) findPreference(KEY_LANGUAGES_LIST);
            notification_priority.setOnPreferenceChangeListener((Preference preference, Object object) -> {
                        String[] strings = getResources().getStringArray(R.array.notification_priority_list);
                        String string = (String) object;
                        int position = Arrays.asList(strings).indexOf(string);
                        settings.setKeyNotificationPriority(position);
                        return false;
                    }
            );
            languages_list.setOnPreferenceChangeListener((Preference preference, Object object) -> {
                        String userSelection = (String) object;
                        String[] country_codes = getResources().getStringArray(R.array.language_codes);
                        String[] langauge_list = getResources().getStringArray(R.array.language_list);
                        int position = Arrays.asList(langauge_list).indexOf(userSelection);
                        settings.setAppLocale(country_codes[position]);
                        recreate();
                        return false;
                    }
            );
            showPermissionActivity.setOnPreferenceClickListener((Preference preference) -> {
                        Intent i = new Intent(ctx, PermissionsActivity.class);
                        startActivity(i);
                        return false;
                    }
            );

            mChangeTheme.setOnPreferenceClickListener((Preference preference) ->
            {
                Intent i = new Intent(ctx, ChangeThemeActivity.class);
                startActivity(i);
                return true;
            });
        }

        public boolean onClick(Preference pref) {
            if (pref == active_app) {
                settings.setActiveAppEnabled(power, active_app.isChecked());
                settings.setTempExitAppEnabled(Util.checkPower(ctx), true);
                Util.runServices(pref.getContext());
                settings.setTempExitAppEnabled(Util.checkPower(ctx), false);
            } else if (pref == scrobble) {
                settings.setScrobblingEnabled(power, scrobble.isChecked());
                return true;
            } else if (pref == np) {
                settings.setNowPlayingEnabled(power, np.isChecked());
                return true;
            } else if (pref == also_on_complete) {
                settings.setAdvancedOptionsAlsoOnComplete(power,
                        also_on_complete.isChecked());
                return true;
            } else if (pref == roaming) {
                settings.setSubmitOnRoaming(power, roaming.isChecked());
                return true;
            } else if (pref == exportdatabase) {
                Util.exportAllDatabases(getApplicationContext());
            }
            return false;
        }

        private void update() {
            AdvancedOptions ao = settings.getAdvancedOptions_raw(power);
            setScrobblingOptionsRestEnabled(ao);

            chooser.setSummary(ao.getName(OptionsActivity.this));
            chooser.setValue(ao.toString());

            active_app.setChecked(settings.isActiveAppEnabled(power));
            scrobble.setChecked(settings.isScrobblingEnabled(power));
            np.setChecked(settings.isNowPlayingEnabled(power));

            AdvancedOptionsWhen aow = settings.getAdvancedOptionsWhen(power);
            when.setSummary(aow.getName(OptionsActivity.this));
            when.setValue(aow.toString());

            also_on_complete.setChecked(settings
                    .getAdvancedOptionsAlsoOnComplete(power));

            NetworkOptions no = settings.getNetworkOptions(power);
            net.setSummary(getString(R.string.advanced_options_net_summary)
                    .replace("%1", no.getName(OptionsActivity.this)));
            net.setValue(no.toString());

            roaming.setChecked(settings.getSubmitOnRoaming(power));
        }

        private void setScrobblingOptionsRestEnabled(AdvancedOptions ao) {
            active_app.setEnabled(ao == AdvancedOptions.CUSTOM);
            scrobble.setEnabled(ao == AdvancedOptions.CUSTOM);
            np.setEnabled(ao == AdvancedOptions.CUSTOM);
            when.setEnabled(ao == AdvancedOptions.CUSTOM);
            also_on_complete.setEnabled(ao == AdvancedOptions.CUSTOM);
            net.setEnabled(ao == AdvancedOptions.CUSTOM);
            roaming.setEnabled(ao == AdvancedOptions.CUSTOM);
        }

        private void createChooserPreference() {
            chooser = new ListPreference(OptionsActivity.this);
            category.addPreference(chooser);
            chooser.setTitle(R.string.options_title);

            AdvancedOptions[] scrobOpts = power.getApplicableOptions();
            // set the entries for mOptionsChooser
            CharSequence[] vals = new CharSequence[scrobOpts.length];
            for (int i = 0; i < scrobOpts.length; i++)
                vals[i] = scrobOpts[i].getName(OptionsActivity.this);
            chooser.setEntries(vals);

            // set the values for mOptionsChooser
            vals = new CharSequence[scrobOpts.length];
            for (int i = 0; i < scrobOpts.length; i++)
                vals[i] = scrobOpts[i].toString();
            chooser.setEntryValues(vals);

            chooser.setOnPreferenceChangeListener(mOnListPrefChange);
        }

        private void createActiveAppPreference() {
            active_app = new CheckBoxPreference(OptionsActivity.this);
            category.addPreference(active_app);
            active_app.setTitle(R.string.active_app);
            active_app.setSummary(R.string.active_app_summary);
        }

        private void createScrobbleEnablePreference() {
            scrobble = new CheckBoxPreference(OptionsActivity.this);
            category.addPreference(scrobble);
            scrobble.setTitle(R.string.scrobbling);
            scrobble.setSummaryOff(R.string.scrobbling_enable);
        }

        private void createNPEnablePreference() {
            np = new CheckBoxPreference(OptionsActivity.this);
            category.addPreference(np);
            np.setTitle(R.string.nowplaying);
            np.setSummaryOff(R.string.nowplaying_enable);
        }

        private void createWhenPreference() {
            when = new ListPreference(OptionsActivity.this);
            category.addPreference(when);
            when.setTitle(R.string.advanced_options_when_title);

            AdvancedOptionsWhen[] scrobOptsWhen = AdvancedOptionsWhen.values();
            CharSequence[] vals = new CharSequence[scrobOptsWhen.length];
            for (int i = 0; i < scrobOptsWhen.length; i++)
                vals[i] = scrobOptsWhen[i].getName(OptionsActivity.this);
            when.setEntries(vals);

            // set the values for mOptionsChooser
            vals = new CharSequence[scrobOptsWhen.length];
            for (int i = 0; i < scrobOptsWhen.length; i++)
                vals[i] = scrobOptsWhen[i].toString();
            when.setEntryValues(vals);

            when.setOnPreferenceChangeListener(mOnListPrefChange);
        }

        private void createAOCPreference() {
            also_on_complete = new CheckBoxPreference(OptionsActivity.this);
            category.addPreference(also_on_complete);
            also_on_complete
                    .setTitle(R.string.advanced_options_also_on_complete_title);
            also_on_complete
                    .setSummary(R.string.advanced_options_also_on_complete_summary);
        }

        private void createNetPreference() {
            net = new ListPreference(OptionsActivity.this);
            category.addPreference(net);
            net.setTitle(R.string.advanced_options_net_title);

            NetworkOptions[] nopps = NetworkOptions.values();

            CharSequence[] vals = new CharSequence[nopps.length];
            for (int i = 0; i < nopps.length; i++)
                vals[i] = nopps[i].getName(OptionsActivity.this);
            net.setEntries(vals);

            // set the values for mOptionsChooser
            vals = new CharSequence[nopps.length];
            for (int i = 0; i < nopps.length; i++)
                vals[i] = nopps[i].toString();
            net.setEntryValues(vals);

            net.setOnPreferenceChangeListener(mOnListPrefChange);
        }

        private void createRoamingPreference() {
            roaming = new CheckBoxPreference(OptionsActivity.this);
            category.addPreference(roaming);
            roaming.setTitle(R.string.advanced_options_net_roaming_title);
            roaming
                    .setSummaryOff(R.string.advanced_options_net_roaming_summary_off);
            roaming
                    .setSummaryOn(R.string.advanced_options_net_roaming_summary_on);
        }

        private OnPreferenceChangeListener mOnListPrefChange = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                if (!(newValue instanceof CharSequence)) {
                    Log.e(TAG, "Got weird newValue on options_prefs change: "
                            + newValue);
                    return false;
                }
                CharSequence newcs = (CharSequence) newValue;

                if (pref == chooser) {
                    AdvancedOptions so = AdvancedOptions.valueOf(newcs
                            .toString());
                    settings.setAdvancedOptions(power, so);
                } else if (pref == when) {
                    AdvancedOptionsWhen aow = AdvancedOptionsWhen.valueOf(newcs
                            .toString());
                    settings.setAdvancedOptionsWhen(power, aow);
                } else if (pref == net) {
                    NetworkOptions no = NetworkOptions
                            .valueOf(newcs.toString());
                    settings.setNetworkOptions(power, no);
                } else {
                    Log.e(TAG, "Got weird change for a list preference: "
                            + newValue);
                }

                OptionsActivity.this.update();
                return true;
            }
        };
    }
}
