package com.adam.aslfms.service;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;

import com.adam.aslfms.AppSettings;
import com.adam.aslfms.AppTransaction;
import com.adam.aslfms.R;
import com.adam.aslfms.Status.BadAuthException;
import com.adam.aslfms.Status.FailureException;
import com.adam.aslfms.Status.TemporaryFailureException;
import com.adam.aslfms.util.MD5;

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
	 * Internal, should only be called by tryHandshake()
	 * 
	 * @param username
	 * @param pwdMd5
	 * @param firstAuth
	 * @return status
	 */
	public HandshakeInfo handshake() throws BadAuthException,
			TemporaryFailureException, FailureException {
		Log.d(TAG, "handshaking");

		String username = settings.getUsername();
		String pwdMd5 = settings.getPwdMd5();

		Log.d(TAG, "username: " + username);

		if (username.length() == 0) {
			Log.d(TAG, "Invalid username");
			throw new BadAuthException(mCtx.getString(R.string.auth_bad_auth));
		}

		Log.d(TAG, "pwdMd5: " + pwdMd5);

		// for debug
		//String clientid = "tst";
		//String clientver = "1.0";
		// for apps with real client-id and client-ver
		String clientid = mCtx.getString(R.string.client_id);
		String clientver = mCtx.getString(R.string.client_ver);

		String time = new Long(AppTransaction.currentTimeUTC()).toString();
		Log.d(TAG, "time: " + time);

		Log.d(TAG, "concat: " + pwdMd5 + time);

		String authToken = MD5.getHashString(pwdMd5 + time);
		Log.d(TAG, "authToken: " + authToken);

		String uri = "http://post.audioscrobbler.com/?hs=true&p=1.2.1&c="
				+ clientid + "&v=" + clientver + "&u=" + username + "&t="
				+ time + "&a=" + authToken;
		Log.d(TAG, "uri: " + uri);

		DefaultHttpClient http = new DefaultHttpClient();
		HttpGet request = new HttpGet(uri);

		try {
			ResponseHandler<String> handler = new BasicResponseHandler();
			String response = http.execute(request, handler);
			Log.d(TAG, "hresponse: " + response);
			String[] lines = response.split("\n");
			if (lines.length == 4 && lines[0].equals("OK")) {
				// handshake succeded
				Log.i(TAG, "Handshake succeeded!");

				HandshakeInfo hi = new HandshakeInfo(lines[1], lines[2],
						lines[3]);

				return hi;
			} else if (lines.length == 1) {
				if (lines[0].startsWith("BANNED")) {
					Log.e(TAG, "Handshake fails: client banned");
					throw new FailureException(mCtx
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
					throw new FailureException(mCtx
							.getString(R.string.auth_server_error)
							+ " ");
				}
			} else {
				throw new FailureException("Weird response from handskake-req");
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

	public static class HandshakeInfo {
		public final String sessionId;
		public final String nowPlayingUri;
		public final String scrobbleUri;

		public HandshakeInfo(String sessionId, String nowPlayingUri,
				String scrobbleUri) {
			super();
			this.sessionId = sessionId;
			this.nowPlayingUri = nowPlayingUri;
			this.scrobbleUri = scrobbleUri;
		}

	}
}
