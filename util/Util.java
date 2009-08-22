package com.adam.aslfms.util;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.text.format.DateUtils;

public class Util {
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
		return DateUtils.formatDateTime(ctx, millis, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE);
	}
}
