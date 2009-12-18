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

package com.adam.aslfms.receiver;

import java.util.HashMap;
import java.util.Map;

public enum MusicApp {
	ANDROID_MUSIC(0x01, "Android Music Player", "com.android.music", null, true), //
	HERO_MUSIC(0x02, "Hero Music Player", "com.htc.music", null, true), //
	SCROBBLE_DROID_SUPPORTED_APPS(0x99, "\"Scrobble Droid Apps\"", null,
			"Apps supported by Scrobble Droid", true);

	private final int val;
	private final String name;
	private final String pkg;
	private final String msg;
	private final boolean clashWithScrobbleDroid;

	private MusicApp(int val, String name, String pkg, String msg,
			boolean clashWithScrobbleDroid) {
		this.val = val;
		this.name = name;
		this.pkg = pkg;
		this.msg = msg;
		this.clashWithScrobbleDroid = clashWithScrobbleDroid;
	}

	public int getValue() {
		return this.val;
	}

	public String getName() {
		return this.name;
	}

	public String getPackage() {
		return this.pkg;
	}

	public String getMsg() {
		return msg;
	}

	public boolean clashesWithScrobbleDroid() {
		return clashWithScrobbleDroid;
	}
	
	private static Map<Integer, MusicApp> mValMusicAppMap;

	static {
		mValMusicAppMap = new HashMap<Integer, MusicApp>();
		for (MusicApp app : MusicApp.values())
			mValMusicAppMap.put(app.getValue(), app);
	}

	public static MusicApp fromValue(int value) {
		MusicApp app = mValMusicAppMap.get(value);
		if (app == null) {
			throw new IllegalArgumentException("Got null musicapp in fromValue: " + value);
		}
		return app;
	}
}
