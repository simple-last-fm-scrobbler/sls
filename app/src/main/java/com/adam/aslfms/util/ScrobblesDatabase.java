/**
 * This file is part of Simple Scrobbler.
 * <p>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p>
 * Copyright 2011 Simple Scrobbler Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.adam.aslfms.util;

import android.app.TaskStackBuilder;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tgwizard
 * @since 0.9
 */
public class ScrobblesDatabase {

    private static final String TAG = "ScrobblesDatabase";

    private SQLiteDatabase mDb;

    private final Context mCtx;

    public static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 7;

    private static final String ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = ON;";

    private static final String TABLENAME_SCROBBLES = "scrobbles";
    private static final String TABLENAME_HEARTS = "hearts";
    private static final String TABLENAME_CORRNETAPP = "scrobbles_netapp";
    private static final String TABLENAME_CORRNETAPP_REPAIRED = "scrobbles_netapp_repaired";
    private static final String TABLENAME_CORRECTION_RULES = "correction_rules";
    private static final String TABLENAME_RULE_CHANGES = "rule_changes";
    private static final String TRIGGER_NAME_CHECK_CORRECTION_RULES = "check_correction_rules";

    private static final String end_cmd = ";";
    private static final String begin_curve_brace = "(";
    private static final String end_curve_brace = ")";
    private static final String comma = ",";
    private static final String sp = " ";
    private static final String if_exists = "IF EXISTS";
    private static final String if_not_exists = "IF NOT EXISTS";
    private static final String table = "table";
    private static final String create = "create";
    private static final String drop = "drop";
    private static final String alter = "alter";
    private static final String trigger = "trigger";
    private static final String add_column = "add column";
    private static final String int_opt = "INTEGER NOT NULL";
    private static final String str_opt = "TEXT NOT NULL DEFAULT ''";
    private static final String id_int_prim_key_auto_opt = "_id integer primary key autoincrement";
    private static final String music_api_int = "musicapp integer not null";
    private static final String on_update_cascade = "on delete cascade on update cascade";
    private static final String foreign_key_ref_track_id_refs_scrobbles_id = "foreign key (track_id) references scrobbles(_id)";
    private static final String foreign_key_ref_trackid_refs_scrobbles_id = "foreign key (trackid) references scrobbles(_id)";

    private static final String[] correction_rules_strings = {"track_to_change", "album_to_change", "artist_to_change", "track_correction", "album_correction", "artist_correction"};

    private static final String rule_changes_track_id = "track_id integer primary key";
    private static final String[] rule_changes_strings = {"original_track","original_album","original_artist"};

    private static final String[] scrobbles_heart_only_strings = {"track", "artist", "netapp", "rating"};
    private static final String[] scrobbles_strings = {"album", "albumartist", "trackartist", "tracknr", "mbid", "source"};
    private static final String[] scrobbles_ints = {"duration", "whenplayed"};

    private static final String[] scrobbles_netapp_strings = {"netappid", "trackid", "sentstatus", "acceptedstatus"};
    private static final String scrobbles_netapp_primary_key = "primary key (netappid, trackid)"; // failure DEPRECATED, do not use


    private static String buildStringOptions(String[] stringArray, String option){
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < stringArray.length; j++){
            stringBuilder.append(sp);
            stringBuilder.append(stringArray[j]);
            stringBuilder.append(sp);
            stringBuilder.append(option);
            if (j != stringArray.length - 1) stringBuilder.append(comma);
        }
        return stringBuilder.toString();
    }

    private static final String DATABASE_CREATE_CORRECTION_RULES = TABLENAME_CORRECTION_RULES + sp + begin_curve_brace + id_int_prim_key_auto_opt + comma + buildStringOptions(correction_rules_strings,str_opt) + end_curve_brace + end_cmd;
    private static final String DATABASE_CREATE_RULE_CHANGES = TABLENAME_RULE_CHANGES + sp + begin_curve_brace + rule_changes_track_id + comma + buildStringOptions(rule_changes_strings, str_opt) + comma + sp + foreign_key_ref_track_id_refs_scrobbles_id + on_update_cascade + end_curve_brace + end_cmd;
    private static final String DATABASE_CREATE_SCROBBLES = TABLENAME_SCROBBLES + sp + begin_curve_brace + id_int_prim_key_auto_opt + comma + sp + music_api_int + comma + sp +
                                                            buildStringOptions(scrobbles_heart_only_strings, str_opt) + comma + sp + buildStringOptions(scrobbles_strings, str_opt) + comma + sp + buildStringOptions(scrobbles_ints, int_opt) + end_curve_brace + end_cmd;
    private static final String DATABASE_CREATE_CORRNETAPP_REPAIRED = TABLENAME_CORRNETAPP_REPAIRED + sp + begin_curve_brace + sp + id_int_prim_key_auto_opt + comma + buildStringOptions(scrobbles_netapp_strings, str_opt) + comma + sp + foreign_key_ref_trackid_refs_scrobbles_id + on_update_cascade + end_curve_brace ;
    private static final String DATABASE_CREATE_HEARTS = TABLENAME_HEARTS + sp + begin_curve_brace + id_int_prim_key_auto_opt + comma + buildStringOptions(scrobbles_heart_only_strings, str_opt) + end_curve_brace + end_cmd;
    private static final String TRIGGGER_CREATE_CHECK_CORRECTION_RULES =
            "check_correction_rules" +
                    "	after insert on "+ TABLENAME_SCROBBLES +
                    "	for each row" +
                    "	when (select count(*) from correction_rules where new.track = track_to_change and new.album = album_to_change and new.artist = artist_to_change) = 1 " +
                    "begin" +
                    "	insert into rule_changes (track_id, original_track, original_album, original_artist)" +
                    "		select _id track_id, track original_track, album original_album, artist original_artist" +
                    "		from " + TABLENAME_SCROBBLES +
                    "		where _id = new._id;" +
                    "	update " + TABLENAME_SCROBBLES +
                    "		set track = (select track_correction from correction_rules where new.track = track_to_change and new.album = album_to_change and new.artist = artist_to_change)," +
                    "		album = (select album_correction from correction_rules where new.track = track_to_change and new.album = album_to_change and new.artist = artist_to_change)," +
                    "		artist = (select artist_correction from correction_rules where new.track = track_to_change and new.album = album_to_change and new.artist = artist_to_change) where _id = new._id; " +
                    "end;";

    private static final String TRIGGGER_CREATE_CHECK_CORRECTION_RULES_HEARTS =
            "check_correction_rules_hearts" +
                    "	after insert on "+ TABLENAME_HEARTS +
                    "	for each row" +
                    "	when (select count(*) from correction_rules where new.track = track_to_change and new.artist = artist_to_change) = 1 " +
                    "begin" +
                    "	insert into rule_changes (track_id, original_track,original_artist)" +
                    "		select _id track_id, track original_track, artist original_artist" +
                    "		from " + TABLENAME_HEARTS +
                    "		where _id = new._id;" +
                    "	update " + TABLENAME_HEARTS +
                    "		set track = (select track_correction from correction_rules where new.track = track_to_change and new.artist = artist_to_change)," +
                    "		artist = (select artist_correction from correction_rules where new.track = track_to_change and new.artist = artist_to_change) where _id = new._id; " +
                    "end;";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private DatabaseHelper(Context _context) {
            super(_context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        private static class DatabaseConnection {
            public Long connection_count;
            public SQLiteDatabase db;

            DatabaseConnection() {
                connection_count = 0L;
                db = null;
            }
        }

        private static final DatabaseConnection databaseConnection = new DatabaseConnection();

        public static SQLiteDatabase getDatabase(Context _context) {
            synchronized (databaseConnection) {
                if (databaseConnection.db == null) {
                    DatabaseHelper dbh = new DatabaseHelper(
                            _context.getApplicationContext());
                    databaseConnection.db = dbh.getWritableDatabase();
                    if (!databaseConnection.db.isOpen()) {
                        databaseConnection.db = null;
                        Log.e(TAG, "Unable to open the ScrobblesDatabase database");
                        throw new RuntimeException("Failed to open the ScrobblesDatabase database");
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
            db.execSQL(ENABLE_FOREIGN_KEYS);
            String pre_cmd = create + sp + table + sp;
            String pre_cmd_with_trigger = create + sp + trigger + sp;
            Log.d(TAG, "create sql scrobbles: " + DATABASE_CREATE_SCROBBLES);
            Log.d(TAG, "create sql corrnetapp: " + DATABASE_CREATE_CORRNETAPP_REPAIRED);
            db.execSQL(pre_cmd + DATABASE_CREATE_SCROBBLES);
            db.execSQL(pre_cmd + DATABASE_CREATE_CORRNETAPP_REPAIRED);
            // Tables and trigger for updating scrobbles based on rules.
            Log.d(TAG, "create sql correction_rules: " + DATABASE_CREATE_CORRECTION_RULES);
            Log.d(TAG, "create sql rules_changes: " + DATABASE_CREATE_RULE_CHANGES);
            Log.d(TAG, "create sql hearts: " + DATABASE_CREATE_HEARTS);
            Log.d(TAG, "create sql triggers: " + TRIGGGER_CREATE_CHECK_CORRECTION_RULES);
            Log.d(TAG, "create sql triggers_hearts: " + TRIGGGER_CREATE_CHECK_CORRECTION_RULES_HEARTS);
            db.execSQL(pre_cmd + DATABASE_CREATE_CORRECTION_RULES);
            db.execSQL(pre_cmd + DATABASE_CREATE_RULE_CHANGES);
            db.execSQL(pre_cmd + DATABASE_CREATE_HEARTS);
            db.execSQL(pre_cmd_with_trigger + TRIGGGER_CREATE_CHECK_CORRECTION_RULES);
            db.execSQL(pre_cmd_with_trigger + TRIGGGER_CREATE_CHECK_CORRECTION_RULES_HEARTS);
        }

        private void verifyOrAddColumnInTable(String[] cols, String opt, String myTable, SQLiteDatabase db){
            Cursor cursor = db.rawQuery("SELECT * FROM " + myTable, null);
            for (String col : cols) {
                int columnIndex = cursor.getColumnIndex(col);
                if (columnIndex < 0) {
                    db.execSQL(alter + sp + table + sp + myTable + sp + add_column + sp + col + sp + opt);
                }
            }
        }

        private void repairCoreNetAppTable(SQLiteDatabase db){
            Cursor c = db.rawQuery( "SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLENAME_CORRNETAPP + "'",null); // check if table exists
            if (c.getCount() > 0) {
                Cursor dbCursor = db.query(TABLENAME_CORRNETAPP_REPAIRED, null, null, null, null, null, null);
                String[] columnNames = dbCursor.getColumnNames();
                Log.d(TAG, columnNames[0]);
                db.execSQL("INSERT INTO " + TABLENAME_CORRNETAPP_REPAIRED + "(_id," + scrobbles_netapp_strings[0] + "," + scrobbles_netapp_strings[1] + "," +
                        scrobbles_netapp_strings[2] + "," + scrobbles_netapp_strings[3] +  ")  SELECT " + null + "," + scrobbles_netapp_strings[0] + "," + scrobbles_netapp_strings[1] + ", '','' FROM " + TABLENAME_CORRNETAPP + ";");
            }
            db.execSQL(drop + sp + table + sp + if_exists + sp + TABLENAME_CORRNETAPP);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String pre_cmd = create + sp + table + sp + if_not_exists + sp;
            String pre_cmd_with_trigger = create + sp + trigger + sp + if_not_exists + sp;
            Log.w(TAG, "Upgrading scrobbles database from version "
                    + oldVersion + " to " + newVersion
                    + ", which will test and update database functionality.");
            if (newVersion > oldVersion) {
                db.execSQL(ENABLE_FOREIGN_KEYS);
                db.execSQL(pre_cmd + DATABASE_CREATE_CORRNETAPP_REPAIRED);
                db.execSQL(pre_cmd + DATABASE_CREATE_SCROBBLES);
                // Tables and trigger for updating scrobbles based on rules.
                db.execSQL(pre_cmd + DATABASE_CREATE_CORRECTION_RULES);
                db.execSQL(pre_cmd + DATABASE_CREATE_RULE_CHANGES);
                db.execSQL(pre_cmd + DATABASE_CREATE_HEARTS);
                db.execSQL(pre_cmd_with_trigger + TRIGGGER_CREATE_CHECK_CORRECTION_RULES);
                db.execSQL(pre_cmd_with_trigger + TRIGGGER_CREATE_CHECK_CORRECTION_RULES_HEARTS);
                // check for primary key bug in CORENETAPP
                repairCoreNetAppTable(db);
                // alter database to add missing values if possible
                // scrobbles table
                verifyOrAddColumnInTable(scrobbles_heart_only_strings, str_opt, TABLENAME_SCROBBLES, db);
                verifyOrAddColumnInTable(scrobbles_strings, str_opt, TABLENAME_SCROBBLES, db);
                verifyOrAddColumnInTable(scrobbles_ints, int_opt, TABLENAME_SCROBBLES, db);
                // netapp table
                verifyOrAddColumnInTable(scrobbles_netapp_strings, str_opt, TABLENAME_CORRNETAPP_REPAIRED, db);
                // hearts table
                verifyOrAddColumnInTable(scrobbles_heart_only_strings, str_opt, TABLENAME_HEARTS, db);
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            db.execSQL(ENABLE_FOREIGN_KEYS);
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
        vals.put("albumartist", track.getAlbumArtist());
        vals.put("trackartist", track.getTrackArtist());
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
     * Return the new rowId for that heart, otherwise return a -1 to indicate
     * failure.
     *
     * @return rowId or -1 if failed
     */
    public long insertHeart(Track track, NetApp netApp){
        ContentValues vals = new ContentValues();
        vals.put("artist", track.getArtist());
        vals.put("track", track.getTrack());
        vals.put("netapp", netApp.getValue());
        return mDb.insert(TABLENAME_HEARTS, null, vals);
    }

    /**
     *
     * @param napp
     *            the NetApp which we'll wanna scrobble to
     * @param trackid
     *            the rowId of the track to scrobble, see {@link Track}
     * @return true if the insert succeeded, false otherwise
     */
    public boolean insertScrobble(NetApp napp, long trackid) {
        ContentValues iVals = new ContentValues();
        iVals.put("netappid", napp.getValue());
        iVals.put("trackid", trackid);

        return mDb.insert(TABLENAME_CORRNETAPP_REPAIRED, null, iVals) > 0;
    }

    public long verifyOrUpdateScrobblesAlreadyInCache(NetApp napp){
        open();
        Cursor c;
        String sql = "select * from scrobbles";
        c = mDb.rawQuery(sql, null);
        c.moveToFirst();
        long count = 0;
        long temp = 0;
        while (!c.isAfterLast()) {
            ContentValues iVals = new ContentValues();
            iVals.put("netappid", napp.getValue());
            iVals.put("trackid", c.getInt(c.getColumnIndex("_id")));
            // insert not duplicated scrobble for newly authenticated app
            mDb.execSQL("insert into " + TABLENAME_CORRNETAPP_REPAIRED + "(" + scrobbles_netapp_strings[0] + "," + scrobbles_netapp_strings[1] +
                            ") SELECT ?, ? WHERE NOT EXISTS ( SELECT 1 FROM " + TABLENAME_CORRNETAPP_REPAIRED +
                            " WHERE " + scrobbles_netapp_strings[0] + " =? AND " + scrobbles_netapp_strings[1]  + " =? )",
                            new String[] { Integer.toString(napp.getValue()), Integer.toString(c.getInt(c.getColumnIndex("_id"))),
                                    Integer.toString(napp.getValue()), Integer.toString(c.getInt(c.getColumnIndex("_id")))});
            c.moveToNext();
        }
        return count;
    }

    public int deleteScrobble(NetApp napp, int trackId) {
        if (trackId == -1) {
            Log.e(TAG, "Trying to delete scrobble with trackId == -1");
            return -2;
        }
        return mDb.delete(TABLENAME_CORRNETAPP_REPAIRED, "netappid = ? and trackid = ?",
                new String[]{"" + napp.getValue(), "" + trackId});
    }

    public int setSentField(NetApp napp, int trackId) {
        if (trackId == -1) {
            Log.e(TAG, "Failed to set sent field");
            return -2;
        }
        Log.d(TAG, "Trying to set sent field");
        ContentValues contentValues = new ContentValues();
        contentValues.put("sentstatus", "sent");
        return mDb.update(TABLENAME_CORRNETAPP_REPAIRED, contentValues,"netappid = " + napp.getValue() +" and _id = " + trackId, null);
    }

    public int deleteHeart(String[] s) {
        return mDb.delete(TABLENAME_HEARTS, scrobbles_heart_only_strings[0] + " = ? and " + scrobbles_heart_only_strings[1] + " = ? and " + scrobbles_heart_only_strings[2] + " = ?",
                new String[]{"" + s[0], "" + s[1], "" + s[2]});
    }

    public void setAlbum(String album, int trackId) {
        ContentValues values = new ContentValues();
        values.put("album", album);
        mDb.update("scrobbles", values, "_id=" + trackId, null);
    }

    public void setAlbumArtist(String albumartist, int trackId) {
        ContentValues values = new ContentValues();
        values.put("albumartist", albumartist);
        mDb.update("scrobbles", values, "_id=" + trackId, null);
    }

    public void setTrackArtist(String trackartist, int trackId) {
        ContentValues values = new ContentValues();
        values.put("trackartist", trackartist);
        mDb.update("scrobbles", values, "_id=" + trackId, null);
    }

    public void setArtist(String artist, int trackId) {
        ContentValues values = new ContentValues();
        values.put("artist", artist);
        mDb.update("scrobbles", values, "_id=" + trackId, null);
    }

    public void setTrack(String track, int trackId) {
        ContentValues values = new ContentValues();
        values.put("track", track);
        mDb.update("scrobbles", values, "_id=" + trackId, null);
    }

    /**
     *
     * @param napp the NetApp instance
     * @return the number of rows affected
     */
    public int deleteAllScrobbles(NetApp napp) {
        return mDb.delete(TABLENAME_CORRNETAPP_REPAIRED, "netappid = ?",
                new String[]{"" + napp.getValue()});
    }

    public int deleteAllScrobbledTracks(NetApp napp) {
        return mDb.delete(TABLENAME_CORRNETAPP_REPAIRED, "netappid = ? AND sentstatus = ?",
                new String[]{"" + napp.getValue(), "sent"});
    }

    public boolean cleanUpTracks() {
        mDb.execSQL("delete from scrobbles where scrobbles._id not in "
                + "(select trackid as _id from " + TABLENAME_CORRNETAPP_REPAIRED + ")");
        return true;
    }

    public boolean cleanUpScrobbledTracks() {
        mDb.execSQL("delete from scrobbles where scrobbles._id not in "
                + "(select trackid as _id from " + TABLENAME_CORRNETAPP_REPAIRED + " WHERE " + TABLENAME_CORRNETAPP_REPAIRED + ".sentstatus = 'sent')");
        return true;
    }

    private Track readTrack(Cursor c) {
        Track.Builder b = new Track.Builder();
        b.setMusicAPI(MusicAPI.fromDatabase(mCtx, c.getLong(c
                .getColumnIndex("musicapp"))));
        b.setArtist(c.getString(c.getColumnIndex("artist")));
        b.setAlbum(c.getString(c.getColumnIndex("album")));
        b.setTrack(c.getString(c.getColumnIndex("track")));
        b.setAlbumArtist(c.getString(c.getColumnIndex("albumartist")));
        b.setTrackArtist(c.getString(c.getColumnIndex("trackartist")));
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
        String sql = "select * from scrobbles, " + TABLENAME_CORRNETAPP_REPAIRED
                + " where scrobbles._id = trackid and sentstatus = '' and netappid = " + napp.getValue();
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

    public String[][] fetchHeartsArray(NetApp netApp){
        Cursor c;
        // try {
        String sql = "select * from " + TABLENAME_HEARTS + " where netapp = " + netApp.getValue() ;
        c = mDb.rawQuery(sql, null);

        int count = c.getCount();
        c.moveToFirst();
        String[][] tracks = new String[count][3];
        for (int i = 0; i < count; i++) {
            tracks[i][0] = c.getString(c.getColumnIndex(scrobbles_heart_only_strings[0]));
            tracks[i][1] = c.getString(c.getColumnIndex(scrobbles_heart_only_strings[1]));
            tracks[i][2] = c.getString(c.getColumnIndex(scrobbles_heart_only_strings[2]));
            c.moveToNext();
        }
        c.close();
        return tracks;
    }

    public Cursor fetchTracksCursor(NetApp napp, SortField sf) {
        Cursor c;
        String sql = "select * from scrobbles, " + TABLENAME_CORRNETAPP_REPAIRED
                + " where scrobbles._id = trackid and netappid = " + napp.getValue()
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

    public Track fetchRecentTrack() {
        String sql = "select * from scrobbles order by rowid desc limit 1";
        Cursor c = mDb.rawQuery(sql, null);

        if (c.getCount() == 0)
            return null;

        c.moveToFirst();
        Track track = readTrack(c);
        c.close();
        return track;
    }

    public NetApp[] fetchNetAppsForScrobble(int trackId) {
        String sql = "select netappid from  " + TABLENAME_CORRNETAPP_REPAIRED + " where trackid = "
                + trackId + " and sentstatus != 'sent'";
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
        if (mDb == null || !mDb.isOpen()) {
            open();
        }
        Cursor c = mDb.rawQuery("select count(_id) from scrobbles", null);
        int count = c.getCount();
        if (count != 0) {
            c.moveToFirst();
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    public int queryNumberOfUnscrobbledTracks() {
        if (mDb == null || !mDb.isOpen()) {
            open();
        }
        Cursor c = mDb.rawQuery("select count(distinct trackid) from " + TABLENAME_CORRNETAPP_REPAIRED + " where sentstatus = ''", null);
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
                "select count(trackid) from " + TABLENAME_CORRNETAPP_REPAIRED + " where netappid = "
                        + napp.getValue() + " and sentstatus != 'sent'"  , null);
        int count = c.getCount();
        if (count != 0) {
            c.moveToFirst();
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    private ContentValues generateCorrectionRuleValues(CorrectionRule rule) {
        ContentValues values = new ContentValues();
        values.put("track_to_change", rule.getTrackToChange());
        values.put("album_to_change", rule.getAlbumToChange());
        values.put("artist_to_change", rule.getArtistToChange());
        values.put("track_correction", rule.getTrackCorrection());
        values.put("album_correction", rule.getAlbumCorrection());
        values.put("artist_correction", rule.getArtistCorrection());
//        values.put("musicapp", rule.getMusicApp().getId());
        return values;
    }

    public long insertCorrectionRule(CorrectionRule rule) {
        return mDb.insert(TABLENAME_CORRECTION_RULES, null, generateCorrectionRuleValues(rule));
    }

    public int deleteCorrectionRule(int ruleId) {
        return mDb.delete(TABLENAME_CORRECTION_RULES, "_id = ?", new String[]{String.valueOf(ruleId)});
    }

    public Cursor fetchAllCorrectionRulesCursor() {
        return mDb.query(TABLENAME_CORRECTION_RULES, null, null, null, null, null, null, null);
    }

    public int deleteAllCorrectionRules() {
        return mDb.delete(TABLENAME_CORRECTION_RULES, null, null);
    }

    public int updateCorrectionRule(CorrectionRule rule) {
        return mDb.update(TABLENAME_CORRECTION_RULES, generateCorrectionRuleValues(rule), "_id = ?", new String[]{String.valueOf(rule.getId())});
    }

    public CorrectionRule fetchCorrectioneRule(int id) {
        Cursor c = mDb.query(TABLENAME_CORRECTION_RULES, null, "_id = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (c.getCount() == 0)
            return null;

        c.moveToFirst();
        CorrectionRule rule = new CorrectionRule();
        rule.setId(c.getInt(c.getColumnIndex("_id")));
//        rule.setMusicApp(MusicAPI.fromDatabase(mCtx, c.getLong(c.getColumnIndex("musicapp"))));
        rule.setTrackToChange(c.getString(c.getColumnIndex("track_to_change")));
        rule.setTrackCorrection(c.getString(c.getColumnIndex("track_correction")));
        rule.setAlbumToChange(c.getString(c.getColumnIndex("album_to_change")));
        rule.setAlbumCorrection(c.getString(c.getColumnIndex("album_correction")));
        rule.setArtistToChange(c.getString(c.getColumnIndex("artist_to_change")));
        rule.setArtistCorrection(c.getString(c.getColumnIndex("artist_correction")));
        c.close();
        return rule;
    }
}
