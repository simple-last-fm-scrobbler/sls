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

		Log.v(TAG, "Action received was: " + action);

		// check to make sure we actually got something
		if (action == null) {
			Log.w(TAG, "Got null action");
			return;
		}

		if (bundle == null) {
			bundle = Bundle.EMPTY;
		}

		// we must be logged in to scrobble
		AppSettings settings = new AppSettings(context);
		if (!settings.isAnyAuthenticated()) {
			Log
					.d(TAG,
							"The user has not authenticated, won't propagate the submission request");
			return;
		}

		mService = new Intent(context, ScrobblingService.class);
		mService.setAction(ScrobblingService.ACTION_PLAYSTATECHANGED);

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
				Log.d(TAG, "App: " + mMusicAPI.getName()
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
