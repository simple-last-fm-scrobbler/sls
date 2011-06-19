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

package com.adam.aslfms.service;

import android.content.Context;
import android.util.Log;

import com.adam.aslfms.R;
import com.adam.aslfms.service.Handshaker.HandshakeResult;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.Util.NetworkStatus;
import com.adam.aslfms.util.enums.SubmissionType;

public abstract class AbstractSubmitter extends NetRunnable {

	private static final String TAG = "ASubmitter";

	protected final AppSettings settings;

	public AbstractSubmitter(NetApp napp, Context ctx, Networker net) {
		super(napp, ctx, net);
		this.settings = new AppSettings(ctx);
	}

	@Override
	public final void run() {

		// check network status
		NetworkStatus ns = Util.checkForOkNetwork(getContext());
		if (ns != NetworkStatus.OK) {
			Log.d(TAG, "Waits on network, network-status: " + ns);
			getNetworker().launchNetworkWaiter();
			relaunchThis();
			return;
		}

		HandshakeResult hInfo = getNetworker().getHandshakeResult();
		if (hInfo == null) {
			getNetworker().launchHandshaker();
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
				getNetworker().launchHandshaker();
				relaunchThis();
			}
		}
	}

	protected void notifySubmissionStatusSuccessful(SubmissionType stype,
			Track track, int statsInc) {
		settings.setLastSubmissionSuccess(getNetApp(), stype, true);
		settings.setLastSubmissionTime(getNetApp(), stype, Util
				.currentTimeMillisLocal());
		settings.setNumberOfSubmissions(getNetApp(), stype, settings
				.getNumberOfSubmissions(getNetApp(), stype)
				+ statsInc);
		settings
				.setLastSubmissionInfo(getNetApp(), stype, "\""
						+ track.getTrack() + "\" "
						+ getContext().getString(R.string.by) + " "
						+ track.getArtist());
		notifyStatusUpdate();
	}

	protected void notifySubmissionStatusFailure(SubmissionType stype,
			String reason) {
		settings.setLastSubmissionSuccess(getNetApp(), stype, false);
		settings.setLastSubmissionTime(getNetApp(), stype, Util
				.currentTimeMillisLocal());
		settings.setLastSubmissionInfo(getNetApp(), stype, reason);
		notifyStatusUpdate();
	}

	/**
	 * 
	 * @param hInfo
	 *            struct with urls and stuff
	 * @return true if successful, false otherwise
	 */
	protected abstract boolean doRun(HandshakeResult hInfo);

	protected abstract void relaunchThis();
}
