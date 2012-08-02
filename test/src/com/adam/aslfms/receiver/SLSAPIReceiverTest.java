package com.adam.aslfms.receiver;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Track.State;
import com.adam.aslfms.util.TrackTestUtils;

public class SLSAPIReceiverTest extends AbstractReceiverTest {

	@Override
	BroadcastReceiver createReceiver() {
		return new SLSAPIReceiver();
	}

	@Override
	Scrobble assembleScrobbleIntent(State state) {
		MusicAPI mapi = getMusicAPI("Fake App Name",
				"com.adam.fake.app");
		Track t = TrackTestUtils.buildFullTrack(mapi);
		Intent i = new Intent();
		i.setAction(SLSAPIReceiver.SLS_API_BROADCAST_INTENT);
		int istate = -1;
		switch (state) {
		case PLAYLIST_FINISHED:
		case COMPLETE:
		case UNKNOWN_NONPLAYING:
			istate = SLSAPIReceiver.STATE_COMPLETE;
			break;
		case START:
			istate = SLSAPIReceiver.STATE_START;
			break;
		case RESUME:
			istate = SLSAPIReceiver.STATE_RESUME;
			break;
		case PAUSE:
			istate = SLSAPIReceiver.STATE_PAUSE;
			break;
		default:
			throw new IllegalArgumentException("Unknown state: " + state);
		}
		i.putExtra("app-name", mapi.getName());
		i.putExtra("app-package", mapi.getPackage());
		i.putExtra("state", istate);
		i.putExtra("artist", t.getArtist());
		i.putExtra("album", t.getAlbum());
		i.putExtra("track", t.getTrack());
		i.putExtra("duration", t.getDuration());
		i.putExtra("track-number", Integer.valueOf(t.getTrackNr()));
		i.putExtra("mbid", t.getMbid());
		i.putExtra("source", t.getSource());
		return new Scrobble(t, i);
	}

	MusicAPI getMusicAPI(String app_name, String app_package) {
		return MusicAPI.fromReceiver(ctx, app_name, app_package, null, false);
	}

}
