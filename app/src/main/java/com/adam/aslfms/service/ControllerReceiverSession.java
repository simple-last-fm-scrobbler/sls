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


package com.adam.aslfms.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * @author a93h
 * @since 1.5.8
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class ControllerReceiverSession implements MediaSessionManager.OnActiveSessionsChangedListener {
    private final static String TAG = "ContrlRcvrSession";

    private HashMap<MediaSession.Token, Pair<MediaController, MediaController.Callback>> controllersMap = new HashMap<>();
    private List<MediaController> mControllers = null;
    private int sessionCount = 0;
    private Context mContext;

    public ControllerReceiverSession(Context context) {
        super();
        this.mContext = context;
    }

    public void onActiveSessionsChanged(List<MediaController> controllers) {
        Log.d(TAG, "active session change detected");
        mControllers = controllers;
        HashSet<MediaSession.Token> tokens = new HashSet<>();
        HashSet<String> pacakgeNames = new HashSet<>();
        if (controllers != null) {
            for (MediaController controller : controllers) {
                tokens.add(controller.getSessionToken());
                pacakgeNames.add(controller.getPackageName());
                if (!controllersMap.containsKey(controller.getSessionToken())) {
                    Log.d(TAG, "onActiveSessionsChanged [" + controllers.size() + "] : " + controller.getPackageName());
                    MediaController.Callback mccb = new ControllerReceiverCallback(mContext, controller.getPackageName(), controller);
                    controller.registerCallback(mccb);
                    Pair<MediaController,MediaController.Callback> pair = Pair.create(controller, mccb);
                    synchronized (controllersMap) {
                        controllersMap.put(controller.getSessionToken(), pair);
                    }
                }
            }
        }
        // Now remove old sessions that are not longer active.
        removeSessions(tokens,pacakgeNames);
    }

    public void removeSessions(HashSet<MediaSession.Token> tokens, HashSet<String> packageNames) {
        Iterator it = controllersMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry group = (Map.Entry)it.next();
            MediaSession.Token token = (MediaSession.Token) group.getKey();
            Pair pair = (Pair) group.getValue();
            MediaController mediaController = (MediaController) pair.first;
            MediaController.Callback mediaControllerCallback = (MediaController.Callback) pair.second;
            if ((tokens != null && !tokens.contains(token))) { // bad code ?? || (packageNames != null && packageNames.contains(mediaController.getPackageName()))
                mediaController.unregisterCallback(mediaControllerCallback);
                synchronized(controllersMap) {
                    it.remove();
                    Log.d(TAG,"controller " + mediaController.getPackageName() + " removed.");
                }
            }
        }
        sessionCount = controllersMap.size();
        Log.d(TAG,"number of active sessions: " + sessionCount);
    }
}
