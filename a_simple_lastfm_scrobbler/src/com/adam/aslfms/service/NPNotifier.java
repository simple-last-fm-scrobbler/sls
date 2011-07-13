/**
 *  This file is part of Simple Last.fm Scrobbler.
 *
 *  Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
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
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.AuthStatus.BadSessionException;
import com.adam.aslfms.util.AuthStatus.TemporaryFailureException;
import com.adam.aslfms.util.AuthStatus.UnknownResponseException;
import com.adam.aslfms.util.enums.SubmissionType;

/**
 * 
 * @author tgwizard
 * 
 */
public class NPNotifier extends AbstractSubmitter {

	private static final String TAG = "NPNotifier";

	private final Track mTrack;

	public NPNotifier(NetApp napp, Context ctx, Networker net, Track track) {
		super(napp, ctx, net);

		mTrack = track;
	}

	@Override
	protected boolean doRun(HandshakeResult hInfo) {
		boolean ret;
		try {
			notifyNowPlaying(mTrack, hInfo);

			// status stuff
			notifySubmissionStatusSuccessful(mTrack, 1);

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
		getNetworker().launchNPNotifier(mTrack);
	}

	private void notifySubmissionStatusFailure(String reason) {
		super.notifySubmissionStatusFailure(SubmissionType.NP, reason);
	}

	private void notifySubmissionStatusSuccessful(Track track, int statsInc) {
		super.notifySubmissionStatusSuccessful(SubmissionType.NP, track,
				statsInc);
	}

	/**
	 * Connects to Last.fm servers and requests a Now Playing notification of
	 * <code>track</code>. If an error occurs, exceptions are thrown.
	 * 
	 * @param track
	 *            the track to send as notification
	 * @throws BadSessionException
	 *             means that a new handshake is needed
	 * @throws TemporaryFailureException
	 * @throws UnknownResponseException
	 *             {@link UnknownResponseException}
	 * 
	 */
	public void notifyNowPlaying(Track track, HandshakeResult hInfo)
			throws BadSessionException, TemporaryFailureException {
		Log.d(TAG, "Notifying now playing: " + getNetApp().getName());

		Log.d(TAG, getNetApp().getName() + ": " + track.toString());

		DefaultHttpClient http = new DefaultHttpClient();
		HttpPost request = new HttpPost(hInfo.nowPlayingUri);

		List<BasicNameValuePair> data = new LinkedList<BasicNameValuePair>();

		data.add(new BasicNameValuePair("s", hInfo.sessionId));
		data.add(new BasicNameValuePair("a", track.getArtist()));
		data.add(new BasicNameValuePair("b", track.getAlbum()));
		data.add(new BasicNameValuePair("t", track.getTrack()));
		data.add(new BasicNameValuePair("l", Integer.toString(track
				.getDuration())));
		data.add(new BasicNameValuePair("n", track.getTrackNr()));
		data.add(new BasicNameValuePair("m", track.getMbid()));

		try {
			request.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			ResponseHandler<String> handler = new BasicResponseHandler();
			String response = http.execute(request, handler);
			if (response.startsWith("OK")) {
				Log.i(TAG, "Nowplaying success: " + getNetApp().getName());
			} else if (response.startsWith("BADSESSION")) {
				throw new BadSessionException(
						"Nowplaying failed because of badsession");
			} else {
				throw new TemporaryFailureException(
						"NowPlaying failed weirdly: " + response);
			}

		} catch (ClientProtocolException e) {
			throw new TemporaryFailureException(TAG + ": " + e.getMessage());
		} catch (IOException e) {
			throw new TemporaryFailureException(TAG + ": " + e.getMessage());
		} finally {
			http.getConnectionManager().shutdown();
		}
	}

}
