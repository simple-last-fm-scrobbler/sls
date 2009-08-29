/**
 *  This file is part of A Simple Last.fm Scrobbler.
 *
 *  A Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  A Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with A Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms.adc2.util;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.DateUtils;

public class Util {
	@SuppressWarnings("unused")
	private static final String TAG = "Util";

	/**
	 * 
	 * @return the current time since 1970, UTC, in seconds
	 */
	public static long currentTimeSecsUTC() {
		return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
				.getTimeInMillis() / 1000;
	}

	public static long currentTimeMillisLocal() {
		return Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
	}

	public static String timeFromLocalMillis(Context ctx, long millis) {
		return DateUtils.formatDateTime(ctx, millis, DateUtils.FORMAT_SHOW_TIME
				| DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE);
	}
	
	public static boolean checkForInstalledApp(Context ctx, String pkgString) {
		try {
			PackageManager pm = ctx.getPackageManager();
			pm.getPackageInfo(pkgString, 0);
			//Log.d(TAG, pkgString + " is installed");
			return true;
		} catch (NameNotFoundException e) {
			//Log.d(TAG, pkgString + " is not installed");
		}
		return false;
	}
}
