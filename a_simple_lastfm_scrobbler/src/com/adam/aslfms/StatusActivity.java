package com.adam.aslfms;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.AppSettings;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

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
					getString(R.string.status), getResources().getDrawable(napp.getLogoRes()))
					.setContent(i));
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
