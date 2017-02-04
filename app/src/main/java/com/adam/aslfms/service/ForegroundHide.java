/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p/>
 * https://github.com/tgwizard/sls
 * <p/>
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adam.aslfms.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.adam.aslfms.R;
import com.adam.aslfms.SettingsActivity;

public class ForegroundHide extends Service {

    Context mCtx = this;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        //Bundle extras = i.getExtras();


        Intent targetIntent = new Intent(mCtx, SettingsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mCtx)
                        .setContentTitle("")
                        .setSmallIcon(R.mipmap.ic_notify)
                        .setContentText("")
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setContentIntent(contentIntent);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
            builder.setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(),
                    R.mipmap.ic_launcher));
        }

        this.startForeground(14619, builder.build());

        this.stopForeground(true);
        return Service.START_NOT_STICKY;
    }
}
