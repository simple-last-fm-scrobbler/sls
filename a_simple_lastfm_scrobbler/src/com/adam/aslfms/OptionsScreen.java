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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.AppSettingsEnums.AdvancedOptions;
import com.adam.aslfms.util.AppSettingsEnums.AdvancedOptionsWhen;
import com.adam.aslfms.util.AppSettingsEnums.NetworkOptions;
import com.adam.aslfms.util.AppSettingsEnums.PowerOptions;

public class OptionsScreen extends PreferenceActivity {
	private static final String TAG = "OptionsGeneralScreen";

	private static final String KEY_SCROBBLE_POINT = "scrobble_pointer";

	private static final String KEY_BATTERY = "ao_battery";
	private static final String KEY_PLUGGED = "ao_plugged";

	private AppSettings settings;

	private class PowerSpecificPrefs {

		public PowerSpecificPrefs(PowerOptions power,
				PreferenceCategory category) {
			super();
			this.power = power;
			this.category = category;
		}

		public PowerOptions power;
		public PreferenceCategory category;

		public ListPreference chooser;
		public CheckBoxPreference scrobble;
		public CheckBoxPreference np;
		public ListPreference when;
		public CheckBoxPreference also_on_complete;
		public ListPreference net;

		public boolean update(Preference pref) {
			if (pref == scrobble) {
				settings.setScrobblingEnabled(power, scrobble.isChecked());
				return true;
			} else if (pref == np) {
				settings.setNowPlayingEnabled(power, np.isChecked());
				return true;
			} else if (pref == also_on_complete) {
				settings.setAdvancedOptionsAlsoOnComplete(power,
						also_on_complete.isChecked());
				return true;
			}
			return false;
		}

	}

	private SeekBarPreference mScrobblePoint;

	private PowerSpecificPrefs mBatteryOptions;
	private PowerSpecificPrefs mPluggedOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.options);

		settings = new AppSettings(this);

		mScrobblePoint = (SeekBarPreference) findPreference(KEY_SCROBBLE_POINT);
		mScrobblePoint.setDefaults(settings.getScrobblePoint() - 50, 50);
		mScrobblePoint.setSaver(new SeekBarPreference.Saver() {
			@Override
			public void save(int value) {
				settings.setScrobblePoint(value + 50);
				mScrobblePoint
						.setDefaults(settings.getScrobblePoint() - 50, 50);
				OptionsScreen.this.update();
			}
		});

		mBatteryOptions = new PowerSpecificPrefs(PowerOptions.BATTERY,
				(PreferenceCategory) findPreference(KEY_BATTERY));
		createChooserPreference(mBatteryOptions);
		createScrobbleEnablePreference(mBatteryOptions);
		createNPEnablePreference(mBatteryOptions);
		createWhenPreference(mBatteryOptions);
		createAOCPreference(mBatteryOptions);
		createNetPreference(mBatteryOptions);

		mPluggedOptions = new PowerSpecificPrefs(PowerOptions.PLUGGED_IN,
				(PreferenceCategory) findPreference(KEY_PLUGGED));
		createChooserPreference(mPluggedOptions);
		createScrobbleEnablePreference(mPluggedOptions);
		createNPEnablePreference(mPluggedOptions);
		createWhenPreference(mPluggedOptions);
		createAOCPreference(mPluggedOptions);
		createNetPreference(mPluggedOptions);
	}

	public void createChooserPreference(PowerSpecificPrefs prefs) {
		prefs.chooser = new ListPreference(this);
		prefs.category.addPreference(prefs.chooser);
		prefs.chooser.setTitle(R.string.options_title);

		AdvancedOptions[] scrobOpts = prefs.power.getApplicableOptions();
		// set the entries for mOptionsChooser
		CharSequence[] vals = new CharSequence[scrobOpts.length];
		for (int i = 0; i < scrobOpts.length; i++)
			vals[i] = scrobOpts[i].getName(this);
		prefs.chooser.setEntries(vals);

		// set the values for mOptionsChooser
		vals = new CharSequence[scrobOpts.length];
		for (int i = 0; i < scrobOpts.length; i++)
			vals[i] = scrobOpts[i].toString();
		prefs.chooser.setEntryValues(vals);

		prefs.chooser.setOnPreferenceChangeListener(mOnOptionsChange);
	}

	public void createScrobbleEnablePreference(PowerSpecificPrefs prefs) {
		prefs.scrobble = new CheckBoxPreference(this);
		prefs.category.addPreference(prefs.scrobble);
		prefs.scrobble.setTitle(R.string.scrobbling);
		prefs.scrobble.setSummaryOff(R.string.scrobbling_enable);
	}

	public void createNPEnablePreference(PowerSpecificPrefs prefs) {
		prefs.np = new CheckBoxPreference(this);
		prefs.category.addPreference(prefs.np);
		prefs.np.setTitle(R.string.nowplaying);
		prefs.np.setSummaryOff(R.string.nowplaying_enable);
	}

	public void createWhenPreference(PowerSpecificPrefs prefs) {
		prefs.when = new ListPreference(this);
		prefs.category.addPreference(prefs.when);
		prefs.when.setTitle(R.string.advanced_options_when_title);

		AdvancedOptionsWhen[] scrobOptsWhen = AdvancedOptionsWhen.values();
		CharSequence[] vals = new CharSequence[scrobOptsWhen.length];
		for (int i = 0; i < scrobOptsWhen.length; i++)
			vals[i] = scrobOptsWhen[i].getName(this);
		prefs.when.setEntries(vals);

		// set the values for mOptionsChooser
		vals = new CharSequence[scrobOptsWhen.length];
		for (int i = 0; i < scrobOptsWhen.length; i++)
			vals[i] = scrobOptsWhen[i].toString();
		prefs.when.setEntryValues(vals);

		prefs.when.setOnPreferenceChangeListener(mOnOptionsWhenChange);
	}

	public void createAOCPreference(PowerSpecificPrefs prefs) {
		prefs.also_on_complete = new CheckBoxPreference(this);
		prefs.category.addPreference(prefs.also_on_complete);
		prefs.also_on_complete
				.setTitle(R.string.advanced_options_also_on_complete_title);
		prefs.also_on_complete
				.setSummary(R.string.advanced_options_also_on_complete_summary);
	}

	public void createNetPreference(PowerSpecificPrefs prefs) {
		prefs.net = new ListPreference(this);
		prefs.category.addPreference(prefs.net);
		prefs.net.setTitle(R.string.advanced_options_net_title);

		NetworkOptions[] nopps = NetworkOptions.values();

		CharSequence[] vals = new CharSequence[nopps.length];
		for (int i = 0; i < nopps.length; i++)
			vals[i] = nopps[i].getName(this);
		prefs.net.setEntries(vals);

		// set the values for mOptionsChooser
		vals = new CharSequence[nopps.length];
		for (int i = 0; i < nopps.length; i++)
			vals[i] = nopps[i].toString();
		prefs.net.setEntryValues(vals);

		prefs.net.setOnPreferenceChangeListener(mOnNetOptionsChange);

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
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {

		if (mBatteryOptions.update(pref)) {
			update();
			return true;
		} else if (mPluggedOptions.update(pref)) {
			update();
			return true;
		}

		return super.onPreferenceTreeClick(prefScreen, pref);
	}

	private void update() {
		Log.d(TAG, "updating...");
		String sp = Integer.toString(settings.getScrobblePoint());
		mScrobblePoint.setSummary(getString(R.string.scrobble_point_summary)
				.replaceAll("%1", sp));
		updateInner(mBatteryOptions);
		updateInner(mPluggedOptions);
	}

	private void updateInner(PowerSpecificPrefs prefs) {
		AdvancedOptions ao = settings.getAdvancedOptions_raw(prefs.power);
		setScrobblingOptionsRestEnabled(prefs, ao);

		prefs.chooser.setSummary(ao.getName(this));
		prefs.chooser.setValue(ao.toString());

		prefs.scrobble.setChecked(settings.isScrobblingEnabled(prefs.power));
		prefs.np.setChecked(settings.isNowPlayingEnabled(prefs.power));

		prefs.when.setSummary(settings.getAdvancedOptionsWhen(prefs.power)
				.getName(this));
		prefs.when.setValue(settings.getAdvancedOptionsWhen(prefs.power)
				.toString());

		prefs.also_on_complete.setChecked(settings
				.getAdvancedOptionsAlsoOnComplete(prefs.power));

		NetworkOptions no = settings.getNetworkOptions(prefs.power);
		prefs.net.setSummary(getString(R.string.advanced_options_net_summary)
				.replace("%1", no.getName(this)));
		prefs.net.setValue(no.toString());
	}

	private void setScrobblingOptionsRestEnabled(PowerSpecificPrefs prefs,
			AdvancedOptions ao) {
		prefs.scrobble.setEnabled(ao == AdvancedOptions.CUSTOM);
		prefs.np.setEnabled(ao == AdvancedOptions.CUSTOM);
		prefs.when.setEnabled(ao == AdvancedOptions.CUSTOM);
		prefs.also_on_complete.setEnabled(ao == AdvancedOptions.CUSTOM);
		prefs.net.setEnabled(ao == AdvancedOptions.CUSTOM);
	}

	private OnPreferenceChangeListener mOnOptionsChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference pref, Object newValue) {
			if (!(newValue instanceof CharSequence)) {
				Log.e(TAG, "Got weird newValue on options change: " + newValue);
				return false;
			}
			CharSequence newcs = (CharSequence) newValue;
			AdvancedOptions so = AdvancedOptions.valueOf(newcs.toString());

			PowerOptions pow = PowerOptions.BATTERY;
			if (pref == mPluggedOptions.chooser)
				pow = PowerOptions.PLUGGED_IN;

			settings.setAdvancedOptions(pow, so);

			OptionsScreen.this.update();
			return true;
		}
	};

	private OnPreferenceChangeListener mOnOptionsWhenChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference pref, Object newValue) {
			if (!(newValue instanceof CharSequence)) {
				Log.e(TAG, "Got weird newValue on options when change: "
						+ newValue);
				return false;
			}
			CharSequence newcs = (CharSequence) newValue;
			AdvancedOptionsWhen sow = AdvancedOptionsWhen.valueOf(newcs
					.toString());

			PowerOptions pow = PowerOptions.BATTERY;
			if (pref == mPluggedOptions.when)
				pow = PowerOptions.PLUGGED_IN;

			settings.setAdvancedOptionsWhen(pow, sow);
			update();
			return true;
		}
	};

	private OnPreferenceChangeListener mOnNetOptionsChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference pref, Object newValue) {
			if (!(newValue instanceof CharSequence)) {
				Log.e(TAG, "Got weird newValue on options when change: "
						+ newValue);
				return false;
			}
			CharSequence newcs = (CharSequence) newValue;
			NetworkOptions no = NetworkOptions.valueOf(newcs.toString());

			PowerOptions pow = PowerOptions.BATTERY;
			if (pref == mPluggedOptions.net)
				pow = PowerOptions.PLUGGED_IN;

			settings.setNetworkOptions(pow, no);
			update();
			return true;
		}
	};
}
