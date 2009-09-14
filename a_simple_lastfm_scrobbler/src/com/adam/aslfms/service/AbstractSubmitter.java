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

package com.adam.aslfms.service;

import android.content.Context;

import com.adam.aslfms.service.Handshaker.HandshakeResult;

public abstract class AbstractSubmitter extends NetRunnable {

	@SuppressWarnings("unused")
	private static final String TAG = "ASubmitter";

	public AbstractSubmitter(Context ctx, Networker net) {
		super(ctx, net);
	}

	@Override
	public final void run() {
		HandshakeResult hInfo = getNetworker().getHandshakeResult();
		if (hInfo == null) {
			getNetworker().launchHandshaker(false);
			relaunchThis();
			return;
		} else {
			int rCount = 0;
			boolean retry = false;
			do {
				retry = !doRun(hInfo);
				rCount++;
			} while (retry && rCount < 3);

			if (rCount >= 3) {
				getNetworker().launchHandshaker(false);
				relaunchThis();
			}
		}
	}

	/**
	 * 
	 * @param hInfo struct with urls and stuff
	 * @return true if successful, false otherwise
	 */
	protected abstract boolean doRun(HandshakeResult hInfo);

	protected abstract void relaunchThis();
}
