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

package com.adam.aslfms.adc2;

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

	/**
	 * The duration to use when a track's duration is unknown. E.g., the android
	 * music player doesn't broadcast duration information, so this constant is
	 * used instead.
	 */
	public static final int DEFAULT_TRACK_LENGTH = 180;

	private CharSequence artist;
	private CharSequence album;
	private CharSequence track;
	private int duration;
	private long when;
	private int rowId;

	/**
	 * This factory method should be used when the track data is NOT taken from
	 * the scrobble database.
	 * 
	 * @param artist
	 * @param album
	 * @param track
	 * @param duration
	 *            The duration of the track, in seconds
	 * @param when
	 *            The time the track started playing, in seconds since epoch,
	 *            UTC
	 */
	public static Track createTrack(CharSequence artist, CharSequence album,
			CharSequence track, int duration, long when) {
		return new Track(artist, album, track, duration, when, -1);
	}

	/**
	 * This factory method should be used when the track data IS taken from the
	 * scrobble database.
	 * 
	 * @param artist
	 *            The artist name
	 * @param album
	 *            The album name
	 * @param track
	 *            The song name
	 * @param duration
	 *            The duration of the track, in seconds
	 * @param when
	 *            The time the track started playing, in seconds since epoch,
	 *            UTC
	 * @param rowId
	 *            The id for the corresponding row in the scrobble database.
	 */
	public static Track createTrackFromDb(CharSequence artist,
			CharSequence album, CharSequence track, int duration, long when,
			int rowId) {
		return new Track(artist, album, track, duration, when, rowId);
	}

	private Track(CharSequence artist, CharSequence album, CharSequence track,
			int duration, long when, int rowId) {
		super();
		this.artist = artist;
		this.album = album;
		this.track = track;
		this.duration = duration;
		this.when = when;
		this.rowId = rowId;
	}

	public CharSequence getArtist() {
		return artist;
	}

	public CharSequence getAlbum() {
		return album;
	}

	public CharSequence getTrack() {
		return track;
	}

	public int getDuration() {
		return duration;
	}

	public long getWhen() {
		return when;
	}

	public int getRowId() {
		return rowId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((album == null) ? 0 : album.hashCode());
		result = prime * result + ((artist == null) ? 0 : artist.hashCode());
		result = prime * result + ((track == null) ? 0 : track.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "Track [album=" + album + ", artist=" + artist + ", duration="
				+ duration + ", rowId=" + rowId + ", track=" + track
				+ ", when=" + when + "]";
	}

	/**
	 * Only checks artist, album and track strings, which means that tracks sent
	 * to ScrobblingService can be properly compared.
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
		if (album == null) {
			if (other.album != null)
				return false;
		} else if (!album.equals(other.album))
			return false;
		if (artist == null) {
			if (other.artist != null)
				return false;
		} else if (!artist.equals(other.artist))
			return false;
		if (track == null) {
			if (other.track != null)
				return false;
		} else if (!track.equals(other.track))
			return false;
		return true;
	}

}
