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

package com.adam.aslfms;

import com.adam.aslfms.service.ScrobblingService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author tgwizard
 * 
 */
public class PlayStatusReceiver extends BroadcastReceiver {

	private static final String TAG = "PlayStatusReceiver";

	private static final String ACTION_PLAYSTATECHANGED_ANDROID = "com.android.music.playstatechanged";
	private static final String ACTION_STOP_ANDROID = "com.android.music.playbackcomplete";
	private static final String ACTION_METACHANGED_ANDROID = "com.android.music.metachanged";

	private static final String ACTION_PLAYSTATECHANGED_HTC = "com.htc.music.playstatechanged";
	private static final String ACTION_STOP_HTC = "com.htc.music.playbackcomplete";
	private static final String ACTION_METACHANGED_HTC = "com.htc.music.metachanged";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle bundle = intent.getExtras();

		if (action == null || bundle == null) {
			Log.e(TAG, "Got null action or null bundle");
			return;
		}

		Log.d(TAG, "Action received was: " + action);

		Intent service = new Intent(ScrobblingService.ACTION_PLAYSTATECHANGED);
		Track track = null;
		if (action.equals(ACTION_PLAYSTATECHANGED_ANDROID)
				|| action.equals(ACTION_STOP_ANDROID)
				|| action.equals(ACTION_METACHANGED_ANDROID)
				|| action.equals(ACTION_PLAYSTATECHANGED_HTC)
				|| action.equals(ACTION_STOP_HTC)
				|| action.equals(ACTION_METACHANGED_HTC)) {
			CharSequence ar = bundle.getCharSequence("artist");
			CharSequence al = bundle.getCharSequence("album");
			CharSequence tr = bundle.getCharSequence("track");

			// As of cupcake, it is not possible (feasible) to get the actual
			// duration of the playing track, so I default it to three minutes
			track = new Track(ar, al, tr, 180, AppTransaction.currentTimeUTC());

			if (action.equals(ACTION_STOP_ANDROID)
					|| action.equals(ACTION_STOP_HTC)) {
				service.putExtra("stopped", true);
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
