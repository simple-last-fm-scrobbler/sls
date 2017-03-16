package com.adam.aslfms.service.applemusic;

import android.content.Context;
import android.content.Intent;

/**
 * Created by 4-Eyes on 16/3/2017.
 *
 */

class AppleMusicBroadcaster {

    private final Context context;
    private final String appName = "Apple Music";
    private final String packageName = "com.apple.android.music";

    AppleMusicBroadcaster(Context context) {
        this.context = context;
    }

    void broadcast(TrackData data, BroadcastState state, long duration) {
        Intent broadcastIntent = new Intent("com.adam.aslfms.notify.playstatechanged");
        broadcastIntent.putExtra("state", state.getValue());
        broadcastIntent.putExtra("app-name", appName);
        broadcastIntent.putExtra("app-package", packageName);
        broadcastIntent.putExtra("track", data.getTitle());
        broadcastIntent.putExtra("artist", data.getArtist());
        broadcastIntent.putExtra("album", data.getAlbum());
        broadcastIntent.putExtra("duration", (int)(duration / 1000));

        context.sendBroadcast(broadcastIntent);
    }
}
