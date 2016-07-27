/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p/>
 * Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.Networker;
import com.adam.aslfms.service.NetworkerManager;
import com.adam.aslfms.util.enums.AdvancedOptionsWhen;
import com.adam.aslfms.util.enums.PowerOptions;

import java.util.Iterator;
import java.util.Set;

/**
 * Receiver for listening to connectivity changes. Currently attempts to
 * scrobble tracks when Wifi connectivity becomes available.
 *
 * @author Malachi Soord
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {


    static String TAG = "CCR";

   /** public static void dumpIntent(Bundle bundle) {
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.e(TAG, "Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                Log.e(TAG, "[" + key + "=" + bundle.get(key) + "]");
            }
            Log.e(TAG, "Dumping Intent end");
        }
    }*/


    @Override
    public void onReceive(final Context context, final Intent intent) {

       // Log.d(TAG, "ACTION: " + intent.getAction());
       // dumpIntent(intent.getExtras());


        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            final NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                ScrobblesDatabase db = new ScrobblesDatabase(context);
                final int numInCache = db.queryNumberOfTracks();
                PowerOptions pow = Util.checkPower(context);
                AppSettings settings = new AppSettings(context);
                AdvancedOptionsWhen aow = settings.getAdvancedOptionsWhen(pow);

                NetworkerManager mNetManager = new NetworkerManager(context, db);

                for (NetApp napp : NetApp.values()) {
                    if (numInCache >= aow.getTracksToWaitFor()) {
                        mNetManager.launchScrobbler(napp);
                    }
                }
            }
        }
    }
}