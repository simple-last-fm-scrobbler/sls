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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Status;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.AppSettingsEnums.SubmissionType;

public class StatusInfoNetApp extends Activity {

	private static final String TAG = "StatusInfoNetApp";

	private static final int MENU_SCROBBLE_NOW_ID = 0;

	private static final int MENU_RESET_STATS_ID = 1;

	private NetApp mNetApp;

	private AppSettings settings;
	private ScrobblesDatabase mDb;

	private TextView mAuthText;
	private TextView mScrobbleText;
	private TextView mNPText;
	private TextView mCacheText;
	private TextView mScrobbleStatsText;
	private TextView mNPStatsText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String snapp = getIntent().getExtras().getString("netapp");
		if (snapp == null) {
			Log.e(TAG, "Got null snetapp");
			finish();
		}
		mNetApp = NetApp.valueOf(snapp);

		settings = new AppSettings(this);

		// TODO: remove
		mDb = new ScrobblesDatabase(this);
		try {
			mDb.open();
		} catch (SQLException e) {
			Log.e(TAG, "Cannot open database!");
			Log.e(TAG, e.getMessage());
			mDb = null;
		}

		setContentView(R.layout.status_info);

		mAuthText = (TextView) findViewById(R.id.status_auth);
		mScrobbleText = (TextView) findViewById(R.id.status_scrobbling);
		mNPText = (TextView) findViewById(R.id.status_np);
		mCacheText = (TextView) findViewById(R.id.scrobble_cache);
		mScrobbleStatsText = (TextView) findViewById(R.id.status_scrobble_stats);
		mNPStatsText = (TextView) findViewById(R.id.status_np_stats);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDb.close();
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(onChange);
	}

	@Override
	protected void onResume() {
		super.onResume();

		IntentFilter ifs = new IntentFilter();
		ifs.addAction(ScrobblingService.BROADCAST_ONSTATUSCHANGED);
		ifs.addAction(ScrobblingService.BROADCAST_ONAUTHCHANGED);
		registerReceiver(onChange, ifs);

		update();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);

		int numInCache = 0;
		if (mDb != null) {
			numInCache = mDb.queryNumberOfRows(mNetApp);
		}

		menu.add(0, MENU_SCROBBLE_NOW_ID, 0, R.string.scrobble_now).setEnabled(
				numInCache > 0);
		menu.add(0, MENU_RESET_STATS_ID, 0, R.string.reset_stats);
		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SCROBBLE_NOW_ID:
			Log.d(TAG, "Will scrobble any tracks in local cache: "
					+ mNetApp.getName());
			Intent i = new Intent(ScrobblingService.ACTION_JUSTSCROBBLE);
			i.putExtra("netapp", mNetApp.getIntentExtraValue());
			startService(i);
			return true;
		case MENU_RESET_STATS_ID:
			Util.confirmDialog(this, getString(R.string.confirm_stats_reset)
					.replaceAll("%1", mNetApp.getName()),
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							settings.clearSubmissionStats(mNetApp);
							update();
						}
					});
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void update() {

		int color = getResources().getColor(R.color.status_highlight);
		int numInCache = 0;
		if (mDb != null) {
			numInCache = mDb.queryNumberOfRows(mNetApp);
		}

		// authText
		if (settings.getAuthStatus(mNetApp) == Status.AUTHSTATUS_NOAUTH) {
			mAuthText.setText(R.string.everything_disabled);
			color = getResources().getColor(R.color.status_lowlight);
		} else {
			mAuthText.setText(mNetApp.getStatusSummary(this, settings));
		}

		// scrobbleText
		mScrobbleText.setTextColor(color);
		mScrobbleText.setText(getSubmissionStatus(SubmissionType.SCROBBLE));

		// npText
		mNPText.setTextColor(color);
		mNPText.setText(getSubmissionStatus(SubmissionType.NP));

		// scrobbles in cache
		mCacheText.setTextColor(color);
		if (mDb != null) {
			mCacheText.setText(getString(R.string.scrobbles_cache).replace(
					"%1", Integer.toString(numInCache)));
		} else {
			mCacheText.setText(getString(R.string.scrobbles_cache).replace(
					"%1", getString(R.string.db_error)));
		}

		// statsText
		mScrobbleStatsText.setTextColor(color);
		mScrobbleStatsText.setText(getString(R.string.stats_scrobbles)
				+ " "
				+ settings.getNumberOfSubmissions(mNetApp,
						SubmissionType.SCROBBLE));

		mNPStatsText.setTextColor(color);
		mNPStatsText.setText(getString(R.string.stats_nps) + " "
				+ settings.getNumberOfSubmissions(mNetApp, SubmissionType.NP));
	}

	private String getSubmissionStatus(SubmissionType stype) {
		if (!settings.isSubmissionsEnabled(stype)) {
			return sGetDisabled(stype);
		} else {
			long time = settings.getLastSubmissionTime(mNetApp, stype);
			String when;
			String what;
			if (time == -1) {
				when = getString(R.string.never);
				what = "";
			} else {
				when = Util.timeFromLocalMillis(this, time);
				what = "\n" + settings.getLastSubmissionInfo(mNetApp, stype);
			}

			String succ = "";
			if (settings.wasLastSubmissionSuccessful(mNetApp, stype)) {
				succ = sGetLastAt(stype);
			} else {
				succ = sGetLastFailAt(stype);
			}
			return succ + " " + when + what;
		}
	}

	private String sGetDisabled(SubmissionType stype) {
		if (stype == SubmissionType.SCROBBLE) {
			return getString(R.string.scrobbling_disabled);
		} else {
			return getString(R.string.nowplaying_disabled);
		}
	}

	private String sGetLastAt(SubmissionType stype) {
		if (stype == SubmissionType.SCROBBLE) {
			return getString(R.string.scrobble_last_at);
		} else {
			return getString(R.string.nowplaying_last_at);
		}
	}

	private String sGetLastFailAt(SubmissionType stype) {
		if (stype == SubmissionType.SCROBBLE) {
			return getString(R.string.scrobble_last_fail_at);
		} else {
			return getString(R.string.nowplaying_last_fail_at);
		}
	}

	private BroadcastReceiver onChange = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String snapp = getIntent().getExtras().getString("netapp");
			if (snapp == null) {
				Log.e(TAG, "Got null snetapp from broadcast");
				return;
			}
			NetApp napp = NetApp.valueOf(snapp);
			if (napp == mNetApp) {
				StatusInfoNetApp.this.update();
			}
		}
	};
}
