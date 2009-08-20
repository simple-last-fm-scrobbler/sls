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

import com.adam.aslfms.ScrobblesDbAdapter;
import com.adam.aslfms.Status;
import com.adam.aslfms.Track;
import com.adam.aslfms.Status.BadSessionException;
import com.adam.aslfms.Status.FailureException;
import com.adam.aslfms.Status.TemporaryFailureException;
import com.adam.aslfms.service.Handshaker.HandshakeInfo;

import android.content.Context;
import android.util.Log;

public class Scrobbler {

	private static final String TAG = "Scrobbler";

	private final Context mCtx;
	private final Handshaker.HandshakeInfo hInfo;
	private final ScrobblesDbAdapter mDbHelper;

	public Scrobbler(Context ctx, HandshakeInfo hInfo,
			ScrobblesDbAdapter dbHelper) {
		super();
		this.mCtx = ctx;
		this.hInfo = hInfo;
		this.mDbHelper = dbHelper;
	}

	public int scrobbleCommit() throws BadSessionException,
			TemporaryFailureException, FailureException {
		Log.d(TAG, "Scrobble commit");

		// TODO: fetch max 50
		Track[] tracks = mDbHelper.fetchScrobblesArray();
		if (tracks.length == 0) {
			Log.d(TAG, "Retrieved 0 tracks from db, no submissions");
			return 0;
		}
		Log.d(TAG, "Retrieved " + tracks.length + " tracks from db");
		for (Track track : tracks) {
			Log.d(TAG, track.toString());
		}

		DefaultHttpClient http = new DefaultHttpClient();
		HttpPost request = new HttpPost(hInfo.scrobbleUri);

		List<BasicNameValuePair> data = new LinkedList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("s", hInfo.sessionId));

		for (int i = 0; i < tracks.length; i++) {
			Track track = tracks[i];
			String is = "[" + i + "]";
			data.add(new BasicNameValuePair("a" + is, track.getArtist()
					.toString()));
			data.add(new BasicNameValuePair("b" + is, track.getAlbum()
					.toString()));
			data.add(new BasicNameValuePair("t" + is, track.getTrack()
					.toString()));
			data.add(new BasicNameValuePair("i" + is, "" + track.getWhen()));
			data.add(new BasicNameValuePair("o" + is, "P")); // source (player)
			data.add(new BasicNameValuePair("l" + is, ""
							+ track.getDuration()));
			data.add(new BasicNameValuePair("n" + is, "")); // track-number
			data.add(new BasicNameValuePair("m" + is, "")); // mbid
			data.add(new BasicNameValuePair("r" + is, "")); // rating
		}

		try {
			request.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			ResponseHandler<String> handler = new BasicResponseHandler();
			String response = http.execute(request, handler);
			Log.d(TAG, "sresponse: " + response);
			String[] lines = response.split("\n");
			if (response.startsWith("OK")) {
				Log.i(TAG, "Scrobble success");

				Log.d(TAG, "Removing tracks from db");
				for (Track track : tracks) {
					mDbHelper.deleteScrobble(track);
				}
				return tracks.length;
			} else if (response.startsWith("BADSESSION")) {
				throw new BadSessionException(
						"Scrobble failed because of badsession");
			} else if (response.startsWith("FAILED")) {
				String reason = lines[0].substring(7);
				throw new FailureException("Scrobble failed: " + reason);
			} else {
				throw new FailureException("Scrobble failed weirdly");
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
