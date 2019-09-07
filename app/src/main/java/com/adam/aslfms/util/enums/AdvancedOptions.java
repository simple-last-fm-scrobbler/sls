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
import android.util.Log;

import com.adam.aslfms.R;

import java.util.HashMap;
import java.util.Map;

public enum AdvancedOptions {
    // the values below for SAME will be ignored
    SAME_AS_BATTERY(
            "ao_same_as_battery", true, true, true, AdvancedOptionsWhen.AFTER_1, true, NetworkOptions.ANY, true,
            R.string.advanced_options_type_same_as_battery_name),
    STANDARD(
            "ao_standard", true,  true, true, AdvancedOptionsWhen.AFTER_1, true, NetworkOptions.ANY, true,
            R.string.advanced_options_type_standard_name),
    // not available for plugged in
    BATTERY_SAVING(
            "ao_battery", true, true, false, AdvancedOptionsWhen.AFTER_10, true, NetworkOptions.ANY, false,
            R.string.advanced_options_type_battery_name),
    // the values below for CUSTOM will be ignored
    CUSTOM(
            "ao_custom", true, true, true, AdvancedOptionsWhen.AFTER_1, true, NetworkOptions.ANY, true,
            R.string.advanced_options_type_custom_name);

    private final String settingsVal;
    private final boolean enableActiveApp;
    private final boolean enableScrobbling;
    private final boolean enableNp;
    private final AdvancedOptionsWhen when;
    private final boolean alsoOnComplete;
    private final NetworkOptions networkOptions;
    private final boolean roaming;
    private final int nameRID;

    AdvancedOptions(String settingsVal, boolean enableActiveApp, boolean enableScrobbling, boolean enableNp, AdvancedOptionsWhen when,
                    boolean alsoOnComplete, NetworkOptions networkOptions, boolean roaming, int nameRID) {
        this.settingsVal = settingsVal;
        this.enableActiveApp = enableActiveApp;
        this.enableScrobbling = enableScrobbling;
        this.enableNp = enableNp;
        this.when = when;
        this.alsoOnComplete = alsoOnComplete;
        this.networkOptions = networkOptions;
        this.roaming = roaming;
        this.nameRID = nameRID;
    }

    // these methods are intentionally package-private, they are only used
    // by AppSettings

    public String getSettingsVal() {
        return settingsVal;
    }

    public boolean isActiveAppEnabled() {
        return enableActiveApp;
    }

    public boolean isScrobblingEnabled() {
        return enableScrobbling;
    }

    public boolean isNpEnabled() {
        return enableNp;
    }

    public AdvancedOptionsWhen getWhen() {
        return when;
    }

    public boolean getAlsoOnComplete() {
        return alsoOnComplete;
    }

    public NetworkOptions getNetworkOptions() {
        return networkOptions;
    }

    public boolean getRoaming() {
        return roaming;
    }

    public String getName(Context ctx) {
        return ctx.getString(nameRID);
    }

    private static final String TAG = "SLSAdvancedOptions";
    private static Map<String, AdvancedOptions> mSAOMap;

    static {
        AdvancedOptions[] aos = AdvancedOptions.values();
        mSAOMap = new HashMap<String, AdvancedOptions>(aos.length);
        for (AdvancedOptions ao : aos)
            mSAOMap.put(ao.getSettingsVal(), ao);
    }

    public static AdvancedOptions fromSettingsVal(String s) {
        AdvancedOptions ao = mSAOMap.get(s);
        if (ao == null) {
            Log.e(TAG, "got null advanced option from settings, defaulting to standard");
            ao = AdvancedOptions.STANDARD;
        }
        return ao;
    }
}