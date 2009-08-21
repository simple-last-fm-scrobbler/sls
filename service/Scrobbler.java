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

import com.adam.aslfms.ScrobblesDbAdapter;
import com.adam.aslfms.Track;
import com.adam.aslfms.Status.BadSessionException;
import com.adam.aslfms.Status.FailureException;
import com.adam.aslfms.Status.TemporaryFailureException;
import com.adam.aslfms.service.Handshaker.HandshakeInfo;

/**
 * 
 * @author tgwizard
 * 
 */
public class Scrobbler {

	private static final String TAG = "Scrobbler";

	//private final Context mCtx;
	private final Handshaker.HandshakeInfo hInfo;
	private final ScrobblesDbAdapter mDbHelper;

	private static final int MAX_SCROBBLE_LIMIT = 50;
	private Track[] mTracks;

	public Scrobbler(Context ctx, HandshakeInfo hInfo,
			ScrobblesDbAdapter dbHelper) {
		super();
		//this.mCtx = ctx;
		this.hInfo = hInfo;
		this.mDbHelper = dbHelper;
		this.mTracks = new Track[MAX_SCROBBLE_LIMIT];
	}

	/**
	 * 
	 * @return true if there are more scrobbles left
	 * @throws BadSessionException
	 * @throws TemporaryFailureException
	 * @throws FailureException
	 */
	public boolean scrobbleCommit() throws BadSessionException,
			TemporaryFailureException, FailureException {
		Log.d(TAG, "Scrobble commit");

		int count = mDbHelper.fetchScrobblesArray(mTracks, MAX_SCROBBLE_LIMIT);
		if (count == 0) {
			Log.d(TAG, "Retrieved 0 tracks from db, no submissions");
			return false;
		}

		boolean ret = false;
		Log.d(TAG, "Retrieved " + count + " tracks from db");
		if (count > MAX_SCROBBLE_LIMIT) {
			Log.d(TAG, "But only " + MAX_SCROBBLE_LIMIT
					+ " will be submitted just now");
			ret = true;
			count = MAX_SCROBBLE_LIMIT;
		}
		for (int i = 0; i < count; i++) {
			Log.d(TAG, mTracks[i].toString());
		}

		DefaultHttpClient http = new DefaultHttpClient();
		HttpPost request = new HttpPost(hInfo.scrobbleUri);

		List<BasicNameValuePair> data = new LinkedList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("s", hInfo.sessionId));

		for (int i = 0; i < count; i++) {
			Track track = mTracks[i];
			String is = "[" + i + "]";
			data.add(new BasicNameValuePair("a" + is, track.getArtist()
					.toString()));
			data.add(new BasicNameValuePair("b" + is, track.getAlbum()
					.toString()));
			data.add(new BasicNameValuePair("t" + is, track.getTrack()
					.toString()));
			data.add(new BasicNameValuePair("i" + is, "" + track.getWhen()));
			data.add(new BasicNameValuePair("o" + is, "P")); // source (player)
			data
					.add(new BasicNameValuePair("l" + is, ""
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
				for (int i = 0; i < count; i++) {
					mDbHelper.deleteScrobble(mTracks[i]);
				}

				// resettings mTracks in finally below

				return ret;
			} else if (response.startsWith("BADSESSION")) {
				throw new BadSessionException(
						"Scrobble failed because of badsession");
			} else if (response.startsWith("FAILED")) {
				String reason = lines[0].substring(7);
				throw new TemporaryFailureException("Scrobble failed: " + reason);
			} else {
				throw new FailureException("Scrobble failed weirdly: " + response);
			}

		} catch (ClientProtocolException e) {
			throw new TemporaryFailureException(TAG + ": " + e.getMessage());
		} catch (IOException e) {
			throw new TemporaryFailureException(TAG + ": " + e.getMessage());
		} finally {

			// reset mTracks, so that we don't hold unnecessary objects
			// in memory
			for (int i = 0; i < mTracks.length; i++) {
				mTracks[i] = null;
			}

			http.getConnectionManager().shutdown();
		}
	}
}
