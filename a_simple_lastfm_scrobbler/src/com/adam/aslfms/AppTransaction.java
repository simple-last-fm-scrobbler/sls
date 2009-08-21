/**
 *  This file is part of A Simple Last.fm Scrobbler.
 *
 *  A Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  A Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with A Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.TimeZone;

/**
 * FIXME: rename this class
 * 
 * @author tgwizard
 * 
 */
public class AppTransaction {
	private static final Object syncObject = new Object();
	private static LinkedList<Track> tracks = new LinkedList<Track>();

	public static void pushTrack(Track t) {
		synchronized (syncObject) {
			tracks.addLast(t);
		}
	}

	public static Track popTrack() {
		synchronized (syncObject) {
			if (tracks.isEmpty())
				return null;
			return tracks.removeFirst();
		}
	}

	/**
	 * FIXME: where does this method belong?
	 * @return the current time since 1970, UTC, in seconds
	 */
	public static long currentTimeUTC() {
		return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
				.getTimeInMillis() / 1000;
	}
}
