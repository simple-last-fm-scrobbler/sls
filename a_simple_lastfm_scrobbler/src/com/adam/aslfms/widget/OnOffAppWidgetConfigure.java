package com.adam.aslfms.widget;

import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.adam.aslfms.R;
import com.adam.aslfms.UserCredActivity;
import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.Util;

public class OnOffAppWidgetConfigure extends PreferenceActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "OnOffAppWidgetConfigure";

	private static final String KEY_ALSO_DISABLE_NP = "also_disable_np";
	private static final String KEY_OK = "ok";

	private AppSettings settings;

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	CheckBoxPreference mAlsoDisableNP;
	Preference mOk;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the result to CANCELED. This will cause the widget host to cancel
		// out of the widget placement if they press the back button.
		setResult(RESULT_CANCELED);

		addPreferencesFromResource(R.xml.onoff_appwidget_configure_layout);

		settings = new AppSettings(this);

		mAlsoDisableNP = (CheckBoxPreference) findPreference(KEY_ALSO_DISABLE_NP);
		mOk = findPreference(KEY_OK);

		// Find the widget id from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen,
			Preference pref) {
		if (pref == mAlsoDisableNP) {
			return true;
		} else if (pref == mOk) {
			// save settings when clicking OK
			settings.setWidgetAlsoDisableNP(mAlsoDisableNP.isChecked());

			// Push widget update to surface with newly set prefix
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(this);
			OnOffAppWidgetProvider.updateAppWidget(this, appWidgetManager,
					mAppWidgetId);

			// Make sure we pass back the original appWidgetId
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
		}
		return super.onPreferenceTreeClick(prefScreen, pref);
	}

	@Override
	protected void onResume() {
		super.onResume();
		update();
	}

	private void update() {
		mAlsoDisableNP.setChecked(settings.getWidgetAlsoDisableNP());
	}

}
