/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/simple-last-fm-scrobbler/sls
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


package com.adam.aslfms.util;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.adam.aslfms.R;
import com.adam.aslfms.SettingsActivity;
import com.adam.aslfms.service.ControllerReceiverService;
import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.NotificationBarService;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.enums.NetworkOptions;
import com.adam.aslfms.util.enums.PowerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.TimeZone;

import static android.content.Context.POWER_SERVICE;

/**
 * This class is way too bloated. FIXME
 *
 * @author tgwizard
 */

public class Util {
    private static final String TAG = "Util";
    public final static String POPUP_CHANNEL_ID = "com.adam.aslfms.popup";

    /**
     * Returns whether the phone is running on battery or if it is connected to
     * a charger.
     *
     * @param ctx context to get access to battery-checking methods
     * @return an enum indicating what the power source is
     * @see PowerOptions
     */
    public static PowerOptions checkPower(Context ctx) {

        IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = ctx.registerReceiver(null, battFilter);

        if (intent != null) {
            int plugged = intent.getIntExtra("plugged", -1);
            if (plugged == 0) { // == 0 means on battery
                return PowerOptions.BATTERY;
            } else {
                return PowerOptions.PLUGGED_IN;
            }
        }

        Log.d(TAG, "Failed to get intent, assuming battery");
        return PowerOptions.BATTERY;
    }

    /**
     * Network status.
     */
    public enum NetworkStatus {
        OK, UNFIT, DISCONNECTED
    }

    public static NetworkStatus checkForOkNetwork(Context ctx) {

        AppSettings settings = new AppSettings(ctx);
        PowerOptions powerOptions = checkPower(ctx);

        NetworkOptions networkOptions = settings.getNetworkOptions(powerOptions);
        boolean roaming = settings.getSubmitOnRoaming(powerOptions);

        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo == null) {
            Log.d(TAG, "netInfo == null");
        }
        if (netInfo != null) {
            Log.d(TAG, "conn: " + netInfo.isConnected() + " : " + netInfo.toString());
        }

        if (netInfo == null || !netInfo.isConnected()) {
            return NetworkStatus.DISCONNECTED;
        }

        if (netInfo.isRoaming() && !roaming) {
            return NetworkStatus.UNFIT;
        }

        int netType = netInfo.getType();
        int netSubType = netInfo.getSubtype();

        Log.d(TAG, "netType: " + netType);
        Log.d(TAG, "netSubType: " + netSubType);

        if (networkOptions.isNetworkTypeForbidden(netType)) {
            Log.d(TAG, "Network type forbidden");
            return NetworkStatus.UNFIT;
        }
        if (networkOptions.isNetworkSubTypeForbidden(netType, netSubType)) {
            Log.d(TAG, "Network sub type forbidden");
            return NetworkStatus.UNFIT;
        }

        return NetworkStatus.OK;
    }

    /**
     * Returns the current time since 1970, UTC, in seconds.
     *
     * @return the current time since 1970, UTC, in seconds
     */
    public static long currentTimeSecsUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                .getTimeInMillis() / 1000;
    }

    /**
     * Returns the current time since 1970, UTC, in milliseconds.
     *
     * @return the current time since 1970, UTC, in milliseconds
     */
    public static long currentTimeMillisUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                .getTimeInMillis();
    }

    /**
     * Converts time from a long to a string in a format set by the user in the
     * phone's settings.
     *
     * @param ctx  context to get access to the conversion methods
     * @param secs time since 1970, UTC, in seconds
     * @return the time since 1970, UTC, as a string (e.g. 2009-10-23 12:25)
     */
    public static String timeFromUTCSecs(Context ctx, long secs) {
        return DateUtils.formatDateTime(ctx, secs * 1000,
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_NUMERIC_DATE);
    }

    /**
     * Returns the current time since 1970, local time zone, in milliseconds.
     *
     * @return the current time since 1970, local time zone, in milliseconds
     */
    public static long currentTimeMillisLocal() {
        return Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
    }

    public static String timeFromLocalMillis(Context ctx, long millis) {
        return DateUtils.formatDateTime(ctx, millis, DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE);
    }

    public static void confirmDialog(Context ctx, String msg, int posButton,
                                     int negButton, OnClickListener onPositive) {
        new AlertDialog.Builder(ctx).setTitle(R.string.are_you_sure)
                .setMessage(msg).setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(posButton, onPositive).setNegativeButton(
                negButton, null).show();
    }

    public static void warningDialog(Context ctx, String msg) {
        new AlertDialog.Builder(ctx).setTitle(R.string.warning).setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
                R.string.close, null).show();
    }

    public static void scrobbleIfPossible(Context ctx, NetApp napp,
                                          int numInCache) {
        if (numInCache > 0) {
            Intent intent = new Intent(ctx, ScrobblingService.class);
            intent.setAction(ScrobblingService.ACTION_JUSTSCROBBLE);
            intent.putExtra("netapp", napp.getIntentExtraValue());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent);
            } else {
                ctx.startService(intent);
            }
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.no_scrobbles_in_cache),
                    Toast.LENGTH_LONG).show();
        }
    }

    public static void scrobbleAllIfPossible(Context ctx, int numInCache) {
        if (numInCache > 0) {
            Intent service = new Intent(ctx, ScrobblingService.class);
            service.setAction(ScrobblingService.ACTION_JUSTSCROBBLE);
            service.putExtra("scrobbleall", true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(service);
            } else {
                ctx.startService(service);
            }
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.no_scrobbles_in_cache),
                    Toast.LENGTH_LONG).show();
        }
    }

    public static void heartIfPossible(Context ctx) {

        try {
            Intent service = new Intent(ctx, ScrobblingService.class);
            service.setAction(ScrobblingService.ACTION_HEART);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(service);
            } else {
                ctx.startService(service);
            }
        } catch (Exception e) {
            Toast.makeText(ctx, ctx.getString(R.string.no_heart_track),
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "CAN'T HEART TRACK" + e);
        }
    }

    public static void copyIfPossible(Context ctx) {
        try {
            Intent service = new Intent(ctx, ScrobblingService.class);
            service.setAction(ScrobblingService.ACTION_COPY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(service);
            } else {
                ctx.startService(service);
            }
        } catch (Exception e) {
            Toast.makeText(ctx, ctx.getString(R.string.no_copy_track),
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "CAN'T COPY TRACK" + e);
        }
    }

    public static void deleteScrobbleFromCache(Context ctx,
                                               final ScrobblesDatabase db, final NetApp napp, final Cursor cursor,
                                               final int id) {
        Util.confirmDialog(ctx, ctx.getString(R.string.confirm_delete_sc)
                        .replaceAll("%1", napp.getName()), R.string.remove,
                android.R.string.cancel, (dialog, which) -> {

                    Log.d(TAG, "Will remove scrobble from cache: "
                            + napp.getName() + ", " + id);
                    db.deleteScrobble(napp, id);
                    db.cleanUpTracks();
                    // need to refill data, otherwise the screen won't
                    // update
                    if (cursor != null)
                        cursor.requery();
                });
    }

    public static void deleteScrobbleFromAllCaches(Context ctx,
                                                   final ScrobblesDatabase db, final Cursor cursor, final int id) {
        Util.confirmDialog(ctx, ctx
                        .getString(R.string.confirm_delete_sc_from_all),
                R.string.remove, android.R.string.cancel, (dialog, which) -> {

                    Log.d(TAG, "Will remove scrobble from all caches: "
                            + id);
                    for (NetApp napp : NetApp.values())
                        db.deleteScrobble(napp, id);
                    db.cleanUpTracks();
                    // need to refill data, otherwise the screen won't
                    // update
                    if (cursor != null)
                        cursor.requery();
                });
    }

    public static void deleteAllScrobblesFromCache(Context ctx,
                                                   final ScrobblesDatabase db, final NetApp napp, final Cursor cursor) {
        int numInCache = db.queryNumberOfScrobbles(napp);
        if (numInCache > 0) {
            Util.confirmDialog(ctx, ctx.getString(
                    R.string.confirm_delete_all_sc).replaceAll("%1",
                    napp.getName()), R.string.clear_cache, android.R.string.cancel,
                    (dialog, which) -> {
                        Log.d(TAG, "Will remove all scrobbles from cache: "
                                + napp.getName());
                        db.deleteAllScrobbles(napp);
                        db.cleanUpTracks();
                        // need to refill data, otherwise the screen won't
                        // update
                        if (cursor != null)
                            cursor.requery();
                    });
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.no_scrobbles_in_cache),
                    Toast.LENGTH_LONG).show();
        }
    }

    public static void deleteAllScrobblesFromAllCaches(Context ctx,
                                                       final ScrobblesDatabase db, final Cursor cursor) {
        int numInCache = db.queryNumberOfTracks();
        if (numInCache > 0) {
            Util.confirmDialog(ctx, ctx
                            .getString(R.string.confirm_delete_all_sc_from_all),
                    R.string.clear_cache, android.R.string.cancel,
                    (dialog, which) -> {
                        Log
                                .d(TAG,
                                        "Will remove all scrobbles from cache for all netapps");
                        for (NetApp napp : NetApp.values())
                            db.deleteAllScrobbles(napp);
                        db.cleanUpTracks();
                        // need to refill data, otherwise the screen won't
                        // update
                        if (cursor != null)
                            cursor.requery();
                    });
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.no_scrobbles_in_cache),
                    Toast.LENGTH_LONG).show();
        }
    }

    public static String getStatusSummary(Context ctx, AppSettings settings,
                                          NetApp napp) {
        return getStatusSummary(ctx, settings, napp, true);
    }

    /**
     * TODO: Should it be here? (And it is quite ugly...)
     *
     * @param ctx
     * @param settings
     * @return
     */
    public static String getStatusSummary(Context ctx, AppSettings settings,
                                          NetApp napp, boolean includeValues) {
        if (settings.getAuthStatus(napp) == AuthStatus.AUTHSTATUS_BADAUTH) {
            return ctx.getString(R.string.auth_bad_auth);
        } else if (settings.getAuthStatus(napp) == AuthStatus.AUTHSTATUS_FAILED) {
            return ctx.getString(R.string.auth_internal_error);
        } else if (settings.getAuthStatus(napp) == AuthStatus.AUTHSTATUS_RETRYLATER) {
            return ctx.getString(R.string.auth_network_error_retrying);
        } else if (settings.getAuthStatus(napp) == AuthStatus.AUTHSTATUS_NETWORKUNFIT) {
            return ctx.getString(R.string.auth_network_unfit);
        } else if (settings.getAuthStatus(napp) == AuthStatus.AUTHSTATUS_OK) {
            if (includeValues)
                return ctx.getString(R.string.logged_in_as).replace("%1",
                        settings.getUsername(napp));
            else
                return ctx.getString(R.string.logged_in_just);
        } else if (settings.getAuthStatus(napp) == AuthStatus.AUTHSTATUS_NOAUTH) {
            if (includeValues)
                return ctx.getString(R.string.user_credentials_summary)
                        .replace("%1", napp.getName());
            else
                return ctx.getString(R.string.not_logged_in);
        } else if (settings.getAuthStatus(napp) == AuthStatus.AUTHSTATUS_UPDATING) {
            return ctx.getString(R.string.auth_updating);
        } else if (settings.getAuthStatus(napp) == AuthStatus.AUTHSTATUS_CLIENTBANNED) {
            return ctx.getString(R.string.auth_client_banned);
        } else {
            return "";
        }
    }

    public static boolean checkForInstalledApp(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            pm.getPackageInfo(pkgName, 0);
            // Log.d(TAG, pkgString + " is installed");
            return true;
        } catch (NameNotFoundException e) {
            // Log.d(TAG, pkgString + " is not installed");
        }
        return false;
    }

    public static String getAppName(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
            String label = pm.getApplicationLabel(appInfo).toString();
            return label;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    public static String getAppVersionName(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageInfo(pkgName, 0);
            String ver = pkgInfo.versionName;
            return ver;
        } catch (NameNotFoundException e) {
            return "0";
        }
    }

    public static int getAppVersionCode(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageInfo(pkgName, 0);
            return pkgInfo.versionCode;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

    public static void initChannels(Context context) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(POPUP_CHANNEL_ID,
                context.getString(R.string.app_name_short),
                notificationStringToInt(context));
        channel.setDescription(context.getString(R.string.app_name));
        channel.setSound(null, null);
        notificationManager.createNotificationChannel(channel);
    }

    public static void myNotify(Context mCtx, String title, String content, int notID, Class myClass) {
        try {
            initChannels(mCtx);
            // notification builder
            NotificationCompat.Builder notificationBuilder = null;
            Notification notification = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notificationBuilder = new NotificationCompat.Builder(mCtx, POPUP_CHANNEL_ID);
                notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
            } else {
                notificationBuilder = new NotificationCompat.Builder(mCtx);
            }
            Intent targetIntent = new Intent(mCtx, myClass);
            PendingIntent contentIntent = PendingIntent.getActivity(mCtx, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_icon)
                    .setContentText(content)
                    .setPriority(oldNotificationStringToInt(mCtx))
                    .setContentIntent(contentIntent)
                    .setColor(Color.RED)
                    .setChannelId(POPUP_CHANNEL_ID);

            NotificationManager nManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(),
                        R.drawable.ic_icon));
            }
            notification = notificationBuilder.build();
            nManager.notify(notID, notification);
        } catch (Exception e) {
            Log.d(TAG, "Phone Notification failed. " + e);
        }
    }

    private static void exportDB(String dbName, Context ctx) {
        File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + ctx.getPackageName() + "/databases/" + dbName;
        String backupDBPath = "simple.last.fm.scrobbler.db";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Util.myNotify(ctx, "Database Exported", backupDB.toString(), 57109, SettingsActivity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportAllDatabases(Context ctx) {
        exportDB(ScrobblesDatabase.DATABASE_NAME, ctx);
    }

    public static int oldNotificationStringToInt(Context ctx) {
        AppSettings settings = new AppSettings(ctx);
        switch (settings.getKeyNotificationPriority()) {
            case "min":
                return Notification.PRIORITY_MIN;
            case "low":
                return Notification.PRIORITY_LOW;
            case "default":
                return Notification.PRIORITY_DEFAULT;
            case "high":
                return Notification.PRIORITY_HIGH;
            case "max":
                return Notification.PRIORITY_MAX;
            default:
                break;
        }
        return NotificationManager.IMPORTANCE_DEFAULT;
    }

    public static int notificationStringToInt(Context ctx) {
        AppSettings settings = new AppSettings(ctx);
        switch (settings.getKeyNotificationPriority()) {
            case "min":
                return NotificationManager.IMPORTANCE_MIN;
            case "low":
                return NotificationManager.IMPORTANCE_LOW;
            case "default":
                return NotificationManager.IMPORTANCE_DEFAULT;
            case "high":
                return NotificationManager.IMPORTANCE_HIGH;
            case "max":
                return NotificationManager.IMPORTANCE_MAX;
            default:
                break;
        }
        return NotificationManager.IMPORTANCE_DEFAULT;
    }

    public static void stopMyService(Intent i, Context context) {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.stopService(i);
    }

    public static void stopAllServices(Context context) {
        Intent i = new Intent(context, NotificationBarService.class);
        Intent ii = new Intent(context, ScrobblingService.class);
        Intent iii = new Intent(context, ControllerReceiverService.class);
        stopMyService(i, context);
        stopMyService(ii, context);
        stopMyService(iii, context);
    }

    public static void runServices(Context context) {
        // Start listening service if applicable
        Intent i = new Intent(context, NotificationBarService.class);
        Intent ii = new Intent(context, ScrobblingService.class);
        i.setAction(NotificationBarService.ACTION_NOTIFICATION_BAR_UPDATE);
        i.putExtra("track", "");
        i.putExtra("artist", "");
        i.putExtra("album", "");
        i.putExtra("app_name", "");
        ii.setAction(ScrobblingService.ACTION_START_SCROBBLER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent iii = new Intent(context, ControllerReceiverService.class);
            Log.d(TAG, "(re)starting controllerreceiver");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(iii);
            } else {
                context.startService(iii);
            }
        }
        Log.d(TAG, "(re)starting scrobbleservice, notificationbarservice");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
            context.startForegroundService(ii);
        } else {
            context.startService(i);
            context.startService(ii);
        }
    }

    public static boolean checkNotificationListenerPermission(Context ctx){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (!NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.getPackageName())) {        //ask for permission
                    Log.e(TAG, "Problem, ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    return false;
                }
            }
        } catch(Exception e){
            Log.e(TAG, "Exception, ACTION_NOTIFICATION_LISTENER_SETTINGS" + e);
            return false;
        }
        return true;
    }
    // external storage
    public static boolean checkExternalPermission(Context ctx) {
        try {
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Problem, *_EXTERNAL_STORAGE. ");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception, *_EXTERNAL_STORAGE. " + e);
            return false;
        }
        return true;
    }
    // battery optimization
    public static boolean checkBatteryOptimizationBasicPermission(Context ctx){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Problem, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS. ");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS. " + e);
            return false;
        }
        return true;
    }

    public static boolean checkBatteryOptimizationsPermission(Context ctx){
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String packageName = ctx.getPackageName();
                PowerManager pm = (PowerManager) ctx.getSystemService(POWER_SERVICE);
                if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                    Log.d(TAG, "Problem, POWER_OPTIMIZATIONS. ");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception, POWER_OPTIMIZATIONS. " + e);
            return false;
        }
        return true;
    }
}