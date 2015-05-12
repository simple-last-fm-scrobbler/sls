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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.AuthStatus;
import com.adam.aslfms.util.InternalTrackTransmitter;
import com.adam.aslfms.util.Track;

public abstract class AbstractReceiverTest extends AndroidTestCase {

	static class StartServiceMockContext extends IsolatedContext {
		List<Intent> startedServices;

		public StartServiceMockContext(ContentResolver resolver, Context targetContext) {
			super(resolver, targetContext);
			startedServices = new LinkedList<Intent>();
		}

		@Override
		public ComponentName startService(Intent service) {
			startedServices.add(service);
			return null;
		}

		public List<Intent> getStartedServices() {
			return startedServices;
		}
	}

	static class Scrobble {
		Track track;
		Intent intent;

		public Scrobble(Track track, Intent intent) {
			super();
			this.track = track;
			this.intent = intent;
		}

	}

	static final NetApp FAKE_NETAPP = NetApp.LASTFM;

	StartServiceMockContext ctx;
	BroadcastReceiver receiver;
	AppSettings settings;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ctx = new StartServiceMockContext(new MockContentResolver(), getContext());
		settings = new AppSettings(ctx);
		fakeAuthenticate();
		receiver = createReceiver();
	}

	void fakeAuthenticate() {
		// any netapp will do
		settings.setAuthStatus(FAKE_NETAPP, AuthStatus.AUTHSTATUS_OK);
	}

	void fakeUnauthenticate() {
		settings.setAuthStatus(FAKE_NETAPP, AuthStatus.AUTHSTATUS_NOAUTH);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		MusicAPITestUtils.deleteDatabase(ctx);
		ctx = null;
		receiver = null;
	}

	abstract BroadcastReceiver createReceiver();

	abstract Scrobble assembleScrobbleIntent(Track.State state);

	/*
	 * Fixture tests
	 */

	public void testReceiveStart_notAuthenticated() {
		fakeUnauthenticate();
		Scrobble scrobble = assembleScrobbleIntent(Track.State.START);
		sendScrobbleIntent(scrobble.intent);
		assertServiceIntentDoesntExist();
	}

	public void testReceiveStart_valid() {
		Scrobble scrobble = assembleScrobbleIntent(Track.State.START);
		sendAndExpectEqualsExtended(scrobble.intent, scrobble.track, Track.State.START, Track.State.RESUME);
	}
	
	public void testReceiveResume_valid() {
		Scrobble scrobble = assembleScrobbleIntent(Track.State.RESUME);
		sendAndExpectEqualsExtended(scrobble.intent, scrobble.track, Track.State.START, Track.State.RESUME);
	}

	/*
	 * Helper methods
	 */
	Track sendAndExpectEquals(Intent scrobbleIntent, Track expectedTrack, Track.State... acceptableStates) {
		Track actualTrack = sendScrobbleIntent(scrobbleIntent);
		assertServiceIntentStatus(acceptableStates);
		assertTrackEquals(expectedTrack, actualTrack);
		return actualTrack;
	}

	Track sendAndExpectEqualsExtended(Intent scrobbleIntent, Track expectedTrack, Track.State... acceptableStates) {
		Track actualTrack = sendAndExpectEquals(scrobbleIntent, expectedTrack, acceptableStates);
		assertTrackEqualsExtended(expectedTrack, actualTrack);
		return actualTrack;
	}

	Track sendScrobbleIntent(Intent scrobbleIntent) {
		receiver.onReceive(ctx, scrobbleIntent);
		return InternalTrackTransmitter.popTrack();
	}

	void assertTrackEquals(Track expectedTrack, Track actualTrack) {
		assertEquals(expectedTrack, actualTrack);
	}

	void assertTrackEqualsExtended(Track expectedTrack, Track actualTrack) {
		if (expectedTrack != null) {
			assertEquals(expectedTrack.getDuration(), actualTrack.getDuration());
			assertEquals(expectedTrack.getMbid(), actualTrack.getMbid());
			assertEquals(expectedTrack.getRating(), actualTrack.getRating());
			assertEquals(expectedTrack.getSource(), actualTrack.getSource());
			assertEquals(expectedTrack.getTrackNr(), actualTrack.getTrackNr());
		}
	}

	void assertServiceIntentStatus(Track.State... acceptableStates) {
		assertServiceIntentExists();

		Track.State state = getStateOrNull();

		if (state != null && acceptableStates.length == 0) {
			fail("Expected no state, received: " + state);
		} else if (state == null && acceptableStates.length > 0) {
			fail("Expected state: " + Arrays.toString(acceptableStates) + ", received no state");
		}

		boolean found = false;
		for (Track.State acceptableState : acceptableStates) {
			if (acceptableState.equals(state)) {
				found = true;
			}
		}

		if (!found) {
			fail("Expected state: " + Arrays.toString(acceptableStates) + ", got: " + state);
		}
	}

	Track.State getStateOrNull() {
		Intent serviceIntent = ctx.getStartedServices().get(0);
		if (serviceIntent.hasExtra("state")) {
			return Track.State.valueOf(serviceIntent.getStringExtra("state"));
		}
		return null;
	}

	void assertServiceIntentExists() {
		assertServiceIntentExistence(true);
	}

	void assertServiceIntentDoesntExist() {
		assertServiceIntentExistence(false);
	}

	void assertServiceIntentExistence(boolean exists) {
		if (exists && ctx.getStartedServices().isEmpty()) {
			fail("Expected service intent, not sent");
		} else if (!exists && !ctx.getStartedServices().isEmpty()) {
			fail("Expected NO service intent, one sent anyway");
		}
	}

}
