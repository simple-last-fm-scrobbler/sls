/**
 * 
 */
package com.adam.aslfms.util.enums;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.adam.aslfms.R;

public enum NetworkOptions {
	ANY(
			"any", new int[] {}, new int[] {}, R.string.advanced_options_net_any_name),
	THREEG_AND_UP(
			"3g_and_up", new int[] {}, new int[] { TelephonyManager.NETWORK_TYPE_UNKNOWN,
					TelephonyManager.NETWORK_TYPE_GPRS }, R.string.advanced_options_net_3gup_name),
	WIFI_ONLY(
			"wifi", new int[] { ConnectivityManager.TYPE_MOBILE }, new int[] {},
			R.string.advanced_options_net_wifi_name);

	private final String settingsVal;
	private final int[] forbiddenNetworkTypes;
	private final int[] forbiddenMobileNetworkSubTypes;
	private final int nameRID;

	private NetworkOptions(String settingsVal, int[] forbiddenNetworkTypes, int[] forbiddenMobileNetworkSubTypes,
			int nameRID) {
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
			Log.e(TAG, "got null network option from settings, defaulting to standard");
			no = NetworkOptions.ANY;
		}
		return no;
	}
}