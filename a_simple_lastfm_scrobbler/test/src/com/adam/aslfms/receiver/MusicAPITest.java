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

package com.adam.aslfms.receiver;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;
import android.test.suitebuilder.annotation.MediumTest;

@MediumTest
public class MusicAPITest extends AndroidTestCase {

	static final String TEST_NAME_1 = "Test Name 1";
	static final String TEST_PKG_1 = "asdf.asdf.asdf";
	static final boolean TEST_CLASH_1 = false;
	static final String TEST_MSG_1 = null;

	static final String TEST_NAME_2 = "Awesome Music Player";
	static final String TEST_PKG_2 = "bara.cuda.is.a.fish";
	static final boolean TEST_CLASH_2 = false;
	static final String TEST_MSG_2 = null;

	static final String RANDOM_NAME = "345967363904567390456";
	static final String RANDOM_PKG = "asdf.wotrul.aweiroya.sdoiert.";
	static final String RANDOM_MSG = "dot dot dot dot comma";
	static final boolean RANDOM_CLASH = true;

	private Context ctx;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ctx = new IsolatedContext(new MockContentResolver(), getContext());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		MusicAPITestUtils.deleteDatabase(ctx);
		ctx = null;
	}

	MusicAPI getMusicAPIA() {
		return MusicAPI.fromReceiver(ctx, TEST_NAME_1, TEST_PKG_1, TEST_MSG_1, TEST_CLASH_1);
	}

	MusicAPI getMusicAPIB() {
		return MusicAPI.fromReceiver(ctx, TEST_NAME_2, TEST_PKG_2, TEST_MSG_2, TEST_CLASH_2);
	}

	public void testFromReceiver() {
		MusicAPI a = getMusicAPIA();
		assertNotNull(a);
	}

	public void testFromReceiverInvalidName() {
		try {
			MusicAPI.fromReceiver(ctx, null, TEST_PKG_1, TEST_MSG_1, TEST_CLASH_1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testFromReceiverInvalidPkg() {
		try {
			MusicAPI.fromReceiver(ctx, TEST_NAME_1, null, TEST_MSG_1, TEST_CLASH_1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testGetters() {
		MusicAPI a = getMusicAPIA();
		assertEquals(1, a.getId());
		assertEquals(TEST_NAME_1, a.getName());
		assertEquals(TEST_PKG_1, a.getPackage());
		assertEquals(TEST_MSG_1, a.getMessage());
		assertEquals(TEST_CLASH_1, a.clashesWithScrobbleDroid());
		MusicAPI b = getMusicAPIB();
		assertEquals(2, b.getId());
		assertEquals(TEST_NAME_2, b.getName());
		assertEquals(TEST_PKG_2, b.getPackage());
		assertEquals(TEST_MSG_2, b.getMessage());
		assertEquals(TEST_CLASH_2, b.clashesWithScrobbleDroid());
		MusicAPI c = MusicAPI.fromReceiver(ctx, RANDOM_NAME, RANDOM_PKG, RANDOM_MSG, RANDOM_CLASH);
		assertEquals(3, c.getId());
		assertEquals(RANDOM_NAME, c.getName());
		assertEquals(RANDOM_PKG, c.getPackage());
		assertEquals(RANDOM_MSG, c.getMessage());
		assertEquals(RANDOM_CLASH, c.clashesWithScrobbleDroid());
	}

	public void testGettersFromDB() {
		MusicAPI a = getMusicAPIA();
		a = MusicAPI.fromDatabase(ctx, a.getId());
		assertEquals(1, a.getId());
		assertEquals(TEST_NAME_1, a.getName());
		assertEquals(TEST_PKG_1, a.getPackage());
		assertEquals(TEST_MSG_1, a.getMessage());
		assertEquals(TEST_CLASH_1, a.clashesWithScrobbleDroid());
		MusicAPI b = getMusicAPIB();
		b = MusicAPI.fromDatabase(ctx, b.getId());
		assertEquals(2, b.getId());
		assertEquals(TEST_NAME_2, b.getName());
		assertEquals(TEST_PKG_2, b.getPackage());
		assertEquals(TEST_MSG_2, b.getMessage());
		assertEquals(TEST_CLASH_2, b.clashesWithScrobbleDroid());
	}

	public void testEnabling() {
		MusicAPI a = getMusicAPIA();
		assertTrue(a.isEnabled());
		a.setEnabled(ctx, false);
		assertFalse(a.isEnabled());
		MusicAPI b = MusicAPI.fromDatabase(ctx, a.getId());
		assertFalse(b.isEnabled());
		b.setEnabled(ctx, true);
		assertTrue(b.isEnabled());
		a = MusicAPI.fromDatabase(ctx, b.getId());
		assertTrue(a.isEnabled());
	}

	public void testEqualsSame() {
		MusicAPI a = getMusicAPIA();
		MusicAPI b = getMusicAPIA();
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(a, b);
	}

	public void testFromEqualsDifferentButSame() {
		MusicAPI a = getMusicAPIA();
		MusicAPI b = MusicAPI.fromReceiver(ctx, TEST_NAME_2, TEST_PKG_1, TEST_MSG_2, TEST_CLASH_2);
		assertNotNull(a);
		assertNotNull(b);
		assertEquals(a, b);
	}

	public void testNotEquals() {
		MusicAPI a = getMusicAPIA();
		MusicAPI b = getMusicAPIB();
		MusicAPI c = MusicAPI.fromReceiver(ctx, TEST_NAME_2, RANDOM_PKG, TEST_MSG_2, TEST_CLASH_2);
		assertNotNull(a);
		assertNotNull(b);
		assertNotNull(c);
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		assertFalse(b.equals(c));
		assertFalse(c.equals(b));
	}

	public void testFromDatabase() {
		MusicAPI a = getMusicAPIA();
		assertNotNull(a);
		MusicAPI b = MusicAPI.fromDatabase(ctx, a.getId());
		assertEquals(a, b);
	}

	public void testFromDatabase2() {
		MusicAPI a = getMusicAPIA();
		MusicAPI b = getMusicAPIB();
		assertNotNull(a);
		assertNotNull(b);
		MusicAPI a2 = MusicAPI.fromDatabase(ctx, a.getId());
		MusicAPI b2 = MusicAPI.fromDatabase(ctx, b.getId());
		assertNotNull(a2);
		assertNotNull(b2);
		assertEquals(a, a2);
		assertEquals(b, b2);
	}

	public void testGetAllOne() {
		MusicAPI a = getMusicAPIA();
		MusicAPI[] all = MusicAPI.all(ctx);
		assertEquals(1, all.length);
		assertEquals(a, all[0]);
	}

	public void testGetAllTwo() {
		MusicAPI a = getMusicAPIA();
		MusicAPI b = getMusicAPIB();
		MusicAPI[] all = MusicAPI.all(ctx);
		assertEquals(2, all.length);
		assertEquals(a, all[0]);
		assertEquals(b, all[1]);
	}

}
