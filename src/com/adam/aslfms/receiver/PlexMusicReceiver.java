/**
* This file is part of Simple Last.fm Scrobbler.
*
* Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Simple Last.fm Scrobbler. If not, see <http://www.gnu.org/licenses/>.
*
* See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
*/

package com.adam.aslfms.receiver;

/**
* A BroadcastReceiver for intents sent by the Plex Music Player.
*
* @see BuiltInMusicAppReceiver
*
* @author tgwizard
* @since 1.3.2
*/
public class PlexMusicReceiver extends BuiltInMusicAppReceiver {
@SuppressWarnings("unused")
private static final String TAG = "SLSPlexReceiver";

public static final String ACTION_WINAMP_START = "com.plexapp.plex.audio.action.PLAY";
public static final String ACTION_WINAMP_PAUSERESUME = "com.plexapp.plex.audio.action.PAUSE";
// doesn't seem to work
public static final String ACTION_WINAMP_STOP = "com.plexapp.plex.audio.action.STOP";

public PlexMusicReceiver() {
super(ACTION_WINAMP_STOP, "com.plexapp.plex", "Plex");
}

}
