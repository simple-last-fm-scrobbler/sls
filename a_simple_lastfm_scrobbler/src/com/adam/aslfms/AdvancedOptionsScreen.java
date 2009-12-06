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

import java.util.ArrayList;
import java.util.Arrays;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.AppSettingsEnums.AdvancedOptions;
import com.adam.aslfms.util.AppSettingsEnums.AdvancedOptionsWhen;
import com.adam.aslfms.util.AppSettingsEnums.PowerOptions;

public class AdvancedOptionsScreen extends PreferenceActivity {
	private static final String TAG = "AdvancedOptionsScreen";
	
	private static final String KEY_SCROBBLE_POINT = "scrobble_pointer";

	private static final String KEY_BATTERY_SCROBBLING = "toggle_battery_scrobbling";
	private static final String KEY_BATTERY_NP = "toggle_battery_np";
	private static final String KEY_BATTERY_CHOOSER = "ao_battery_chooser";
	private static final String KEY_BATTERY_WHEN = "ao_battery_when";
	private static final String KEY_BATTERY_AOC = "ao_battery_aoc";

	private static final String KEY_PLUGGED_SCROBBLING = "toggle_plugged_scrobbling";
	private static final String KEY_PLUGGED_NP = "toggle_plugged_np";
	private static final String KEY_PLUGGED_CHOOSER = "ao_plugged_chooser";
	private static final String KEY_PLUGGED_WHEN = "ao_plugged_when";
	private static final String KEY_PLUGGED_AOC = "ao_plugged_aoc";

	private AppSettings settings;

	private static class AOptionsPrefs {
		public CheckBoxPreference scrobble;
		public CheckBoxPreference np;
		public ListPreference chooser;
		public ListPreference when;
		public CheckBoxPreference also_on_complete;
	}

	private SeekBarPreference mScrobblePoint;
	private AOptionsPrefs mBatteryOptions;
	private AOptionsPrefs mPluggedOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.advanced_options);

		settings = new AppSettings(this);
		
		mScrobblePoint = (SeekBarPreference) findPreference(KEY_SCROBBLE_POINT);
		mScrobblePoint.setDefaults(settings.getScrobblePoint()-50, 50);
		mScrobblePoint.setSaver(new SeekBarPreference.Saver() {
			@Override
			public void save(int value) {
				settings.setScrobblePoint(value+50);
				mScrobblePoint.setDefaults(settings.getScrobblePoint()-50, 50);
				AdvancedOptionsScreen.this.update();
			}
		});

		mBatteryOptions = new AOptionsPrefs();

		mBatteryOptions.scrobble = (CheckBoxPreference) findPreference(KEY_BATTERY_SCROBBLING);
		mBatteryOptions.np = (CheckBoxPreference) findPreference(KEY_BATTERY_NP);
		mBatteryOptions.chooser = (ListPreference) findPreference(KEY_BATTERY_CHOOSER);
		mBatteryOptions.when = (ListPreference) findPreference(KEY_BATTERY_WHEN);
		mBatteryOptions.also_on_complete = (CheckBoxPreference) findPreference(KEY_BATTERY_AOC);

		init(mBatteryOptions, new ArrayList<AdvancedOptions>(Arrays.asList(AdvancedOptions.values())));

		mPluggedOptions = new AOptionsPrefs();

		mPluggedOptions.scrobble = (CheckBoxPreference) findPreference(KEY_PLUGGED_SCROBBLING);
		mPluggedOptions.np = (CheckBoxPreference) findPreference(KEY_PLUGGED_NP);
		mPluggedOptions.chooser = (ListPreference) findPreference(KEY_PLUGGED_CHOOSER);
		mPluggedOptions.when = (ListPreference) findPreference(KEY_PLUGGED_WHEN);
		mPluggedOptions.also_on_complete = (CheckBoxPreference) findPreference(KEY_PLUGGED_AOC);
		
		AdvancedOptions[] ar = AdvancedOptions.values();
		ArrayList<AdvancedOptions> arrr = new ArrayList<AdvancedOptions>();
		for (AdvancedOptions ao : ar)
			if (ao != AdvancedOptions.BATTERY_SAVING)
				arrr.add(ao);
		
		init(mPluggedOptions, arrr);
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

	private void init(AOptionsPrefs prefs, ArrayList<AdvancedOptions> scrobOpts) {
		// set the entries for mOptionsChooser
		CharSequence[] vals = new CharSequence[scrobOpts.size()];
		for (int i = 0; i < scrobOpts.size(); i++)
			vals[i] = scrobOpts.get(i).getName(this);
		prefs.chooser.setEntries(vals);

		// set the values for mOptionsChooser
		vals = new CharSequence[scrobOpts.size()];
		for (int i = 0; i < scrobOpts.size(); i++)
			vals[i] = scrobOpts.get(i).toString();
		prefs.chooser.setEntryValues(vals);

		AdvancedOptionsWhen[] scrobOptsWhen = AdvancedOptionsWhen.values();
		vals = new CharSequence[scrobOptsWhen.length];
		for (int i = 0; i < scrobOptsWhen.length; i++)
			vals[i] = scrobOptsWhen[i].getName(this);
		prefs.when.setEntries(vals);

		// set the values for mOptionsChooser
		vals = new CharSequence[scrobOptsWhen.length];
		for (int i = 0; i < scrobOptsWhen.length; i++)
			vals[i] = scrobOptsWhen[i].toString();
		prefs.when.setEntryValues(vals);

		prefs.chooser.setOnPreferenceChangeListener(mOnOptionsChange);
		prefs.when.setOnPreferenceChangeListener(mOnOptionsWhenChange);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {

		if (pref == mBatteryOptions.scrobble) {
			settings.setScrobblingEnabled(PowerOptions.BATTERY, mBatteryOptions.scrobble.isChecked());
			update();
			return true;
		} else if (pref == mPluggedOptions.scrobble) {
			settings.setScrobblingEnabled(PowerOptions.PLUGGED_IN, mPluggedOptions.scrobble.isChecked());
			update();
			return true;
		} else if (pref == mBatteryOptions.np) {
			settings.setNowPlayingEnabled(PowerOptions.BATTERY, mBatteryOptions.np.isChecked());
			update();
			return true;
		} else if (pref == mPluggedOptions.np) {
			settings.setNowPlayingEnabled(PowerOptions.PLUGGED_IN, mPluggedOptions.np.isChecked());
			update();
			return true;
		} else if (pref == mBatteryOptions.also_on_complete) {
			settings.setAdvancedOptionsAlsoOnComplete(PowerOptions.BATTERY,
					mBatteryOptions.also_on_complete.isChecked());
			update();
			return true;
		} else if (pref == mPluggedOptions.also_on_complete) {
			settings.setAdvancedOptionsAlsoOnComplete(PowerOptions.PLUGGED_IN,
					mPluggedOptions.also_on_complete.isChecked());
			update();
			return true;
		} else 

		return super.onPreferenceTreeClick(prefScreen, pref);
	}

	private void update() {
		Log.d(TAG, "updating...");
		String sp = Integer.toString(settings.getScrobblePoint());
		mScrobblePoint.setSummary(getString(R.string.scrobble_point_summary).replaceAll("%1", sp));
		updateInner(mBatteryOptions, PowerOptions.BATTERY);
		updateInner(mPluggedOptions, PowerOptions.PLUGGED_IN);
	}
	
	private void updateInner(AOptionsPrefs prefs, PowerOptions pow) {
		AdvancedOptions so = settings.getAdvancedOptions(pow);
		setScrobblingOptionsRestEnabled(prefs, so == AdvancedOptions.CUSTOM);
		
		prefs.scrobble.setChecked(settings.isScrobblingEnabled(pow));
		prefs.np.setChecked(settings.isNowPlayingEnabled(pow));
		
		prefs.chooser.setSummary(so.getName(this));
		prefs.when.setSummary(settings.getAdvancedOptionsWhen(pow).getName(
				this));
		prefs.also_on_complete.setChecked(settings.getAdvancedOptionsAlsoOnComplete(pow));

		prefs.chooser.setValue(settings.getAdvancedOptions(pow).toString());
		prefs.when.setValue(settings.getAdvancedOptionsWhen(pow).toString());
	}

	private void setScrobblingOptionsRestEnabled(AOptionsPrefs prefs, boolean enabled) {
		prefs.scrobble.setEnabled(enabled);
		prefs.np.setEnabled(enabled);
		prefs.when.setEnabled(enabled);
		prefs.also_on_complete.setEnabled(enabled);
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
			
			update();
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
}
