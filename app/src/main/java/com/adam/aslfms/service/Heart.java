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

import android.content.Context;
import android.util.Log;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.MD5;
import com.adam.aslfms.util.Track;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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

        String sign2 = "api_key" + settings.getAPIkey() + "artist" + hearTrack.getArtist() + "methodtrack.lovesk" + settings.getSessionKey(NetApp.LASTFM) + "track" + hearTrack.getTrack() + settings.getSecret();

        String sig2 = MD5.getHashString(sign2);

        String heartResult = "";
        //Log.d(TAG, "sk: "+settings.getSessionKey(NetApp.LASTFM));

        InputStream os;
        try {
            os = postHeartTrack(hearTrack, settings.getAPIkey(), sig2, settings.getSessionKey(NetApp.LASTFM));
            BufferedReader in = new BufferedReader(new InputStreamReader(os));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                heartResult += inputLine;
            }
            Log.d(TAG, "Post result: " + heartResult);

            if (heartResult.contains("status=\"ok\"")) {
                Log.d(TAG,"Successful heart track.");
            } else {
                // store hearTrack in database or allow failure.
            }
        } catch (Exception e) {
            Log.e(TAG, "Heart track fail "+e);
            e.printStackTrace();
        }
    }

    private InputStream postHeartTrack(Track track, String testAPI, String signature, String
            sessionKey) throws IOException {
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

        Log.d(TAG, "Response code: " + conn.getResponseCode());
        InputStream error = conn.getErrorStream();
        InputStream streamOut;
        if (error == null) {
            streamOut = conn.getInputStream();
        } else {
            streamOut = error;
        }
        conn.disconnect();
        return streamOut;
    }


}
