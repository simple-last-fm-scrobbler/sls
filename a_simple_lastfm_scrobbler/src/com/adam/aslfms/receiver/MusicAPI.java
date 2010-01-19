package com.adam.aslfms.receiver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MusicAPI {

	private static final String TAG = "MusicAPI";

	public static final String NOT_AN_APPLICATION_PACKAGE = "not.an.application.";

	private long id;
	private String name;
	private String pkg;
	private String msg;
	private int clashWithScrobbleDroid;
	private int enabled;

	private MusicAPI(long id, String name, String pkg, String msg,
			boolean clashWithScrobbleDroid, boolean enabled) {
		super();
		this.id = id;
		this.name = name;
		this.pkg = pkg;
		this.msg = msg;
		this.clashWithScrobbleDroid = clashWithScrobbleDroid ? 1 : 0;
		this.enabled = enabled ? 1 : 0;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPackage() {
		return pkg;
	}

	public String getMessage() {
		return msg;
	}

	public boolean clashesWithScrobbleDroid() {
		return clashWithScrobbleDroid == 1;
	}

	public boolean isEnabled() {
		return enabled == 1;
	}

	public void setEnabled(Context ctx, boolean enabled) {
		int en = enabled ? 1 : 0;
		if (en == this.enabled)
			return;

		DatabaseHelper dbHelper = new DatabaseHelper(ctx);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String update = "update music_api set enabled = " + en
				+ " where _id = " + this.id;
		db.execSQL(update);
		this.enabled = en;

		dbHelper.close();
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

	// ------------

	public static MusicAPI fromReceiver(Context ctx, String name, String pkg,
			String msg, boolean clashWithScrobbleDroid) {

		if (name == null)
			throw new IllegalArgumentException("null music api name");
		if (pkg == null)
			throw new IllegalArgumentException("null music api pkg");

		DatabaseHelper dbHelper = new DatabaseHelper(ctx);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

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
				String update = "update music_api set " //
						+ "name = '" + mapi.name + "', " //
						+ "pkg = '" + mapi.pkg + "', " //
						+ "msg = '" + mapi.msg + "', " //
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
		dbHelper.close();
		return mapi;
	}

	public static MusicAPI fromDatabase(Context ctx, long l) {
		DatabaseHelper dbHelper = new DatabaseHelper(ctx);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String sql = "select * from music_api where _id = " + l;
		Cursor c = db.rawQuery(sql, null);

		MusicAPI mapi = null;
		c.moveToFirst();
		if (!c.isAfterLast()) {
			mapi = readMusicAPI(c);
		}
		c.close();
		dbHelper.close();
		return mapi;
	}

	public static MusicAPI[] all(Context ctx) {
		DatabaseHelper dbHelper = new DatabaseHelper(ctx);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String sql = "select * from music_api";
		Cursor c = db.rawQuery(sql, null);

		MusicAPI[] mapis = new MusicAPI[c.getCount()];
		c.moveToFirst();
		for (int i = 0; i < mapis.length; i++) {
			mapis[i] = readMusicAPI(c);
			c.moveToNext();
		}
		c.close();
		dbHelper.close();
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

	private static final String DATABASE_NAME = "music_apis";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE_MUSIC_API = "create table music_api ("
			+ "_id integer primary key autoincrement, "
			+ "name text not null, "
			+ "pkg text unique not null, "
			+ "msg text, " //
			+ "sdclash integer not null, " //
			+ "enabled integer not null);";

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null,
					DATABASE_VERSION);
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
