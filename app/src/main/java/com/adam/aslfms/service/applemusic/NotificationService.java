package com.adam.aslfms.service.applemusic;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.adam.aslfms.util.AppSettings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 4-Eyes on 15/3/2017.
 *
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService {

    private NotificationHandler handler;
    private PackageInfo applePackageInfo;
    private static final String APPLE_PACKAGE_NAME = "com.apple.android.music";

    static long DEFAULT_SONG_LENGTH = 4 * 60000; // Four minutes

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppSettings settings = new AppSettings(this);
        handler = new NotificationHandler(this, settings);
        Log.i("AppleNotification", "Notification listener created");
        try {
            applePackageInfo = getPackageManager().getPackageInfo(APPLE_PACKAGE_NAME, 0);
            Log.i("AppleNotification", "Package version is: " + applePackageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i("AppleNotification", "Apple Music package not installed");
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        super.onNotificationPosted(notification);

        // Filter out all notifications that do not come from Apple Music
        if (!notification.getPackageName().equals(APPLE_PACKAGE_NAME)) return;

        // Attempt to retrieve the
        RemoteViews views = notification.getNotification().bigContentView;
        if (views == null) return;

        Log.i("AppleNotification", "New notification being processed");

        TrackData data = new TrackData();
        int dataCount = 0;
        try {
            Field field = views.getClass().getDeclaredField("mActions");
            field.setAccessible(true);

            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);

            for (Parcelable p : actions) {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);

                int tag = parcel.readInt();
                if (tag != 2 && tag != 12) continue;
                parcel.readInt();
                String methodName = parcel.readString();
                if (methodName == null) continue;

                if (tag == 2) {
                    // This is for ReflectionAction objects
                    switch (methodName) {
                        case "setText": {
                            parcel.readInt();

                            String text = TextUtils.CHAR_SEQUENCE_CREATOR
                                    .createFromParcel(parcel).toString().trim();
                            switch (dataCount) {
                                case 0:
                                    data.setTitle(text);
                                    break;
                                case 1:
                                    if (applePackageInfo.versionCode < 431) {
                                        data.setAlbum(text);
                                    } else {
                                        String[] dataList = text.split(" — ");
                                        data.setArtist(dataList[0]);
                                        data.setAlbum(dataList[1]);
                                    }
                                    break;
                                case 2:
                                    data.setArtist(text);
                                    break;
                            }
                            dataCount++;
                            break;
                        }
                        case "setContentDescription": {
                            parcel.readInt();

                            String text = TextUtils.CHAR_SEQUENCE_CREATOR
                                    .createFromParcel(parcel).toString().trim();
                            data.setContentType(text);
                            break;
                        }
                        case "setEnabled":
                            // parcel.readInt();

                            // boolean enabled = parcel.readByte() != 0;
                            // TODO see if this can be used in help determine when a song ends
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("AppleNotification", "Failed to parse Apple Notification");
            e.printStackTrace();
        }

        data.setStartTime(new Date(System.currentTimeMillis()));

        handler.push(data);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {
        super.onNotificationRemoved(notification);
    }

}
