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
            "https://www.last.fm/join", "https://www.last.fm/user/%1"), //
    LIBREFM(
            0x02, "Libre.fm", "http://turtle.libre.fm/?hs=true", "librefm",
            "https://libre.fm/", "https://libre.fm/user/%1");

    private final int val;
    private final String name;
    private final String handshakeUrl;
    private final String settingsPrefix;
    private final String signUpUrl;
    private final String profileUrl;

    private NetApp(int val, String name, String handshakeUrl,
                   String settingsPrefix, String signUpUrl, String profileUrl) {
        this.val = val;
        this.name = name;
        this.handshakeUrl = handshakeUrl;
        this.settingsPrefix = settingsPrefix;
        this.signUpUrl = signUpUrl;
        this.profileUrl = profileUrl;
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

    public String getHandshakeUrl() {
        return this.handshakeUrl;
    }

    public String getSettingsPrefix() {
        return settingsPrefix;
    }

    public String getProfileUrl(AppSettings settings) {
        return profileUrl.replaceAll("%1", settings.getUsername(this));
    }

    public String getSignUpUrl() {
        return signUpUrl;
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

}
