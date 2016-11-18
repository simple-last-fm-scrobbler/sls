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
    private EditText mNixtapeUrl;
    private EditText mGnukeboxUrl;
    private EditText mListenBrainzToken;

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
        if (mNetApp == NetApp.LISTENBRAINZ) {
            mUsername = (EditText) view.findViewById(R.id.username);
            mListenBrainzToken = (EditText) view.findViewById(R.id.listenBrainzToken);
            view.findViewById(R.id.listenBrainz).setVisibility(View.VISIBLE);
            view.findViewById(R.id.userOnly).setVisibility(View.VISIBLE);
        } else {
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

            view.findViewById(R.id.userOnly).setVisibility(View.VISIBLE);
            view.findViewById(R.id.pwdOnly).setVisibility(View.VISIBLE);
            if (mNetApp == NetApp.CUSTOM) {
                mNixtapeUrl = (EditText) view.findViewById(R.id.nixtapeUrl);
                mGnukeboxUrl = (EditText) view.findViewById(R.id.gnukeboxUrl);
                mNixtapeUrl.setText(settings.getNixtapeUrl(mNetApp));
                mGnukeboxUrl.setText(settings.getGnukeboxUrl(mNetApp));

                view.findViewById(R.id.customSettings).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            Intent service = new Intent(getContext(), ScrobblingService.class);
            service.setAction(ScrobblingService.ACTION_AUTHENTICATE);
            service.putExtra("netapp", mNetApp.getIntentExtraValue());
            if (mNetApp == NetApp.LISTENBRAINZ) {
                String token = mListenBrainzToken.getText().toString().replaceAll("[^\\x20-\\x7E]", "").trim();
                settings.setListenBrainzToken(mNetApp, token);
                String username = mUsername.getText().toString().trim();
                settings.setUsername(mNetApp, username);
            } else {
                String username = mUsername.getText().toString().trim();
                settings.setUsername(mNetApp, username);

                String password = mPassword.getText().toString();
                // Here we save the plain-text password temporarily. When the
                // authentication request succeeds, it is removed by
                // Handshaker.run()
                settings.setSessionKey(mNetApp, "");
                settings.setPassword(mNetApp, password);
                settings.setPwdMd5(mNetApp, MD5.getHashString(password));

                if (mNetApp == NetApp.CUSTOM) {
                    String nixtapeUrl = mNixtapeUrl.getText().toString().trim();
                    settings.setNixtapeUrl(mNetApp, nixtapeUrl);
                    String gnukeboxUrl = mGnukeboxUrl.getText().toString().trim();
                    settings.setGnukeboxUrl(mNetApp, gnukeboxUrl);
                }
            }
            getContext().startService(service);
        }
    }

}
