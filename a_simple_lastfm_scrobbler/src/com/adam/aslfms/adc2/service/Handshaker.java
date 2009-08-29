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

package com.adam.aslfms.adc2.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;

import com.adam.aslfms.adc2.R;
import com.adam.aslfms.adc2.AppSettings;
import com.adam.aslfms.adc2.Status.BadAuthException;
import com.adam.aslfms.adc2.Status.ClientBannedException;
import com.adam.aslfms.adc2.Status.TemporaryFailureException;
import com.adam.aslfms.adc2.Status.UnknownResponseException;
import com.adam.aslfms.adc2.util.MD5;
import com.adam.aslfms.adc2.util.Util;

/**
 * 
 * @author tgwizard 2009
 * 
 */
public class Handshaker {

	private static final String TAG = "Handshaker";

	private final Context mCtx;
	private final AppSettings settings;

	public Handshaker(Context ctx) {
		super();
		this.mCtx = ctx;
		this.settings = new AppSettings(ctx);
	}

	/**
	 * Connects to Last.fm servers and tries to handshake/authenticate. A
	 * successful handshake is needed for all other submission requests. If an
	 * error occurs, exceptions are thrown.
	 * 
	 * @return the result of a successful handshake, {@link HandshakeResult}
	 * @throws BadAuthException
	 *             means that the username/password provided by the user was
	 *             wrong, or that the user requested his/her credentials to be
	 *             cleared.
	 * @throws TemporaryFailureException
	 * @throws UnknownResponseException
	 *             {@link UnknownResponseException}
	 * @throws ClientBannedException
	 *             this version of the client has been banned
	 */
	public HandshakeResult handshake() throws BadAuthException,
			TemporaryFailureException, UnknownResponseException,
			ClientBannedException {
		Log.d(TAG, "Handshaking");

		String username = settings.getUsername();
		String pwdMd5 = settings.getPwdMd5();

		if (username.length() == 0) {
			Log.d(TAG, "Invalid username");
			throw new BadAuthException(mCtx.getString(R.string.auth_bad_auth));
		}

		// -----------------------------------------------------------------------
		// ------------ for debug ------------------------------------------------
		// use these values if you are testing or developing a new app
		// String clientid = "tst";
		// String clientver = "1.0";
		// -----------------------------------------------------------------------
		// ------------ for this app ---------------------------------------------
		// -----------------------------------------------------------------------
		// these values should only be used for this app. If other code
		// misbehaves using these values, this app might get banned.
		// You can ask last.fm for new id's for your app
		String clientid = mCtx.getString(R.string.client_id);
		String clientver = mCtx.getString(R.string.client_ver);
		// ------------ end ------------------------------------------------------
		// -----------------------------------------------------------------------

		String time = new Long(Util.currentTimeSecsUTC()).toString();

		String authToken = MD5.getHashString(pwdMd5 + time);

		String uri = "http://post.audioscrobbler.com/?hs=true&p=1.2.1&c="
				+ clientid + "&v=" + clientver + "&u=" + enc(username) + "&t="
				+ time + "&a=" + authToken;

		DefaultHttpClient http = new DefaultHttpClient();
		HttpGet request = new HttpGet(uri);

		try {
			ResponseHandler<String> handler = new BasicResponseHandler();
			String response = http.execute(request, handler);
			String[] lines = response.split("\n");
			if (lines.length == 4 && lines[0].equals("OK")) {
				// handshake succeeded
				Log.i(TAG, "Handshake succeeded!");

				HandshakeResult hi = new HandshakeResult(lines[1], lines[2],
						lines[3]);

				return hi;
			} else if (lines.length == 1) {
				if (lines[0].startsWith("BANNED")) {
					Log.e(TAG, "Handshake fails: client banned");
					throw new ClientBannedException(mCtx
							.getString(R.string.auth_client_banned));
				} else if (lines[0].startsWith("BADAUTH")) {
					Log.i(TAG, "Handshake fails: bad auth");
					throw new BadAuthException(mCtx
							.getString(R.string.auth_bad_auth));
				} else if (lines[0].startsWith("BADTIME")) {
					Log.e(TAG, "Handshake fails: bad time");
					throw new TemporaryFailureException(mCtx
							.getString(R.string.auth_timing_error));
				} else if (lines[0].startsWith("FAILED")) {
					String reason = lines[0].substring(7);
					Log.e(TAG, "Handshake fails: FAILED " + reason);
					throw new TemporaryFailureException(mCtx
							.getString(R.string.auth_server_error)
							+ " ");
				}
			} else {
				throw new UnknownResponseException(
						"Weird response from handskake-req: " + response);
			}

		} catch (ClientProtocolException e) {
			throw new TemporaryFailureException(e.getMessage());
		} catch (IOException e) {
			throw new TemporaryFailureException(mCtx
					.getString(R.string.auth_network_error));
		} finally {
			http.getConnectionManager().shutdown();
		}
		return null;
	}

	private static String enc(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "URLEncoder lacks support for UTF-8!?");
			return null;
		}
	}

	/**
	 * Small struct holding the results of a successful handshake. All the
	 * fields are final and public, as they will never change as long as the
	 * handshake is valid.
	 * 
	 * @author tgwizard
	 * 
	 */
	public static class HandshakeResult {
		/**
		 * The id needed for all submission requests.
		 */
		public final String sessionId;

		/**
		 * The URI to send now-playing-notification requests to.
		 */
		public final String nowPlayingUri;

		/**
		 * The URI to send scrobble requests to.
		 */
		public final String scrobbleUri;

		/**
		 * Constructs a new handshake info struct. Only {@link Handshaker} can
		 * get the information needed to instantiate this class, and therefore
		 * the constructor is private.
		 * 
		 * @param sessionId
		 *            {@link HandshakeResult#sessionId sessionId}
		 * @param nowPlayingUri
		 *            {@link HandshakeResult#nowPlayingUri nowPlayingUri}
		 * @param scrobbleUri
		 *            {@link HandshakeResult#scrobbleUri scrobbleUri}
		 */
		private HandshakeResult(String sessionId, String nowPlayingUri,
				String scrobbleUri) {
			super();
			this.sessionId = sessionId;
			this.nowPlayingUri = nowPlayingUri;
			this.scrobbleUri = scrobbleUri;
		}

	}
}
