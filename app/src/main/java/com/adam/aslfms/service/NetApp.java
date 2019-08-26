/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/simple-last-fm-scrobbler/sls
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

import android.util.SparseArray;

import com.adam.aslfms.R;
import com.adam.aslfms.util.AppSettings;

public enum NetApp {
    LASTFM(
            0x01, "Last fm", "http://post.audioscrobbler.com/?hs=true", R.string.settings_prefix_last_fm,
            "https://www.last.fm/join", "https://www.last.fm/user/%1",
            "https://ws.audioscrobbler.com/2.0/", R.drawable.ic_last_fm, true), //
    LIBREFM(
            0x02, "Libre fm", "http://turtle.libre.fm/?hs=true", R.string.settings_prefix_libre_fm,
            "https://libre.fm/", "https://libre.fm/user/%1",
            "https://libre.fm/2.0/", R.drawable.ic_libre_fm, true),
    LISTENBRAINZ(
            0x03, "ListenBrainz", "LISTENBRAINZ_URL", R.string.settings_prefix_listenbrainz,
            "https://listenbrainz.org/login/", "https://listenbrainz.org/user/%1",
            "https://api.listenbrainz.org/1/", R.drawable.ic_listenbrainz, true),
    LIBREFMCUSTOM(
            0x04, "GNU-FM Server", "[[GNUKEBOX_URL]]/?hs=true", R.string.settings_prefix_libre_fm_custom,
            "[[NIXTAPE_URL]]", "[[NIXTAPE_URL]]/user/%1", "[[NIXTAPE_URL]]/2.0/",
            R.drawable.ic_libre_fm, true),
    LISTENBRAINZCUSTOM(
            0x05, "ListenBrainz Server", "LISTENBRAINZ_URL_CUSTOM", R.string.settings_prefix_listenbrainz_custom,
            "[[LISTENBRAINZ_URL]]/login/", "[[LISTENBRAINZ_URL]]/user/%1",
            "[[LISTENBRAINZ_API_URL]]/1/", R.drawable.ic_listenbrainz, true
    );

    private final int val;
    private final String name;
    private final String handshakeUrl;
    private final int settingsPrefix;
    private final String signUpUrl;
    private final String profileUrl;
    private final String webserviceUrl;
    private final int iconLocation;
    private final boolean enableSecureSocket;

    NetApp(int val, String name, String handshakeUrl,
           int settingsPrefix, String signUpUrl, String profileUrl, String webserviceUrl, int iconLocation, boolean enableSecureSocket) {
        this.val = val;
        this.name = name;
        this.handshakeUrl = handshakeUrl;
        this.settingsPrefix = settingsPrefix;
        this.signUpUrl = signUpUrl;
        this.profileUrl = profileUrl;
        this.webserviceUrl = webserviceUrl;
        this.iconLocation = iconLocation;
        this.enableSecureSocket = enableSecureSocket;
    }

    public Boolean getSecureSocketEnabled(AppSettings settings){
        if (val == 4) {
            return settings.getSecureSocketLibreFm(this);
        }
        if (val == 5) {
            return settings.getSecureSocketListenbrainz(this);
        }
        return enableSecureSocket;
    }

    public String getIntentExtraValue() {
        return toString();
    }

    public int getValue() {
        return this.val;
    }

    public String getName() {
        return this.name;
    }

    /* public int getIconLocation(){
        return this.iconLocation;
    }*/

    public String getHandshakeUrl(AppSettings settings) {
        return replacePlaceholders(settings, this.handshakeUrl);
    }

    public int getSettingsPrefix() {
        return settingsPrefix;
    }

    public String getProfileUrl(AppSettings settings) {
        return replacePlaceholders(
                settings, profileUrl.replaceAll("%1", settings.getUsername(this))
        );
    }

    public String getSignUpUrl(AppSettings settings) {
        return replacePlaceholders(
                settings, signUpUrl
        );
    }

    public String getWebserviceUrl(AppSettings settings) {
        return replacePlaceholders(settings, webserviceUrl);
    }

    private static SparseArray<NetApp> mValNetAppMap;

    static {

        mValNetAppMap = new SparseArray<NetApp>();
        for (NetApp napp : NetApp.values()) {
            mValNetAppMap.put(napp.getValue(), napp);
        }
    }

    public static NetApp fromValue(int value) {
        NetApp napp = mValNetAppMap.get(value);
        if (napp == null) {
            throw new IllegalArgumentException("Got null NetApp in fromValue: "
                    + value);
        }
        return napp;
    }

    private String replacePlaceholders(AppSettings settings, String value) {
        if (val == 4) {
            value = value.replace("[[GNUKEBOX_URL]]", settings.getGnukeboxUrl(this));
            value = value.replace("[[NIXTAPE_URL]]", settings.getNixtapeUrl(this));
        }
        if (val == 5) {
            value = value.replace("[[LISTENBRAINZ_URL]]", settings.getListenBrainzUrl(this));
            value = value.replace("[[LISTENBRAINZ_API_URL]]", settings.getListenBrainzApiUrl(this));
        }
        return value;
    }
}
