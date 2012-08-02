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

package com.adam.aslfms.util;

import com.adam.aslfms.receiver.MusicAPI;
import com.adam.aslfms.receiver.MusicAPITestUtils;

import com.adam.aslfms.util.Track;

public class TrackTestUtils {

	public static final MusicAPI TEST_MUSIC_API = MusicAPITestUtils.getDummyMusicAPI();
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
		return buildSimpleTrack(TEST_MUSIC_API);
	}

	public static Track buildSimpleTrack(MusicAPI mapi) {
		return buildTrack(mapi, TEST_ARTIST, TEST_ALBUM, TEST_TRACK);
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
		return buildFullTrack(TEST_MUSIC_API);
	}

	public static Track buildFullTrack(MusicAPI mapi) {
		return buildTrack(mapi, TEST_ARTIST, TEST_ALBUM, TEST_TRACK, TEST_DURATION, TEST_TRACK_NR, TEST_MBID,
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
