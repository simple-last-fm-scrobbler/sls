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

import android.util.Log;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

/**
 * Created by Debugs on 8/13/2016.
 */
public class MyHandshakeCompletedListener implements HandshakeCompletedListener {

    private static final String TAG = "HandShakeListenR";

    @Override
    public void handshakeCompleted(HandshakeCompletedEvent event) {
        SSLSession session = event.getSession();
        String protocol = session.getProtocol();
        String cipherSuite = session.getCipherSuite();
        String peerName = null;


        try {
            peerName = session.getPeerPrincipal().getName();
            Log.d(TAG, "peerName: " + peerName);
        } catch (SSLPeerUnverifiedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "session: " + session);
        Log.d(TAG, "protocol: " + protocol);
        Log.d(TAG, "cipherSuite: " + cipherSuite);

    }
}

