package com.adam.aslfms;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Util;

public class ViewScrobbleCacheActivity extends ListActivity {
	private static final String TAG = "VSCacheActivity";

	private static final int MENU_SCROBBLE_NOW_ID = 0;

	private static final int CONTEXT_MENU_DELETE_ID = 0;

	private ScrobblesDatabase mDb;
	private NetApp mNetApp;

	private Cursor scrobblesCursor;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String snapp = getIntent().getExtras().getString("netapp");
		if (snapp == null) {
			Log.e(TAG, "Got null snetapp");
			finish();
		}
		mNetApp = NetApp.valueOf(snapp);

		setTitle(getString(R.string.view_sc_title_for).replaceAll("%1",
				mNetApp.getName()));
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

		// TODO: see if this is necessary
		//fillData();
	}

	private void fillData() {

		scrobblesCursor = mDb.fetchTracksCursor(mNetApp);
		startManagingCursor(scrobblesCursor);

		String[] from = new String[] { ScrobblesDatabase.KEY_TRACK_TRACK,
				ScrobblesDatabase.KEY_TRACK_ARTIST,
				ScrobblesDatabase.KEY_TRACK_ALBUM,
				ScrobblesDatabase.KEY_TRACK_WHEN };

		int[] to = new int[] { R.id.track, R.id.artist, R.id.album, R.id.when };

		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
				R.layout.scrobble_cache_row, scrobblesCursor, from, to);

		listAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				// we need to convert the timestamp from seconds since epoch
				// start to a more human-readable format
				if (cursor.getColumnName(columnIndex).equals(
						ScrobblesDatabase.KEY_TRACK_WHEN)) {
					TextView tv = (TextView) view;
					int time = cursor.getInt(columnIndex);
					String ts = Util.timeFromUTCSecs(
							ViewScrobbleCacheActivity.this, time);
					tv.setText(ts);
					return true;
				}
				// all other columns should be set by SimpleCursorAdapter, so
				// return false
				return false;
			}
		});
		setListAdapter(listAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_SCROBBLE_NOW_ID, 0, R.string.scrobble_now);
		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SCROBBLE_NOW_ID:
			Util.doScrobbleIfPossible(this, mNetApp, scrobblesCursor.getCount());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_MENU_DELETE_ID, 0, R.string.delete_sc);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_MENU_DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			deleteSC((int) info.id);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position,
			final long id) {
		super.onListItemClick(l, v, position, id);
		Log.d(TAG, "onListItemClick");
		deleteSC((int) id);
	}

	private void deleteSC(final int id) {
		Util.confirmDialog(this, getString(R.string.confirm_delete_sc)
				.replaceAll("%1", mNetApp.getName()), R.string.remove,
				R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						NetApp napp = mNetApp;
						Log.d(TAG, "Will remove scrobble from cache: "
								+ napp.getName() + ", " + id);
						mDb.deleteScrobble(napp, id);
						mDb.cleanUpTracks();
						// need to refill data, otherwise the screen won't
						// update
						fillData();
					}
				});
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
				fillData();
			}
		}
	};
}
