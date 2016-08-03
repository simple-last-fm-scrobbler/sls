/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p/>
 * https://github.com/tgwizard/sls
 * <p/>
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adam.aslfms.service;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.adam.aslfms.R;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.MD5;
import com.adam.aslfms.util.Track;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by Debugs on 7/9/2016.
 */
public class Heart extends NetRunnable {

    private static final String TAG = "Heart";

    protected Track hearTrack;
    protected AppSettings settings;
    Context mCtx;


    public Heart(NetApp napp, Context ctx, Networker net, Track hearTrack, AppSettings settings) {
        super(napp, ctx, net);
        this.hearTrack = hearTrack;
        this.settings = settings;
        this.mCtx = ctx;
    }

    public final void run() {

// can't heart track

        String sigText = "api_key"
                + settings.rcnvK(settings.getAPIkey())
                + "artist" + hearTrack.getArtist()
                + "methodtrack.lovesk"
                + settings.getSessionKey(NetApp.LASTFM)
                + "track" + hearTrack.getTrack()
                + settings.rcnvK(settings.getSecret());

        String signature = MD5.getHashString(sigText);

        try {
            String heartResult = postHeartTrack(hearTrack, settings.rcnvK(settings.getAPIkey()), signature, settings.getSessionKey(NetApp.LASTFM));

            if (heartResult.contains("status=\"ok\"")) {

                Handler h = new Handler(mCtx.getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mCtx, mCtx.getString(R.string.loved_track),Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d(TAG, "Successful heart track.");
            } else if (heartResult.contains("code=\"9\"")) {
                // store hearTrack in database or allow failure.
                // settings.setSessionKey(NetApp.LASTFM, "");
            } else {
                Log.d(TAG, "Failed heart track.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Heart track fail " + e);
            e.printStackTrace();
        }
    }

    private String postHeartTrack(Track track, String testAPI, String signature, String
            sessionKey) throws IOException, NullPointerException {
        URL url = new URL("http://ws.audioscrobbler.com/2.0/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setReadTimeout(7000);
        conn.setConnectTimeout(7000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        conn.setDoOutput(true);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("method", "track.love");
        params.put("track", track.getTrack());
        params.put("artist", track.getArtist());
        params.put("api_key", testAPI);
        params.put("api_sig", signature);
        params.put("sk", sessionKey);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

        //Log.d(TAG,"Love track post: "+postData.toString());

        conn.getOutputStream().write(postDataBytes);
        //Log.i(TAG, params.toString());

        conn.connect();

        int resCode = conn.getResponseCode();
        Log.d(TAG, "Response code: " + resCode);

        BufferedReader outputStream;
        if (resCode==200) {
            outputStream = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            outputStream = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        conn.disconnect();
        String heartResult = "";
        String inputLine;
        while ((inputLine = outputStream.readLine()) != null) {
            heartResult += inputLine;
        }
        Log.d(TAG, "Heart result: " + heartResult);
        return heartResult; //close()
    }
}
