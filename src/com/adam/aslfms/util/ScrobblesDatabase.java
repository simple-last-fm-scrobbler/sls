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

package com.adam.aslfms.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adam.aslfms.receiver.MusicAPI;
import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.enums.SortField;

/**
 * 
 * @author tgwizard
 * @since 0.9
 */
public class ScrobblesDatabase {

	private static final String TAG = "ScrobblesDatabase";

	private SQLiteDatabase mDb;

	private final Context mCtx;

	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 6;

	private static final String TABLENAME_SCROBBLES = "scrobbles";
	private static final String TABLENAME_CORRNETAPP = "scrobbles_netapp";

	private static final String DATABASE_CREATE_SCROBBLES = "create table scrobbles ("
			+ "_id integer primary key autoincrement, " //
			+ "musicapp integer not null, " //
			+ "artist text not null, " //
			+ "album text not null, " //
			+ "track text not null, " //
			+ "tracknr text not null, " //
			+ "mbid text not null, " //
			+ "source text not null, " //
			+ "duration integer not null, " //
			+ "whenplayed integer not null," //
			+ "rating text not null);";

	private static final String DATABASE_CREATE_CORRNETAPP = "create table scrobbles_netapp ("
			+ "netappid integer not null, "
			+ "trackid integer not null, "
			+ "primary key (netappid, trackid), "
			+ "foreign key (trackid) references scrobbles_netapp(_id) "
			+ "on delete cascade on update cascade)";

	private static class DatabaseHelper extends SQLiteOpenHelper {

	    private DatabaseHelper(Context _context) {
	        super(_context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

		private static class DatabaseConnection {
			public Long connection_count = Long.valueOf(0);
			public SQLiteDatabase db = null;
			DatabaseConnection(){
				connection_count = Long.valueOf(0);
				db = null;
			}
		}
	    
		private static DatabaseConnection databaseConnection = new DatabaseConnection();
		
		public static SQLiteDatabase getDatabase(Context _context) {
			synchronized (databaseConnection) {
				if (databaseConnection.db == null) {
					DatabaseHelper dbh = new DatabaseHelper(
							_context.getApplicationContext());
					databaseConnection.db = dbh.getWritableDatabase();
					if (databaseConnection.db.isOpen() == false) {
						Log.e(TAG, "Could not open ScrobblesDatabase");
						databaseConnection.db = null;
						return null;
					}
				}
				++databaseConnection.connection_count;
				return databaseConnection.db;
			}
		}
	    public static void closeDatabase(){
			synchronized (databaseConnection) {
				--databaseConnection.connection_count;
				if (databaseConnection.connection_count == 0) {
					databaseConnection.db.close();
					databaseConnection.db = null;
				}
			}
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
			Log.w(TAG, "Upgrading scrobbles database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
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
		mDb = DatabaseHelper.getDatabase(mCtx.getApplicationContext());
	}

	public void close() {
		DatabaseHelper.closeDatabase();
	}

	/**
	 * Return the new rowId for that scrobble, otherwise return a -1 to indicate
	 * failure.
	 * 
	 * @return rowId or -1 if failed
	 */
	public long insertTrack(Track track) {
		ContentValues vals = new ContentValues();
		vals.put("musicapp", track.getMusicAPI().getId());
		vals.put("artist", track.getArtist());
		vals.put("album", track.getAlbum());
		vals.put("track", track.getTrack());
		vals.put("whenplayed", track.getWhen());
		vals.put("duration", track.getDuration());
		vals.put("tracknr", track.getTrackNr());
		vals.put("mbid", track.getMbid());
		vals.put("source", track.getSource());
		vals.put("rating", track.getRating());

		return mDb.insert(TABLENAME_SCROBBLES, null, vals);
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

	private Track readTrack(Cursor c) {
		Track.Builder b = new Track.Builder();
		b.setMusicAPI(MusicAPI.fromDatabase(mCtx, c.getLong(c
				.getColumnIndex("musicapp"))));
		b.setArtist(c.getString(c.getColumnIndex("artist")));
		b.setAlbum(c.getString(c.getColumnIndex("album")));
		b.setTrack(c.getString(c.getColumnIndex("track")));
		b.setWhen(c.getLong(c.getColumnIndex("whenplayed")));
		b.setDuration(c.getInt(c.getColumnIndex("duration")));
		b.setRowId(c.getInt(c.getColumnIndex("_id")));
		b.setTrackNr(c.getString(c.getColumnIndex("tracknr")));
		b.setMbid(c.getString(c.getColumnIndex("mbid")));
		b.setSource(c.getString(c.getColumnIndex("source")));
		b.setRating(c.getString(c.getColumnIndex("rating")));

		return b.build(); // theoretically might throw, shouldn't though
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
			tracks[i] = readTrack(c);
			c.moveToNext();
		}
		c.close();
		return tracks;
	}

	public Cursor fetchTracksCursor(NetApp napp, SortField sf) {
		Cursor c;
		String sql = "select * from scrobbles, scrobbles_netapp "
				+ "where _id = trackid and netappid = " + napp.getValue()
				+ " order by " + sf.getSql();
		c = mDb.rawQuery(sql, null);
		return c;
	}

	public Cursor fetchAllTracksCursor(SortField sf) {
		Cursor c;
		String sql = "select * from scrobbles order by " + sf.getSql();
		c = mDb.rawQuery(sql, null);
		return c;
	}

	public Track fetchTrack(int trackId) {
		String sql = "select * from scrobbles where _id = " + trackId;
		Cursor c = mDb.rawQuery(sql, null);

		if (c.getCount() == 0)
			return null;

		c.moveToFirst();
		Track track = readTrack(c);
		c.close();
		return track;
	}

	public NetApp[] fetchNetAppsForScrobble(int trackId) {
		String sql = "select netappid from scrobbles_netapp where trackid = "
				+ trackId;
		Cursor c = mDb.rawQuery(sql, null);

		if (c.getCount() == 0)
			return new NetApp[0];

		NetApp[] ret = new NetApp[c.getCount()];
		c.moveToFirst();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = NetApp.fromValue(c.getInt(0));
			c.moveToNext();
		}
		c.close();
		return ret;
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
