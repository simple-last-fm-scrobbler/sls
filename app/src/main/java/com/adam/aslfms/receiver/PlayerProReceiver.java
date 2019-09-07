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

/**
 * A BroadcastReceiver for intents sent by the Player Pro.
 *
 * @author metanota <metanota@gmail.com>
 * @see BuiltInMusicAppReceiver
 * @since 1.4.7
 */
public class PlayerProReceiver extends BuiltInMusicAppReceiver {

    public static final String ACTION_PLAYER_PRO_STOP = "com.tbig.playerpro.playbackcomplete";
    public static final String ACTION_PLAYER_PRO_PLAYSTATECHANGED = "com.tbig.playerpro.playstatechanged";
    public static final String ACTION_PLAYER_PRO_METACHANGED = "com.tbig.playerpro.metachanged";

    public PlayerProReceiver() {
        super(ACTION_PLAYER_PRO_STOP, "com.tbig.playerpro", "Player Pro");
    }
}
