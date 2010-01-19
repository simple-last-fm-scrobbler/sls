package com.adam.aslfms.receiver;

import android.content.Context;
import android.os.Bundle;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

public class SLSAPIReceiver extends AbstractPlayStatusReceiver {
	@SuppressWarnings("unused")
	private static final String TAG = "SLSAPIReceiver";

	public static final String SLS_API_BROADCAST_INTENT = "com.adam.aslfms.notify.playstatechanged";

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle)
			throws IllegalArgumentException {

		// music api
		String appname = bundle.getString("app-name");
		String apppkg  = bundle.getString("app-package");
		
		MusicAPI musicAPI = MusicAPI.fromReceiver(ctx, appname, apppkg, null, false);
		setMusicAPI(musicAPI);
		
		// state
		int state = bundle.getInt("state", -1);
		
		if (state == -1)
			throw new IllegalArgumentException("no state");
		
		if (state == 0)
			setState(Track.State.START);
		else if (state == 1)
			setState(Track.State.RESUME);
		else if (state == 2)
			setState(Track.State.PAUSE);
		else if (state == 3)
			setState(Track.State.COMPLETE);
		else
			throw new IllegalArgumentException("bad state: " + state);
		
		Track.Builder b = new Track.Builder();
		b.setMusicAPI(musicAPI);
		b.setWhen(Util.currentTimeSecsUTC());
		b.setArtist(bundle.getString("artist"));
		b.setAlbum(bundle.getString("album"));
		b.setTrack(bundle.getString("track"));
		
		// duration
		int duration = bundle.getInt("duration", -1);
		if (duration == -1)
			throw new IllegalArgumentException("no duration");
		b.setDuration(duration);
		
		// tracknr
		int tracknr = bundle.getInt("track-number", -1);
		if (tracknr != -1)
			b.setTrackNr(Integer.toString(tracknr));
		
		String mbid = bundle.getString("mbid");
		b.setMbid(mbid);
		
		String source = bundle.getString("source");
		source = (source == null) ? "P" : source;
		b.setSource(source);

		// throws on bad data
		setTrack(b.build());
	}
}
