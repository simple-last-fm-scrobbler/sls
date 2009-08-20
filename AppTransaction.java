package com.adam.aslfms;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.TimeZone;

public class AppTransaction {
	private static final Object syncObject = new Object();
	private static LinkedList<Track> tracks = new LinkedList<Track>();

	public static void pushTrack(Track t) {
		synchronized (syncObject) {
			tracks.addLast(t);
		}
	}

	public static Track popTrack() {
		synchronized (syncObject) {
			if (tracks.isEmpty())
				return null;
			return tracks.removeFirst();
		}
	}

	/*
	 * @return current time sinze 1970 in seconds
	 */
	public static long currentTimeUTC() {
		return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
				.getTimeInMillis() / 1000;
	}
}
