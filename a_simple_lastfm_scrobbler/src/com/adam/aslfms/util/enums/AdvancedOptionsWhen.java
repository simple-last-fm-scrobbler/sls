/**
 * 
 */
package com.adam.aslfms.util.enums;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.adam.aslfms.R;

public enum AdvancedOptionsWhen {
	AFTER_1(
			"aow_1", 1, R.string.advanced_options_when_1_name),
	AFTER_5(
			"aow_5", 5, R.string.advanced_options_when_5_name),
	AFTER_10(
			"aow_10", 10, R.string.advanced_options_when_10_name),
	AFTER_25(
			"aow_25", 25, R.string.advanced_options_when_25_name),
	NEVER(
			"aow_never", Integer.MAX_VALUE, R.string.advanced_options_when_never_name);

	private final String settingsVal;
	private final int tracksToWaitFor;
	private final int nameRID;

	private AdvancedOptionsWhen(String settingsVal, int tracksToWaitFor, int nameRID) {
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
			Log.e(TAG, "got null advanced options when from settings, defaulting to 1");
			aow = AdvancedOptionsWhen.AFTER_1;
		}
		return aow;
	}
}