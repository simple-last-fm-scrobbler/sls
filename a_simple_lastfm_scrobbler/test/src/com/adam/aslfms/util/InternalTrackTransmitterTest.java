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

import com.adam.aslfms.util.InternalTrackTransmitter;
import com.adam.aslfms.util.Track;

import android.test.AndroidTestCase;

public class InternalTrackTransmitterTest extends AndroidTestCase {

	Track[] tracks;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tracks = new Track[] { TrackTestUtils.buildTrack(), TrackTestUtils.buildTrack() };
	}

	public void testPopEmpty() {
		assertNull(InternalTrackTransmitter.popTrack());
	}

	public void testAppendAndPop() {
		InternalTrackTransmitter.appendTrack(tracks[0]);
		assertSame(tracks[0], InternalTrackTransmitter.popTrack());
		assertNull(InternalTrackTransmitter.popTrack());
	}

	public void testAppendAndPopOrder() {
		InternalTrackTransmitter.appendTrack(tracks[0]);
		InternalTrackTransmitter.appendTrack(tracks[1]);
		assertSame(tracks[0], InternalTrackTransmitter.popTrack());
		assertSame(tracks[1], InternalTrackTransmitter.popTrack());
		assertNull(InternalTrackTransmitter.popTrack());
	}
}
