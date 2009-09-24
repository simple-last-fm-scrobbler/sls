/**
 *  This file is part of A Simple Last.fm Scrobbler.
 *
 *  A Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  A Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with A Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adam.aslfms.service.NetApp;

/**
 * 
 * @author tgwizard
 * 
 */
public class ScrobblesDatabase {

	private static final String TAG = "ScrobblesDatabase";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "data";
	private static final String TABLENAME_SCROBBLES = "scrobbles";
	private static final String TABLENAME_CORRNETAPP = "scrobbles_netapp";

	public static final String KEY_TRACK_ARTIST = "artist";
	public static final String KEY_TRACK_ALBUM = "album";
	public static final String KEY_TRACK_TRACK = "track";
	public static final String KEY_TRACK_WHEN = "whenplayed";

	private static final String DATABASE_CREATE_SCROBBLES = "create table scrobbles ("
			+ "_id integer primary key autoincrement, "
			+ "artist text not null, "
			+ "album text not null, "
			+ "track text not null, "
			+ "duration integer not null, "
			+ "whenplayed integer not null);";

	private static final String DATABASE_CREATE_CORRNETAPP = "create table scrobbles_netapp ("
			+ "netappid integer not null, "
			+ "trackid integer not null, "
			+ "primary key (netappid, trackid), "
			+ "foreign key (trackid) references scrobbles_netapp(_id) "
			+ "on delete cascade on update cascade)";

	private static final int DATABASE_VERSION = 3;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "create sql scrobbles: " + DATABASE_CREATE_SCROBBLES);
			Log.d(TAG, "create sql corrnetapp: " + DATABASE_CREATE_CORRNETAPP);
			db.execSQL(DATABASE_CREATE_SCROBBLES);
			db.execSQL(DATABASE_CREATE_CORRNETAPP);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_SCROBBLES);
			db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_CORRNETAPP);
			onCreate(db);
		}
	}

	public ScrobblesDatabase(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * 
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public void open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Return the new rowId for that scrobble, otherwise return a -1 to indicate
	 * failure.
	 * 
	 * @return rowId or -1 if failed
	 */
	public long insertTrack(Track track) {
		ContentValues initialValues = new ContentValues();
		initialValues.put("artist", track.getArtist().toString());
		initialValues.put("album", track.getAlbum().toString());
		initialValues.put("track", track.getTrack().toString());
		initialValues.put("duration", track.getDuration());
		initialValues.put("whenplayed", track.getWhen());

		return mDb.insert(TABLENAME_SCROBBLES, null, initialValues);
	}

	/**
	 * 
	 * @param napp
	 *            the NetApp which we'll wanna scrobble to
	 * @param trackid
	 *            the rowId of the track to scrobble, see {@link Track.getRowId}
	 * @return true if the insert succeeded, false otherwise
	 */
	public boolean insertScrobble(NetApp napp, long trackid) {
		ContentValues iVals = new ContentValues();
		iVals.put("netappid", napp.getValue());
		iVals.put("trackid", trackid);

		return mDb.insert(TABLENAME_CORRNETAPP, null, iVals) > 0;
	}

	public int deleteScrobble(NetApp napp, int trackId) {
		if (trackId == -1) {
			Log.e(TAG, "Trying to delete scrobble with trackId == -1");
			return -2;
		}
		return mDb.delete(TABLENAME_CORRNETAPP, "netappid = ? and trackid = ?",
				new String[] { "" + napp.getValue(), "" + trackId });
	}

	/**
	 * 
	 * @param napp
	 * @return the number of rows affected
	 */
	public int deleteAllScrobbles(NetApp napp) {
		return mDb.delete(TABLENAME_CORRNETAPP, "netappid = ?",
				new String[] { "" + napp.getValue() });
	}

	public boolean cleanUpTracks() {
		mDb.execSQL("delete from scrobbles where _id not in "
				+ "(select trackid as _id from scrobbles_netapp)");
		return true;
	}

	public Track[] fetchTracksArray(NetApp napp, int maxFetch) {
		Cursor c;
		// try {
		String sql = "select * from scrobbles, scrobbles_netapp "
				+ "where _id = trackid and netappid = " + napp.getValue();
		c = mDb.rawQuery(sql, null);
		/*
		 * } catch (SQLiteException e) { Log.e(TAG,
		 * "fetchTracksArray sql query failed"); Log.e(TAG, e.getMessage());
		 * return new Track[0]; }
		 */

		int count = c.getCount();
		if (count > maxFetch)
			count = maxFetch;
		c.moveToFirst();
		Track[] tracks = new Track[count];
		for (int i = 0; i < count; i++) {
			tracks[i] = Track.createTrackFromDb(c.getString(1), c.getString(2),
					c.getString(3), c.getInt(4), c.getLong(5), c.getInt(0));
			c.moveToNext();
		}
		c.close();
		return tracks;
	}

	public Cursor fetchTracksCursor(NetApp napp) {
		Cursor c;
		String sql = "select * from scrobbles, scrobbles_netapp "
				+ "where _id = trackid and netappid = " + napp.getValue();
		c = mDb.rawQuery(sql, null);

		return c;
	}

	public int queryNumberOfTracks() {
		Cursor c;
		c = mDb.rawQuery("select count(_id) from scrobbles", null);
		int count = c.getCount();
		if (count != 0) {
			c.moveToFirst();
			count = c.getInt(0);
		}
		c.close();
		return count;
	}

	public int queryNumberOfScrobbles(NetApp napp) {
		Cursor c;
		c = mDb.rawQuery(
				"select count(trackid) from scrobbles_netapp where netappid = "
						+ napp.getValue(), null);
		int count = c.getCount();
		if (count != 0) {
			c.moveToFirst();
			count = c.getInt(0);
		}
		c.close();
		return count;
	}

}
