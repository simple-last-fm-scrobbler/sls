package com.adam.aslfms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.Toast;

import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.AppSettingsEnums.AdvancedOptions;
import com.adam.aslfms.util.AppSettingsEnums.AdvancedOptionsWhen;

public class AdvancedOptionsScreen extends PreferenceActivity {
	private static final String TAG = "AdvancedOptionsScreen";

	private static final String KEY_ADVANCED_OPTIONS_CHOOSER = "advanced_options_chooser";
	private static final String KEY_ADVANCED_OPTIONS_WHEN = "advanced_options_when";
	private static final String KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE = "advanced_options_also_on_complete";

	private static final String KEY_SCROBBLE_ALL_NOW = "scrobble_all_now";

	private AppSettings settings;
	private ScrobblesDatabase mDb;

	private ListPreference mAOptionsChooser;
	private ListPreference mAOptionsWhen;
	private CheckBoxPreference mAOptionsAlsoOnComplete;
	private Preference mScrobbleAllNow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.advanced_options);

		settings = new AppSettings(this);

		mDb = new ScrobblesDatabase(this);
		try {
			mDb.open();
		} catch (SQLException e) {
			Log.e(TAG, "Cannot open database!");
			Log.e(TAG, e.getMessage());
			mDb = null;
		}

		mAOptionsChooser = (ListPreference) findPreference(KEY_ADVANCED_OPTIONS_CHOOSER);
		mAOptionsWhen = (ListPreference) findPreference(KEY_ADVANCED_OPTIONS_WHEN);
		mAOptionsAlsoOnComplete = (CheckBoxPreference) findPreference(KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE);

		init();

		mScrobbleAllNow = findPreference(KEY_SCROBBLE_ALL_NOW);

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
	}

	private void init() {
		// set the entries for mOptionsChooser
		AdvancedOptions[] scrobOpts = AdvancedOptions.values();
		CharSequence[] vals = new CharSequence[scrobOpts.length];
		for (int i = 0; i < scrobOpts.length; i++)
			vals[i] = scrobOpts[i].getName(this);
		mAOptionsChooser.setEntries(vals);

		// set the values for mOptionsChooser
		vals = new CharSequence[scrobOpts.length];
		for (int i = 0; i < scrobOpts.length; i++)
			vals[i] = scrobOpts[i].toString();
		mAOptionsChooser.setEntryValues(vals);

		AdvancedOptionsWhen[] scrobOptsWhen = AdvancedOptionsWhen.values();
		vals = new CharSequence[scrobOptsWhen.length];
		for (int i = 0; i < scrobOptsWhen.length; i++)
			vals[i] = scrobOptsWhen[i].getName(this);
		mAOptionsWhen.setEntries(vals);

		// set the values for mOptionsChooser
		vals = new CharSequence[scrobOptsWhen.length];
		for (int i = 0; i < scrobOptsWhen.length; i++)
			vals[i] = scrobOptsWhen[i].toString();
		mAOptionsWhen.setEntryValues(vals);

		mAOptionsChooser.setOnPreferenceChangeListener(mOnOptionsChange);
		mAOptionsWhen.setOnPreferenceChangeListener(mOnOptionsWhenChange);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {

		if (pref == mAOptionsAlsoOnComplete) {
			settings.setAdvancedOptionsAlsoOnComplete(mAOptionsAlsoOnComplete
					.isChecked());
			update();
			return true;
		} else if (pref == mScrobbleAllNow) {
			Intent service = new Intent(ScrobblingService.ACTION_JUSTSCROBBLE);
			service.putExtra("scrobbleall", true);
			startService(service);
			return true;
		}

		return super.onPreferenceTreeClick(prefScreen, pref);
	}

	private void update() {
		AdvancedOptions so = settings.getAdvancedOptions();
		setScrobblingOptionsRestEnabled(so == AdvancedOptions.CUSTOM);

		mAOptionsChooser.setSummary(so.getName(this));
		mAOptionsWhen.setSummary(settings.getAdvancedOptionsWhen()
				.getName(this));
		mAOptionsAlsoOnComplete.setChecked(settings
				.getAdvancedOptionsAlsoOnComplete());

		mAOptionsChooser.setValue(settings.getAdvancedOptions().toString());
		mAOptionsWhen.setValue(settings.getAdvancedOptionsWhen().toString());

		int numCache = mDb.queryNumberOfAllRows();
		mScrobbleAllNow.setSummary(getString(R.string.scrobbles_cache).replace(
				"%1", Integer.toString(numCache)));
		mScrobbleAllNow.setEnabled(numCache > 0);
	}

	private void setScrobblingOptionsRestEnabled(boolean enabled) {
		mAOptionsWhen.setEnabled(enabled);
		mAOptionsAlsoOnComplete.setEnabled(enabled);
	}

	private OnPreferenceChangeListener mOnOptionsChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (!(newValue instanceof CharSequence)) {
				Log.e(TAG, "Got weird newValue on options change: " + newValue);
				return false;
			}
			CharSequence newcs = (CharSequence) newValue;
			AdvancedOptions so = AdvancedOptions.valueOf(newcs.toString());
			settings.setAdvancedOptions(so);

			if (so == AdvancedOptions.BATTERY_SAVING) {
				Toast.makeText(AdvancedOptionsScreen.this,
						getString(R.string.should_disable_np),
						Toast.LENGTH_LONG).show();
			}
			update();
			return true;
		}
	};
	private OnPreferenceChangeListener mOnOptionsWhenChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (!(newValue instanceof CharSequence)) {
				Log.e(TAG, "Got weird newValue on options when change: "
						+ newValue);
				return false;
			}
			CharSequence newcs = (CharSequence) newValue;
			AdvancedOptionsWhen sow = AdvancedOptionsWhen.valueOf(newcs
					.toString());
			settings.setAdvancedOptionsWhen(sow);
			update();
			return true;
		}
	};

	private BroadcastReceiver onStatusChange = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			AdvancedOptionsScreen.this.update();
		}
	};
}
