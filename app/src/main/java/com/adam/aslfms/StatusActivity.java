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

import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Util;

public class StatusActivity extends TabActivity {
	private TabHost mTabHost;

	private static final int MENU_SCROBBLE_NOW_ID = 0;
	private static final int MENU_VIEW_CACHE_ID = 1;
	private static final int MENU_RESET_STATS_ID = 2;

	private ScrobblesDatabase mDb;
	private AppSettings settings;

	int currTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_activity);

		mTabHost = getTabHost();

		for (NetApp napp : NetApp.values()) {
			Intent i = new Intent(this, StatusInfoNetApp.class);
			i.putExtra("netapp", napp.getIntentExtraValue());
			mTabHost.addTab(mTabHost.newTabSpec(napp.toString())
					.setIndicator(napp.getName()).setContent(i));
		}

		// switch to the first netapp that is authenticated
		AppSettings settings = new AppSettings(this);
		currTab = 0;
		NetApp[] napps = NetApp.values();
		for (int i = 0; i < napps.length; i++) {
			if (settings.isAuthenticated(napps[i])) {
				currTab = i;
				break;
			}
		}

		mTabHost.setCurrentTab(currTab);

		mDb = new ScrobblesDatabase(this);
		mDb.open();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, MENU_SCROBBLE_NOW_ID, 0, R.string.scrobble_now).setIcon(
				android.R.drawable.ic_menu_upload);
		menu.add(0, MENU_RESET_STATS_ID, 0, R.string.reset_stats).setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, MENU_VIEW_CACHE_ID, 0, R.string.view_sc).setIcon(
				android.R.drawable.ic_menu_view);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final StatusInfoNetApp currentActivity = (StatusInfoNetApp) getLocalActivityManager()
				.getCurrentActivity();
		final NetApp mNetApp = currentActivity.getNetApp();
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
					android.R.string.cancel,
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							settings = new AppSettings(StatusActivity.this);
							settings.clearSubmissionStats(mNetApp);
							currentActivity.fillData();
						}
					});
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
