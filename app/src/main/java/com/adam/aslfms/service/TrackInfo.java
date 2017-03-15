package com.adam.aslfms.service;

import android.content.Context;
import android.util.Log;

import com.adam.aslfms.util.AppSettings;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 4-Eyes on 15/3/2017.
 *
 */

public class TrackInfo extends NetRunnable {


    private static final String TAG = "TrackInfo";
    private final AppSettings settings;
    private final Context mCtx;

    public TrackInfo(NetApp napp, Context ctx, Networker net, AppSettings settings) {
        super(napp, ctx, net);
        this.settings = settings;
        this.mCtx = ctx;
    }

    @Override
    public void run() {
        HttpURLConnection conn = null;
        try {
            String response = getTrackInfo();

            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("error")) {
                // TODO maybe do something with this
                int code = jsonObject.getInt("error");
                settings.setCurrentAppleTrackDuration(3 * 60000);
            } else {
                long duration = jsonObject.getJSONObject("track").getLong("duration");
                settings.setCurrentAppleTrackDuration(duration);
                Log.i(TAG, "Successfully got duration for apple track");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTrackInfo() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(getNetApp().getWebserviceUrl(settings));

            conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(7000);
            conn.setConnectTimeout(7000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("method", "track.getInfo");
            params.put("track", settings.getCurrentAppleTrack());
            params.put("artist", settings.getCurrentAppleArtist());
            params.put("api_key", settings.rcnvK(settings.getAPIkey()));
            params.put("format", "json");

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
            //Log.i(TAG, params.toString());

            conn.connect();

            int resCode = conn.getResponseCode();
            Log.d(TAG, "Response code: " + this.getNetApp().getName() + ": " + resCode);
            BufferedReader r;
            if (resCode == -1) {
                return "";
            } else if (resCode == 200) {
                r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            String response = stringBuilder.toString();
            Log.d(TAG, response);
            return response;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return "";
    }
}
