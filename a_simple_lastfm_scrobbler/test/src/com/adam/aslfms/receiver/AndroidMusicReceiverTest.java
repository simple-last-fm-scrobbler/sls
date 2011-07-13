package com.adam.aslfms.receiver;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.TrackTestUtils;
import com.adam.aslfms.util.Track.State;

public class AndroidMusicReceiverTest extends BuiltInMusicAppReceiverTest {

	@Override
	protected BroadcastReceiver createReceiver() {
		return new AndroidMusicReceiver();
	}

	@Override
	Scrobble assembleScrobbleIntent(State state) {
		Track t = TrackTestUtils.buildSimpleTrack(getMusicAPI());
		Intent i = new Intent();
		switch (state) {
		case PLAYLIST_FINISHED:
		case COMPLETE:
		case UNKNOWN_NONPLAYING:
			i.setAction(AndroidMusicReceiver.ACTION_ANDROID_STOP);
			break;
		case START:
		case RESUME:
		case PAUSE:
			i.setAction(AndroidMusicReceiver.ACTION_ANDROID_METACHANGED);
			break;
		default:
			throw new IllegalArgumentException("Unknown state: " + state);
		}
		i.putExtra("artist", t.getArtist());
		i.putExtra("album", t.getAlbum());
		i.putExtra("track", t.getTrack());
		return new Scrobble(t, i);
	}

	MusicAPI getMusicAPI() {
		return MusicAPI.fromReceiver(ctx, AndroidMusicReceiver.NAME, AndroidMusicReceiver.PACKAGE_NAME, null, false);
	}

}
