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

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Status;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.AppSettingsEnums.SubmissionType;

public class StatusInfoNetApp extends ListActivity {

	private static final String TAG = "StatusInfoNetApp";

	private static final int MENU_SCROBBLE_NOW_ID = 0;
	private static final int MENU_VIEW_CACHE_ID = 1;
	private static final int MENU_RESET_STATS_ID = 2;

	private NetApp mNetApp;

	private AppSettings settings;
	private ScrobblesDatabase mDb;

	private int mProfilePageLinkPosition = -1;

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
		mDb.open();

		setContentView(R.layout.status_info_list);

		fillData();
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

		fillData();
	}

	private void fillData() {
		List<Pair> list = new ArrayList<Pair>();
		int numInCache = mDb.queryNumberOfScrobbles(mNetApp);

		// auth
		Pair auth = new Pair();
		if (settings.getAuthStatus(mNetApp) == Status.AUTHSTATUS_OK) {
			auth.setKey(getString(R.string.logged_in_just));
			auth.setValue(settings.getUsername(mNetApp));
		} else {
			auth.setKey(getString(R.string.not_logged_in));
			auth
					.setValue(Util.getStatusSummary(this, settings, mNetApp,
							false));
		}
		list.add(auth);

		// link to profile
		Pair prof_link = new Pair();
		prof_link.setKey(getString(R.string.profile_page));
		if (settings.getAuthStatus(mNetApp) == Status.AUTHSTATUS_OK) {
			prof_link.setValue(mNetApp.getProfileUrl(settings));
		} else {
			prof_link.setValue(getString(R.string.not_logged_in));
		}
		list.add(prof_link);
		mProfilePageLinkPosition = list.size() - 1;

		// scrobble
		Pair scrobble = new Pair();
		scrobble.setKey(getSubmissionStatusKey(SubmissionType.SCROBBLE));
		scrobble.setValue(getSubmissionStatusValue(SubmissionType.SCROBBLE));
		list.add(scrobble);

		// np
		Pair np = new Pair();
		np.setKey(getSubmissionStatusKey(SubmissionType.NP));
		np.setValue(getSubmissionStatusValue(SubmissionType.NP));
		list.add(np);

		// scrobbles in cache
		Pair cache = new Pair();
		cache.setKey(getString(R.string.scrobbles_cache_nonum));
		cache.setValue(Integer.toString(numInCache));
		list.add(cache);

		// scrobble stats
		Pair scstats = new Pair();
		scstats.setKey(getString(R.string.stats_scrobbles));
		scstats.setValue(Integer.toString(settings.getNumberOfSubmissions(
				mNetApp, SubmissionType.SCROBBLE)));
		list.add(scstats);

		// np stats
		Pair npstats = new Pair();
		npstats.setKey(getString(R.string.stats_nps));
		npstats.setValue(Integer.toString(settings.getNumberOfSubmissions(
				mNetApp, SubmissionType.NP)));
		list.add(npstats);

		ArrayAdapter<Pair> adapter = new MyArrayAdapter(this,
				R.layout.status_info_row, R.id.key, list);

		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_SCROBBLE_NOW_ID, 0, R.string.scrobble_now).setIcon(
				android.R.drawable.ic_menu_upload);
		menu.add(0, MENU_RESET_STATS_ID, 0, R.string.reset_stats).setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, MENU_VIEW_CACHE_ID, 0, R.string.view_sc).setIcon(
				android.R.drawable.ic_menu_view);

		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SCROBBLE_NOW_ID:
			int numInCache = mDb.queryNumberOfScrobbles(mNetApp);
			Util.scrobbleIfPossible(this, mNetApp, numInCache);
			return true;
		case MENU_VIEW_CACHE_ID:
			Intent j = new Intent(this, ViewScrobbleCacheActivity.class);
			j.putExtra("netapp", mNetApp.getIntentExtraValue());
			startActivity(j);
			return true;
		case MENU_RESET_STATS_ID:
			Util.confirmDialog(this, getString(R.string.confirm_stats_reset)
					.replaceAll("%1", mNetApp.getName()), R.string.reset,
					R.string.cancel,
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							settings.clearSubmissionStats(mNetApp);
							fillData();
						}
					});
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (position == mProfilePageLinkPosition
				&& settings.getAuthStatus(mNetApp) == Status.AUTHSTATUS_OK) {
			String url = mNetApp.getProfileUrl(settings);
			Log.d(TAG, "Clicked link to profile page, opening: " + url);
			Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(browser);
		}
	}

	private String getSubmissionStatusKey(SubmissionType stype) {
		if (settings.wasLastSubmissionSuccessful(mNetApp, stype)) {
			return sGetLastAt(stype);
		} else {
			return sGetLastFailAt(stype);
		}
	}

	private String getSubmissionStatusValue(SubmissionType stype) {
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

		return when + what;
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
				StatusInfoNetApp.this.fillData();
			}
		}
	};

	private static class Pair {
		private String key;
		private String value;

		private Pair() {
			super();
		}

		private Pair(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	private class MyArrayAdapter extends ArrayAdapter<Pair> {

		public MyArrayAdapter(Context context, int resource,
				int textViewResourceId, List<Pair> list) {
			super(context, resource, textViewResourceId, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(getContext()).inflate(
					R.layout.status_info_row, parent, false);

			Pair item = this.getItem(position);

			TextView keyView = (TextView) view.findViewById(R.id.key);
			keyView.setText(item.getKey());

			TextView valueView = (TextView) view.findViewById(R.id.value);
			valueView.setText(item.getValue());

			return view;
		}

	}
}
