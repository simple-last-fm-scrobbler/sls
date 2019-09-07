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


package com.adam.aslfms.util;

import com.adam.aslfms.receiver.AbstractPlayStatusReceiver;
import com.adam.aslfms.service.ScrobblingService;

import java.util.LinkedList;

/**
 * Internal class that transmits tracks from the scrobbling API listeners to the
 * {@link ScrobblingService}.
 *
 * @see AbstractPlayStatusReceiver
 *
 * @author tgwizard
 * @since 0.9
 */
public class InternalTrackTransmitter {
    private static LinkedList<Track> tracks = new LinkedList<Track>();

    /**
     * Appends {@code track} to the queue of tracks that
     * {@link ScrobblingService} will pickup.
     * <p>
     * The method is thread-safe.
     *
     * @see #popTrack()
     *
     * @param track
     *            the track to be appended
     */
    public static synchronized void appendTrack(Track track) {
        tracks.addLast(track);
    }

    /**
     * Pops a {@code Track} from the queue of tracks in FIFO order.
     * <p>
     * The method is thread-safe.
     *
     * @see #appendTrack(Track)
     *
     * @return the track at the front of the list
     */
    public synchronized static Track popTrack() {
        if (tracks.isEmpty())
            return null;
        return tracks.removeFirst();
    }
}
