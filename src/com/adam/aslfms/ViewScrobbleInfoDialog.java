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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

public class ViewScrobbleInfoDialog {
	@SuppressWarnings("unused")
	private static final String TAG = "WhatsNewDialog";
	private final Context mCtx;
	private final ScrobblesDatabase mDb;
	private final Cursor mParentCursor;
	private final Track mTrack;

	private final NetApp mNetApp;
	private NetApp[] mNetApps;

	public ViewScrobbleInfoDialog(Context mCtx, ScrobblesDatabase mDb,
		NetApp mNetApp, Cursor mParentCursor, Track mTrack) {
		super();
		this.mCtx = mCtx;
		this.mDb = mDb;
		this.mNetApp = mNetApp;
		this.mParentCursor = mParentCursor;
		this.mTrack = mTrack;

		if (mNetApp == null) {
			mNetApps = mDb.fetchNetAppsForScrobble(mTrack.getRowId());
		} else {
			mNetApps = null;
		}
	}

	public void show() {
		ListAdapter adapter = fillData();

		AlertDialog.Builder adBuilder = new AlertDialog.Builder(mCtx).setTitle(
			R.string.track_info).setIcon(android.R.drawable.ic_dialog_info).setAdapter(
			adapter, null).setPositiveButton(R.string.remove,
			new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mNetApp == null) {
						Util.deleteScrobbleFromAllCaches(mCtx, mDb,
							mParentCursor, mTrack.getRowId());
					} else {
						Util.deleteScrobbleFromCache(mCtx, mDb, mNetApp,
							mParentCursor, mTrack.getRowId());
					}

				}
			}).setNegativeButton(R.string.close, null);

		adBuilder.show();
	}

	private ListAdapter fillData() {
		List<String> list = new ArrayList<String>();

		String time = Util.timeFromUTCSecs(mCtx, mTrack.getWhen());
		list.add(time);
		list.add(mTrack.getMusicAPI().getName());
		list.add(mTrack.getTrack());
		list.add(mTrack.getArtist());
		list.add(mTrack.getAlbum());

		if (mNetApps != null) {
			StringBuilder sb = new StringBuilder();
			for (NetApp napp : mNetApps) {
				sb.append(napp.getName());
				sb.append(", ");
			}
			sb.setLength(sb.length() - 2);
			list.add(sb.toString());
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mCtx, R.layout.scrobble_info_row, R.id.text, list);
		return adapter;
	}
}
