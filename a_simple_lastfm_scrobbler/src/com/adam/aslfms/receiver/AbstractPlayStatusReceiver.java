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

import com.adam.aslfms.AppSettings;
import com.adam.aslfms.InternalTrackTransmitter;
import com.adam.aslfms.Track;
import com.adam.aslfms.service.ScrobblingService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Base class for play status receivers.
 * @author tgwizard
 *
 */
public abstract class AbstractPlayStatusReceiver extends BroadcastReceiver {

	private static final String TAG = "PlayStatusReceiver";
	
	/*public static final String ACTION_HTC_PLAYSTATECHANGED = "com.htc.music.playstatechanged";
	public static final String ACTION_HTC_STOP = "com.htc.music.playbackcomplete";
	public static final String ACTION_HTC_METACHANGED = "com.htc.music.metachanged";*/
	
	private MusicApp mApp;
	
	private Intent mService = null;
	private Track mTrack = null;
	
	

	public AbstractPlayStatusReceiver(MusicApp app) {
		super();
		this.mApp = app;
	}
	
	public MusicApp getApp() {
		return mApp;
	}

	@Override
	public final void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle bundle = intent.getExtras();

		if (action == null || bundle == null) {
			Log.e(TAG, "Got null action or null bundle");
			return;
		}
		
		AppSettings settings = new AppSettings(context);
		if (!settings.isAuthenticated()) {
			Log.i(TAG, "The user has not authenticated, won't propagate the scrobble/np-notification request");
			return;
		}
		
		if (!settings.isAppEnabled(mApp)) {
			Log.i(TAG, "App: " + mApp.getName() + " has been disabled, won't propagate");
			return;
		}
		
		Log.d(TAG, "Action received was: " + action);
		
		mService = new Intent(ScrobblingService.ACTION_PLAYSTATECHANGED);
		
		parseIntent(action, bundle);
		
		if (mTrack == null) {
			Log.e(TAG, "Somehow mTrack was null");
			return;
		}
		
		InternalTrackTransmitter.appendTrack(mTrack);
		context.startService(mService);
	}
	
	protected final void setStopped(boolean stopped) {
		mService.putExtra("stopped", stopped);
	}
	
	protected final void setTrack(Track track) {
		this.mTrack = track;
	}
	
	protected abstract void parseIntent(String action, Bundle bundle);

}
