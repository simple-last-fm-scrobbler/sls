/**
 *  This file is part of Simple Last.fm Scrobbler.
 *
 *  Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms.service;

import java.util.ArrayList;
import java.util.Comparator;

public class NetRunnableComparator implements Comparator<Runnable> {

	private ArrayList<Class<? extends NetRunnable>> mPriorityList = new ArrayList<Class<? extends NetRunnable>>(
			5);

	public NetRunnableComparator() {
		// in order of priority, from highest to lowest
		mPriorityList.add(NetworkWaiter.class);
		mPriorityList.add(Sleeper.class);
		mPriorityList.add(Handshaker.class);
		mPriorityList.add(Scrobbler.class);
		mPriorityList.add(NPNotifier.class);
	}

	@Override
	public int compare(Runnable a, Runnable b) {
		int ap = mPriorityList.indexOf(a.getClass());
		int bp = mPriorityList.indexOf(b.getClass());

		if (ap < bp)
			return -1;
		else if (ap == bp)
			return 0;
		else
			return 1;
	}
}
