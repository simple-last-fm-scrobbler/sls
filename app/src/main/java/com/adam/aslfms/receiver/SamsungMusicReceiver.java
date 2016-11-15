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
 * A BroadcastReceiver for intents sent by the Samsung default Music Player.
 *
 * @see AbstractPlayStatusReceiver
 *
 * @author tgwizard
 * @since 1.3.1
 */
public class SamsungMusicReceiver extends BuiltInMusicAppReceiver {

    public static final String ACTION_SAMSUNG_PLAYSTATECHANGED = "com.samsung.sec.android.MusicPlayer.playstatechanged";
    public static final String ACTION_SAMSUNG_STOP = "com.samsung.sec.android.MusicPlayer.playbackcomplete";
    public static final String ACTION_SAMSUNG_METACHANGED = "com.samsung.sec.android.MusicPlayer.metachanged";

    public SamsungMusicReceiver() {
        super(ACTION_SAMSUNG_STOP, "com.samsung.sec.android.MusicPlayer",
                "Samsung Music Player");
    }
}
