package com.adam.aslfms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

public class ScrobblesDbAdapter {

	private static final String TAG = "ScrobblesDbAdapter";

	public static final String KEY_ARTIST = "artist";
	public static final String KEY_ALBUM = "album";
	public static final String KEY_TRACK = "track";
	public static final String KEY_DURATION = "duration";
	public static final String KEY_WHEN = "whenplayed";
	public static final String KEY_ROWID = "_id";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE = "create table scrobbles (_id integer primary key autoincrement,"
			+ KEY_ARTIST
			+ " text not null,"
			+ KEY_ALBUM
			+ " text not null,"
			+ KEY_TRACK
			+ " text not null,"
			+ KEY_DURATION
			+ " integer not null," + KEY_WHEN + " integer not null);";

	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "scrobbles";
	private static final int DATABASE_VERSION = 2;

	private static final int SCROBBLE_LIMIT = 10;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS scrobbles");
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public ScrobblesDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public ScrobblesDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
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
	public long insertScrobble(Track track) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ARTIST, track.getArtist().toString());
		initialValues.put(KEY_ALBUM, track.getAlbum().toString());
		initialValues.put(KEY_TRACK, track.getTrack().toString());
		initialValues.put(KEY_DURATION, track.getDuration());
		initialValues.put(KEY_WHEN, track.getWhen());

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the scrobble with the given rowId
	 * 
	 * @param rowId
	 *            id of scrobble to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteScrobble(Track track) {
		if (track.getRowId() == -1) {
			Log.e(TAG, "Trying to delete scrobble with rowId == -1");
			return false;
		}
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + track.getRowId(),
				null) > 0;
	}

	/**
	 * Return a Cursor over the list of all scrobbles in the database
	 * 
	 * @return Cursor over all scrobbles
	 */
	public Cursor fetchAllScrobbles() {

		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_ARTIST,
				KEY_ALBUM, KEY_TRACK, KEY_DURATION, KEY_WHEN }, null, null,
				null, null, null);
	}

	public Track[] fetchScrobblesArray() {
		Cursor c = mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID,
				KEY_ARTIST, KEY_ALBUM, KEY_TRACK, KEY_DURATION, KEY_WHEN },
				null, null, null, null, null);
		Track[] tracks = new Track[c.getCount()];
		c.moveToFirst();
		for (int i = 0; i < tracks.length; i++) {
			tracks[i] = new Track(c.getString(1), c.getString(2), c
					.getString(3), c.getInt(4), c.getLong(5), c.getInt(0));
			c.moveToNext();
		}
		c.close();
		return tracks;
	}

}
