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

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.adam.aslfms.R;

public final class AppSettingsEnums {
	public static enum SubmissionType {
		SCROBBLE("status_last_scrobble", "status_nscrobbles"), // 
		NP("status_last_np", "status_nnps");

		private final String lastPrefix;
		private final String numberOfPrefix;

		SubmissionType(String lastPrefix, String numberOfPrefix) {
			this.lastPrefix = lastPrefix;
			this.numberOfPrefix = numberOfPrefix;
		}

		public String getLastPrefix() {
			return lastPrefix;
		}

		public String getNumberOfPrefix() {
			return numberOfPrefix;
		}
	}

	public static enum PowerOptions {
		BATTERY("", new AdvancedOptions[] { AdvancedOptions.STANDARD,
				AdvancedOptions.BATTERY_SAVING, AdvancedOptions.CUSTOM }), //
		PLUGGED_IN("_plugged", new AdvancedOptions[] {
				AdvancedOptions.SAME_AS_BATTERY, AdvancedOptions.STANDARD,
				AdvancedOptions.CUSTOM });

		private final String settingsPath;
		private final AdvancedOptions[] applicableOptions;

		private PowerOptions(String settingsPath,
				AdvancedOptions[] applicableOptions) {
			this.settingsPath = settingsPath;
			this.applicableOptions = applicableOptions;
		}

		String getSettingsPath() {
			return settingsPath;
		}

		public AdvancedOptions[] getApplicableOptions() {
			return applicableOptions;
		}

	}

	public static enum AdvancedOptions {
		// the values below for SAME will be ignored
		SAME_AS_BATTERY("ao_same_as_battery", true, true,
				AdvancedOptionsWhen.AFTER_1, true, NetworkOptions.ANY, false,
				R.string.advanced_options_type_same_as_battery_name), // 
		STANDARD("ao_standard", true, true, AdvancedOptionsWhen.AFTER_1, true,
				NetworkOptions.ANY, false,
				R.string.advanced_options_type_standard_name), // 
		// not available for plugged in
		BATTERY_SAVING("ao_battery", true, false, AdvancedOptionsWhen.AFTER_10,
				true, NetworkOptions.ANY, false,
				R.string.advanced_options_type_battery_name), //
		// the values below for CUSTOM will be ignored
		CUSTOM("ao_custom", true, true, AdvancedOptionsWhen.AFTER_1, true,
				NetworkOptions.ANY, false,
				R.string.advanced_options_type_custom_name);

		private final String settingsVal;
		private final boolean enableScrobbling;
		private final boolean enableNp;
		private final AdvancedOptionsWhen when;
		private final boolean alsoOnComplete;
		private final NetworkOptions networkOptions;
		private final boolean roaming;
		private final int nameRID;

		private AdvancedOptions(String settingsVal, boolean enableScrobbling,
				boolean enableNp, AdvancedOptionsWhen when,
				boolean alsoOnComplete, NetworkOptions networkOptions,
				boolean roaming, int nameRID) {
			this.settingsVal = settingsVal;
			this.enableScrobbling = enableScrobbling;
			this.enableNp = enableNp;
			this.when = when;
			this.alsoOnComplete = alsoOnComplete;
			this.networkOptions = networkOptions;
			this.roaming = roaming;
			this.nameRID = nameRID;
		}

		// these methods are intentionally package-private, they are only used
		// by AppSettings

		String getSettingsVal() {
			return settingsVal;
		}

		boolean isScrobblingEnabled() {
			return enableScrobbling;
		}

		boolean isNpEnabled() {
			return enableNp;
		}

		AdvancedOptionsWhen getWhen() {
			return when;
		}

		boolean getAlsoOnComplete() {
			return alsoOnComplete;
		}

		public NetworkOptions getNetworkOptions() {
			return networkOptions;
		}

		public boolean getRoaming() {
			return roaming;
		}

		public String getName(Context ctx) {
			return ctx.getString(nameRID);
		}

		private static final String TAG = "SLSAdvancedOptions";
		private static Map<String, AdvancedOptions> mSAOMap;
		static {
			AdvancedOptions[] aos = AdvancedOptions.values();
			mSAOMap = new HashMap<String, AdvancedOptions>(aos.length);
			for (AdvancedOptions ao : aos)
				mSAOMap.put(ao.getSettingsVal(), ao);
		}

		public static AdvancedOptions fromSettingsVal(String s) {
			AdvancedOptions ao = mSAOMap.get(s);
			if (ao == null) {
				Log
						.e(TAG,
								"got null advanced option from settings, defaulting to standard");
				ao = AdvancedOptions.STANDARD;
			}
			return ao;
		}
	}

	public static enum AdvancedOptionsWhen {
		AFTER_1("aow_1", 1, R.string.advanced_options_when_1_name), //
		AFTER_5("aow_5", 5, R.string.advanced_options_when_5_name), //
		AFTER_10("aow_10", 10, R.string.advanced_options_when_10_name), //
		AFTER_25("aow_25", 25, R.string.advanced_options_when_25_name), //
		NEVER("aow_never", Integer.MAX_VALUE,
				R.string.advanced_options_when_never_name);

		private final String settingsVal;
		private final int tracksToWaitFor;
		private final int nameRID;

		private AdvancedOptionsWhen(String settingsVal, int tracksToWaitFor,
				int nameRID) {
			this.settingsVal = settingsVal;
			this.tracksToWaitFor = tracksToWaitFor;
			this.nameRID = nameRID;
		}

		String getSettingsVal() {
			return settingsVal;
		}

		public int getTracksToWaitFor() {
			return tracksToWaitFor;
		}

		public String getName(Context ctx) {
			return ctx.getString(nameRID);
		}

		private static final String TAG = "SLSAdvancedOptionsWhen";
		private static Map<String, AdvancedOptionsWhen> mSAOWMap;
		static {
			AdvancedOptionsWhen[] aows = AdvancedOptionsWhen.values();
			mSAOWMap = new HashMap<String, AdvancedOptionsWhen>(aows.length);
			for (AdvancedOptionsWhen aow : aows)
				mSAOWMap.put(aow.getSettingsVal(), aow);
		}

		public static AdvancedOptionsWhen fromSettingsVal(String s) {
			AdvancedOptionsWhen aow = mSAOWMap.get(s);
			if (aow == null) {
				Log
						.e(TAG,
								"got null advanced options when from settings, defaulting to 1");
				aow = AdvancedOptionsWhen.AFTER_1;
			}
			return aow;
		}
	}

	public static enum NetworkOptions {
		ANY("any", new int[] {}, new int[] {},
				R.string.advanced_options_net_any_name), //
		THREEG_AND_UP("3g_and_up", new int[] {}, new int[] {
				TelephonyManager.NETWORK_TYPE_UNKNOWN,
				TelephonyManager.NETWORK_TYPE_GPRS },
				R.string.advanced_options_net_3gup_name), //
		WIFI_ONLY("wifi", new int[] { ConnectivityManager.TYPE_MOBILE },
				new int[] {}, R.string.advanced_options_net_wifi_name);

		private final String settingsVal;
		private final int[] forbiddenNetworkTypes;
		private final int[] forbiddenMobileNetworkSubTypes;
		private final int nameRID;

		private NetworkOptions(String settingsVal, int[] forbiddenNetworkTypes,
				int[] forbiddenMobileNetworkSubTypes, int nameRID) {
			this.settingsVal = settingsVal;
			this.forbiddenNetworkTypes = forbiddenNetworkTypes;
			this.forbiddenMobileNetworkSubTypes = forbiddenMobileNetworkSubTypes;
			this.nameRID = nameRID;
		}

		public String getSettingsVal() {
			return settingsVal;
		}

		public int[] getForbiddenNetworkTypes() {
			return forbiddenNetworkTypes;
		}

		public boolean isNetworkTypeForbidden(int netType) {
			for (int nt : forbiddenNetworkTypes)
				if (nt == netType)
					return true;
			return false;
		}

		public int[] getForbiddenMobileNetworkSubTypes() {
			return forbiddenMobileNetworkSubTypes;
		}

		public boolean isNetworkSubTypeForbidden(int netType, int netSubType) {
			if (netType != ConnectivityManager.TYPE_MOBILE)
				return false;
			for (int nt : forbiddenMobileNetworkSubTypes)
				if (nt == netSubType)
					return true;
			return false;
		}

		public int getNameRID() {
			return nameRID;
		}

		public String getName(Context ctx) {
			return ctx.getString(nameRID);
		}

		private static final String TAG = "SLSNetworkOptions";
		private static Map<String, NetworkOptions> mNOMap;
		static {
			NetworkOptions[] nos = NetworkOptions.values();
			mNOMap = new HashMap<String, NetworkOptions>(nos.length);
			for (NetworkOptions no : nos)
				mNOMap.put(no.getSettingsVal(), no);
		}

		public static NetworkOptions fromSettingsVal(String s) {
			NetworkOptions no = mNOMap.get(s);
			if (no == null) {
				Log
						.e(TAG,
								"got null network option from settings, defaulting to standard");
				no = NetworkOptions.ANY;
			}
			return no;
		}
	}

	private AppSettingsEnums() {

	}

	/**
	 * A convenient way to send the sort order of a query as a parameter to the
	 * method.
	 * 
	 * @author tgwizard
	 * 
	 */
	public static enum SortOrder {
		ASCENDING("asc"), DESCENDING("desc");

		private final String sql;

		private SortOrder(String sql) {
			this.sql = sql;
		}

		public String getSql() {
			return sql;
		}
	}

	public static enum SortField {
		WHEN_ASC("whenplayed", SortOrder.ASCENDING, R.string.sc_sort_when_asc), //
		WHEN_DESC("whenplayed", SortOrder.DESCENDING,
				R.string.sc_sort_when_desc), //
		ARTIST_ASC("artist", SortOrder.ASCENDING, R.string.sc_sort_artist_asc), //
		ARTIST_DESC("artist", SortOrder.DESCENDING,
				R.string.sc_sort_artist_desc), //
		ALBUM_ASC("album", SortOrder.ASCENDING, R.string.sc_sort_album_asc), //
		ALBUM_DESC("album", SortOrder.DESCENDING, R.string.sc_sort_album_desc), //
		TRACK_ASC("track", SortOrder.ASCENDING, R.string.sc_sort_track_asc), //
		TRACK_DESC("track", SortOrder.DESCENDING, R.string.sc_sort_track_desc);

		private final String field;
		private final SortOrder sortOrder;
		private final int nameRID;

		private SortField(String field,
				com.adam.aslfms.util.AppSettingsEnums.SortOrder sortOrder,
				int nameRID) {
			this.field = field;
			this.sortOrder = sortOrder;
			this.nameRID = nameRID;
		}

		public String getField() {
			return field;
		}

		public SortOrder getSortOrder() {
			return sortOrder;
		}

		public int getNameRID() {
			return nameRID;
		}

		public String getName(Context ctx) {
			return ctx.getString(nameRID);
		}

		public String getSql() {
			return field + " " + sortOrder.getSql();
		}

		public static CharSequence[] toCharSequenceArray(Context ctx) {
			return new CharSequence[] { ctx.getString(WHEN_ASC.nameRID),
					ctx.getString(WHEN_DESC.nameRID),
					ctx.getString(ARTIST_ASC.nameRID),
					ctx.getString(ARTIST_DESC.nameRID),
					ctx.getString(ALBUM_ASC.nameRID),
					ctx.getString(ALBUM_DESC.nameRID),
					ctx.getString(TRACK_ASC.nameRID),
					ctx.getString(TRACK_DESC.nameRID), };
		}
	}
}
