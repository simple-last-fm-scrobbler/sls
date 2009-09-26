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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.ScrobblesDatabase.SortOrder;

public class ViewScrobbleCacheActivity extends ListActivity {
	private static final String TAG = "VSCacheActivity";

	private static final int MENU_SCROBBLE_NOW_ID = 0;
	private static final int MENU_CLEAR_CACHE_ID = 1;

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
		// fillData();
	}

	private void fillData() {

		scrobblesCursor = mDb.fetchTracksCursor(mNetApp, SortOrder.DESCENDING);
		startManagingCursor(scrobblesCursor);

		CursorAdapter adapter = new MyAdapter(this, scrobblesCursor);

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
			Util
					.doScrobbleIfPossible(this, mNetApp, scrobblesCursor
							.getCount());
			return true;
		case MENU_CLEAR_CACHE_ID:
			deleteAllSC();
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

						Log.d(TAG, "Will remove scrobble from cache: "
								+ mNetApp.getName() + ", " + id);
						mDb.deleteScrobble(mNetApp, id);
						mDb.cleanUpTracks();
						// need to refill data, otherwise the screen won't
						// update
						fillData();
					}
				});
	}

	private void deleteAllSC() {
		int numInCache = mDb.queryNumberOfScrobbles(mNetApp);
		if (numInCache > 0) {
			Util.confirmDialog(this, getString(R.string.confirm_delete_all_sc),
					R.string.clear_cache, R.string.cancel,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Will remove all scrobbles from cache: "
									+ mNetApp.getName());
							mDb.deleteAllScrobbles(mNetApp);
							mDb.cleanUpTracks();
							// need to refill data, otherwise the screen won't
							// update
							fillData();
						}
					});
		} else {
			Toast.makeText(this, getString(R.string.no_scrobbles_in_cache),
					Toast.LENGTH_LONG).show();
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
				fillData();
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
