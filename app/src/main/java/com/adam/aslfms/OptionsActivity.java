/**
 *  This file is part of Simple Last.fm Scrobbler.
 *
 *  Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms;

import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.enums.AdvancedOptions;
import com.adam.aslfms.util.enums.AdvancedOptionsWhen;
import com.adam.aslfms.util.enums.NetworkOptions;
import com.adam.aslfms.util.enums.PowerOptions;
import com.example.android.supportv7.app.AppCompatPreferenceActivity;

public class OptionsActivity extends AppCompatPreferenceActivity {
	private static final String TAG = "OptionsGeneralScreen";

	private static final String KEY_SCROBBLE_POINT = "scrobble_pointer";

	private static final String KEY_BATTERY = "ao_battery";
	private static final String KEY_PLUGGED = "ao_plugged";

	private AppSettings settings;

	private SeekBarPreference mScrobblePoint;

	private PowerSpecificPrefs mBatteryOptions;
	private PowerSpecificPrefs mPluggedOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.options);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		settings = new AppSettings(this);

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

		private ListPreference chooser;
		private CheckBoxPreference scrobble;
		private CheckBoxPreference np;
		private ListPreference when;
		private CheckBoxPreference also_on_complete;
		private ListPreference net;
		private CheckBoxPreference roaming;

		public void create() {
			createChooserPreference();
			createScrobbleEnablePreference();
			createNPEnablePreference();
			createWhenPreference();
			createAOCPreference();
			createNetPreference();
			createRoamingPreference();
		}

		public boolean onClick(Preference pref) {
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
			} else if (pref == roaming) {
				settings.setSubmitOnRoaming(power, roaming.isChecked());
				return true;
			}
			return false;
		}

		private void update() {
			AdvancedOptions ao = settings.getAdvancedOptions_raw(power);
			setScrobblingOptionsRestEnabled(ao);

			chooser.setSummary(ao.getName(OptionsActivity.this));
			chooser.setValue(ao.toString());

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
					Log.e(TAG, "Got weird newValue on options change: "
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
