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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adam.aslfms.R;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.MD5;
import com.adam.aslfms.util.AuthStatus;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.AuthStatus.BadAuthException;
import com.adam.aslfms.util.AuthStatus.ClientBannedException;
import com.adam.aslfms.util.AuthStatus.TemporaryFailureException;
import com.adam.aslfms.util.AuthStatus.UnknownResponseException;
import com.adam.aslfms.util.Util.NetworkStatus;

/**
 * 
 * @author tgwizard 2009
 * 
 */
public class Handshaker extends NetRunnable {

	private static final String TAG = "Handshaker";

	public enum HandshakeAction {
		HANDSHAKE, AUTH, CLEAR_CREDS
	}

	private final AppSettings settings;

	private final HandshakeAction hsAction;

	public Handshaker(NetApp napp, Context ctx, Networker net,
			HandshakeAction hsAction) {
		super(napp, ctx, net);
		this.hsAction = hsAction;
		this.settings = new AppSettings(ctx);
	}

	@Override
	public void run() {

		if (hsAction == HandshakeAction.CLEAR_CREDS) {
			settings.clearCreds(getNetApp());
			// current hInfo is invalid
			getNetworker().setHandshakeResult(null);
			// this should mean that the user called launchClearCreds, and that
			// all user information is gone
			notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_NOAUTH);
			// can't scrobble without a user
			getNetworker().unlaunchScrobblingAndNPNotifying();
			return;
		}

		if (hsAction == HandshakeAction.AUTH)
			notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_UPDATING);

		// check network status
		NetworkStatus ns = Util.checkForOkNetwork(getContext());
		if (ns != NetworkStatus.OK) {
			Log.d(TAG, "Waits on network, network-status: " + ns);
			if (hsAction == HandshakeAction.AUTH) {
				if (ns == NetworkStatus.UNFIT)
					notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_NETWORKUNFIT);
				else
					notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_RETRYLATER);
			}

			getNetworker().launchNetworkWaiter();
			getNetworker().launchHandshaker(hsAction);
			return;
		}

		try {
			// current hInfo is invalid
			getNetworker().setHandshakeResult(null);

			// might throw stuff
			HandshakeResult hInfo = handshake();

			getNetworker().setHandshakeResult(hInfo);

			// no more sleeping, handshake succeeded
			getNetworker().resetSleeper();

			// we don't need/want it anymore, settings.getPwdMd5() is enough
			settings.setPassword(getNetApp(), "");

			notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_OK);

			// won't do anything if there aren't any scrobbles,
			// but will submit those tracks that were prepared
			// but interrupted by a badauth
			// getNetworker().launchScrobbler();

		} catch (BadAuthException e) {
			if (hsAction == HandshakeAction.AUTH
					|| hsAction == HandshakeAction.HANDSHAKE)
				notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_BADAUTH);
			else {
				// CLEAR_CREDS should've been caught eariler
				Log.e(TAG, "got badauth when doAuth is weird: "
						+ hsAction.toString());
			}
			// badauth means we cant do any scrobbling/notifying, so clear them
			// the scrobbles already prepared will be sent at a later time
			getNetworker().unlaunchScrobblingAndNPNotifying();
		} catch (TemporaryFailureException e) {
			Log.i(TAG, "Tempfail: " + e.getMessage() + ": "
					+ getNetApp().getName());

			if (hsAction == HandshakeAction.AUTH)
				notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_RETRYLATER);

			if (Util.checkForOkNetwork(getContext()) != NetworkStatus.OK) {
				// no more sleeping, network down
				getNetworker().resetSleeper();
				getNetworker().launchNetworkWaiter();
				getNetworker().launchHandshaker(hsAction);
			} else {
				getNetworker().launchSleeper();
				getNetworker().launchHandshaker(hsAction);
			}

		} catch (ClientBannedException e) {
			Log.e(TAG, "This version of the client has been banned!!" + ": "
					+ getNetApp().getName());
			Log.e(TAG, e.getMessage());
			// TODO: what??
			notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_CLIENTBANNED);
		}
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
			TemporaryFailureException, ClientBannedException {
		Log.d(TAG, "Handshaking: " + getNetApp().getName());

		String username = settings.getUsername(getNetApp());
		String pwdMd5 = settings.getPwdMd5(getNetApp());

		if (username.length() == 0) {
			Log
					.d(TAG, "Invalid (empty) username for: "
							+ getNetApp().getName());
			throw new BadAuthException(getContext().getString(
					R.string.auth_bad_auth));
		}

		// -----------------------------------------------------------------------
		// ------------ for debug
		// ------------------------------------------------
		// use these values if you are testing or developing a new app
		// String clientid = "tst";
		// String clientver = "1.0";
		// -----------------------------------------------------------------------
		// ------------ for this app
		// ---------------------------------------------
		// -----------------------------------------------------------------------
		// These values should only be used for SLS. If other code
		// misbehaves using these values, this app might get banned.
		// You can ask Last.fm for your own ids.
		String clientid = getContext().getString(R.string.client_id);
		String clientver = getContext().getString(R.string.client_ver);
		// ------------ end
		// ------------------------------------------------------
		// -----------------------------------------------------------------------

		String time = new Long(Util.currentTimeSecsUTC()).toString();

		String authToken = MD5.getHashString(pwdMd5 + time);

		String uri = getNetApp().getHandshakeUrl() + "&p=1.2.1&c=" + clientid
				+ "&v=" + clientver + "&u=" + enc(username) + "&t=" + time
				+ "&a=" + authToken;

		DefaultHttpClient http = new DefaultHttpClient();
		HttpGet request = new HttpGet(uri);

		try {
			ResponseHandler<String> handler = new BasicResponseHandler();
			String response = http.execute(request, handler);
			String[] lines = response.split("\n");
			if (lines.length == 4 && lines[0].equals("OK")) {
				// handshake succeeded
				Log.i(TAG, "Handshake succeeded!: " + getNetApp().getName());

				HandshakeResult hi = new HandshakeResult(lines[1], lines[2],
						lines[3]);

				return hi;
			} else if (lines.length == 1) {
				if (lines[0].startsWith("BANNED")) {
					Log.e(TAG, "Handshake fails: client banned: "
							+ getNetApp().getName());
					throw new ClientBannedException(getContext().getString(
							R.string.auth_client_banned));
				} else if (lines[0].startsWith("BADAUTH")) {
					Log.i(TAG, "Handshake fails: bad auth: "
							+ getNetApp().getName());
					throw new BadAuthException(getContext().getString(
							R.string.auth_bad_auth));
				} else if (lines[0].startsWith("BADTIME")) {
					Log.e(TAG, "Handshake fails: bad time: "
							+ getNetApp().getName());
					throw new TemporaryFailureException(getContext().getString(
							R.string.auth_timing_error));
				} else if (lines[0].startsWith("FAILED")) {
					String reason = lines[0].substring(7);
					Log.e(TAG, "Handshake fails: FAILED " + reason + ": "
							+ getNetApp().getName());
					throw new TemporaryFailureException(getContext().getString(
							R.string.auth_server_error).replace("%1", reason));
				}
			} else {
				throw new TemporaryFailureException(
						"Weird response from handskake-req: " + response + ": "
								+ getNetApp().getName());
			}

		} catch (ClientProtocolException e) {
			throw new TemporaryFailureException(TAG + ": " + e.getMessage());
		} catch (IOException e) {
			throw new TemporaryFailureException(TAG + ": " + e.getMessage());
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

	private void notifyAuthStatusUpdate(int st) {
		settings.setAuthStatus(getNetApp(), st);
		Intent i = new Intent(ScrobblingService.BROADCAST_ONAUTHCHANGED);
		i.putExtra("netapp", getNetApp().getIntentExtraValue());
		getContext().sendBroadcast(i);
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
