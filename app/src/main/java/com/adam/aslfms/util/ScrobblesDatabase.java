/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p>
 * Copyright 2011 Simple Last.fm Scrobbler Team
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

    private static final String ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = ON;";

    private static final String TABLENAME_SCROBBLES = "scrobbles";
    private static final String TABLENAME_CORRNETAPP = "scrobbles_netapp";
    private static final String TABLENAME_CORRECTION_RULES = "correction_rules";
    private static final String TABLENAME_RULE_CHANGES = "rule_changes";
    private static final String TRIGGER_NAME_CHECK_CORRECTION_RULES = "check_correction_rules";

    private static final String DATABASE_CREATE_CORRECTION_RULES =
            "create table correction_rules (" +
                    "	_id integer primary key autoincrement," +
                    "	track_to_change text not null," +
                    "	album_to_change text not null," +
                    "	artist_to_change text not null," +
                    "	track_correction text not null," +
                    "	album_correction text not null," +
                    "	artist_correction text not null" + // Remember to add ',' when integrating musicapp support
//                    "	musicapp integer not null" +
                    ");";

    private static final String DATABASE_CREATE_RULE_CHANGES =
            "create table rule_changes (" +
                    "	track_id integer primary key," +
                    "	original_track text not null," +
                    "	original_album text not null," +
                    "	original_artist text not null," +
                    "	foreign key (track_id) references scrobbles(_id) on delete cascade on update cascade" +
                    ");";

    private static final String DATABASE_CREATE_SCROBBLES = "create table scrobbles ("
            + "_id integer primary key autoincrement, " //
            + "musicapp integer not null, " //
            + "artist text not null, " //
            + "album text not null, " //
            + "albumartist text not null, " //
            + "trackartist text not null, " //
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
            + "foreign key (trackid) references scrobbles(_id) "
            + "on delete cascade on update cascade)";

    private static final String TRIGGGER_CREATE_CHECK_CORRECTION_RULES =
            "create trigger check_correction_rules" +
                    "	after insert on scrobbles" +
                    "	for each row" +
                    "	when (select count(*) from correction_rules where new.track = track_to_change and new.album = album_to_change and new.artist = artist_to_change) = 1 " +
                    "begin" +
                    "	insert into rule_changes (track_id, original_track, original_album, original_artist)" +
                    "		select _id track_id, track original_track, album original_album, artist original_artist" +
                    "		from scrobbles" +
                    "		where _id = new._id;" +
                    "	update scrobbles" +
                    "		set track = (select track_correction from correction_rules where new.track = track_to_change and new.album = album_to_change and new.artist = artist_to_change)," +
                    "		album = (select album_correction from correction_rules where new.track = track_to_change and new.album = album_to_change and new.artist = artist_to_change)," +
                    "		artist = (select artist_correction from correction_rules where new.track = track_to_change and new.album = album_to_change and new.artist = artist_to_change) where _id = new._id; " +
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
            Log.d(TAG, "create sql scrobbles: " + DATABASE_CREATE_SCROBBLES);
            Log.d(TAG, "create sql corrnetapp: " + DATABASE_CREATE_CORRNETAPP);
            db.execSQL(DATABASE_CREATE_SCROBBLES);
            db.execSQL(DATABASE_CREATE_CORRNETAPP);
            // Tables and trigger for updating scrobbles based on rules.
            db.execSQL(DATABASE_CREATE_CORRECTION_RULES);
            db.execSQL(DATABASE_CREATE_RULE_CHANGES);
            db.execSQL(TRIGGGER_CREATE_CHECK_CORRECTION_RULES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading scrobbles database from version "
                    + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_SCROBBLES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_CORRNETAPP);
            // TODO add migration of old rules if/when necessary
            db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_CORRECTION_RULES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_RULE_CHANGES);
            db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_NAME_CHECK_CORRECTION_RULES);
            onCreate(db);
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

        return mDb.insert(TABLENAME_CORRNETAPP, null, iVals) > 0;
    }

    public int deleteScrobble(NetApp napp, int trackId) {
        if (trackId == -1) {
            Log.e(TAG, "Trying to delete scrobble with trackId == -1");
            return -2;
        }
        return mDb.delete(TABLENAME_CORRNETAPP, "netappid = ? and trackid = ?",
                new String[]{"" + napp.getValue(), "" + trackId});
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
        return mDb.delete(TABLENAME_CORRNETAPP, "netappid = ?",
                new String[]{"" + napp.getValue()});
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

    public void loveRecentTrack() {
        String sql = "select * from scrobbles order by rowid desc limit 1";
        Cursor c = mDb.rawQuery(sql, null);

        if (c.getCount() == 0)
            return;

        c.moveToFirst();
        long trackId = c.getLong(c.getColumnIndex("_id"));
        ContentValues values = new ContentValues();
        values.put("rating", "L");
        mDb.update("scrobbles", values, "_id=" + trackId, null);
        c.close();
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

    // TODO: DELETE ME AFTER !!!

    public void alterDataBaseOnce(){
        try {
            try {
                mDb.execSQL("SELECT trackartist FROM " + TABLENAME_SCROBBLES); // check if table column exists
            } catch (Exception e){
                mDb.execSQL("ALTER TABLE " + TABLENAME_SCROBBLES + " ADD COLUMN trackartist text DEFAULT '' not null"); // if column not exists create column
            }
        } catch (Exception ignore) {
            // may capture already exists trackartist
        }
        try {
            try {
                mDb.execSQL("SELECT albumartist FROM " + TABLENAME_SCROBBLES); // check if table column exists
            } catch (Exception e){
                mDb.execSQL("ALTER TABLE " + TABLENAME_SCROBBLES + " ADD COLUMN albumartist text DEFAULT '' not null"); // if column not exists create column
            }
        } catch (Exception ignore) {
            // may capture already exists albumartist
        }
    }
}
