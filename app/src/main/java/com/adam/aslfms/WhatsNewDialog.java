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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class WhatsNewDialog {
	private static final String TAG = "WhatsNewDialog";
	private final Context mCtx;

	public WhatsNewDialog(Context ctx) {
		super();
		this.mCtx = ctx;
	}

	public void show() {
		final LayoutInflater factory = LayoutInflater.from(mCtx);

		View dialogView = factory.inflate(R.layout.whats_new, null);

		innerUpdate(dialogView);

		AlertDialog.Builder adBuilder = new AlertDialog.Builder(mCtx).setTitle(
                R.string.whats_new).setIcon(android.R.drawable.ic_dialog_info)
				.setView(dialogView).setNegativeButton(R.string.close, null);

		adBuilder.show();
	}

	private void innerUpdate(View dialogView) {
		TextView tv = (TextView) dialogView.findViewById(R.id.changelog);

		String text = "";
		try {
			InputStream is = mCtx.getAssets().open("changelog.txt");
			BufferedReader buffy = new BufferedReader(new InputStreamReader(is));
			String s;
			while ((s = buffy.readLine()) != null)
				text += s + "\n";
		} catch (IOException e) {
			Log.e(TAG, "Couldn't read changelog file!");
			Log.e(TAG, e.getMessage());
			text = mCtx.getString(R.string.file_error);
		}

		tv.setText(text);
	}
}
