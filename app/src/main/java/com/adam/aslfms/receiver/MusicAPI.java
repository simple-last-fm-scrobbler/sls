/**
 * This file is part of Simple Last.fm Scrobbler.
 * 
 *     http://code.google.com/p/a-simple-lastfm-scrobbler/
 * 
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adam.aslfms.receiver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adam.aslfms.MusicAppsActivity;
import com.adam.aslfms.R;

/**
 * A class for representing and dealing different scrobbling APIs / music apps.
 * 
 * @see #fromReceiver(Context, String, String, String, boolean)
 * @see #fromDatabase(Context, long)
 * @see #all(Context)
 * 
 * @author tgwizard
 * @since 1.2.3
 */
public class MusicAPI {

	private static final String TAG = "MusicAPI";

	/**
	 * Package name to prefix to "APIs" (e.g. the Scrobble Droid API) which
	 * don't have "packages" of their own.
	 */
	public static final String NOT_AN_APPLICATION_PACKAGE = "not.an.application.";

	long id;
	String name;
	String pkg;
	String msg;
	int clashWithScrobbleDroid;
	int enabled;

	MusicAPI(long id, String name, String pkg, String msg,
			boolean clashWithScrobbleDroid, boolean enabled) {
		super();

		if (name == null)
			throw new IllegalArgumentException("null music api name");
		if (pkg == null)
			throw new IllegalArgumentException("null music api pkg");

		this.id = id;
		this.name = name;
		this.pkg = pkg;
		this.msg = msg;
		this.clashWithScrobbleDroid = clashWithScrobbleDroid ? 1 : 0;
		this.enabled = enabled ? 1 : 0;
	}

	/**
	 * Returns the id for this {@code MusicAPI}, used in the database.
	 * 
	 * @return a valid id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns a name that can be shown to the user. A music app can change this
	 * name at any time through broadcasts using the SLS API.
	 * 
	 * @return a user-friendly name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The package of the application that scrobbles using this API, or
	 * {@link #NOT_AN_APPLICATION_PACKAGE} + [appropriate name] - which means
	 * essentially any application.
	 * 
	 * @return a package name, or {@link #NOT_AN_APPLICATION_PACKAGE} +
	 *         [appropriate name]
	 */
	public String getPackage() {
		return pkg;
	}

	/**
	 * Gives extra information to be displayed to the user in the
	 * {@link MusicAppsActivity}.
	 * 
	 * @return a string with extra information for the user
	 */
	public String getMessage() {
		return msg;
	}

	/**
	 * Returns whether Scrobble Droid also can scrobble from this API / music
	 * app.
	 * 
	 * @return true if Scrobble Droid can scrobble from this API / music app,
	 *         false otherwise.
	 */
	public boolean clashesWithScrobbleDroid() {
		return clashWithScrobbleDroid == 1;
	}

	/**
	 * Returns true if the user has enabled scrobbling through this API / music
	 * app. Default is true.
	 * 
	 * @see MusicAppsActivity
	 * 
	 * @return true if scrobbling from this API / music app is enabled, fales
	 *         otherwise.
	 */
	public boolean isEnabled() {
		return enabled == 1;
	}

	/**
	 * Enables / disables scrobbling from this API / music app.
	 * 
	 * @see MusicAppsActivity
	 * 
	 * @param ctx
	 *            context to enable database calls.
	 * @param enabled
	 *            whether this API / app should be enabled or disabled
	 */
	public void setEnabled(Context ctx, boolean enabled) {
		int en = enabled ? 1 : 0;
		if (en == this.enabled)
			return;

		SQLiteDatabase db = DatabaseHelper.getDatabase(ctx.getApplicationContext());

		String update = "update music_api set enabled = " + en
				+ " where _id = " + this.id;
		db.execSQL(update);
		this.enabled = en;

		DatabaseHelper.closeDatabase();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MusicAPI other = (MusicAPI) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MusicAPI [clashWithScrobbleDroid=" + clashWithScrobbleDroid
				+ ", enabled=" + enabled + ", id=" + id + ", msg=" + msg
				+ ", name=" + name + ", pkg=" + pkg + "]";
	}

	/**
	 * Takes some parameters describing an API / music app and (1) saves it to a
	 * database and (2) returns it as a {@code MusicAPI} object.
	 * <p>
	 * All {@code MusicAPI} objects need to have unique package names when saved
	 * to the database. This means that if this method is called twice with the
	 * same {@code pkg} but different names, the last {@code name} will be the
	 * one left in the database.
	 * 
	 * @param ctx
	 *            context to enable database calls
	 * @param name
	 *            name of the music app, see {@link #getName()}
	 * @param pkg
	 *            package of the music app, see {@link #getPackage()}
	 * @param msg
	 *            extra info for the user, see {@link #getMessage()}
	 * @param clashWithScrobbleDroid
	 *            see {@link #clashesWithScrobbleDroid()}
	 * @return a {@code MusicAPI} object with the parameters as values, "never"
	 *         null
	 * @throws IllegalArgumentException
	 *             if {@code pkg} or {@code name} is null
	 */
	public static MusicAPI fromReceiver(Context ctx, String name, String pkg,
			String msg, boolean clashWithScrobbleDroid) {

		if (name == null)
			throw new IllegalArgumentException("null music api name");
		if (pkg == null)
			throw new IllegalArgumentException("null music api pkg");

		SQLiteDatabase db = DatabaseHelper.getDatabase(ctx.getApplicationContext());

		String sql = "select * from music_api where pkg = '" + pkg + "'";
		Cursor c = db.rawQuery(sql, null);

		MusicAPI mapi = null;
		c.moveToFirst();
		if (!c.isAfterLast()) {
			// already in db
			mapi = readMusicAPI(c);
			c.close();

			boolean changed = false;

			if (!mapi.name.equals(name)) {
				mapi.name = name;
				changed = true;
			}

			if (!mapi.pkg.equals(pkg)) {
				mapi.pkg = pkg;
				changed = true;
			}

			if ((mapi.msg == null && msg != null)
					|| (mapi.msg != null && !mapi.msg.equals(msg))) {
				mapi.msg = msg;
				changed = true;
			}

			int sdclash = clashWithScrobbleDroid ? 1 : 0;
			if (mapi.clashWithScrobbleDroid != sdclash) {
				mapi.clashWithScrobbleDroid = sdclash;
				changed = true;
			}

			if (changed) {
				String msgNull = mapi.msg == null ? "NULL" : "'" + mapi.msg
						+ "'";
				String update = "update music_api set " //
						+ "name = '" + mapi.name + "', " //
						+ "pkg = '" + mapi.pkg + "', " //
						+ "msg = " + msgNull + ", " //
						+ "sdclash = " + mapi.clashWithScrobbleDroid //
						+ " where _id = " + mapi.id;
				Log.d(TAG, "doing an music api db update");
				db.execSQL(update);
			}
		} else {
			// not in db
			c.close();

			ContentValues vals = new ContentValues();
			vals.put("name", name);
			vals.put("pkg", pkg);
			vals.put("msg", msg);
			vals.put("sdclash", clashWithScrobbleDroid ? 1 : 0);
			vals.put("enabled", 1);

			long id = db.insert("music_api", null, vals);

			if (id == -1) {
				Log.e(TAG, "new mapi couldn't be inserted into db");
			} else {
				Log.d(TAG, "new mapiinserted into db");
			}

			mapi = new MusicAPI(id, name, pkg, msg, clashWithScrobbleDroid,
					true);
			Log.d(TAG, mapi.toString());
		}
		DatabaseHelper.closeDatabase();
		return mapi;
	}

	/**
	 * Returns the {@code MusicAPI} stored in the database with the id {@code
	 * id}.
	 * 
	 * @param ctx
	 *            context to enable database calls
	 * @param id
	 *            id of a {@code MusicAPI} in the database
	 * @return the {@code MusicAPI} in the database with {@code id}, "never"
	 *         null
	 */
	public static MusicAPI fromDatabase(Context ctx, long id) {
		SQLiteDatabase db = DatabaseHelper.getDatabase(ctx.getApplicationContext());

		String sql = "select * from music_api where _id = " + id;
		Cursor c = db.rawQuery(sql, null);

		MusicAPI mapi = null;
		c.moveToFirst();
		if (!c.isAfterLast()) {
			mapi = readMusicAPI(c);
		} else {
			// this means that the music api the user used to listen to this
			// track
			// hasn't been played after the upgrade to v1.2.3
			mapi = new MusicAPI(-1, ctx.getString(R.string.unknown_mapi),
					NOT_AN_APPLICATION_PACKAGE + "pre_1_2_3", null, false, true);
		}
		c.close();
		DatabaseHelper.closeDatabase();
		return mapi;
	}

	/**
	 * Returns all {@code MusicAPI} objects stored in the database. If there are
	 * no such objects, an array with length zero will be returned.
	 * 
	 * @param ctx
	 *            context to enable database calls
	 * @return all {@code MusicAPI} objects stored in the database, never null
	 */
	public static MusicAPI[] all(Context ctx) {
		SQLiteDatabase db = DatabaseHelper.getDatabase(ctx.getApplicationContext());

		String sql = "select * from music_api";
		Cursor c = db.rawQuery(sql, null);

		MusicAPI[] mapis = new MusicAPI[c.getCount()];
		c.moveToFirst();
		for (int i = 0; i < mapis.length; i++) {
			mapis[i] = readMusicAPI(c);
			c.moveToNext();
		}
		c.close();
		DatabaseHelper.closeDatabase();
		return mapis;
	}

	private static MusicAPI readMusicAPI(Cursor c) {
		return new MusicAPI(c.getInt(c.getColumnIndex("_id")), //
				c.getString(c.getColumnIndex("name")), //
				c.getString(c.getColumnIndex("pkg")), //
				c.getString(c.getColumnIndex("msg")), //
				c.getInt(c.getColumnIndex("sdclash")) == 1, //
				c.getInt(c.getColumnIndex("enabled")) == 1);
	}

	static final String DATABASE_NAME = "music_apis";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE_MUSIC_API = "create table music_api ("
			+ "_id integer primary key autoincrement, "
			+ "name text not null, "
			+ "pkg text unique not null, "
			+ "msg text, " //
			+ "sdclash integer not null, " //
			+ "enabled integer not null);";

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
						Log.e(TAG, "Could not open MusicAPI database");
						databaseConnection.db = null;
						return null;
					}
				}
				++databaseConnection.connection_count;
				return databaseConnection.db;
			}
		}

		public static void closeDatabase() {
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
			Log.d(TAG, "create sql music api: " + DATABASE_CREATE_MUSIC_API);
			db.execSQL(DATABASE_CREATE_MUSIC_API);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading music api database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS music_api");
			onCreate(db);
		}
	}
}
