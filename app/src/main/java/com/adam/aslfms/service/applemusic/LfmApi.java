package com.adam.aslfms.service.applemusic;

import android.util.Log;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.AppSettings;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 4-Eyes on 16/3/2017.
 *
 */

class LfmApi {

    private final AppSettings settings;

    LfmApi(AppSettings settings) {
        this.settings = settings;
    }

    private String getTrackInfo(TrackData data) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(NetApp.LASTFM.getWebserviceUrl(settings));

            conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(7000);
            conn.setConnectTimeout(7000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("method", "track.getInfo");
            params.put("track", data.getTitle());
            params.put("artist", data.getArtist());
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

            conn.connect();

            int resCode = conn.getResponseCode();
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
            return stringBuilder.toString();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return "";
    }

    long getTrackDuration(TrackData data) {
        String response = getTrackInfo(data);

        try {
            JSONObject object = new JSONObject(response);
            if (object.has("error")) {
                // TODO maybe do something with this
                int code = object.getInt("error");
                Log.e("LfmAPI", String.format("Failed to get track duration with error code %s", code));
            } else {
                long duration = object.getJSONObject("track").getLong("duration");
                Log.i("LfmAPI", String.format("Successfully got duration for song %s, by %s",
                        data.getTitle(), data.getArtist()));
                return duration == 0 ? NotificationService.DEFAULT_SONG_LENGTH : duration;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return NotificationService.DEFAULT_SONG_LENGTH;

    }
}

