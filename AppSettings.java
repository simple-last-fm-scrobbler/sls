package com.adam.aslfms;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppSettings {

	private final String SETTINGS_NAME = "settings";

	private final String KEY_USERNAME = "username";
	private final String KEY_PASSWORD = "password";
	private final String KEY_PWDMD5 = "pwdMd5";

	private final String KEY_SCROBBLING_ENABLE = "enable_scrobbling";
	private final String KEY_NOWPLAYING_ENABLE = "enable_nowplaying";

	private final String KEY_AUTH_STATUS = "authstatus";
	private final String KEY_AUTH_FAIL = "authfail";
	
	private final String KEY_LAST_LISTEN_TIME = "last_listen_time";

	private SharedPreferences prefs;

	public AppSettings(Context ctx) {
		super();
		prefs = ctx.getSharedPreferences(SETTINGS_NAME, 0);
	}

	public void clearSettings() {
		setUsername("");
		setPassword("");
		setPwdMd5("");
		setAuthStatus(Status.AUTHSTATUS_NOAUTH);
		setAuthFail("");
		setScrobblingEnabled(false); // TODO::
		setNowPlayingEnabled(false);
	}

	public void setUsername(String s) {
		Editor e = prefs.edit();
		e.putString(KEY_USERNAME, s);
		e.commit();
	}

	public String getUsername() {
		return prefs.getString(KEY_USERNAME, "");
	}

	public void setPassword(String s) {
		Editor e = prefs.edit();
		e.putString(KEY_PASSWORD, s);
		e.commit();
	}

	public String getPassword() {
		return prefs.getString(KEY_PASSWORD, "");
	}

	public void setPwdMd5(String s) {
		Editor e = prefs.edit();
		e.putString(KEY_PWDMD5, s);
		e.commit();
	}

	public String getPwdMd5() {
		return prefs.getString(KEY_PWDMD5, "");
	}

	public void setScrobblingEnabled(boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_SCROBBLING_ENABLE, b);
		e.commit();
	}

	public boolean isScrobblingEnabled() {
		return prefs.getBoolean(KEY_SCROBBLING_ENABLE, false);
	}

	public void setNowPlayingEnabled(boolean b) {
		Editor e = prefs.edit();
		e.putBoolean(KEY_NOWPLAYING_ENABLE, b);
		e.commit();
	}

	public boolean isNowPlayingEnabled() {
		return prefs.getBoolean(KEY_NOWPLAYING_ENABLE, false);
	}

	public void setAuthStatus(int i) {
		Editor e = prefs.edit();
		e.putInt(KEY_AUTH_STATUS, i);
		e.commit();
	}

	public int getAuthStatus() {
		return prefs.getInt(KEY_AUTH_STATUS, Status.AUTHSTATUS_NOAUTH);
	}

	public boolean isAuthenticated() {
		return getAuthStatus() == Status.AUTHSTATUS_OK;
	}

	public void setAuthFail(String s) {
		Editor e = prefs.edit();
		e.putString(KEY_AUTH_FAIL, s);
		e.commit();
	}

	public String getAuthFail() {
		return prefs.getString(KEY_AUTH_FAIL, "");
	}
	
	public void setLastScrobbleTime(long time) {
		Editor e = prefs.edit();
		e.putLong(KEY_LAST_LISTEN_TIME, time);
		e.commit();
	}
	
	public long getLastListenTime() {
		return prefs.getLong(KEY_LAST_LISTEN_TIME, 0);
	}
}
