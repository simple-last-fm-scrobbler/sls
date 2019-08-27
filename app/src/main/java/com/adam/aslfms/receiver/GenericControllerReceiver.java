package com.adam.aslfms.receiver;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.adam.aslfms.R;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

import java.math.BigDecimal;

public class GenericControllerReceiver extends AbstractPlayStatusReceiver {
    public static final String ACTION_INTENT = "com.adam.aslfms.receiver.controller";
    static final String APP_NAME = "GenericController";
    static final String TAG = "GenControllerReceiver";

    private MusicAPI mMusicApi = null;

    public void onReceive(Context ctx, String action, Bundle bundle) {
        parseData(ctx, action, bundle);
    }

    @Override
    protected void parseIntent(Context ctx, String action, Bundle bundle) throws IllegalArgumentException {
        parseData(ctx, action, bundle);
    }

    private void parseData(Context ctx, String action, Bundle bundle){
        String playerPackage = null;
        String playerName = null;
        try {
            if (action == ACTION_INTENT) {
                try {
                    if (bundle.containsKey("player")) {
                        playerPackage = bundle.getString("player");
                        if (playerPackage != null && !playerPackage.isEmpty()) {
                            PackageManager packageManager = ctx.getPackageManager();
                            playerName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(playerPackage, PackageManager.GET_META_DATA)).toString();
                            mMusicApi = MusicAPI.fromReceiver(ctx, playerName, playerPackage, null, false);
                            setMusicAPI(mMusicApi);
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, e.toString());
                    mMusicApi = MusicAPI.fromReceiver(ctx, ctx.getResources().getString(R.string.notification_controller), ctx.getPackageName(), null, false);
                    setMusicAPI(mMusicApi);
                }
                if (bundle.containsKey("track")) {
                    Track.Builder b = new Track.Builder();
                    b.setMusicAPI(mMusicApi);
                    b.setWhen(Util.currentTimeSecsUTC());

                    b.setArtist(bundle.getString("artist"));
                    b.setAlbum(bundle.getString("album"));
                    b.setTrack(bundle.getString("track"));
                    b.setAlbumArtist(bundle.getString("albumartist"));
                    // duration should be an Integer in seconds.
                    if (bundle.containsKey("duration")) {
                        Object tmp = bundle.get("duration");
                        if (tmp != null) {
                            if (tmp instanceof Double) {
                                try {
                                    double du = bundle.getDouble("duration");
                                    b.setDuration(new BigDecimal(Math.round(bundle.getDouble("duration") / 1000)).intValueExact());
                                    Log.d(TAG, "Double: " + du);
                                } catch (Exception e) {
                                    Log.e(TAG, "dbl duration: " + e);
                                }
                            } else if (tmp instanceof Long) {
                                try {
                                    long du = bundle.getLong("duration");
                                    b.setDuration(new BigDecimal(Math.round(bundle.getLong("duration") / 1000)).intValueExact());
                                    Log.d(TAG, "Long: " + du);
                                } catch (Exception e) {
                                    Log.e(TAG, "long duration: " + e);
                                }
                            } else if (tmp instanceof Integer) {
                                try {
                                    int du = bundle.getInt("duration");
                                    b.setDuration(new BigDecimal(bundle.getInt("duration") / 1000).intValueExact());
                                    Log.d(TAG, "Integer: " + du);
                                } catch (Exception e) {
                                    Log.e(TAG, "int duration: " + e);
                                }
                            }
                        }
                    }
                    Log.d(TAG,
                            bundle.getString("artist") + " - "
                                    + bundle.getString("track") + " ("
                                    + bundle.getInt("length", 0) + ")");
                    setTrack(b.build());
                }
                boolean playing = bundle.getBoolean("playing");
                if (playing) {
                    setState(Track.State.RESUME);
                    Log.d(TAG, "Setting state to RESUME");
                } else {
                    setState(Track.State.PAUSE);
                    Log.d(TAG, "Setting state to PAUSE");
                }
            }
        } catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }
}
