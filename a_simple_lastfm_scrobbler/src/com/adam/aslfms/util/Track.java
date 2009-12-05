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

import com.adam.aslfms.receiver.MusicApp;

/**
 * Simple structure that holds information about a track. It has two factory
 * methods to construct new tracks,
 * {@link Track#createTrack(CharSequence, CharSequence, CharSequence, int, long)
 * createTrack()} and
 * {@link Track#createTrackFromDb(CharSequence, CharSequence, CharSequence, int, long, int)
 * createTrackFromDB()}.
 * 
 * @see Track#createTrack(CharSequence, CharSequence, CharSequence, int, long)
 * @see Track#createTrackFromDb(CharSequence, CharSequence, CharSequence, int,
 *      long, int)
 * @author tgwizard
 * 
 */
public class Track {

	// TODO: move me
	public enum State {
		START, RESUME, PAUSE, COMPLETE, PLAYLIST_FINISHED, UNKNOWN_NONPLAYING
	};
	
	// We have to use this, as signals sent to scrobble droid can be void of any
	// track information if it's "playing" boolean is set to false
	public static final Track SAME_AS_CURRENT;
	
	static {
		Builder b = new Builder();
		b.setMusicApp(MusicApp.SCROBBLE_DROID_SUPPORTED_APPS);
		b.setArtist("This is not good");
		b.setTrack("But whatever");
		b.setWhen(1);
		SAME_AS_CURRENT = b.build();
	}

	/**
	 * The duration to use when a track's duration is unknown. E.g., the android
	 * music player doesn't broadcast duration information, so this constant is
	 * used instead.
	 */
	public static final int DEFAULT_TRACK_LENGTH = 180;
	public static final long UNKNOWN_COUNT_POINT = -1;

	private MusicApp mMusicApp;
	private String mArtist;
	private String mAlbum;
	private String mTrack;
	private int mDuration;
	private boolean mUnknownDuration;
	private String mTracknr;
	private String mMbId;
	private String mSource;

	private long mWhen;
	private int mRowId;
	
	// non-track-data stuff
	private long mTimePlayed; // in milliseconds
	private long mWhenToCountTimeFrom; // in milliseconds
	private boolean mQueued;

	public static class Builder {
		Track _track;

		public Builder() {
			_track = new Track();
		}

		public void setMusicApp(MusicApp musicApp) {
			_track.mMusicApp = musicApp;
		}

		public void setArtist(String artist) {
			_track.mArtist = artist;
		}

		public void setAlbum(String album) {
			_track.mAlbum = album == null ? "" : album;
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

		public void setWhen(long when) {
			_track.mWhen = when;
		}

		public void setRowId(int rowId) {
			_track.mRowId = rowId;
		}

		public Track build() throws IllegalArgumentException {
			_track.validate();
			return _track;
		}
	}

	private Track() {
		mMusicApp = null;
		mArtist = null;
		mAlbum = "";
		mTrack = null;
		mDuration = DEFAULT_TRACK_LENGTH;
		mUnknownDuration = true;
		mTracknr = "";
		mMbId = "";
		mSource = "P";
		mWhen = -1;
		mRowId = -1;
		
		// non-track-data stuff
		mQueued = false;
		mTimePlayed = 0;
		mWhenToCountTimeFrom = UNKNOWN_COUNT_POINT;
	}

	public void validate() throws IllegalArgumentException {
		if (mMusicApp == null)
			throw new IllegalArgumentException("music app is null");
		
		if (mArtist == null || mArtist.length() == 0)
			throw new IllegalArgumentException("artist is null or empty");

		if (mAlbum == null)
			throw new IllegalArgumentException("album is null");

		if (mTrack == null || mTrack.length() == 0)
			throw new IllegalArgumentException("track is null or empty");

		if (mDuration < 0)
			throw new IllegalArgumentException("duration is negative");

		if (mTracknr == null)
			throw new IllegalArgumentException("tracknr is null");

		if (mMbId == null) {
			throw new IllegalArgumentException("mbid is null");
		}

		if (!mSource.equals("P") && !mSource.equals("R") && !mSource.equals("U")
				&& !mSource.equals("E")) {
			throw new IllegalArgumentException("source is invalid, \"" + mSource
					+ "\"");
		}

		if (mWhen < 0) {
			throw new IllegalArgumentException("when is negative");
		}
	}

	public MusicApp getMusicApp() {
		return mMusicApp;
	}

	public String getArtist() {
		return mArtist;
	}

	public String getAlbum() {
		return mAlbum;
	}

	public String getTrack() {
		return mTrack;
	}

	public int getDuration() {
		return mDuration;
	}
	
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

	public long getWhen() {
		return mWhen;
	}

	public int getRowId() {
		return mRowId;
	}
	
	public void setQueued() {
		mQueued = true;
	}
	
	public boolean hasBeenQueued() {
		return mQueued;
	}
	
	public long getTimePlayed() {
		return mTimePlayed;
	}
	
	public void updateTimePlayed(long currentTime) {
		if (currentTime != UNKNOWN_COUNT_POINT && mWhenToCountTimeFrom != UNKNOWN_COUNT_POINT) {
			// only if we have a valid points to count from
			incTimePlayed(currentTime - mWhenToCountTimeFrom);
		}
		mWhenToCountTimeFrom = currentTime;
	}
	/**
	 * 
	 * @param tp nonnegative time increase in milliseconds
	 */
	public void incTimePlayed(long tp) {
		if (tp < 0)
			throw new IllegalArgumentException("time-played increase was negative: " + tp);
		mTimePlayed += tp;
	}

	@Override
	public String toString() {
		return "Track [mAlbum=" + mAlbum + ", mArtist=" + mArtist
				+ ", mDuration=" + mDuration + ", mMbId=" + mMbId
				+ ", mMusicApp=" + mMusicApp + ", mQueued=" + mQueued
				+ ", mRowId=" + mRowId + ", mSource=" + mSource
				+ ", mTimePlayed=" + mTimePlayed + ", mTrack=" + mTrack
				+ ", mTracknr=" + mTracknr + ", mUnknownDuration="
				+ mUnknownDuration + ", mWhen=" + mWhen + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mAlbum == null) ? 0 : mAlbum.hashCode());
		result = prime * result + ((mArtist == null) ? 0 : mArtist.hashCode());
		result = prime * result
				+ ((mMusicApp == null) ? 0 : mMusicApp.hashCode());
		result = prime * result + ((mTrack == null) ? 0 : mTrack.hashCode());
		return result;
	}

	/**
	 * Only checks artist, album and track strings (+ MusicApp), which means
	 * that tracks sent to ScrobblingService can be properly compared.
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
		if (mAlbum == null) {
			if (other.mAlbum != null)
				return false;
		} else if (!mAlbum.equals(other.mAlbum))
			return false;
		if (mArtist == null) {
			if (other.mArtist != null)
				return false;
		} else if (!mArtist.equals(other.mArtist))
			return false;
		if (mMusicApp == null) {
			if (other.mMusicApp != null)
				return false;
		} else if (!mMusicApp.equals(other.mMusicApp))
			return false;
		if (mTrack == null) {
			if (other.mTrack != null)
				return false;
		} else if (!mTrack.equals(other.mTrack))
			return false;
		return true;
	}
}
