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
    public static final String ACTION_INTENT_PLAYSTATE = "com.adam.aslfms.receiver.controller.PLAYSTATE_CHANGE";
    public static final String ACTION_INTENT_METADATA = "com.adam.aslfms.receiver.controller.METADATA_CHANGE";
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
            if (action != null) {
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
                }
                if (mMusicApi == null) {
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
                int playing = bundle.getInt("playing");
                switch (playing) {
                    case 1:
                        setState(Track.State.START);
                    case 2:
                        setState(Track.State.RESUME);
                        break;
                    case 3:
                        setState(Track.State.PAUSE);
                        break;
                    case 4:
                        setState(Track.State.COMPLETE);
                        break;
                    case 5:
                    default:
                        setState(Track.State.UNKNOWN_NONPLAYING);
                        break;
                }
            }
        } catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }
}
