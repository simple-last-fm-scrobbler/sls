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
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.AppSettings;

public class StatusActivity extends TabActivity {
	private TabHost mTabHost;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_activity);

		mTabHost = getTabHost();

		for (NetApp napp : NetApp.values()) {
			Intent i = new Intent(this, StatusInfoNetApp.class);
			i.putExtra("netapp", napp.getIntentExtraValue());
			mTabHost.addTab(mTabHost.newTabSpec(napp.toString()).setIndicator(
				napp.getName()).setContent(i));
		}

		// switch to the first netapp that is authenticated
		AppSettings settings = new AppSettings(this);
		int curr = 0;
		NetApp[] napps = NetApp.values();
		for (int i = 0; i < napps.length; i++) {
			if (settings.isAuthenticated(napps[i])) {
				curr = i;
				break;
			}
		}

		mTabHost.setCurrentTab(curr);
	}
}
