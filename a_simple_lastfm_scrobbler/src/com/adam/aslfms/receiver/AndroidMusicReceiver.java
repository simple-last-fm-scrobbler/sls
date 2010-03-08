/**
 *  This file is part of Simple Last.fm Scrobbler.
 *
 *  Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms.receiver;

/**
 * A BroadcastReceiver for intents sent by the Android Music Player.
 * 
 * @see AbstractPlayStatusReceiver
 * 
 * @author tgwizard
 * @since 1.0.1
 */
public class AndroidMusicReceiver extends BuiltInMusicAppReceiver {

	public static final String ACTION_ANDROID_PLAYSTATECHANGED = "com.android.music.playstatechanged";
	public static final String ACTION_ANDROID_STOP = "com.android.music.playbackcomplete";
	public static final String ACTION_ANDROID_METACHANGED = "com.android.music.metachanged";

	public AndroidMusicReceiver() {
		super(ACTION_ANDROID_STOP, "com.android.music", "Android Music Player");
	}

}
