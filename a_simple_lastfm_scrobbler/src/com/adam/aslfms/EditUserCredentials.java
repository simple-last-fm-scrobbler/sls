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

public class EditUserCredentials extends DialogPreference {

	private static final String TAG = "EditUserCredentials";

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
