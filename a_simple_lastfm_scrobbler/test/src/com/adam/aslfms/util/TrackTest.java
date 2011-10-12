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
 * 
 */

package com.adam.aslfms.util;

import com.adam.aslfms.util.Track.Builder;

import android.test.AndroidTestCase;

import static com.adam.aslfms.util.TrackTestUtils.*;

public class TrackTest extends AndroidTestCase {

	public static final String RANDOM_TEST_STRING = "-asdf934";
	Track emptyTrack, simpleTrack, a, b, fullTrack;

	// TODO: test time played
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		emptyTrack = buildTrack();
		simpleTrack = buildSimpleTrack();
		a = buildSimpleTrack();
		b = buildSimpleTrack();
		fullTrack = buildFullTrack();
	}

	public void testGettersSimple() {
		assertSame(fullTrack.getMusicAPI(), TEST_MUSIC_API);
		assertEquals(fullTrack.getArtist(), TEST_ARTIST);
		assertEquals(fullTrack.getAlbum(), TEST_ALBUM);
		assertEquals(fullTrack.getTrack(), TEST_TRACK);
		assertEquals(fullTrack.getDuration(), TEST_DURATION);
		assertEquals(fullTrack.getTrackNr(), TEST_TRACK_NR);
		assertEquals(fullTrack.getMbid(), TEST_MBID);
		assertEquals(fullTrack.getSource(), TEST_SOURCE);
	}

	public void testQueued() {
		assertFalse(emptyTrack.hasBeenQueued());
		emptyTrack.setQueued();
		assertTrue(emptyTrack.hasBeenQueued());
	}

	public void testEquals() {
		assertEquals(emptyTrack, emptyTrack);
		assertEquals(a, b);
	}

	public void testEqualsIgnoredFields() {
		assertEquals(simpleTrack, fullTrack);
		a.mDuration = 15;
		assertEquals(a, b);
		b.mMbId = "asdf";
		assertEquals(a, b);
		a.mTracknr = "123123";
		assertEquals(a, b);
		a.mWhen = 123;
		b.mWhen = 456;
		assertEquals(a, b);
	}

	public void testNotEquals() {
		// TODO: MusicAPI
		Track a = buildSimpleTrack();
		Track b;
		b = buildSimpleTrack();
		b.mArtist = RANDOM_TEST_STRING;
		assertFalse(a.equals(b));
		b = buildSimpleTrack();
		b.mAlbum = RANDOM_TEST_STRING;
		assertFalse(a.equals(b));
		b = buildSimpleTrack();
		b.mTrack = RANDOM_TEST_STRING;
		assertFalse(a.equals(b));
		assertFalse(emptyTrack.equals(fullTrack));
		assertFalse(emptyTrack.equals(null));
	}

	public void testValidateSimple() {
		ensureValid(fullTrack);
	}

	public void testValidateMusicAPI() {
		fullTrack.mMusicAPI = null;
		ensureInvalid(fullTrack);
	}

	public void testValidateArtist() {
		fullTrack.mArtist = null;
		ensureInvalid(fullTrack);
		fullTrack.mArtist = "";
		ensureInvalid(fullTrack);
	}

	public void testValidateAlbum() {
		fullTrack.mAlbum = null;
		ensureInvalid(fullTrack);
	}

	public void testValidateTrack() {
		fullTrack.mTrack = null;
		ensureInvalid(fullTrack);
		fullTrack.mTrack = "";
		ensureInvalid(fullTrack);
	}

	public void testValidateDuration() {
		fullTrack.mDuration = -1;
		ensureInvalid(fullTrack);
		fullTrack.mDuration = 0;
		ensureValid(fullTrack);
		fullTrack.mDuration = 180;
		ensureValid(fullTrack);
	}

	public void testValidateTrackNr() {
		fullTrack.mTracknr = null;
		ensureInvalid(fullTrack);
	}

	public void testValidateMbId() {
		fullTrack.mMbId = null;
		ensureInvalid(fullTrack);
	}

	public void testValidateSource() {
		fullTrack.mSource = null;
		ensureInvalid(fullTrack);
		fullTrack.mSource = "";
		ensureInvalid(fullTrack);
		fullTrack.mSource = RANDOM_TEST_STRING;
		ensureInvalid(fullTrack);
		fullTrack.mSource = "P";
		ensureValid(fullTrack);
		fullTrack.mSource = "U";
		ensureValid(fullTrack);
		fullTrack.mSource = "R";
		ensureValid(fullTrack);
		fullTrack.mSource = "E";
		ensureValid(fullTrack);
	}

	public void testValidateRating() {
		fullTrack.mRating = null;
		ensureInvalid(fullTrack);
		fullTrack.mRating = RANDOM_TEST_STRING;
		ensureInvalid(fullTrack);
		fullTrack.mRating = "";
		ensureValid(fullTrack);
		fullTrack.mRating = "L";
		ensureValid(fullTrack);
	}

	public void testValidateWhen() {
		fullTrack.mWhen = -1;
		ensureInvalid(fullTrack);
		fullTrack.mWhen = 0;
		ensureValid(fullTrack);
		fullTrack.mWhen = 180;
		ensureValid(fullTrack);
	}

	void ensureValid(Track t) {
		try {
			t.validate();
		} catch (IllegalArgumentException e) {
			fail();
		}
	}

	void ensureInvalid(Track t) {
		try {
			t.validate();
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testBuilderMinimal() {
		Builder b = new Builder();
		b.setMusicAPI(TEST_MUSIC_API);
		b.setArtist(TEST_ARTIST);
		b.setAlbum(TEST_ALBUM);
		b.setTrack(TEST_TRACK);
		b.setWhen(TEST_WHEN);
		Track t = b.build();
		assertEquals(t, simpleTrack);
	}

	public void testBuilderFull() {
		Builder b = new Builder();
		b.setMusicAPI(TEST_MUSIC_API);
		b.setArtist(TEST_ARTIST);
		b.setAlbum(TEST_ALBUM);
		b.setTrack(TEST_TRACK);
		b.setDuration(TEST_DURATION);
		b.setMbid(TEST_MBID);
		b.setTrackNr(TEST_TRACK_NR);
		b.setSource(TEST_SOURCE);
		b.setWhen(TEST_WHEN);
		b.setRowId(TEST_ROWID);
		Track t = b.build();
		assertEquals(t, fullTrack);
	}

	public void testBuilderMissingWhen() {
		Builder b = new Builder();
		b.setMusicAPI(TEST_MUSIC_API);
		b.setArtist(TEST_ARTIST);
		b.setAlbum(TEST_ALBUM);
		try {
			b.build();
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

}
