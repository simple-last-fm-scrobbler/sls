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

package com.adam.aslfms.receiver;

import android.content.Context;
import android.os.Bundle;

public class MIUIMusicReceiver extends BuiltInMusicAppReceiver {

    static final String APP_PACKAGE = "com.miui.player";
    static final String ACTION_MIUI_STOP = "com.miui.player.playbackcomplete";
    static final String ACTION_MIUI_METACHANGED = "com.miui.player.metachanged";

    static final String ACTION_MIUI_SERVICE_METACHANGED = "com.miui.player.service.metachanged";
    static final String ACTION_MIUI_SERVICE_PLAYSTATECHANGED = "com.miui.player.service.playstatechanged";


    public MIUIMusicReceiver() {
        super(ACTION_MIUI_STOP, APP_PACKAGE, "MIUI Music Player");
    }

    @Override
    protected void parseIntent(Context ctx, String action, Bundle bundle) throws IllegalArgumentException {
        super.parseIntent(ctx, action, bundle);
    }

}
