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

public enum MusicApp {
	ANDROID_MUSIC(0x01, "Android Music Player", "com.android.music", true), //
	HERO_MUSIC(0x02, "Hero Music Player", "com.htc.music", true);

	private final int val;
	private final String name;
	private final String pkg;
	private final boolean clashWithScrobbleDroid;

	private MusicApp(int val, String name, String pkg,
			boolean clashWithScrobbleDroid) {
		this.val = val;
		this.name = name;
		this.pkg = pkg;
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

	public boolean clashesWithScrobbleDroid() {
		return clashWithScrobbleDroid;
	}
}
