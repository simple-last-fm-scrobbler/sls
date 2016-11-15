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
            boolean retry;
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
