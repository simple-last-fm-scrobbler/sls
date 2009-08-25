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
	ANDROID_MUSIC(0x01, "Android Music Player"),
	HERO_MUSIC(0x02, "Hero Music Player");

	private final int val;
	private final String name;

	MusicApp(int v, String n) {
		this.val = v;
		this.name = n;
	}

	public int getValue() {
		return this.val;
	}

	public String getName() {
		return this.name;
	}
}
