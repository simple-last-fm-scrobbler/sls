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
import com.adam.aslfms.ScrobblesDatabase;
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
public class Scrobbler extends AbstractSubmitter {

	private static final String TAG = "Scrobbler";

	private final AppSettings settings;

	// private final Context mCtx;
	private final ScrobblesDatabase mDbHelper;

	public static final int MAX_SCROBBLE_LIMIT = 50;
	private Track[] mTracks;

	public Scrobbler(Context ctx, Networker net, ScrobblesDatabase dbHelper) {
		super(ctx, net);
		this.settings = new AppSettings(ctx);
		this.mDbHelper = dbHelper;
		this.mTracks = new Track[MAX_SCROBBLE_LIMIT];
	}

	@Override
	public boolean doRun(HandshakeResult hInfo) {
		// TODO Auto-generated method stub
		boolean ret;
		try {
			ScrobbleResult sr = scrobbleCommit(hInfo);
			if (sr.tracksLeftInDb != 0) {
				relaunchThis();
			}

			// status stuff
			settings.setLastScrobbleSuccess(true);
			if (sr.tracksScrobbled != 0) {
				settings.setLastScrobbleTime(Util.currentTimeMillisLocal());
				settings.setNumberOfScrobbles(settings.getNumberOfScrobbles()
						+ sr.tracksScrobbled);
				if (sr.lastTrack != null) {
					settings.setLastScrobbleInfo("\"" + sr.lastTrack.getTrack()
							+ "\" " + getContext().getString(R.string.by) + " "
							+ sr.lastTrack.getArtist());
				} else {
					Log.e(TAG, "Got null track left over from Scrobbler");
				}
			}
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
		getNetworker().launchScrobbler();
	}

	/**
	 * 
	 * @return a {@link ScrobbleResult} struct with some info
	 * @throws BadSessionException
	 * @throws TemporaryFailureException
	 * @throws UnknownResponseException
	 */
	public ScrobbleResult scrobbleCommit(HandshakeResult hInfo)
			throws BadSessionException, TemporaryFailureException {

		int count = mDbHelper.fetchScrobblesArray(mTracks, MAX_SCROBBLE_LIMIT);
		if (count == 0) {
			Log.d(TAG, "Retrieved 0 tracks from db, no scrobbling");
			return new ScrobbleResult(0, 0, null);
		}

		ScrobbleResult res;

		Log.d(TAG, "Will scrobble");
		Log.d(TAG, "Retrieved " + count + " tracks from db");
		if (count > MAX_SCROBBLE_LIMIT) {
			res = new ScrobbleResult(count - MAX_SCROBBLE_LIMIT,
					MAX_SCROBBLE_LIMIT, mTracks[MAX_SCROBBLE_LIMIT - 1]);
			Log.d(TAG, "But only " + MAX_SCROBBLE_LIMIT
					+ " will be submitted just now");
			count = MAX_SCROBBLE_LIMIT;
		} else {
			res = new ScrobbleResult(0, count, mTracks[count - 1]);
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
			String[] lines = response.split("\n");
			if (response.startsWith("OK")) {
				Log.i(TAG, "Scrobble success");

				for (int i = 0; i < count; i++) {
					mDbHelper.deleteScrobble(mTracks[i]);
				}

				// resettings mTracks in the finally-clause below

				return res;
			} else if (response.startsWith("BADSESSION")) {
				throw new BadSessionException(
						"Scrobble failed because of badsession");
			} else if (response.startsWith("FAILED")) {
				String reason = lines[0].substring(7);
				throw new TemporaryFailureException("Scrobble failed: "
						+ reason);
			} else {
				throw new TemporaryFailureException("Scrobble failed weirdly: "
						+ response);
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

	/**
	 * Small struct holding the results of a successful scrobble request. All
	 * the fields are final and public.
	 * 
	 * @author tgwizard
	 * 
	 */
	public static class ScrobbleResult {
		/**
		 * The number of tracks left in the db after the scrobble was completed.
		 * If this is not 0, then {@link ScrobbleResult#tracksScrobbled
		 * tracksScrobbled} equals {@link Scrobbler#MAX_SCROBBLE_LIMIT}.
		 */
		public final int tracksLeftInDb;

		/**
		 * The number of tracks this scrobble request submitted to Last.fm.
		 */
		public final int tracksScrobbled;

		/**
		 * The last played of the tracks submitted in the scrobble request, or
		 * <code>null</code> if none were sent.
		 */
		public final Track lastTrack;

		/**
		 * Constructs a new struct holding the result of a scrobble request.
		 * Only {@link Scrobbler} can get the information needed to instantiate
		 * this class, and therefore the constructor is private.
		 * 
		 * @param tracksLeftInDb
		 *            {@link ScrobbleResult#tracksLeftInDb tracksLeftInDb}
		 * @param tracksScrobbled
		 *            {@link ScrobbleResult#tracksScrobbled tracksScrobbled}
		 * @param lastTrack
		 *            {@link ScrobbleResult#lastTrack lastTrack}
		 */
		private ScrobbleResult(int tracksLeftInDb, int tracksScrobbled,
				Track lastTrack) {
			super();
			this.tracksLeftInDb = tracksLeftInDb;
			this.tracksScrobbled = tracksScrobbled;
			this.lastTrack = lastTrack;
		}
	}

}
