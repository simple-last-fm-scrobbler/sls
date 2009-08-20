package com.adam.aslfms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PlayStatusReceiver extends BroadcastReceiver {

	private static final String TAG = "PlayStatusReceiver";

	private static final String SERVICE = "com.adam.aslfms.playstatechanged";

	private static final String ACTION_CHANGED = "com.android.music.playstatechanged";
	private static final String ACTION_STOP = "com.android.music.playbackcomplete";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle bundle = intent.getExtras();

		if (action == null || bundle == null) {
			Log.e(TAG, "Got null action or null bundle");
			return;
		}

		Intent service = new Intent(SERVICE);
		Track track = null;
		if (action.equals(ACTION_CHANGED) || action.equals(ACTION_STOP)) {
			CharSequence ar = bundle.getCharSequence("artist");
			CharSequence al = bundle.getCharSequence("album");
			CharSequence tr = bundle.getCharSequence("track");
			
			// As of cupcake, it is not possible (feasible) to get the actual
			// duration of the playing track, so I default it to three minutes
			track = new Track(ar, al, tr, 180, AppTransaction.currentTimeUTC());

			if (intent.getAction().equals(ACTION_STOP)) {
				Log.d(TAG, "Action was stop");
				service.putExtra("stopped", true);
			} else {
				Log.d(TAG, "Action was change");
			}
		} else {
			Log.e(TAG, "Weird action reponded to by bcast-receiver");
			return;
		}

		if (track == null) {
			Log.e(TAG, "Somehow track was null");
			return;
		}

		AppTransaction.pushTrack(track);
		context.startService(service);
	}

}
