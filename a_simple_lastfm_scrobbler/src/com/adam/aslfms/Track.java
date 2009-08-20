package com.adam.aslfms;

public class Track {
	private CharSequence artist;
	private CharSequence album;
	private CharSequence track;
	private int duration;
	private long when;
	private int rowId;

	public Track(CharSequence artist, CharSequence album, CharSequence track,
			int duration, long when) {
		this(artist, album, track, duration, when, -1);
	}

	public Track(CharSequence artist, CharSequence album, CharSequence track,
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
