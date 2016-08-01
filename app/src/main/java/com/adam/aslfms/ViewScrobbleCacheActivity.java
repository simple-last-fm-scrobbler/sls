/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/tgwizard/sls
 * <p>
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.adam.aslfms;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.enums.SortField;

public class ViewScrobbleCacheActivity extends AppCompatActivity {
    private static final String TAG = "VSCacheActivity";

    private AppSettings settings;

    private ScrobblesDatabase mDb;
    /**
     * mNetApp == null means that we should view cache for all netapps
     */
    private NetApp mNetApp;

    private Cursor mScrobblesCursor = null;

    private ListView mListView;
    private TextView mSortHeaderTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        settings = new AppSettings(this);

        Bundle extras = getIntent().getExtras();

        String name;
        if (extras.getBoolean("viewall")) {
            Log.d(TAG, "Will view cache for all netapps");
            mNetApp = null;
            name = getString(R.string.all_websites);
        } else {
            String snapp = extras.getString("netapp");
            if (snapp == null) {
                Log.e(TAG, "Got null snetapp");
                finish();
            }
            mNetApp = NetApp.valueOf(snapp);
            name = mNetApp.getName();
        }

        setTitle(getString(R.string.view_sc_title_for).replaceAll("%1", name));
        setContentView(R.layout.scrobble_cache_list);

        mListView = (ListView) findViewById(R.id.cache_list);
        mListView.setEmptyView(findViewById(R.id.empty_list));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id == -1) {
                    viewChangeSortOrder();
                } else {
                    viewSCDetails((int) id);
                }
            }
        });

        View header = getLayoutInflater().inflate(
                R.layout.scrobble_cache_header, null);
        mSortHeaderTextView = (TextView) header.findViewById(R.id.sort_title);
        mListView.addHeaderView(header);

        mDb = new ScrobblesDatabase(this);
        mDb.open();

        fillData();
        registerForContextMenu(mListView);
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

        if (mScrobblesCursor != null)
            mScrobblesCursor.requery();

        IntentFilter ifs = new IntentFilter();
        ifs.addAction(ScrobblingService.BROADCAST_ONSTATUSCHANGED);
        registerReceiver(onChange, ifs);
    }

    private void refillData() {
        if (mScrobblesCursor != null) {
            stopManagingCursor(mScrobblesCursor);
            mScrobblesCursor.close();
        }
        fillData();
    }

    private void fillData() {
        SortField sf = settings.getCacheSortField();
        if (mNetApp == null) {
            mScrobblesCursor = mDb.fetchAllTracksCursor(sf);
        } else {
            mScrobblesCursor = mDb.fetchTracksCursor(mNetApp, sf);
        }
        startManagingCursor(mScrobblesCursor);
        CursorAdapter adapter = new MyAdapter(this, mScrobblesCursor);
        mListView.setAdapter(adapter);

        mSortHeaderTextView.setText(getString(R.string.sc_sorted_by).replaceAll(
                "%1", sf.getName(this)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_scrobble_cache, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scrobble_now:
                if (mNetApp == null) {
                    Util.scrobbleAllIfPossible(this, mScrobblesCursor.getCount());
                } else {
                    Util.scrobbleIfPossible(this, mNetApp,
                            mScrobblesCursor.getCount());
                }
                return true;
            case R.id.menu_clear_cache:
                if (mNetApp == null) {
                    Util.deleteAllScrobblesFromAllCaches(this, mDb,
                            mScrobblesCursor);
                } else {
                    Util.deleteAllScrobblesFromCache(this, mDb, mNetApp,
                            mScrobblesCursor);
                }
                return true;
            case R.id.menu_change_sort_order:
                viewChangeSortOrder();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        if (info.id < 0)
            return;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_scrobble_cache_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_delete_scrobble:
                if (mNetApp == null) {
                    Util.deleteScrobbleFromAllCaches(this, mDb, mScrobblesCursor,
                            (int) info.id);
                } else {
                    Util.deleteScrobbleFromCache(this, mDb, mNetApp,
                            mScrobblesCursor, (int) info.id);
                }

                return true;
            case R.id.menu_show_scrobble_details:
                viewSCDetails((int) info.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void viewChangeSortOrder() {
        int selected = 0;
        SortField selSf = settings.getCacheSortField();
        SortField[] sfArr = SortField.values();
        for (int i = 0; i < sfArr.length; i++) {
            if (sfArr[i] == selSf) {
                selected = i;
                break;
            }
        }

        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this).setTitle(
                R.string.sc_sort_title).setSingleChoiceItems(
                SortField.toCharSequenceArray(this), selected,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SortField sf = SortField.values()[which];
                        settings.setCacheSortField(sf);
                        refillData();
                        mScrobblesCursor.requery();
                        dialog.dismiss();
                    }
                });

        adBuilder.show();
    }

    private void viewSCDetails(int id) {
        Track track = mDb.fetchTrack(id);
        if (track == null) {
            Log.e(TAG, "Got null track with id: " + id);
            return;
        }
        new ViewScrobbleInfoDialog(this, mDb, mNetApp, mScrobblesCursor, track).show();
    }

    private BroadcastReceiver onChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.e(TAG, "Got null extras from broadcast");
                return;
            }
            String snapp = extras.getString("netapp");
            if (snapp == null) {
                Log.e(TAG, "Got null snetapp from broadcast");
                return;
            }
            NetApp napp = NetApp.valueOf(snapp);
            if (mNetApp == null || napp == mNetApp) {
                mScrobblesCursor.requery();
            }
        }
    };

    private class MyAdapter extends CursorAdapter {
        public MyAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String track = cursor.getString(cursor.getColumnIndex("track"));
            TextView trackView = (TextView) view.findViewById(R.id.track);
            trackView.setText(track);

            int time = cursor.getInt(cursor.getColumnIndex("whenplayed"));
            String timeString = Util.timeFromUTCSecs(
                    ViewScrobbleCacheActivity.this, time);
            TextView timeView = (TextView) view.findViewById(R.id.when);
            timeView.setText(timeString);

            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            TextView artistView = (TextView) view.findViewById(R.id.artist);
            artistView.setText(artist);

            String album = cursor.getString(cursor.getColumnIndex("album"));
            TextView albumView = (TextView) view.findViewById(R.id.album);
            albumView.setText(album);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(
                    R.layout.scrobble_cache_row, parent, false);

        }
    }
}
