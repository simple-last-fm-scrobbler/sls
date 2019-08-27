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

import android.content.Context;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;

import java.util.EnumMap;
import java.util.Map;

public class NetworkerManager {

    @SuppressWarnings("unused")
    private static final String TAG = "SLSNetManager";

    private final AppSettings settings;

    private Map<NetApp, Networker> mSupportedNetApps;

    public NetworkerManager(Context ctx, ScrobblesDatabase db) {
        this.settings = new AppSettings(ctx);
        mSupportedNetApps = new EnumMap<NetApp, Networker>(NetApp.class);
        for (NetApp napp : NetApp.values())
            mSupportedNetApps.put(napp, new Networker(napp, ctx, db));
    }

    public void launchAuthenticator(NetApp napp) {
        mSupportedNetApps.get(napp).launchAuthenticator();
    }

    public void launchClearCreds(NetApp napp) {
        mSupportedNetApps.get(napp).launchClearCreds();
    }

    public void launchHandshaker(NetApp napp) {
        mSupportedNetApps.get(napp).launchHandshaker();
    }

    public void launchHandshakers() {
        for (NetApp napp : NetApp.values()) {
            launchHandshaker(napp);
        }
    }

    public void launchClearAllCreds() {
        for (Networker nw : mSupportedNetApps.values())
            nw.launchClearCreds();
    }

    public void launchNPNotifier(Track track) {
        for (NetApp napp : NetApp.values()) {
            if (settings.isAuthenticated(napp)) {
                mSupportedNetApps.get(napp).launchNPNotifier(track);
            }
        }
    }

    public void launchScrobbler(NetApp napp) {
        if (settings.isAuthenticated(napp)) {
            mSupportedNetApps.get(napp).launchScrobbler();
        }
    }

    public void launchAllScrobblers() {
        for (NetApp napp : NetApp.values()) {
            launchScrobbler(napp);
        }
    }

    public void launchHeartTrack(Track track, NetApp napp) {
        mSupportedNetApps.get(napp).launchHeartTrack(track);
    }

    public void launchGetUserInfo(NetApp napp) {
        mSupportedNetApps.get(napp).launchUserInfo();
    }

}
