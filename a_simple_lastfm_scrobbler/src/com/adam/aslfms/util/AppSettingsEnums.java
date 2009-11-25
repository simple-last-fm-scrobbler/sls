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

package com.adam.aslfms.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.adam.aslfms.R;

public class AppSettingsEnums {
	public enum SubmissionType {
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

	public enum AdvancedOptions {
		STANDARD("ao_standard", AdvancedOptionsWhen.AFTER_1, true, true,
				R.string.advanced_options_type_standard_name), // 
		BATTERY_SAVING("ao_battery", AdvancedOptionsWhen.AFTER_10, true, true,
				R.string.advanced_options_type_battery_name), // 
		CUSTOM("ao_custom", AdvancedOptionsWhen.AFTER_1, true, true,
				R.string.advanced_options_type_custom_name); // these values are
		// ignored for CUSTOM

		private final String settingsVal;
		private final AdvancedOptionsWhen when;
		private final boolean alsoOnComplete;
		private final int nameRID;
		private final boolean alsoOnPlugged;

		private AdvancedOptions(String settingsVal, AdvancedOptionsWhen when,
				boolean alsoOnComplete, boolean alsoOnPlugged, int nameRID) {
			this.settingsVal = settingsVal;
			this.when = when;
			this.alsoOnComplete = alsoOnComplete;
			this.alsoOnPlugged = alsoOnPlugged;
			this.nameRID = nameRID;
		}

		public String getSettingsVal() {
			return settingsVal;
		}

		public AdvancedOptionsWhen getWhen() {
			return when;
		}

		public boolean getAlsoOnComplete() {
			return alsoOnComplete;
		}

		public boolean getAlsoOnPlugged() {
			return alsoOnPlugged;
		}

		public String getName(Context ctx) {
			return ctx.getString(nameRID);
		}

		private static final String TAG = "AdvancedOptions";
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

	public enum AdvancedOptionsWhen {
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

		public String getSettingsVal() {
			return settingsVal;
		}

		public int getTracksToWaitFor() {
			return tracksToWaitFor;
		}

		public String getName(Context ctx) {
			return ctx.getString(nameRID);
		}

		private static final String TAG = "AdvancedOptionsWhen";
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

}
