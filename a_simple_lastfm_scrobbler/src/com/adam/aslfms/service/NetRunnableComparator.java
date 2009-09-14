package com.adam.aslfms.service;

import java.util.ArrayList;
import java.util.Comparator;

public class NetRunnableComparator implements Comparator<Runnable> {

	private ArrayList<Class<? extends NetRunnable>> mPriorityList = new ArrayList<Class<? extends NetRunnable>>(
			5);

	public NetRunnableComparator() {
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
