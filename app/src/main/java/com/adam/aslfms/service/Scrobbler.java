/**
 * This file is part of Simple Last.fm Scrobbler.
 *
 *     https://github.com/tgwizard/sls
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


package com.adam.aslfms.service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

import com.adam.aslfms.R;
import com.adam.aslfms.service.Handshaker.HandshakeResult;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.AuthStatus.BadSessionException;
import com.adam.aslfms.util.AuthStatus.TemporaryFailureException;
import com.adam.aslfms.util.enums.SubmissionType;

/**
 *
 * @author tgwizard
 *
 */
public class Scrobbler extends AbstractSubmitter {

	private static final String TAG = "Scrobbler";

	// private final Context mCtx;
	private final ScrobblesDatabase mDb;

	public static final int MAX_SCROBBLE_LIMIT = 50;

	public Scrobbler(NetApp napp, Context ctx, Networker net,
			ScrobblesDatabase db) {
		super(napp, ctx, net);
		this.mDb = db;
	}

	@Override
	public boolean doRun(HandshakeResult hInfo) {
		boolean ret;
		try {
			Log.d(TAG, "Scrobbling: " + getNetApp().getName());
			Track[] tracks = mDb.fetchTracksArray(getNetApp(), MAX_SCROBBLE_LIMIT);

			if (tracks.length == 0) {
				Log.d(TAG, "Retrieved 0 tracks from db, no scrobbling: " + getNetApp().getName());
				return true;
			}
			Log.d(TAG, "Retrieved " + tracks.length + " tracks from db: " + getNetApp().getName());

			for (Track track : tracks) {
				Log.d(TAG, getNetApp().getName() + ": " + track.toString());
			}

			scrobbleCommit(hInfo, tracks); // throws if unsuccessful

			// delete scrobbles (not tracks) from db (not array)
			for (Track track : tracks) {
				mDb.deleteScrobble(getNetApp(), track.getRowId());
			}

			// clean up tracks if no one else wants to scrobble them
			mDb.cleanUpTracks();

			// there might be more tracks in the db
			if (tracks.length == MAX_SCROBBLE_LIMIT) {
				Log.d(TAG, "Relaunching scrobbler, might be more tracks in db");
				relaunchThis();
			}

			// status stuff
			notifySubmissionStatusSuccessful(tracks[tracks.length - 1],
					tracks.length);

			ret = true;
		} catch (BadSessionException e) {
			Log.i(TAG, "BadSession: " + e.getMessage() + ": "
					+ getNetApp().getName());
			getNetworker().launchHandshaker();
			relaunchThis();
			notifySubmissionStatusFailure(getContext().getString(
					R.string.auth_just_error));
			ret = true;
		} catch (TemporaryFailureException e) {
			Log.i(TAG, "Tempfail: " + e.getMessage() + ": "
					+ getNetApp().getName());
			notifySubmissionStatusFailure(getContext().getString(
					R.string.auth_network_error_retrying));
			ret = false;
		}
		return ret;
	}

	@Override
	protected void relaunchThis() {
		getNetworker().launchScrobbler();
	}

	private void notifySubmissionStatusFailure(String reason) {
		super.notifySubmissionStatusFailure(SubmissionType.SCROBBLE, reason);
	}

	private void notifySubmissionStatusSuccessful(Track track, int statsInc) {
		super.notifySubmissionStatusSuccessful(SubmissionType.SCROBBLE, track,
				statsInc);
	}

	/**
	 *
	 * @return a {@link ScrobbleResult} struct with some info
	 * @throws BadSessionException
	 * @throws TemporaryFailureException
	 */
	public void scrobbleCommit(HandshakeResult hInfo, Track[] tracks)
			throws BadSessionException, TemporaryFailureException {

		DefaultHttpClient http = new DefaultHttpClient();
		HttpPost request = new HttpPost(hInfo.scrobbleUri);

		List<BasicNameValuePair> data = new LinkedList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("s", hInfo.sessionId));

		for (int i = 0; i < tracks.length; i++) {
			Track track = tracks[i];
			String is = "[" + i + "]";
			data.add(new BasicNameValuePair("a" + is, track.getArtist()));
			data.add(new BasicNameValuePair("b" + is, track.getAlbum()));
			data.add(new BasicNameValuePair("t" + is, track.getTrack()));
			data.add(new BasicNameValuePair("i" + is, Long.toString(track
					.getWhen())));
			data.add(new BasicNameValuePair("o" + is, track.getSource()));
			data.add(new BasicNameValuePair("l" + is, Integer.toString(track
					.getDuration())));
			data.add(new BasicNameValuePair("n" + is, track.getTrackNr()));
			data.add(new BasicNameValuePair("m" + is, track.getMbid()));
			data.add(new BasicNameValuePair("r" + is, track.getRating()));
		}

		try {
			request.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			ResponseHandler<String> handler = new BasicResponseHandler();
			String response = http.execute(request, handler);
			String[] lines = response.split("\n");
			if (response.startsWith("OK")) {
				Log.i(TAG, "Scrobble success: " + getNetApp().getName());
			} else if (response.startsWith("BADSESSION")) {
				throw new BadSessionException("Scrobble failed because of badsession");
			} else if (response.startsWith("FAILED")) {
				String reason = lines[0].substring(7);
				throw new TemporaryFailureException("Scrobble failed: " + reason);
			} else {
				throw new TemporaryFailureException("Scrobble failed weirdly: " + response);
			}

		} catch (IOException e) {
			throw new TemporaryFailureException(TAG + ": " + e.getMessage());
		} finally {
			http.getConnectionManager().shutdown();
		}
	}
}
