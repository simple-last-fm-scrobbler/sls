package com.adam.aslfms;

import com.adam.aslfms.service.NetApp;

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
					"Status", getResources().getDrawable(napp.getLogoRes()))
					.setContent(i));
		}

		mTabHost.setCurrentTab(0);
	}
}
