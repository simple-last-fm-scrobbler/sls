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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adam.aslfms.R;
import com.adam.aslfms.SettingsActivity;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.MD5;
import com.adam.aslfms.util.AuthStatus;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.AuthStatus.BadAuthException;
import com.adam.aslfms.util.AuthStatus.ClientBannedException;
import com.adam.aslfms.util.AuthStatus.TemporaryFailureException;
import com.adam.aslfms.util.AuthStatus.UnknownResponseException;
import com.adam.aslfms.util.Util.NetworkStatus;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * @author tgwizard 2009
 */
public class Handshaker extends NetRunnable {

    private static final String TAG = "Handshaker";

    public enum HandshakeAction {
        HANDSHAKE, AUTH, CLEAR_CREDS
    }

    private final AppSettings settings;

    private final HandshakeAction hsAction;

    private final Context mCtx;

    public Handshaker(NetApp napp, Context ctx, Networker net,
                      HandshakeAction hsAction) {
        super(napp, ctx, net);
        this.hsAction = hsAction;
        this.settings = new AppSettings(ctx);
        this.mCtx = ctx;
    }

    @Override
    public void run() {

        if (hsAction == HandshakeAction.CLEAR_CREDS) {
            settings.clearCreds(getNetApp());
            // current hInfo is invalid
            getNetworker().setHandshakeResult(null);
            // this should mean that the user called launchClearCreds, and that
            // all user information is gone
            notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_NOAUTH);
            // can't scrobble without a user
            getNetworker().unlaunchScrobblingAndNPNotifying();
            return;
        }

        if (hsAction == HandshakeAction.AUTH)
            notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_UPDATING);

        // check network status
        NetworkStatus ns = Util.checkForOkNetwork(getContext());
        if (ns != NetworkStatus.OK) {
            Log.d(TAG, "Waits on network, network-status: " + ns);
            if (hsAction == HandshakeAction.AUTH) {
                if (ns == NetworkStatus.UNFIT)
                    notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_NETWORKUNFIT);
                else {
                    notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_RETRYLATER);
                    Util.myNotify(mCtx, SettingsActivity.class, getNetApp().getName(),
                            mCtx.getString(R.string.auth_network_error_retrying), 39201);
                }
            }

            getNetworker().launchNetworkWaiter();
            getNetworker().launchHandshaker(hsAction);
            return;
        }

        try {
            // current hInfo is invalid
            getNetworker().setHandshakeResult(null);


            // might throw stuff
            HandshakeResult hInfo = handshake();

            getNetworker().setHandshakeResult(hInfo);

            // no more sleeping, handshake succeeded
            getNetworker().resetSleeper();
            // we don't need/want it anymore, settings.getPwdMd5() is enough
            settings.setPassword(getNetApp(), "");


            notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_OK);

            // won't do anything if there aren't any scrobbles,
            // but will submit those tracks that were prepared
            // but interrupted by a badauth
            // getNetworker().launchScrobbler();

        } catch (BadAuthException e) {
            e.printStackTrace();
            if (hsAction == HandshakeAction.AUTH
                    || hsAction == HandshakeAction.HANDSHAKE)
                notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_BADAUTH);
            else {
                // CLEAR_CREDS should've been caught eariler
                Log.e(TAG, "got badauth when doAuth is weird: "
                        + hsAction.toString());
            }
            // badauth means we cant do any scrobbling/notifying, so clear them
            // the scrobbles already prepared will be sent at a later time
            e.getStackTrace();
            Util.myNotify(mCtx, SettingsActivity.class, getNetApp().getName(),
                    mCtx.getString(R.string.auth_bad_auth), 39201);

            getNetworker().unlaunchScrobblingAndNPNotifying();
        } catch (TemporaryFailureException e) {
            e.printStackTrace();
            Log.i(TAG, "Tempfail: " + e.getMessage() + ": "
                    + getNetApp().getName());

            if (hsAction == HandshakeAction.AUTH)
                notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_RETRYLATER);

            if (Util.checkForOkNetwork(getContext()) != NetworkStatus.OK) {
                // no more sleeping, network down
                getNetworker().resetSleeper();
                getNetworker().launchNetworkWaiter();
                getNetworker().launchHandshaker(hsAction);
            } else {
                getNetworker().launchSleeper();
                getNetworker().launchHandshaker(hsAction);
            }
            Util.myNotify(mCtx, SettingsActivity.class, getNetApp().getName(),
                    mCtx.getString(R.string.auth_network_error_retrying), 39201);
            e.getStackTrace();
        } catch (ClientBannedException e) {
            Log.e(TAG, "This version of the client has been banned!!" + ": "
                    + getNetApp().getName());
            Log.e(TAG, e.getMessage());
            // TODO: what??  notify user
            notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_CLIENTBANNED);
            Util.myNotify(mCtx, SettingsActivity.class, getNetApp().getName(),
                    mCtx.getString(R.string.auth_client_banned), 39201);
            e.getStackTrace();
        }
    }

    /**
     * Connects to Last.fm servers and tries to handshake/authenticate. A
     * successful handshake is needed for all other submission requests. If an
     * error occurs, exceptions are thrown.
     *
     * @return the result of a successful handshake, {@link HandshakeResult}
     * @throws BadAuthException          means that the username/password provided by the user was
     *                                   wrong, or that the user requested his/her credentials to be
     *                                   cleared.
     * @throws TemporaryFailureException
     * @throws UnknownResponseException  {@link UnknownResponseException}
     * @throws ClientBannedException     this version of the client has been banned
     */

    public HandshakeResult handshake() throws BadAuthException,
            TemporaryFailureException, ClientBannedException {

        NetApp netApp = getNetApp();

        Log.d(TAG, "Handshaking: " + netApp.getName());

        String username = settings.getUsername(netApp);

        if (netApp == NetApp.LASTFM) {

            if (username.length() == 0) {
                Log.d(TAG, "Invalid (empty) credentials for: " + NetApp.LASTFM);
                settings.setSessionKey(NetApp.LASTFM, "");
                throw new BadAuthException(getContext().getString(
                        R.string.auth_bad_auth));
            }


            String sign = "";

            URL url;
            HttpsURLConnection conn = null;

            try {
                url = new URL("https://ws.audioscrobbler.com/2.0/");
                if (!settings.getSessionKey(getNetApp()).equals("")) {
                    return new HandshakeResult(settings.getSessionKey(getNetApp()), url.toString(), url.toString());
                }

                // Log.d(TAG,url.toString());

                //Log.d(TAG, url.toString());
                conn = (HttpsURLConnection) url.openConnection();
                // Create the SSL connection
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, null, new java.security.SecureRandom());
                conn.setSSLSocketFactory(sc.getSocketFactory());

                // set Timeout and method
                conn.setReadTimeout(7000);
                conn.setConnectTimeout(7000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Add any data you wish to post here
                Map<String, Object> params = new TreeMap<>();
                params.put("api_key", settings.rcnvK(settings.getAPIkey()));
                params.put("method", "auth.getMobileSession");
                params.put("password", settings.getPassword(NetApp.LASTFM));
                params.put("username", settings.getUsername(NetApp.LASTFM).toLowerCase());


                for (Map.Entry<String, Object> param : params.entrySet()) {
                    sign += param.getKey() + String.valueOf(param.getValue());
                }

                String signature = MD5.getHashString(sign + settings.rcnvK(settings.getSecret()));
                params.put("api_sig", signature);

                StringBuilder postData = new StringBuilder();
                byte[] postDataBytes;
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    if (param.getValue() == null) {
                        postData.append(URLEncoder.encode("", "UTF-8"));
                    } else {
                        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                    }
                }
                postDataBytes = postData.toString().getBytes("UTF-8");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

                conn.getOutputStream().write(postDataBytes);
                conn.connect();
                int resCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + resCode);
                BufferedReader r;
                if (resCode == 200) {
                    r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder rsponse = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    rsponse.append(line).append('\n');
                }
                String response = rsponse.toString();
                // some redundancy here ?
                String[] lines = response.split("\n");

                //Log.d(TAG, "Session Result: " + lines.length + " : " + lines[1].contains("status=\"ok\"") + ":" + lines[1]);
                if (response.contains("status=\"ok\"")) {
                    final Pattern pattern = Pattern.compile("<key>(.*?)</key>");
                    final Matcher matcher = pattern.matcher(response);
                    if (matcher.find()) {
                        settings.setSessionKey(NetApp.LASTFM, matcher.group(1));
                        // Log.d(TAG, matcher.group(1));
                        Log.i(TAG, "Authentication success Last.fm");
                        return new HandshakeResult(settings.getSessionKey(getNetApp()), url.toString(), url.toString());
                    } else {
                        settings.setSessionKey(NetApp.LASTFM, "");
                        throw new TemporaryFailureException("Weird response from handskake-req: " + response + ": Last.fm");
                    }
                } else {
                    if (response.contains("code=\"6\"")) {
                        Log.e(TAG, "Handshake fails: wrong username/password");
                        throw new BadAuthException(getContext().getString(
                                R.string.auth_bad_auth));
                    } else if (response.contains("code=\"26\"") || response.contains("code=\"10\"")) {
                        Log.e(TAG, "Handshake fails: client banned: " + NetApp.LASTFM);
                        settings.setSessionKey(NetApp.LASTFM, "");
                        throw new ClientBannedException(getContext().getString(
                                R.string.auth_client_banned));
                    } else if (response.contains("code=\"4\"") || response.contains("code=\"9\"")) {
                        Log.i(TAG, "Handshake fails: bad auth: " + NetApp.LASTFM);
                        settings.setSessionKey(NetApp.LASTFM, "");
                        throw new BadAuthException(getContext().getString(
                                R.string.auth_bad_auth));
                    } else {
                        String reason = lines[2].substring(7);
                        Log.e(TAG, "Handshake fails: FAILED " + reason + ": " + NetApp.LASTFM);
                        Log.e(TAG, response);
                        settings.setSessionKey(NetApp.LASTFM, "");
                        throw new TemporaryFailureException(getContext().getString(
                                R.string.auth_server_error).replace("%1", reason));
                    }
                }
            } catch (NullPointerException | IOException | NoSuchAlgorithmException | KeyManagementException e) {
                settings.setSessionKey(NetApp.LASTFM, "");
                throw new TemporaryFailureException(TAG + ": " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } else if (netApp == NetApp.LIBREFM) {

            String pwdMd5 = settings.getPwdMd5(netApp);

            if (username.length() == 0) {
                Log.d(TAG, "Invalid (empty) username for: " + getNetApp().getName() + " : " + settings.getUsername(netApp));
                throw new BadAuthException(getContext().getString(
                        R.string.auth_bad_auth));
            }

            // -----------------------------------------------------------------------
            // ------------ for debug
            // ------------------------------------------------
            // use these values if you are testing or developing a new app
            // String clientid = "tst";
            // String clientver = "1.0";
            // -----------------------------------------------------------------------
            // ------------ for this app
            // ---------------------------------------------
            // -----------------------------------------------------------------------
            // These values should only be used for SLS. If other code
            // misbehaves using these values, this app might get banned.
            // You can ask Last.fm for your own ids.
            String clientid = getContext().getString(R.string.client_id);
            String clientver = getContext().getString(R.string.client_ver);
            // ------------ end
            // ------------------------------------------------------
            // -----------------------------------------------------------------------

            String time = Long.toString(Util.currentTimeSecsUTC());

            String authToken = MD5.getHashString(pwdMd5 + time);

            String uri = netApp.getHandshakeUrl() + "&p=1.2.1&c=" + clientid
                    + "&v=" + clientver + "&u=" + enc(username) + "&t=" + time
                    + "&a=" + authToken;

            URL url;
            HttpURLConnection conn = null;
            try {
                url = new URL(uri);
                // Log.d(TAG,url.toString());

                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestProperty("Accept-Charset", "UTF-8");
                int resCode = conn.getResponseCode();
                BufferedReader r;
                if (resCode == 200) {
                    r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder rsponse = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    rsponse.append(line).append('\n');
                }
                String response = rsponse.toString();
                // some redundancy here ?
                String[] lines = response.split("\n");

                //Log.d(TAG, "Session Result: " + lines.length + " : " + lines[0].contains("OK") + ":" + response);
                //Log.d(TAG, "Session Result: " + lines.length + " : " + lines[0].contains("OK"));
                if (lines[0].startsWith("OK")) {
                    // handshake succeeded
                    Log.i(TAG, "Handshake succeeded!: " + netApp.getName());
                    //Log.e(TAG, lines[1] + lines[2] + lines[3]);
                    return new HandshakeResult(lines[1], lines[2], lines[3]);
                } else if (lines[0].startsWith("BANNED")) {
                    Log.e(TAG, "Handshake fails: client banned: " + netApp.getName());
                    throw new ClientBannedException(getContext().getString(
                            R.string.auth_client_banned));
                } else if (lines[0].startsWith("BADAUTH")) {
                    Log.i(TAG, "Handshake fails: bad auth: " + netApp.getName());
                    throw new BadAuthException(getContext().getString(
                            R.string.auth_bad_auth));
                } else if (lines[0].startsWith("BADTIME")) {
                    Log.e(TAG, "Handshake fails: bad time: " + netApp.getName());
                    throw new TemporaryFailureException(getContext().getString(
                            R.string.auth_timing_error));
                } else if (lines[0].startsWith("FAILED")) {
                    String reason = lines[0].substring(7);
                    Log.e(TAG, "Handshake fails: FAILED " + reason + ": " + netApp.getName());
                    throw new TemporaryFailureException(getContext().getString(
                            R.string.auth_server_error).replace("%1", reason));
                } else {
                    throw new TemporaryFailureException("Weird response from handskake-req: " + response + ": " + netApp.getName());
                }

            } catch (NullPointerException | IOException e) {
                throw new TemporaryFailureException(TAG + ": " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        return null;
    }

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "URLEncoder lacks support for UTF-8!?");
            return null;
        }
    }

    private void notifyAuthStatusUpdate(int st) {
        settings.setAuthStatus(getNetApp(), st);
        Intent i = new Intent(ScrobblingService.BROADCAST_ONAUTHCHANGED);
        i.putExtra("netapp", getNetApp().getIntentExtraValue());
        getContext().sendBroadcast(i);
    }

    /**
     * Small struct holding the results of a successful handshake. All the
     * fields are final and public, as they will never change as long as the
     * handshake is valid.
     *
     * @author tgwizard
     */
    public static class HandshakeResult {
        /**
         * The id needed for all submission requests.
         */
        public final String sessionId;

        /**
         * The URI to send now-playing-notification requests to.
         */
        public final String nowPlayingUri;

        /**
         * The URI to send scrobble requests to.
         */
        public final String scrobbleUri;

        /**
         * Constructs a new handshake info struct. Only {@link Handshaker} can
         * get the information needed to instantiate this class, and therefore
         * the constructor is private.
         *
         * @param sessionId     {@link HandshakeResult#sessionId sessionId}
         * @param nowPlayingUri {@link HandshakeResult#nowPlayingUri nowPlayingUri}
         * @param scrobbleUri   {@link HandshakeResult#scrobbleUri scrobbleUri}
         */
        private HandshakeResult(String sessionId, String nowPlayingUri,
                                String scrobbleUri) {
            super();
            this.sessionId = sessionId;
            this.nowPlayingUri = nowPlayingUri;
            this.scrobbleUri = scrobbleUri;
        }

    }
}
