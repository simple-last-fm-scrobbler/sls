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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.adam.aslfms.util.enums.NetworkOptions;
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

    public static boolean isConnect;
/**
    public static void dumpIntent(Bundle bundle) {
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
    }
*/
    @Override
    public void onReceive(final Context context, final Intent intent) {

        Log.e(TAG, "ACTION: " + intent.getAction());
        //dumpIntent(intent.getExtras());
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        AppSettings settings = new AppSettings(context);
        PowerOptions pow = Util.checkPower(context);
        if (activeNetwork != null) {
            isConnect = activeNetwork.isConnected();
/**
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                Toast.makeText(context, activeNetwork.getTypeName() + isConnect, Toast.LENGTH_SHORT).show();
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                Toast.makeText(context, activeNetwork.getTypeName() + isConnect, Toast.LENGTH_SHORT).show();
            }
*/

            boolean roaming = settings.getSubmitOnRoaming(pow);

            if (!roaming && activeNetwork.isRoaming()) { // check for roaming disabled
                return;
            }
            // check for unacceptable network
            NetworkOptions networkOptions = settings.getNetworkOptions(pow);

            int netType = activeNetwork.getType();
            int netSubType = activeNetwork.getSubtype();

            Log.d(TAG, "netType: " + netType);
            Log.d(TAG, "netSubType: " + netSubType);

            if (networkOptions.isNetworkTypeForbidden(netType)) {
                Log.d(TAG, "Network type forbidden");
                return;
            }
            if (networkOptions.isNetworkSubTypeForbidden(netType, netSubType)) {
                Log.d(TAG, "Network sub type forbidden");
                return;
            }

        } else {
            isConnect = false;
           // Toast.makeText(context, " all " + isConnect, Toast.LENGTH_SHORT).show();
            return;
        }

    }
}