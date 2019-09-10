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
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.NotificationCreator;
import com.adam.aslfms.util.Util;

import java.util.HashSet;

/**
 * @author a93h
 * @since 1.5.8
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class ControllerReceiverService extends NotificationListenerService {
    
    private static final String TAG = "ControllerReceiverSrvc";
    private ControllerReceiverSession mControllerReceiverSession;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Util.checkNotificationListenerPermission(this)){
            return;
        }
        Log.d(TAG,"created");
        AppSettings settings = new AppSettings(this);

        Bundle extras = new Bundle();
        extras.putString("track", "");
        extras.putString("artist", "");
        extras.putString("album", "");
        extras.putString("app_name", "");
        this.startForeground(NotificationCreator.FOREGROUND_ID, NotificationCreator.prepareNotification(extras, this));
        init();
        if (!settings.isActiveAppEnabled(Util.checkPower(this))) {
            this.stopForeground(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Util.checkNotificationListenerPermission(this)){
            return Service.START_NOT_STICKY;
        }
        Log.d(TAG,"started");
        AppSettings settings = new AppSettings(this);
        Bundle extras = new Bundle();
        extras.putString("track", "");
        extras.putString("artist", "");
        extras.putString("album", "");
        extras.putString("app_name", "");
        this.startForeground(NotificationCreator.FOREGROUND_ID, NotificationCreator.prepareNotification(extras, this));
        init();
        if (!settings.isActiveAppEnabled(Util.checkPower(this))) {
            this.stopForeground(true);
            return Service.START_NOT_STICKY;
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"destroyed");
        if (mControllerReceiverSession != null) {
            mControllerReceiverSession.removeSessions(new HashSet<MediaSession.Token>(), new HashSet<String>());
            MediaSessionManager mediaSessionManager = ((MediaSessionManager)getSystemService(Context.MEDIA_SESSION_SERVICE));
            if (mediaSessionManager != null)
                mediaSessionManager.removeOnActiveSessionsChangedListener(mControllerReceiverSession);
            mControllerReceiverSession = null;
        }
    }

    // BEGIN listener stuff

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        init();
    }

    @Override
    @TargetApi(24)
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        requestRebind(new ComponentName(getApplicationContext(), ControllerReceiverService.class));
    }

    public void init(){
        MediaSessionManager mediaSessionManager = null;
        try {
            Log.d(TAG,"Detecting initial media session");
            mediaSessionManager = (MediaSessionManager) this.getApplicationContext().getSystemService(Context.MEDIA_SESSION_SERVICE) ;
            ComponentName listenerComponent =
                    new ComponentName(this, ControllerReceiverService.class);
            ControllerReceiverSession.initialListener(this, mediaSessionManager.getActiveSessions(listenerComponent));
            mControllerReceiverSession = new ControllerReceiverSession(this);
            mediaSessionManager.addOnActiveSessionsChangedListener(mControllerReceiverSession, new ComponentName(this, ControllerReceiverService.class));
            Log.d(TAG, "media session manager loaded");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start media controller: " + e.toString());
            // Try to unregister it, just in case.
            try {
                mediaSessionManager.removeOnActiveSessionsChangedListener(mControllerReceiverSession);
            } catch (Exception er) {
                er.printStackTrace();
            }
        }
    }
}