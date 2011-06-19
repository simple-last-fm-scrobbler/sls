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
		Track initialTrack = tracks[0];
		InternalTrackTransmitter.appendTrack(initialTrack);
		assertSame(initialTrack, InternalTrackTransmitter.popTrack());
		assertNull(InternalTrackTransmitter.popTrack());
	}

	public void testAppendAndPopOrder() {
		Track initialTrackA = tracks[0];
		Track initialTrackB = tracks[1];
		InternalTrackTransmitter.appendTrack(initialTrackA);
		InternalTrackTransmitter.appendTrack(initialTrackB);
		assertSame(initialTrackA, InternalTrackTransmitter.popTrack());
		assertSame(initialTrackB, InternalTrackTransmitter.popTrack());
		assertNull(InternalTrackTransmitter.popTrack());
	}
}
