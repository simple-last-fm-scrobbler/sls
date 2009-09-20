package com.adam.aslfms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.adam.aslfms.AppSettingsEnums.SubmissionType;
import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.Util;

public class StatusInfoNetApp extends Activity implements
		android.view.View.OnClickListener {

	public static final String PACKAGE_SCROBBLE_DROID = "net.jjc1138.android.scrobbler";

	private static final String TAG = "StatusInfoNetApp";

	private NetApp mNetApp;

	private AppSettings settings;
	private ScrobblesDatabase mDb;

	private TextView mAuthText;
	private TextView mScrobbleText;
	private TextView mNPText;
	private TextView mCacheText;
	private TextView mScrobbleStatsText;
	private TextView mNPStatsText;
	private TextView mIncompText;

	private Button mScrobbleNow;
	private Button mResetStats;

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
		mIncompText = (TextView) findViewById(R.id.status_incomp_warning);

		mScrobbleNow = (Button) findViewById(R.id.scrobble_now_button);
		mScrobbleNow.setOnClickListener(this);
		mResetStats = (Button) findViewById(R.id.reset_stats_button);
		mResetStats.setOnClickListener(this);
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
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		if (v == mScrobbleNow) {
			Log.d(TAG, "Will scrobble any tracks in local cache: "
					+ mNetApp.getName());
			Intent i = new Intent(ScrobblingService.ACTION_JUSTSCROBBLE);
			i.putExtra("netapp", mNetApp.getIntentExtraValue());
			startService(i);
		} else if (v == mResetStats) {
			Log.d(TAG, "Will clear submission stats for: " + mNetApp.getName());
			confirm(getString(R.string.confirm_stats_reset),
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							settings.clearSubmissionStats(mNetApp);
							update();
						}
					});

		} else {
			Log.e(TAG, "got weird click: " + v);
		}
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

		boolean canScrobbleNow = settings.getAuthStatus(mNetApp) != Status.AUTHSTATUS_NOAUTH
				&& numInCache > 0;
		mScrobbleNow.setEnabled(canScrobbleNow);

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

		// check for "incompatible" packages
		String incomp = null;
		// check for scrobbledroid
		if (Util.checkForInstalledApp(this, PACKAGE_SCROBBLE_DROID)) {
			incomp = getString(R.string.incompatability).replaceAll("%1",
					Util.getAppName(this, PACKAGE_SCROBBLE_DROID));
		}

		if (incomp == null) {
			mIncompText.setVisibility(View.GONE);
		} else {
			mIncompText.setTextColor(color);
			mIncompText.setText(incomp);
		}
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

	private void confirm(String s,
			android.content.DialogInterface.OnClickListener onPositive) {
		new AlertDialog.Builder(this).setTitle(R.string.are_you_sure)
				.setMessage(s).setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.yes, onPositive).setNegativeButton(
						R.string.no, null).show();
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
