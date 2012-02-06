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

package com.adam.aslfms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.Util;

public class AboutDialog {
	@SuppressWarnings("unused")
	private static final String TAG = "AboutDialog";
	private final Context mCtx;

	public AboutDialog(Context ctx) {
		super();
		this.mCtx = ctx;
	}

	public void show() {
		final LayoutInflater factory = LayoutInflater.from(mCtx);

		View dialogView = factory.inflate(R.layout.about, null);

		innerUpdate(dialogView);

		AlertDialog.Builder adBuilder = new AlertDialog.Builder(mCtx).setTitle(
				R.string.about).setIcon(android.R.drawable.ic_dialog_info)
				.setView(dialogView).setPositiveButton(R.string.whats_new,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								new WhatsNewDialog(mCtx).show();
							}
						}).setNegativeButton(R.string.close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});

		adBuilder.show();
	}

	private void innerUpdate(View dialogView) {
		TextView appName = (TextView) dialogView.findViewById(R.id.app_name);
		TextView author = (TextView) dialogView.findViewById(R.id.author);
		TextView graphics_byline = (TextView) dialogView
				.findViewById(R.id.graphics_byline);
		TextView license = (TextView) dialogView.findViewById(R.id.license);
		TextView whatIsThis = (TextView) dialogView
				.findViewById(R.id.what_is_this);
		TextView netApps = (TextView) dialogView
				.findViewById(R.id.supported_netapps);
		TextView musicApps = (TextView) dialogView
				.findViewById(R.id.supported_musicapps);
		TextView website = (TextView) dialogView.findViewById(R.id.website);
		TextView email = (TextView) dialogView.findViewById(R.id.email);

		// app name & version
		String appText = Util.getAppName(mCtx, mCtx.getPackageName()) + " v"
				+ Util.getAppVersionName(mCtx, mCtx.getPackageName());
		appName.setText(appText);

		// author
		author.setText(R.string.by_adam);

		// graphics, Tha PHLASH
		graphics_byline.setText(R.string.by_tha_phlash);

		// license
		license.setText(R.string.license);

		// text
		whatIsThis.setText(R.string.about_text);

		// supported net apps
		StringBuilder sb = new StringBuilder();
		for (NetApp napp : NetApp.values()) {
			sb.append(napp.getName());
			sb.append(", ");
		}
		sb.setLength(sb.length() - 2);
		netApps.setText(mCtx.getString(R.string.supported_netapps).replace(
				"%1", sb.toString()));

		// supported music apps
		musicApps.setText(R.string.supported_musicapps);

		// website
		website.setText(mCtx.getString(R.string.website) + " "
				+ mCtx.getString(R.string.website_url));

		// email
		email.setText(mCtx.getString(R.string.contact) + " "
				+ mCtx.getString(R.string.email_addr));
	}

}
