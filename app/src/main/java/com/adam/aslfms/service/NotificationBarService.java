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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.NotificationCreator;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Util;

/**
 * @author a93h
 * @since 1.5.8
 */
public class NotificationBarService extends Service {

    private static final String TAG = "NotificationBarService";

    public static final String ACTION_NOTIFICATION_BAR_UPDATE = "com.adam.aslfms.service.notificationbarupdate";

    private AppSettings settings;
    private ScrobblesDatabase mDb;
    private Bundle extras;
    private String action;

    private NetworkerManager mNetManager;

    Context mCtx = this;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        settings = new AppSettings(this);
        mDb = new ScrobblesDatabase(this);
        mDb.open();
        mNetManager = new NetworkerManager(this, mDb);

        this.startForeground(NotificationCreator.FOREGROUND_ID, NotificationCreator.prepareNotification(extras, mCtx));
        if (!settings.isActiveAppEnabled(Util.checkPower(mCtx))) {
            this.stopForeground(true);
        }
    }

    @Override
    public void onDestroy() {
        mDb.close();
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        handleCommand(i, startId);
        this.startForeground(NotificationCreator.FOREGROUND_ID, NotificationCreator.prepareNotification(extras, mCtx));
        if (!settings.isActiveAppEnabled(Util.checkPower(mCtx))) {
            this.stopForeground(true);
            return Service.START_NOT_STICKY;
        }
        return Service.START_STICKY;
    }

    private void handleCommand(Intent i, int startId) {
        if (i == null) {
            Log.e(TAG, "got null intent");
            return;
        }
        action = i.getAction();
        extras = i.getExtras();
    }
}
