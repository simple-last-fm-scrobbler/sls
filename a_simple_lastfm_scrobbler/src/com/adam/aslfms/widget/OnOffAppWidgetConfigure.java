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

package com.adam.aslfms.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.adam.aslfms.R;
import com.adam.aslfms.util.AppSettings;

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
