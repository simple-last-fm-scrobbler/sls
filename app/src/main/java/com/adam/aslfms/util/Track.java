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

import android.os.SystemClock;

import com.adam.aslfms.receiver.MusicAPI;

/**
 * Simple "immutable" structure that holds information about a track. The only
 * way to create a track is through the {@link Builder}.
 * <p>
 * A track is immutable in the sense that the descriptive paramters (artist,
 * album, duration, etc.) cannot be changed. But there are methods for keeping
 * track of how long the track has been playing, for instance. See
 * {@link #updateTimePlayed()}} and {@link #setQueued()}.
 * <p>
 * The tracks are saved in a database using the {@link ScrobblesDatabase}.
 *
 * @author tgwizard
 * @since 0.9
 */
public class Track {

    // TODO: move me
    public enum State {
        START, RESUME, PAUSE, COMPLETE, PLAYLIST_FINISHED, UNKNOWN_NONPLAYING
    }

    /**
     * We have to use this, as signals sent to Scrobble Droid can be void of any
     * track information if it's "playing" boolean is set to false
     */
    public static final Track SAME_AS_CURRENT;

    static {
        SAME_AS_CURRENT = new Track();
        SAME_AS_CURRENT.mArtist = "SAME_AS_CURRENT";
        SAME_AS_CURRENT.mAlbum = "SAME_AS_CURRENT";
        SAME_AS_CURRENT.mTrack = "SAME_AS_CURRENT";
    }

    /**
     * The duration to use when a track's duration is unknown. E.g., the android
     * music player doesn't broadcast duration information, so this constant is
     * used instead.
     */
    public static final int DEFAULT_TRACK_LENGTH = 180;
    public static final long UNKNOWN_COUNT_POINT = -1;

    MusicAPI mMusicAPI;
    String mArtist;
    String mAlbum;
    String mAlbumArtist;
    String mTrackArtist;
    String mTrack;
    int mDuration;
    boolean mUnknownDuration;
    String mTracknr;
    String mMbId;
    String mSource;

    String mRating;

    long mWhen;
    int mRowId;

    // non-track-data stuff
    long mTimePlayed; // in milliseconds
    long mWhenToCountTimeFrom; // in milliseconds
    boolean mQueued;

    //long mStartTime; // in milliseconds

    /**
     * A class for constructing new tracks, using the Builder pattern. The only
     * way to create tracks, which then become "immutable".
     *
     * @see #build()
     *
     * @author tgwizard
     * @since 1.2
     */
    public static class Builder {
        Track _track;

        public Builder() {
            _track = new Track();
        }

        public void setMusicAPI(MusicAPI musicAPI) {
            _track.mMusicAPI = musicAPI;
        }

        public void setArtist(String artist) {
            _track.mArtist = artist;
        }

        public void setAlbum(String album) {
            _track.mAlbum = album == null ? "" : album;
        }

        public void setAlbumArtist(String albumArtist){
            _track.mAlbumArtist = albumArtist == null ? "" : albumArtist;
        }

        public void setTrackArtist(String trackArtist){
            _track.mTrackArtist = trackArtist == null ? "" : trackArtist;
        }

        public void setTrack(String track) {
            _track.mTrack = track;
        }

        public void setDuration(int duration) {
            _track.mDuration = duration;
            _track.mUnknownDuration = false;
        }

        public void setTrackNr(String tracknr) {
            _track.mTracknr = tracknr == null ? "" : tracknr;
        }

        public void setMbid(String mbid) {
            _track.mMbId = mbid == null ? "" : mbid;
        }

        public void setSource(String source) {
            _track.mSource = source;
        }

        public void setRating(String rating) {
            _track.mRating = rating;
        }

        public void setWhen(long when) {
            _track.mWhen = when;
        }

        public void setRowId(int rowId) {
            _track.mRowId = rowId;
        }

        /**
         * Validates and creates a track with the values set using the setters
         * for this class.
         *
         * @return the new, validated, track
         * @throws IllegalArgumentException
         *             if any of the fields for the track-to-be are invalid
         */
        public Track build() throws IllegalArgumentException {
            _track.validate();
            return _track;
        }
    }

    Track() {
        mMusicAPI = null;
        mArtist = null;
        mAlbum = "";
        mAlbumArtist = "";
        mTrackArtist = "";
        mTrack = null;
        mDuration = DEFAULT_TRACK_LENGTH;
        mUnknownDuration = true;
        mTracknr = "";
        mMbId = "";
        mSource = "P";
        mWhen = -1;
        mRowId = -1;
        mRating = "";

        // non-track-data stuff
        mQueued = false;
        mTimePlayed = 0;
        mWhenToCountTimeFrom = UNKNOWN_COUNT_POINT;

        //mStartTime = Util.currentTimeSecsUTC();
    }

    void validate() throws IllegalArgumentException {
        if (mMusicAPI == null)
            throw new IllegalArgumentException("music api is null");

        if (mArtist == null || mArtist.length() == 0)
            throw new IllegalArgumentException("artist is null or empty");

        if (mAlbum == null)
            throw new IllegalArgumentException("album is null");

        if (mAlbumArtist == null)
            throw new IllegalArgumentException("album is null");

        if (mTrackArtist == null)
            throw new IllegalArgumentException("album is null");

        if (mTrack == null || mTrack.length() == 0)
            throw new IllegalArgumentException("track is null or empty");

        if (mDuration < 0)
            throw new IllegalArgumentException("negative duration: "
                    + mDuration);

        if (mTracknr == null)
            throw new IllegalArgumentException("tracknr is null");

        if (mMbId == null) {
            throw new IllegalArgumentException("mbid is null");
        }

        if (mSource == null
                || !(mSource.equals("P") || mSource.equals("R")
                || mSource.equals("U") || mSource.equals("E"))) {
            throw new IllegalArgumentException("source is invalid: " + mSource);
        }

        if (mRating == null || !(mRating.equals("") || mRating.equals("L")))
            throw new IllegalArgumentException("rating is invalid: " + mRating);

        if (mWhen < 0) {
            throw new IllegalArgumentException("when is negative");
        }
    }

    public MusicAPI getMusicAPI() {
        return mMusicAPI;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public String getAlbumArtist() {
        return mAlbumArtist;
    }

    public String getTrackArtist() {
        return mTrackArtist;
    }

    public String getTrack() {
        return mTrack;
    }

    public int getDuration() {
        return mDuration;
    }

    /**
     * Returns whether the duration of this track is unknown (i.e. set to
     * {@link #DEFAULT_TRACK_LENGTH} or known.
     *
     * @return true if the duration is unknown, false otherwise
     */
    public boolean hasUnknownDuration() {
        return mUnknownDuration;
    }

    public String getTrackNr() {
        return mTracknr;
    }

    public String getSource() {
        return mSource;
    }

    public String getMbid() {
        return mMbId;
    }

    public String getRating() {
        return mRating;
    }

    public long getWhen() {
        return mWhen;
    }


    /**
     * Returns the database id for this track, or -1 if not loaded from the
     * database.
     *
     * @see ScrobblesDatabase
     *
     * @return the database id, or -1 if not loaded from the database
     */
    public int getRowId() {
        return mRowId;
    }

    /**
     * Specifies that this track has been queued for scrobbling (i.e. added to
     * the scrobble cache database table, see {@link ScrobblesDatabase}).
     *
     * @see #hasBeenQueued()
     */
    public void setQueued() {
        mQueued = true;
    }

    /**
     * Temporary fix for heart track feature.
     */
    public void setRating() {
        mRating = "L";
    }

    /**
     * Returns whether this track has been queued for scrobbling.
     *
     * @see #setQueued()
     *
     * @return true if this track has been queued for scrobbling, false
     *         otherwise
     */
    public boolean hasBeenQueued() {
        return mQueued;
    }

    /**
     * Returns the duration for which this track has been played, in
     * milliseconds.
     *
     * @see #updateTimePlayed()
     *
     * @return the duration for which this track has been played, in
     *         milliseconds
     */
    public long getTimePlayed() {
        return mTimePlayed;
    }

    /**
     * TODO:
     *
     *
     */

    public void updateTimePlayed() {
        long currentTime = SystemClock.elapsedRealtime();
        if (mWhenToCountTimeFrom != UNKNOWN_COUNT_POINT) {
            // only if we have a valid point to count from
            incTimePlayed(currentTime - mWhenToCountTimeFrom);
        }
        mWhenToCountTimeFrom = currentTime;
    }

    public void stopCountingTime() {
        mWhenToCountTimeFrom = UNKNOWN_COUNT_POINT;
    }

    void incTimePlayed(long tp) {
        if (tp < 0) {
            // this might happen if the user has changed the system clock
            throw new IllegalArgumentException("time-played increase was negative: " + tp);
        } else {
            mTimePlayed += tp;
        }
    }

    @Override
    public String toString() {
        return "Track [mTrack="
                + mTrack + ", mArtist=" + mArtist + ", mAlbum=" + mAlbum
                + ", mAlbumArtist=" + mAlbumArtist
                + ", mDuration=" + mDuration + ", mMbId=" + mMbId
                + ", mMusicAPI=" + mMusicAPI + ", mQueued=" + mQueued
                + ", mRating=" + mRating + ", mRowId=" + mRowId + ", mSource="
                + mSource + ", mTimePlayed=" + mTimePlayed
                + ", mTracknr=" + mTracknr + ", mUnknownDuration="
                + mUnknownDuration + ", mWhen=" + mWhen
                + ", mWhenToCountTimeFrom=" + mWhenToCountTimeFrom + "]";
        //+ ", mStartTime=" + mStartTime + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mAlbum == null) ? 0 : mAlbum.hashCode());
        result = prime * result + ((mArtist == null) ? 0 : mArtist.hashCode());
        result = prime * result
                + ((mMusicAPI == null) ? 0 : mMusicAPI.hashCode());
        result = prime * result + ((mTrack == null) ? 0 : mTrack.hashCode());
        return result;
    }

    /**
     * Only checks artist, album and track strings (+ {@link MusicAPI}), which
     * means that tracks sent to ScrobblingService can be properly compared.
     *
     * Temporary fix for apps with multiple broadcasts. (usually Android Music Player and SLS API.
     * SLS Receiver & Builtin Music Player.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Track other = (Track) obj;
/**        if (mAlbum == null) {
 if (other.mAlbum != null)
 return false;
 } else if (!mAlbum.equals(other.mAlbum))
 return false;
 */
        if (mArtist == null) {
            if (other.mArtist != null)
                return false;
        } else if (!mArtist.equals(other.mArtist))
            return false;
/**        if (mMusicAPI == null) {
 if (other.mMusicAPI != null)
 return false;
 } else if (!mMusicAPI.equals(other.mMusicAPI))
 return false;
 */
        if (mTrack == null) {
            if (other.mTrack != null)
                return false;
        } else if (!mTrack.equals(other.mTrack))
            return false;
        return true;
    }
}
