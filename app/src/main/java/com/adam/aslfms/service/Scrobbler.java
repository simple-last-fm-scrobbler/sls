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
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.AuthStatus.BadSessionException;
import com.adam.aslfms.util.AuthStatus.TemporaryFailureException;
import com.adam.aslfms.util.enums.SubmissionType;

/**
 * @author tgwizard
 */
public class Scrobbler extends AbstractSubmitter {

    private static final String TAG = "Scrobbler";

    // private final Context mCtx;
    private final ScrobblesDatabase mDb;

    public static final int MAX_SCROBBLE_LIMIT = 50;

    public Scrobbler(NetApp napp, Context ctx, Networker net,
                     ScrobblesDatabase db) {
        super(napp, ctx, net);
        this.mDb = db;
    }

    @Override
    public boolean doRun(HandshakeResult hInfo) {
        boolean ret;
        try {
            Log.d(TAG, "Scrobbling: " + getNetApp().getName());
            Track[] tracks = mDb.fetchTracksArray(getNetApp(), MAX_SCROBBLE_LIMIT);

            if (tracks.length == 0) {
                Log.d(TAG, "Retrieved 0 tracks from db, no scrobbling: " + getNetApp().getName());
                return true;
            }
            Log.d(TAG, "Retrieved " + tracks.length + " tracks from db: " + getNetApp().getName());

            for (Track track : tracks) {
                Log.d(TAG, getNetApp().getName() + ": " + track.toString());
            }

            scrobbleCommit(hInfo, tracks); // throws if unsuccessful

            // delete scrobbles (not tracks) from db (not array)
            for (Track track : tracks) {
                mDb.deleteScrobble(getNetApp(), track.getRowId());
            }

            // clean up tracks if no one else wants to scrobble them
            mDb.cleanUpTracks();

            // there might be more tracks in the db
            if (tracks.length == MAX_SCROBBLE_LIMIT) {
                Log.d(TAG, "Relaunching scrobbler, might be more tracks in db");
                relaunchThis();
            }

            // status stuff
            notifySubmissionStatusSuccessful(tracks[tracks.length - 1],
                    tracks.length);

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
        getNetworker().launchScrobbler();
    }

    private void notifySubmissionStatusFailure(String reason) {
        super.notifySubmissionStatusFailure(SubmissionType.SCROBBLE, reason);
    }

    private void notifySubmissionStatusSuccessful(Track track, int statsInc) {
        super.notifySubmissionStatusSuccessful(SubmissionType.SCROBBLE, track,
                statsInc);
    }

    /**
     * @return a {@link ScrobbleResult} struct with some info
     * @throws BadSessionException
     * @throws TemporaryFailureException
     */
    public void scrobbleCommit(HandshakeResult hInfo, Track[] tracks)
            throws BadSessionException, TemporaryFailureException {

        URL url = null;
        HttpURLConnection conn = null;

// handle Exception
        try {
            url = new URL(hInfo.scrobbleUri);
            Log.e(TAG,url.toString());
        } catch (MalformedURLException e) {
            Log.d(TAG, "The URL is not valid.");
            Log.d(TAG, e.getMessage());
            throw new TemporaryFailureException(TAG + ": " + e.getMessage());
        }

        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("s", hInfo.sessionId);
            for (int i = 0; i < tracks.length; i++) {
                Track track = tracks[i];
                String is = "[" + i + "]";
                params.put("a" + is, track.getArtist());
                params.put("b" + is, track.getAlbum());
                params.put("t" + is, track.getTrack());
                params.put("i" + is, Long.toString(track
                        .getWhen()));
                params.put("o" + is, track.getSource());
                params.put("l" + is, Integer.toString(track
                        .getDuration()));
                params.put("n" + is, track.getTrackNr());
                params.put("m" + is, track.getMbid());
                params.put("r" + is, track.getRating());
                try {
                if (url.toString().contains("audioscrobbler")&&track.getRating().equals("L")) {

                    Log.e(TAG,"Launching heart service. "+settings.getPassword(NetApp.LASTFM)+" "+settings.getUsername(NetApp.LASTFM));
                }} catch (Exception e) {
                    Log.e(TAG,"Exc: "+e);
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (NullPointerException e) {
                throw new TemporaryFailureException(TAG + ": " + e.getMessage());
            }
            // Log.d(TAG,conn.toString());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            Log.i(TAG, params.toString());

            BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder rsponse = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                rsponse.append(line).append('\n');
            }
            String response = rsponse.toString();
            Log.d(TAG, response);
            String[] lines = response.split("\n");
            if (response.startsWith("OK")) {
                Log.i(TAG, "Scrobble success: " + getNetApp().getName());
            } else if (response.startsWith("BADSESSION")) {
                throw new BadSessionException("Scrobble failed because of badsession");
            } else if (response.startsWith("FAILED")) {
                String reason = lines[0].substring(7);
                throw new TemporaryFailureException("Scrobble failed: " + reason);
            } else {
                throw new TemporaryFailureException("Scrobble failed weirdly: " + response);
            }

        } catch (IOException e) {
            throw new TemporaryFailureException(TAG + ": " + e.getMessage());
        }
        try {
            conn.disconnect();
        } catch (NullPointerException e) {
            throw new TemporaryFailureException(TAG + ": " + e.getMessage());
        }
    }
}
