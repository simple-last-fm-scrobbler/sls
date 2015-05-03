/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adam.aslfms.receiver;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

/**
 * A BroadcastReceiver for intents sent by the SoundCloud application
 *
 * @see AbstractPlayStatusReceiver
 *
 * @author Malachi Soord <inverse.chi@gmail.com>
 * @since 1.4.8
 */
public class SoundCloudReceiver extends AbstractPlayStatusReceiver {

	// TODO: Get intents with full Strings.
	// current limit is about 40 characters long.

	static final String APP_PACKAGE = "com.soundcloud.android";
	static final String APP_NAME = "SoundCloud";

	public static final String ACTION_ANDROID_PLAYSTATECHANGED = "com.android.music.playstatechanged";
	public static final String ACTION_ANDROID_METACHANGED = "com.android.music.metachanged";

	static final String TAG = "SoundCloud";

	@Override
	protected void parseIntent(Context ctx, String action, Bundle bundle) {

		MusicAPI musicAPI = MusicAPI.fromReceiver(ctx, APP_NAME, APP_PACKAGE,
				null, false);
		setMusicAPI(musicAPI);

		if (action.equals(ACTION_ANDROID_METACHANGED)) {
			setState(Track.State.CHANGED);
			Track.Builder b = new Track.Builder();
			b.setMusicAPI(musicAPI);
			b.setWhen(Util.currentTimeSecsUTC());

			// TODO: Work out correct parsing of artist/track if we get intents
			// with full SoundCloud Strings

			b.setArtist(bundle.getString("artist"));
			b.setTrack(bundle.getString("track"));
			b.setDuration((bundle.getInt("duration") / 1000));
			Log.e(TAG, "Track length: " + String.valueOf(bundle.getInt("duration") / 1000));
			setTrack(b.build());

		} else if (action.equals(ACTION_ANDROID_PLAYSTATECHANGED)) {
			boolean playing = bundle.getBoolean("playing", false);
			if (playing) {
				setState(Track.State.RESUME);
				Log.e(TAG, "Setting state to RESUME");
			} else if (!playing) {
				setState(Track.State.PAUSE);
				Log.e(TAG, "Setting state to PAUSE");
			}
		}
	}
}
