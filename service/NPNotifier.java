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

import com.adam.aslfms.R;
import com.adam.aslfms.Status;
import com.adam.aslfms.Track;
import com.adam.aslfms.R.string;
import com.adam.aslfms.Status.BadSessionException;
import com.adam.aslfms.Status.FailureException;
import com.adam.aslfms.Status.TemporaryFailureException;
import com.adam.aslfms.service.Handshaker.HandshakeInfo;

import android.content.Context;
import android.util.Log;

public class NPNotifier {

	private static final String TAG = "NPNotifier";

	private final Context mCtx;
	private final HandshakeInfo hInfo;

	public NPNotifier(Context ctx, HandshakeInfo hInfo) {
		super();
		this.mCtx = ctx;
		this.hInfo = hInfo;
	}

	public void notifyNowPlaying(Track track) throws BadSessionException,
			TemporaryFailureException, FailureException {
		Log.d(TAG, "Notifing Playing");

		Log.d(TAG, "Track: " + track.toString());

		DefaultHttpClient http = new DefaultHttpClient();
		HttpPost request = new HttpPost(hInfo.nowPlayingUri);

		List<BasicNameValuePair> data = new LinkedList<BasicNameValuePair>();

		data.add(new BasicNameValuePair("s", hInfo.sessionId));
		data.add(new BasicNameValuePair("a", track.getArtist().toString()));
		data.add(new BasicNameValuePair("b", track.getAlbum().toString()));
		data.add(new BasicNameValuePair("t", track.getTrack().toString()));

		try {
			request.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			ResponseHandler<String> handler = new BasicResponseHandler();
			String response = http.execute(request, handler);
			Log.d(TAG, "npresponse: " + response);
			if (response.startsWith("OK")) {
				Log.i(TAG, "Nowplaying success");
			} else if (response.startsWith("BADSESSION")) {
				throw new BadSessionException(
						"Nowplaying failed because of badsession");
			} else {
				throw new FailureException("NowPlaying failed weirdly");
			}

		} catch (ClientProtocolException e) {
			throw new TemporaryFailureException(e.getMessage());
		} catch (IOException e) {
			throw new TemporaryFailureException(mCtx
					.getString(R.string.auth_network_error));
		} finally {
			http.getConnectionManager().shutdown();
		}
	}
}
