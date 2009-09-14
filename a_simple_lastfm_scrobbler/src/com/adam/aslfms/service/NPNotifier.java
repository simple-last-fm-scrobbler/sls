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

import com.adam.aslfms.AppSettings;
import com.adam.aslfms.R;
import com.adam.aslfms.Track;
import com.adam.aslfms.Status.BadSessionException;
import com.adam.aslfms.Status.UnknownResponseException;
import com.adam.aslfms.Status.TemporaryFailureException;
import com.adam.aslfms.service.Handshaker.HandshakeResult;
import com.adam.aslfms.util.Util;

/**
 * 
 * @author tgwizard
 * 
 */
public class NPNotifier extends AbstractSubmitter {

	private static final String TAG = "NPNotifier";

	private final AppSettings settings;

	private Track mTrack;

	public NPNotifier(Context ctx, Networker net, Track track) {
		super(ctx, net);
		this.settings = new AppSettings(ctx);
		mTrack = track;
	}

	@Override
	protected boolean doRun(HandshakeResult hInfo) {
		// TODO Auto-generated method stub
		boolean ret;
		try {
			notifyNowPlaying(mTrack, hInfo);

			// status stuff
			settings.setLastNPSuccess(true);
			settings.setLastNPTime(Util.currentTimeMillisLocal());
			settings.setNumberOfNPs(settings.getNumberOfNPs() + 1);
			settings.setLastNPInfo("\"" + mTrack.getTrack() + "\" "
					+ getContext().getString(R.string.by) + " "
					+ mTrack.getArtist());
			notifyStatusUpdate();

			ret = true;
		} catch (BadSessionException e) {
			Log.i(TAG, e.getMessage());
			getNetworker().launchHandshaker(false);
			relaunchThis();
			ret = true;
		} catch (TemporaryFailureException e) {
			Log.i(TAG, e.getMessage());
			ret = false;
		}
		return ret;
	}

	@Override
	protected void relaunchThis() {
		getNetworker().launchNPNotifier(mTrack);
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
		Log.d(TAG, "Notifying now playing");

		Log.d(TAG, "Track: " + track.toString());

		DefaultHttpClient http = new DefaultHttpClient();
		HttpPost request = new HttpPost(hInfo.nowPlayingUri);

		List<BasicNameValuePair> data = new LinkedList<BasicNameValuePair>();

		data.add(new BasicNameValuePair("s", hInfo.sessionId));
		data.add(new BasicNameValuePair("a", track.getArtist().toString()));
		data.add(new BasicNameValuePair("b", track.getAlbum().toString()));
		data.add(new BasicNameValuePair("t", track.getTrack().toString()));
		data.add(new BasicNameValuePair("l", "" + track.getDuration()));

		try {
			request.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			ResponseHandler<String> handler = new BasicResponseHandler();
			String response = http.execute(request, handler);
			if (response.startsWith("OK")) {
				Log.i(TAG, "Nowplaying success");
			} else if (response.startsWith("BADSESSION")) {
				throw new BadSessionException(
						"Nowplaying failed because of badsession");
			} else {
				throw new TemporaryFailureException(
						"NowPlaying failed weirdly: " + response);
			}

		} catch (ClientProtocolException e) {
			throw new TemporaryFailureException(e.getMessage());
		} catch (IOException e) {
			throw new TemporaryFailureException(getContext().getString(
					R.string.auth_network_error));
		} finally {
			http.getConnectionManager().shutdown();
		}
	}

}
