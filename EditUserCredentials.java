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

import com.adam.aslfms.service.ScrobblingService;
import com.adam.aslfms.util.MD5;

import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

/**
 * 
 * @author tgwizard
 *
 */
public class EditUserCredentials extends DialogPreference {

	//private static final String TAG = "EditUserCredentials";

	private EditText mUsername;
	private EditText mPassword;

	private AppSettings settings;

	public EditUserCredentials(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.edit_user_credentials);

		settings = new AppSettings(context);
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
				// String username = s.toString().replaceAll("\\s", "");
				// The Button.setEnabled(username.length() != 0);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		mUsername.setText(settings.getUsername());
		mPassword.setText(settings.getPassword());

	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			Intent service = new Intent(ScrobblingService.ACTION_AUTHENTICATE);
			String username = mUsername.getText().toString();
			username = username.replaceAll("\\s", "");
			settings.setUsername(username);
			String password = mPassword.getText().toString();
			settings.setPassword(password);
			settings.setPwdMd5(MD5.getHashString(password));
			getContext().startService(service);
		} else {

		}
	}

}
