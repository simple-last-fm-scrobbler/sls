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


package com.adam.aslfms.receiver;

/**
 * A BroadcastReceiver for intents sent by the Winamp Music Player.
 *
 * @see BuiltInMusicAppReceiver
 *
 * @author tgwizard
 * @since 1.3.2
 */
public class WinampMusicReceiver extends BuiltInMusicAppReceiver {
    @SuppressWarnings("unused")
    private static final String TAG = "SLSWinampReceiver";

    public static final String ACTION_WINAMP_METACHANED = "com.nullsoft.winamp.metachanged";
    public static final String ACTION_WINAMP_PAUSERESUME = "com.nullsoft.winamp.playstatechanged";
    // doesn't seem to work
    public static final String ACTION_WINAMP_STOP = "com.nullsoft.winamp.playbackcomplete";

    public WinampMusicReceiver() {
        super(ACTION_WINAMP_STOP, "com.nullsoft.winamp", "Winamp");
    }
}
