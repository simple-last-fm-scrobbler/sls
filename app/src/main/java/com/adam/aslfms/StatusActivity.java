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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Util;

import java.util.ArrayList;
import java.util.List;

public class StatusActivity extends AppCompatActivity {

	private static final int MENU_SCROBBLE_NOW_ID = 0;
	private static final int MENU_VIEW_CACHE_ID = 1;
	private static final int MENU_RESET_STATS_ID = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
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

    private void setupViewPager(ViewPager viewPager) {
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());

        for (NetApp napp : NetApp.values()) {
            adapter.addFragment(StatusFragment.newInstance(napp.getValue()), napp.getName());
        }
        viewPager.setAdapter(adapter);
    }

    static class TabAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public TabAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
