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
