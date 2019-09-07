/**
 * This file is part of Simple Scrobbler.
 * <p>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p>
 * Copyright 2011 Simple Scrobbler Team
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
package com.adam.aslfms.util.enums;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.adam.aslfms.R;

import java.util.HashMap;
import java.util.Map;

public enum NetworkOptions {
    ANY(
            "any", new int[]{}, new int[]{}, R.string.advanced_options_net_any_name),
    THREEG_AND_UP(
            "3g_and_up", new int[]{}, new int[]{TelephonyManager.NETWORK_TYPE_UNKNOWN,
            TelephonyManager.NETWORK_TYPE_GPRS}, R.string.advanced_options_net_3gup_name),
    WIFI_ONLY(
            "wifi", new int[]{ConnectivityManager.TYPE_MOBILE}, new int[]{},
            R.string.advanced_options_net_wifi_name);

    private final String settingsVal;
    private final int[] forbiddenNetworkTypes;
    private final int[] forbiddenMobileNetworkSubTypes;
    private final int nameRID;

    NetworkOptions(String settingsVal, int[] forbiddenNetworkTypes, int[] forbiddenMobileNetworkSubTypes,
                   int nameRID) {
        this.settingsVal = settingsVal;
        this.forbiddenNetworkTypes = forbiddenNetworkTypes;
        this.forbiddenMobileNetworkSubTypes = forbiddenMobileNetworkSubTypes;
        this.nameRID = nameRID;
    }

    public String getSettingsVal() {
        return settingsVal;
    }

    public int[] getForbiddenNetworkTypes() {
        return forbiddenNetworkTypes;
    }

    public boolean isNetworkTypeForbidden(int netType) {
        for (int nt : forbiddenNetworkTypes) {
            if (nt == netType) {
                return true;
            }
        }

        return false;
    }

    public int[] getForbiddenMobileNetworkSubTypes() {
        return forbiddenMobileNetworkSubTypes;
    }

    public boolean isNetworkSubTypeForbidden(int netType, int netSubType) {
        if (netType != ConnectivityManager.TYPE_MOBILE) {
            return false;
        }
        for (int nt : forbiddenMobileNetworkSubTypes) {
            if (nt == netSubType) {
                return true;
            }
        }
        return false;
    }

    public int getNameRID() {
        return nameRID;
    }

    public String getName(Context ctx) {
        return ctx.getString(nameRID);
    }

    private static final String TAG = "SLSNetworkOptions";
    private static Map<String, NetworkOptions> mNOMap;

    static {
        NetworkOptions[] nos = NetworkOptions.values();
        mNOMap = new HashMap<String, NetworkOptions>(nos.length);
        for (NetworkOptions no : nos)
            mNOMap.put(no.getSettingsVal(), no);
    }

    public static NetworkOptions fromSettingsVal(String s) {
        NetworkOptions no = mNOMap.get(s);
        if (no == null) {
            Log.e(TAG, "got null network option from settings, defaulting to standard");
            no = NetworkOptions.ANY;
        }
        return no;
    }
}