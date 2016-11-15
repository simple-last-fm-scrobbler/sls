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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Debugs on 8/13/2016.
 *
 * @author
 */
public class SecureSSLSocketFactory extends SSLSocketFactory {

    private static final String TAG = "SecSSLSockFactory";

    private final SSLSocketFactory delegate;
    private HandshakeCompletedListener handshakeListener;

    public SecureSSLSocketFactory(
            SSLSocketFactory delegate, HandshakeCompletedListener handshakeListener) {
        this.delegate = delegate;
        this.handshakeListener = handshakeListener;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException {
        SSLSocket socket = (SSLSocket) this.delegate.createSocket(s, host, port, autoClose);

        if (null != this.handshakeListener) {
            socket.addHandshakeCompletedListener(this.handshakeListener);
        }

        return socket;
    }

    @Override
    public Socket createSocket(String arg0, int arg1) throws IOException {

        SSLSocket socket = (SSLSocket) this.delegate.createSocket(arg0, arg1);

        if (null != this.handshakeListener) {
            socket.addHandshakeCompletedListener(this.handshakeListener);
        }

        return socket;
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {

        SSLSocket socket = (SSLSocket) this.delegate.createSocket(arg0, arg1);

        if (null != this.handshakeListener) {
            socket.addHandshakeCompletedListener(this.handshakeListener);
        }

        return socket;
    }

    @Override
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
            throws IOException {

        SSLSocket socket = (SSLSocket) this.delegate.createSocket(arg0, arg1, arg2, arg3);

        if (null != this.handshakeListener) {
            socket.addHandshakeCompletedListener(this.handshakeListener);
        }

        return socket;
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
                               int arg3) throws IOException {

        SSLSocket socket = (SSLSocket) this.delegate.createSocket(arg0, arg1, arg2, arg3);

        if (null != this.handshakeListener) {
            socket.addHandshakeCompletedListener(this.handshakeListener);
        }

        return socket;
    }
// and so on for all the other createSocket methods of SSLSocketFactory.

    @Override
    public String[] getDefaultCipherSuites() {
        // TODO: or your own preferences
        return this.delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        // TODO: or your own preferences
        return this.delegate.getSupportedCipherSuites();
    }
}
