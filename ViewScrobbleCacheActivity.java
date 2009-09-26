package com.adam.aslfms;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.ScrobblesDatabase.SortOrder;

public class ViewScrobbleCacheActivity extends ListActivity {
	private static final String TAG = "VSCacheActivity";

	private static final int MENU_SCROBBLE_NOW_ID = 0;
	private static final int MENU_CLEAR_CACHE_ID = 1;

	private static final int CONTEXT_MENU_DETAILS_ID = 0;
	private static final int CONTEXT_MENU_DELETE_ID = 1;

	private ScrobblesDatabase mDb;
	/**
	 * mNetApp == null means that we should view cache for all netapps
	 */
	private NetApp mNetApp;

	private Cursor mScrobblesCursor = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		String name = "??????";
		if (extras.getBoolean("viewall") == true) {
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

		mDb = new ScrobblesDatabase(this);
		mDb.open();

		fillData();
		registerForContextMenu(getListView());
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
		registerReceiver(onChange, ifs);
	}

	private void fillData() {
		if (mNetApp == null) {
			mScrobblesCursor = mDb.fetchAllTracksCursor(SortOrder.DESCENDING);
		} else {
			mScrobblesCursor = mDb.fetchTracksCursor(mNetApp,
					SortOrder.DESCENDING);
		}
		startManagingCursor(mScrobblesCursor);
		CursorAdapter adapter = new MyAdapter(this, mScrobblesCursor);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_SCROBBLE_NOW_ID, 0, R.string.scrobble_now);
		menu.add(0, MENU_CLEAR_CACHE_ID, 0, R.string.clear_cache);
		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SCROBBLE_NOW_ID:
			if (mNetApp == null) {
				Util.scrobbleAllIfPossible(this, mScrobblesCursor.getCount());
			} else {
				Util.scrobbleIfPossible(this, mNetApp, mScrobblesCursor
						.getCount());
			}
			return true;
		case MENU_CLEAR_CACHE_ID:
			if (mNetApp == null) {
				Util.deleteAllScrobblesFromAllCaches(this, mDb,
						mScrobblesCursor);
			} else {
				Util.deleteAllScrobblesFromCache(this, mDb, mNetApp,
						mScrobblesCursor);
			}

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_MENU_DETAILS_ID, 0, R.string.view_sc_details);
		menu.add(0, CONTEXT_MENU_DELETE_ID, 0, R.string.delete_sc);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case CONTEXT_MENU_DELETE_ID:
			if (mNetApp == null) {
				Util.deleteScrobbleFromAllCaches(this, mDb, mScrobblesCursor,
						(int) info.id);
			} else {
				Util.deleteScrobbleFromCache(this, mDb, mNetApp,
						mScrobblesCursor, (int) info.id);
			}

			return true;
		case CONTEXT_MENU_DETAILS_ID:
			viewSCDetails((int) info.id);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		viewSCDetails((int) id);
	}

	private void viewSCDetails(int id) {
		Track track = mDb.fetchTrack(id);
		if (track == null) {
			Log.e(TAG, "Got null track with id: " + id);
			return;
		}
		new ViewScrobbleInfoDialog(this, mDb, mNetApp, mScrobblesCursor, track)
				.show();
	}

	private BroadcastReceiver onChange = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String snapp = intent.getExtras().getString("netapp");
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
			String track = cursor
					.getString(ScrobblesDatabase.INDEX_TRACK_TRACK);
			TextView trackView = (TextView) view.findViewById(R.id.track);
			trackView.setText(track);

			int time = cursor.getInt(ScrobblesDatabase.INDEX_TRACK_WHEN);
			String timeString = Util.timeFromUTCSecs(
					ViewScrobbleCacheActivity.this, time);
			TextView timeView = (TextView) view.findViewById(R.id.when);
			timeView.setText(timeString);

			String artist = cursor
					.getString(ScrobblesDatabase.INDEX_TRACK_ARTIST);
			TextView artistView = (TextView) view.findViewById(R.id.artist);
			artistView.setText(artist);

			String album = cursor
					.getString(ScrobblesDatabase.INDEX_TRACK_ALBUM);
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
