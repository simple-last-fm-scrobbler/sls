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

import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.MD5;

/**
 * 
 * @author tgwizard
 * 
 */
public class EditUserCredentials extends DialogPreference {

	// private static final String TAG = "EditUserCredentials";

	private EditText mUsername;
	private EditText mPassword;

	private AppSettings settings;
	private NetApp mNetApp;

	public EditUserCredentials(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.edit_user_credentials);

		settings = new AppSettings(context);
	}

	public void setNetApp(NetApp napp) {
		this.mNetApp = napp;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		mUsername = (EditText) view.findViewById(R.id.username);
		mPassword = (EditText) view.findViewById(R.id.password);
		mUsername.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO: disable the auth button if username is empty
			}
		});

		mUsername.setText(settings.getUsername(mNetApp));
		mPassword.setText(settings.getPassword(mNetApp));

	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			Intent service = new Intent(getContext(), ScrobblingService.class);
			service.setAction(ScrobblingService.ACTION_AUTHENTICATE);
			service.putExtra("netapp", mNetApp.getIntentExtraValue());

			String username = mUsername.getText().toString().trim();
			settings.setUsername(mNetApp, username);
			
			String password = mPassword.getText().toString();
			// Here we save the plain-text password temporarily. When the
			// authentication request succeeds, it is removed by
			// Handshaker.run()
			settings.setPassword(mNetApp, password);
			settings.setPwdMd5(mNetApp, MD5.getHashString(password));

			getContext().startService(service);
		}
	}

}
