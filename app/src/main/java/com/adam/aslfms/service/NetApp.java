/**
 * This file is part of Simple Last.fm Scrobbler.
 *
 *     https://github.com/tgwizard/sls
 *
 * Copyright 2011 Simple Last.fm Scrobbler Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adam.aslfms.service;

import android.util.SparseArray;

import com.adam.aslfms.util.AppSettings;

public enum NetApp {
    LASTFM(
            0x01, "Last.fm", "http://post.audioscrobbler.com/?hs=true", "",
            "https://www.last.fm/join", "https://www.last.fm/user/%1",
            "https://ws.audioscrobbler.com/2.0/"), //
    LIBREFM(
            0x02, "Libre.fm", "http://turtle.libre.fm/?hs=true", "librefm",
            "https://libre.fm/", "https://libre.fm/user/%1", "https://libre.fm/2.0/"),
    CUSTOM(
            0x03, "Gnu-fm server", "[[GNUKEBOX_URL]]/?hs=true", "custom",
            "[[NIXTAPE_URL]]", "[[NIXTAPE_URL]]/user/%1", "[[NIXTAPE_URL]]/2.0/");

    private final int val;
    private final String name;
    private final String handshakeUrl;
    private final String settingsPrefix;
    private final String signUpUrl;
    private final String profileUrl;
    private final String webserviceUrl;

    NetApp(int val, String name, String handshakeUrl,
           String settingsPrefix, String signUpUrl, String profileUrl, String webserviceUrl) {
        this.val = val;
        this.name = name;
        this.handshakeUrl = handshakeUrl;
        this.settingsPrefix = settingsPrefix;
        this.signUpUrl = signUpUrl;
        this.profileUrl = profileUrl;
        this.webserviceUrl = webserviceUrl;
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

    public String getHandshakeUrl(AppSettings settings) {
        return replacePlaceholders( settings, this.handshakeUrl );
    }

    public String getSettingsPrefix() {
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
        if (settingsPrefix.equals("custom")) {
            value = value.replace("[[GNUKEBOX_URL]]", settings.getGnukeboxUrl(this));
            value = value.replace("[[NIXTAPE_URL]]", settings.getNixtapeUrl(this));
        }
        return value;
    }
}
