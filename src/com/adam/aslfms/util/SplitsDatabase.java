package com.adam.aslfms.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SplitsDatabase {
  private static final String TAG = "SplitsDatabase";

  private SQLiteDatabase mDb;

  private final Context mCtx;

  private static final String DATABASE_NAME = "splits";
  private static final int DATABASE_VERSION = 6;

  private static final String TABLENAME_SPLITS = "splits";

  private static final String DATABASE_CREATE_SPLITS = "create table splits ("
      + "_id integer primary key autoincrement, " //
      + "split text not null );";

  public SplitsDatabase(Context ctx) {
    this.mCtx = ctx;
  }

  public void open() throws SQLException {
    mDb = DatabaseHelper.getDatabase(mCtx.getApplicationContext());
  }

  public void close() {
    DatabaseHelper.closeDatabase();
  }

  public long insertTitleSplit(String split) {
    ContentValues vals = new ContentValues();
    vals.put("split", split);

    return mDb.insert(TABLENAME_SPLITS, null, vals);
  }

  public Cursor fetchAllSplitsCursor() {
    Cursor c;
    String sql = "select * from splits";
    c = mDb.rawQuery(sql, null);
    return c;
  }

  public void clearAllSplits() {
    mDb.delete(TABLENAME_SPLITS, null, null);
  }

  public void removeSplit(long id) {
    mDb.delete(TABLENAME_SPLITS, "_id = ?", new String[] { "" + id });
  }

  public String filterTrack(String track) {
    String splitTitle = null;

    Cursor cursor = fetchAllSplitsCursor();

    for (boolean hasItem = cursor.moveToFirst(); hasItem; hasItem = cursor.moveToNext()) {
      splitTitle = cursor.getString(cursor.getColumnIndex("split"));
      track = track.replace(splitTitle, "");
    }

    return track;
  }

  private static class DatabaseHelper extends SQLiteOpenHelper {

    private DatabaseHelper(Context _context) {
      super(_context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static class DatabaseConnection {
      public Long connection_count = Long.valueOf(0);
      public SQLiteDatabase db = null;

      DatabaseConnection() {
        connection_count = Long.valueOf(0);
        db = null;
      }
    }

    private static DatabaseConnection databaseConnection = new DatabaseConnection();

    public static SQLiteDatabase getDatabase(Context _context) {
      synchronized (databaseConnection) {
        if (databaseConnection.db == null) {
          DatabaseHelper dbh = new DatabaseHelper(_context.getApplicationContext());
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
      Log.d(TAG, "create sql splits: " + DATABASE_CREATE_SPLITS);
      db.execSQL(DATABASE_CREATE_SPLITS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading scrobbles database from version " + oldVersion + " to " + newVersion
          + ", which will destroy all old data");
      db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_SPLITS);
      onCreate(db);
    }
  }
}
