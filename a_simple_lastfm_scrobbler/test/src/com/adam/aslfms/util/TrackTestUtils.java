package com.adam.aslfms.util;

import com.adam.aslfms.receiver.MusicAPI;

import com.adam.aslfms.util.Track;

import static com.adam.aslfms.receiver.MusicAPITestUtils.*;

public class TrackTestUtils {

	public static final MusicAPI TEST_MUSIC_API = getDummyMusicAPI();
	public static final String TEST_ARTIST = "Adam";
	public static final String TEST_ALBUM = "Rocks";
	public static final String TEST_TRACK = "The World";
	public static final int TEST_DURATION = 123;
	public static final String TEST_TRACK_NR = "13";
	// TODO: make it look like a real mbid
	public static final String TEST_MBID = "asdf8o32479213924";
	public static final String TEST_SOURCE = "R";
	public static final String TEST_RATING = "";
	public static final long TEST_WHEN = 12394649;
	public static final int TEST_ROWID = 7;

	public static Track buildTrack() {
		return new Track();
	}

	public static Track buildSimpleTrack() {
		return buildTrack(TEST_MUSIC_API, TEST_ARTIST, TEST_ALBUM, TEST_TRACK);
	}

	public static Track buildTrack(MusicAPI mapi, String artist, String album, String track) {
		Track t = new Track();
		t.mMusicAPI = mapi;
		t.mArtist = artist;
		t.mAlbum = album;
		t.mTrack = track;
		return t;
	}

	public static Track buildFullTrack() {
		return buildTrack(TEST_MUSIC_API, TEST_ARTIST, TEST_ALBUM, TEST_TRACK, TEST_DURATION, TEST_TRACK_NR, TEST_MBID,
				TEST_SOURCE, TEST_RATING, TEST_WHEN, TEST_ROWID);
	}

	public static Track buildTrack(MusicAPI mapi, String artist, String album, String track, int duration,
			String tracknr, String mbid, String source, String rating, long when, int rowid) {
		Track t = new Track();
		t.mMusicAPI = mapi;
		t.mArtist = artist;
		t.mAlbum = album;
		t.mTrack = track;
		t.mDuration = duration;
		t.mTracknr = tracknr;
		t.mMbId = mbid;
		t.mSource = source;
		t.mRating = rating;
		t.mWhen = when;
		t.mRowId = rowid;
		return t;
	}

}
