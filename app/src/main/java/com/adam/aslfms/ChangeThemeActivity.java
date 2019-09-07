/**
 * This file is part of Simple Scrobbler.
 * <p>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p>
 * Copyright 2011 Simple Scrobbler Team
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

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.adam.aslfms.util.AppSettings;
/**
 * @author a93h
 * @since 1.5.8
 */
public class ChangeThemeActivity extends AppCompatActivity {
    private static final String TAG = "ChangeThemeActivity";
    private AppSettings settings;

    @Override
    public Resources.Theme getTheme() {
        settings = new AppSettings(this);
        Resources.Theme theme = super.getTheme();
        theme.applyStyle(settings.getAppTheme(), true);
        Log.d(TAG, getResources().getResourceName(settings.getAppTheme()));
        // you could also use a switch if you have many themes that could apply
        return theme;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme_options);
        settings = new AppSettings(this);
        setTheme(settings.getAppTheme());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onRadioButtonClicked(View view){
        switch (view.getId()) {
            case R.id.default_theme:
                settings.setAppTheme(R.style.AppTheme);
                break;
            case R.id.default_theme_dark:
                settings.setAppTheme(R.style.AppTheme_Dark);
                break;
            case R.id.lastfm_theme:
                settings.setAppTheme(R.style.AppThemeLastFm);
                break;
            case R.id.lastfm_theme_dark:
                settings.setAppTheme(R.style.AppThemeLastFmDark);
                break;
            case R.id.librefm_theme:
                settings.setAppTheme(R.style.AppThemeLibreFm);
                break;
            case R.id.librefm_theme_dark:
                settings.setAppTheme(R.style.AppThemeLibreFmDark);
                break;
            case R.id.listenbrainz_theme:
                settings.setAppTheme(R.style.AppThemeListenBrainz);
                break;
            case R.id.listenbrainz_theme_dark:
                settings.setAppTheme(R.style.AppThemeListenBrainzDark);
                break;
            default:
                settings.setAppTheme(R.style.AppTheme);
        }
        setTheme(settings.getAppTheme());
        Log.d(TAG, view.getResources().getResourceName(settings.getAppTheme()));
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
