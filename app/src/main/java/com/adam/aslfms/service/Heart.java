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
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.adam.aslfms.R;
import com.adam.aslfms.UserCredActivity;
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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;


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


        final String testAPI = "ee08433b0c51f9978bc97bca7ed9620a";
        final String testSharedSecret = "f2483a76d484ef82bc518f6b2dc7ca4e";

// can't heart track

        String passwd = settings.getPassword(NetApp.LASTFM);
        String userName = settings.getUsername(NetApp.LASTFM).toLowerCase();

        String sign = "api_key" + testAPI + "methodauth.getMobileSessionpassword" + passwd + "username" + userName + testSharedSecret;
        String sign2 = "api_key" + testAPI + "artist" + hearTrack.getArtist() + "methodtrack.lovesk" + settings.getSessionKey(NetApp.LASTFM) + "track" + hearTrack.getTrack() + testSharedSecret;
        String signature = MD5.getHashString(sign);
        String sig2 = MD5.getHashString(sign2);

        String result = "";
        String heartResult = "";
        //Log.d(TAG, "sk: "+settings.getSessionKey(NetApp.LASTFM));

        if (settings.getSessionKey(NetApp.LASTFM).equals("")) {
            if(passwd.equals("")){
                Intent credsI = new Intent(mCtx, UserCredActivity.class);
                mCtx.startActivity(credsI);
                return;
            }
            InputStream is;
            try {
                is = authenticateNew(testAPI, signature, userName, passwd);

                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    result += inputLine;
                }

                Log.d(TAG, "Session Result: " + result);
                if (result.contains("status=\"ok\"")) {
                    final Pattern pattern = Pattern.compile("<key>(.*?)</key>");
                    final Matcher matcher = pattern.matcher(result);
                    if (matcher.find()) {
                        settings.setSessionKey(NetApp.LASTFM, matcher.group(1));
                        //Log.d(TAG, matcher.group(1));
                        settings.setPassword(NetApp.LASTFM, "");
                    } else {
                        Log.e(TAG, "Session KEY not FOUND!!");
                    }
                }
            } catch (Exception e) {
                //Log.e(TAG, "Exc: " + e);
                e.printStackTrace();
            }

        } else {
            InputStream os;
            try {
                os = postHeartTrack(hearTrack, testAPI, sig2, settings.getSessionKey(NetApp.LASTFM));
                BufferedReader in = new BufferedReader(new InputStreamReader(os));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    heartResult += inputLine;
                }
                Log.d(TAG, "Post result: " + heartResult);

                if (heartResult.contains("status=\"ok\"")) {
                    settings.setPassword(NetApp.LASTFM, "");
                }
            } catch (Exception e) {
                //Log.e(TAG, "postHeartTrack "+e);
                e.printStackTrace();
            }
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

    private InputStream authenticateNew(String testAPI, String signature, String
            userName, String passwd)
            throws KeyManagementException, NoSuchAlgorithmException, IOException {
        URL url = new URL("https://ws.audioscrobbler.com/2.0/");
        //Log.d(TAG, url.toString());
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        // Create the SSL connection
        SSLContext sc;
        sc = SSLContext.getInstance("TLS");
        sc.init(null, null, new java.security.SecureRandom());
        conn.setSSLSocketFactory(sc.getSocketFactory());

        // set Timeout and method
        conn.setReadTimeout(7000);
        conn.setConnectTimeout(7000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        conn.setDoInput(true);

        // Add any data you wish to post here
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("method", "auth.getMobileSession");
        params.put("username", userName);
        params.put("password", passwd);
        params.put("api_key", testAPI);
        params.put("api_sig", signature);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.getOutputStream().write(postDataBytes);

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
