/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/tgwizard/sls
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.Util.NetworkStatus;

public class NetworkWaiter extends NetRunnable {

    private static final String TAG = "NetworkWaiter";
    private boolean mWait;

    NetworkWaiter(NetApp napp, Context ctx, Networker net) {
        super(napp, ctx, net);
    }

    @Override
    public void run() {
        // register receiver
        IntentFilter ifs = new IntentFilter();
        ifs.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        ifs.addAction(AppSettings.ACTION_NETWORK_OPTIONS_CHANGED);
        getContext().registerReceiver(mConnReceiver, ifs);

        synchronized (this) {
            mWait = Util.checkForOkNetwork(getContext()) != NetworkStatus.OK;
            while (mWait) {
                try {
                    Log.d(TAG, "waiting for network connection: "
                            + getNetApp().getName());
                    this.wait();
                    Log.d(TAG,
                            "woke up, there's probably a network connection: "
                                    + getNetApp().getName());
                } catch (InterruptedException e) {
                    Log.i(TAG, "Got interrupted: " + getNetApp().getName());
                    Log.i(TAG, e.getMessage());
                }
            }
        }

        // unregister receiver
        getContext().unregisterReceiver(mConnReceiver);
    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (NetworkWaiter.this) {
                Log.d(TAG, "received broadcast: " + intent.getAction());
                if (Util.checkForOkNetwork(getContext()) == NetworkStatus.OK) {
                    NetworkWaiter.this.mWait = false;
                    NetworkWaiter.this.notifyAll();
                }
            }
        }
    };

}
