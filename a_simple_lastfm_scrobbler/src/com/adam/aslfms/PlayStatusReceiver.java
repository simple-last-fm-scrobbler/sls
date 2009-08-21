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
 * PlayStatusReceiver listens to broadcasts sent by the android and htc music
 * players, and other music players through aslfms actions. When a broadcast has
 * been received, it is translated into a Track, and sent to ScrobblingService.
 * 
 * This is the only class that external applications should care about. See the
 * link to some example code below for how to use this class.
 * 
 * @see ScrobblingService
 * @see <a
 *      href="http://code.google.com/p/a-simple-lastfm-scrobbler/wiki/Developers">Example
 *      code</a>
 * @author tgwizard
 * 
 */
public class PlayStatusReceiver extends BroadcastReceiver {

	private static final String TAG = "PlayStatusReceiver";

	public static final String ACTION_ASLFMS_PLAYSTATECHANGED = "com.adam.aslfms.notify.playstatechanged";
	public static final String ACTION_ASLFMS_PLAYSTATECOMPLETE = "com.adam.aslfms.notify.playbackcomplete";

	public static final String ACTION_ANDROID_PLAYSTATECHANGED = "com.android.music.playstatechanged";
	public static final String ACTION_ANDROID_STOP = "com.android.music.playbackcomplete";
	public static final String ACTION_ANDROID_METACHANGED = "com.android.music.metachanged";

	public static final String ACTION_HTC_PLAYSTATECHANGED = "com.htc.music.playstatechanged";
	public static final String ACTION_HTC_STOP = "com.htc.music.playbackcomplete";
	public static final String ACTION_HTC_METACHANGED = "com.htc.music.metachanged";

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
		if (action.equals(ACTION_ANDROID_PLAYSTATECHANGED)
				|| action.equals(ACTION_ANDROID_STOP)
				|| action.equals(ACTION_ANDROID_METACHANGED)
				|| action.equals(ACTION_HTC_PLAYSTATECHANGED)
				|| action.equals(ACTION_HTC_STOP)
				|| action.equals(ACTION_HTC_METACHANGED)) {
			CharSequence ar = bundle.getCharSequence("artist");
			CharSequence al = bundle.getCharSequence("album");
			CharSequence tr = bundle.getCharSequence("track");

			// As of cupcake, it is not possible (feasible) to get the actual
			// duration of the playing track, so I default it to three minutes
			track = Track.createTrack(ar, al, tr, Track.DEFAULT_TRACK_LENGTH,
					InternalTrackTransmitter.currentTimeUTC());

			if (action.equals(ACTION_ANDROID_STOP)
					|| action.equals(ACTION_HTC_STOP)) {
				service.putExtra("stopped", true);
			}
		} else if (action.equals(ACTION_ASLFMS_PLAYSTATECHANGED)
				|| action.equals(ACTION_ASLFMS_PLAYSTATECOMPLETE)) {
			CharSequence ar = bundle.getCharSequence("artist");
			CharSequence al = bundle.getCharSequence("album");
			CharSequence tr = bundle.getCharSequence("track");
			int dur = bundle.getInt("duration");
			if (ar == null) {
				Log.e(TAG, "ASLFMS.notify: artist was null");
				return;
			}
			if (al == null) {
				Log.e(TAG, "ASLFMS.notify: album was null");
				return;
			}
			if (tr == null) {
				Log.e(TAG, "ASLFMS.notify: track was null");
				return;
			}
			if (dur < 0) {
				dur = Track.DEFAULT_TRACK_LENGTH;
			}

			track = Track.createTrack(ar, al, tr, dur, InternalTrackTransmitter
					.currentTimeUTC());

			if (action.equals(ACTION_ASLFMS_PLAYSTATECOMPLETE)) {
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

		InternalTrackTransmitter.appendTrack(track);
		context.startService(service);
	}

}
