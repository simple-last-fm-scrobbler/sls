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

import java.util.LinkedList;

import com.adam.aslfms.service.ScrobblingService;

/**
 * Internal class that transmits tracks from {@link PlayStatusReceiver} to
 * {@link ScrobblingService}.
 * 
 * @author tgwizard
 * 
 */
public class InternalTrackTransmitter {
	private static LinkedList<Track> tracks = new LinkedList<Track>();

	/**
	 * Appends <code>track</code> to the queue of tracks that
	 * {@link ScrobblingService} will pickup.
	 * <p>
	 * The method is thread-safe.
	 * 
	 * @param track
	 */
	public static synchronized void appendTrack(Track track) {
		tracks.addLast(track);
	}

	/**
	 * Pops a Track from the queue of tracks in FIFO order.
	 * <p>
	 * The method is thread-safe.
	 * 
	 * @return
	 */
	public synchronized static Track popTrack() {
		if (tracks.isEmpty())
			return null;
		return tracks.removeFirst();
	}
}
