/**
 *  This file is part of Simple Last.fm Scrobbler.
 *
 *  Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms.util;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.adam.aslfms.R;
import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.enums.NetworkOptions;
import com.adam.aslfms.util.enums.PowerOptions;

/**
 * This class is way too bloated. FIXME
 * 
 * @author tgwizard
 * 
 */
public class Util {
	private static final String TAG = "Util";

	/**
	 * Returns whether the phone is running on battery or if it is connected to
	 * a charger.
	 * 
	 * @see PowerOptions
	 * 
	 * @param ctx
	 *            context to get access to battery-checking methods
	 * @return an enum indicating what the power source is
	 */
	public static PowerOptions checkPower(Context ctx) {
		// check if plugged into AC
		IntentFilter battFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		Intent intent = ctx.registerReceiver(null, battFilter);
		int plugged = intent.getIntExtra("plugged", -1);
		if (plugged == 0) { // == 0 means on battery
			return PowerOptions.BATTERY;
		} else {
			return PowerOptions.PLUGGED_IN;
		}
	}

	public static enum NetworkStatus {
		OK, UNFIT, DISCONNECTED
	}

	public static NetworkStatus checkForOkNetwork(Context ctx) {
		AppSettings settings = new AppSettings(ctx);
		PowerOptions pow = checkPower(ctx);

		NetworkOptions no = settings.getNetworkOptions(pow);
		boolean roaming = settings.getSubmitOnRoaming(pow);

		ConnectivityManager cMgr = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cMgr.getActiveNetworkInfo();

		if (netInfo == null)
			return NetworkStatus.DISCONNECTED;
		if (!netInfo.isConnected())
			return NetworkStatus.DISCONNECTED;
		if (netInfo.isRoaming() && !roaming)
			return NetworkStatus.UNFIT;

		int netType = netInfo.getType();
		int netSubType = netInfo.getSubtype();

		// Log.d(TAG, "netType: " + netType);
		// Log.d(TAG, "netSubType: " + netSubType);

		if (no.isNetworkTypeForbidden(netType))
			return NetworkStatus.UNFIT;
		if (no.isNetworkSubTypeForbidden(netType, netSubType))
			return NetworkStatus.UNFIT;

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
	 * @param ctx
	 *            context to get access to the conversion methods
	 * @param secs
	 *            time since 1970, UTC, in seconds
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
			ctx.startService(intent);
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
			ctx.startService(service);
		} else {
			Toast.makeText(ctx, ctx.getString(R.string.no_scrobbles_in_cache),
					Toast.LENGTH_LONG).show();
		}
	}

	public static void deleteScrobbleFromCache(Context ctx,
			final ScrobblesDatabase db, final NetApp napp, final Cursor cursor,
			final int id) {
		Util.confirmDialog(ctx, ctx.getString(R.string.confirm_delete_sc)
				.replaceAll("%1", napp.getName()), R.string.remove,
				android.R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						Log.d(TAG, "Will remove scrobble from cache: "
								+ napp.getName() + ", " + id);
						db.deleteScrobble(napp, id);
						db.cleanUpTracks();
						// need to refill data, otherwise the screen won't
						// update
						if (cursor != null)
							cursor.requery();
					}
				});
	}

	public static void deleteScrobbleFromAllCaches(Context ctx,
			final ScrobblesDatabase db, final Cursor cursor, final int id) {
		Util.confirmDialog(ctx, ctx
				.getString(R.string.confirm_delete_sc_from_all),
				R.string.remove, android.R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						Log.d(TAG, "Will remove scrobble from all caches: "
								+ id);
						for (NetApp napp : NetApp.values())
							db.deleteScrobble(napp, id);
						db.cleanUpTracks();
						// need to refill data, otherwise the screen won't
						// update
						if (cursor != null)
							cursor.requery();
					}
				});
	}

	public static void deleteAllScrobblesFromCache(Context ctx,
			final ScrobblesDatabase db, final NetApp napp, final Cursor cursor) {
		int numInCache = db.queryNumberOfScrobbles(napp);
		if (numInCache > 0) {
			Util.confirmDialog(ctx, ctx.getString(
					R.string.confirm_delete_all_sc).replaceAll("%1",
					napp.getName()), R.string.clear_cache, android.R.string.cancel,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "Will remove all scrobbles from cache: "
									+ napp.getName());
							db.deleteAllScrobbles(napp);
							db.cleanUpTracks();
							// need to refill data, otherwise the screen won't
							// update
							if (cursor != null)
								cursor.requery();
						}
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
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
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
						}
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
}
