package com.adam.aslfms.receiver;

import android.content.Context;
import android.os.Bundle;

public class MIUIMusicReceiver extends BuiltInMusicAppReceiver {

	static final String APP_PACKAGE = "com.miui.player";
	static final String ACTION_MIUI_STOP = "com.miui.player.playbackcomplete";
	static final String ACTION_MIUI_METACHANGED = "com.miui.player.metachanged";

	public MIUIMusicReceiver() {
		super(APP_PACKAGE, "MIUI Music Player");
	}

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle) throws IllegalArgumentException {
		super.parseIntent(ctx, action, bundle);
	}

	@Override
	public String getPlaybackCompleteAction() {
		return ACTION_MIUI_STOP;
	}

	@Override
	public String getMetaChangedAction() {
		return ACTION_MIUI_METACHANGED;
	}
}
