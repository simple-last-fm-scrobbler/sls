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

package com.adam.aslfms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.SQLException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.adam.aslfms.util.Util;

public class StatusInfoDialog {

	public static final String PACKAGE_SCROBBLE_DROID = "net.jjc1138.android.scrobbler";

	private static final String TAG = "StatusInfoDialog";
	private final Context mCtx;
	private final AppSettings settings;

	private AlertDialog mDialog = null;
	private View mDialogView = null;

	private ScrobblesDatabase mDbHelper;

	public StatusInfoDialog(Context ctx) {
		super();
		this.mCtx = ctx;
		this.settings = new AppSettings(ctx);
		
		mDbHelper = new ScrobblesDatabase(ctx);
		try {
			mDbHelper.open();
		} catch (SQLException e) {
			Log.e(TAG, "Cannot open database!");
			Log.e(TAG, e.getMessage());
			mDbHelper = null;
		}
	}

	private void clearStuff() {
		this.mDialog = null;
		this.mDialogView = null;
	}

	public void showDialog() {
		final LayoutInflater factory = LayoutInflater.from(mCtx);

		mDialogView = factory.inflate(R.layout.status_info, null);

		innerUpdate();

		AlertDialog.Builder adBuilder = new AlertDialog.Builder(mCtx).setTitle(
				R.string.status_title).setIcon(
				android.R.drawable.ic_dialog_info).setView(mDialogView)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								StatusInfoDialog.this.clearStuff();
							}
						}).setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						StatusInfoDialog.this.clearStuff();
					}
				});

		mDialog = adBuilder.show();
	}

	public void updateDialog() {
		if (mDialog == null) {
			return;
		}
		innerUpdate();
	}

	private void innerUpdate() {
		TextView authText = (TextView) mDialogView
				.findViewById(R.id.status_auth);
		TextView scrobbleText = (TextView) mDialogView
				.findViewById(R.id.status_scrobbling);
		TextView npText = (TextView) mDialogView.findViewById(R.id.status_np);
		TextView cacheText = (TextView) mDialogView.findViewById(R.id.scrobble_cache);
		TextView scrobbleStatsText = (TextView) mDialogView
				.findViewById(R.id.status_scrobble_stats);
		TextView npStatsText = (TextView) mDialogView
				.findViewById(R.id.status_np_stats);
		TextView incompText = (TextView) mDialogView
				.findViewById(R.id.status_incomp_warning);
		
		// authText
		if (settings.getAuthStatus() == Status.AUTHSTATUS_BADAUTH) {
			authText.setText(R.string.auth_bad_auth);
		} else if (settings.getAuthStatus() == Status.AUTHSTATUS_FAILED) {
			authText.setText(R.string.auth_internal_error);
		} else if (settings.getAuthStatus() == Status.AUTHSTATUS_RETRYLATER) {
			authText.setText(R.string.auth_network_error);
		} else if (settings.getAuthStatus() == Status.AUTHSTATUS_OK) {
			authText.setText(mCtx.getString(R.string.logged_in_as) + " "
					+ settings.getUsername());
		} else if (settings.getAuthStatus() == Status.AUTHSTATUS_UPDATING) {
			authText.setText(R.string.auth_updating);
		} else if (settings.getAuthStatus() == Status.AUTHSTATUS_CLIENTBANNED) {
			authText.setText(R.string.auth_client_banned);
		} else { // Status.AUTHSTATUS_NOAUTH
			authText.setText(R.string.user_credentials_summary);
		}

		// scrobbleText
		if (!settings.isScrobblingEnabled()) {
			scrobbleText.setText(R.string.scrobbling_disabled);
		} else {
			long time = settings.getLastScrobbleTime();
			String when;
			String what;
			if (time == -1) {
				when = mCtx.getString(R.string.never);
				what = "";
			} else {
				when = Util.timeFromLocalMillis(mCtx, time);
				what = "\n" + settings.getLastScrobbleInfo();
			}
			scrobbleText.setText(mCtx.getString(R.string.scrobble_last_at)
					+ " " + when + what);
		}

		// npText
		if (!settings.isNowPlayingEnabled()) {
			npText.setText(R.string.nowplaying_disabled);
		} else {
			long time = settings.getLastNPTime();
			String when;
			String what;
			if (time == -1) {
				when = mCtx.getString(R.string.never);
				what = "";
			} else {
				when = Util.timeFromLocalMillis(mCtx, time);
				what = "\n" + settings.getLastNPInfo();
			}
			npText.setText(mCtx.getString(R.string.nowplaying_last_at) + " "
					+ when + what);
		}
		
		// scrobbles in cache
		if (mDbHelper != null) {
			cacheText.setText(mCtx.getString(R.string.scrobbles_cache) + " " + mDbHelper.queryNumberOfRows());
		} else {
			cacheText.setText(mCtx.getString(R.string.scrobbles_cache) + " " + mCtx.getString(R.string.db_error));
		}

		// statsText
		scrobbleStatsText.setText(mCtx.getString(R.string.stats_scrobbles)
				+ " " + settings.getNumberOfScrobbles());
		npStatsText.setText(mCtx.getString(R.string.stats_nps) + " "
				+ settings.getNumberOfNPs());
		
		// check for "incompatible" packages
		String incomp = null;
		// check for scrobbledroid
		if (Util.checkForInstalledApp(mCtx, PACKAGE_SCROBBLE_DROID)) {
			incomp = mCtx.getString(R.string.incompatability).replaceAll("%1", "ScrobbleDroid");
		}
			
		
		if (incomp == null) {
			incompText.setVisibility(View.GONE);
		} else {
			incompText.setText(incomp);
		}
	}
}
