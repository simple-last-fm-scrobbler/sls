/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/tgwizard/sls
 * <p>
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.adam.aslfms.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

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
 * @author tgwizard
 */
public class    NPNotifier extends AbstractSubmitter {

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
     * @param track the track to send as notification
     * @throws BadSessionException       means that a new handshake is needed
     * @throws TemporaryFailureException
     * @throws UnknownResponseException  {@link UnknownResponseException}
     */
    public void notifyNowPlaying(Track track, HandshakeResult hInfo)
            throws BadSessionException, TemporaryFailureException {
        Log.d(TAG, "Notifying now playing: " + getNetApp().getName());

        Log.d(TAG, getNetApp().getName() + ": " + track.toString());
        URL url;
        HttpURLConnection conn = null;

// handle Exception
        try {
            url = new URL(hInfo.nowPlayingUri);
            // Log.d(TAG,url.toString());
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("s", hInfo.sessionId);
            params.put("a", track.getArtist());
            params.put("b", track.getAlbum());
            params.put("t", track.getTrack());
            params.put("i", Long.toString(track
                    .getWhen()));
            params.put("o", track.getSource());
            params.put("l", Integer.toString(track
                    .getDuration()));
            params.put("n", track.getTrackNr());
            params.put("m", track.getMbid());
            params.put("r", track.getRating());

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            conn = (HttpURLConnection) url.openConnection();
            // Log.d(TAG,conn.toString());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            Log.i(TAG, params.toString());

            int resCode = conn.getResponseCode();
            Log.d(TAG, "Response code: " + resCode);
            BufferedReader r;
            if (resCode==200) {
                r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            StringBuilder rsponse = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                rsponse.append(line).append('\n');
            }
            String response = rsponse.toString();
            // some redundancy here ?
            String[] lines = response.split("\n");
            Log.d(TAG, "Session Result: " + lines.length + " : " + response);

            if (response.startsWith("OK")) {
                Log.i(TAG, "Now playing success: " + getNetApp().getName());
            } else if (response.startsWith("BADSESSION")) {
                throw new BadSessionException("Now Playing failed because of badsession");
            } else if (response.startsWith("FAILED")) {
                String reason = lines[0].substring(7);
                throw new TemporaryFailureException("Now Playing failed: " + reason);
            } else {
                throw new TemporaryFailureException("Now Playing failed weirdly: " + response);
            }

        } catch (IOException | NullPointerException e) {
            throw new TemporaryFailureException(TAG + ": " + e.getMessage());
        } finally {
            if (conn!=null){
                conn.disconnect();
            }
        }
    }
}
