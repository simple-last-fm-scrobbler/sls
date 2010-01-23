/**
 *  This file is part of A Simple Last.fm Scrobbler.
 *
 *  A Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  A Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with A Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.InternalTrackTransmitter;
import com.adam.aslfms.util.Track;

/**
 * Base class for play status receivers.
 * 
 * @see SLSAPIReceiver
 * @see ScrobbleDroidMusicReceiver
 * @see AndroidMusicReceiver
 * @see HeroMusicReceiver
 * @see MusicAPI
 * 
 * @author tgwizard
 * @since 1.0.1
 */
public abstract class AbstractPlayStatusReceiver extends BroadcastReceiver {

	private static final String TAG = "SLSPlayStatusReceiver";

	private MusicAPI mMusicAPI = null;
	private Intent mService = null;
	private Track mTrack = null;

	@Override
	public final void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle bundle = intent.getExtras();

		Log.d(TAG, "Action received was: " + action);

		// check to make sure we actually got something
		if (action == null || bundle == null) {
			Log.e(TAG, "Got null action or null bundle");
			return;
		}

		// we must be logged in to scrobble
		AppSettings settings = new AppSettings(context);
		if (!settings.isAnyAuthenticated()) {
			Log
					.i(TAG,
							"The user has not authenticated, won't propagate the submission request");
			return;
		}

		mService = new Intent(ScrobblingService.ACTION_PLAYSTATECHANGED);

		try {
			parseIntent(context, action, bundle); // might throw

			// parseIntent must have called setMusicAPI and setTrack
			// with non-null values
			if (mMusicAPI == null) {
				throw new IllegalArgumentException("null music api");
			} else if (mTrack == null) {
				throw new IllegalArgumentException("null track");
			}

			// check if the user wants to scrobble music from this MusicAPI
			if (!mMusicAPI.isEnabled()) {
				Log.i(TAG, "App: " + mMusicAPI.getName()
						+ " has been disabled, won't propagate");
				return;
			}

			// submit track for the ScrobblingService
			InternalTrackTransmitter.appendTrack(mTrack);

			// start/call the Scrobbling Service
			context.startService(mService);
		} catch (IllegalArgumentException e) {
			Log.i(TAG, "Got a bad track from: "
					+ ((mMusicAPI == null) ? "null" : mMusicAPI.getName())
					+ ", ignoring it (" + e.getMessage() + ")");
		}

	}

	/**
	 * Sets the {@link MusicAPI} to use for this scrobble request.
	 * 
	 * @param mapi
	 *            the MusicAPI to use send this scrobble request
	 */
	protected final void setMusicAPI(MusicAPI mapi) {
		mMusicAPI = mapi;
	}

	/**
	 * Sets the {@link Track.State} that this received broadcast represents.
	 * 
	 * @param state
	 */
	protected final void setState(Track.State state) {
		mService.putExtra("state", state.name());
	}

	/**
	 * Sets the {@link Track} for this scrobble request
	 * 
	 * @param track
	 *            the Track for this scrobble request
	 */
	protected final void setTrack(Track track) {
		mTrack = track;
	}

	/**
	 * Parses the API / music app specific parts of the received broadcast. This
	 * is extracted into a specific {@link MusicAPI}, {@link Track} and state.
	 * 
	 * @see #setMusicAPI(MusicAPI)
	 * @see #setState(com.adam.aslfms.util.Track.State)
	 * @see #setTrack(Track)
	 * 
	 * @param ctx
	 *            to be able to create {@code MusicAPIs}
	 * @param action
	 *            the action/intent used for this scrobble request
	 * @param bundle
	 *            the data sent with this request
	 * @throws IllegalArgumentException
	 *             when the data received is invalid
	 */
	protected abstract void parseIntent(Context ctx, String action,
			Bundle bundle) throws IllegalArgumentException;

}
