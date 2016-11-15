/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/tgwizard/sls
 * <p>
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
