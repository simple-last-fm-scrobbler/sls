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

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.Util;
/**
 * @author a93h
 * @since 1.5.8
 */
public class PermissionsActivity extends AppCompatActivity {

    private static final String TAG = "PermissionsActivity";

    int WRITE_EXTERNAL_STORAGE;
    int REQUEST_READ_STORAGE;
    int REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
    int disabledColor = Color.argb(25, 0,0,0);
    int enabledColor = Color.argb(75, 0,255,0);
    int warningColor = Color.argb(80,255,0,0);

    Button btnContinue = null;

    @Override
    public Resources.Theme getTheme() {
        AppSettings settings = new AppSettings(this);
        Resources.Theme theme = super.getTheme();
        theme.applyStyle(settings.getAppTheme(), true);
        Log.d(TAG, getResources().getResourceName(settings.getAppTheme()));
        // you could also use a switch if you have many themes that could apply
        return theme;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkCurrrentPermissions();
        permsCheck();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        AppSettings settings = new AppSettings(this);
        setTheme(settings.getAppTheme());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        checkCurrrentPermissions();
        permsCheck();
    }

    public void checkCurrrentPermissions(){
        boolean enabled;
        btnContinue = findViewById(R.id.button_continue);
        Button externalPermBtn = findViewById(R.id.button_permission_external_storage);
        Button notifiPermBtn = findViewById(R.id.button_permission_notification_listener);
        Button batteryPermBtn = findViewById(R.id.button_permission_battery_optimizations);
        Button batteryBasicPermBtn = findViewById(R.id.button_permission_battery_basic);

        TextView findBattery = findViewById(R.id.text_find_battery_optimization_setting);
        TextView findNotify = findViewById(R.id.text_find_notification_setting);

        enabled = Util.checkExternalPermission(this);
        colorPermission(enabled, externalPermBtn);

        enabled = Util.checkNotificationListenerPermission(this);
        colorPermission(enabled, notifiPermBtn);

        enabled = Util.checkBatteryOptimizationsPermission(this);
        colorPermission(enabled, batteryPermBtn);

        enabled = Util.checkBatteryOptimizationBasicPermission(this);
        colorPermission(enabled, batteryBasicPermBtn);

        externalPermBtn.setOnClickListener((View view) -> {
            if (!Util.checkExternalPermission(this)) {
                try {
                    ActivityCompat.requestPermissions(PermissionsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
                } catch (Exception e) {
                    Log.e(TAG,e.toString());
                }
            } else {
                colorPermission(true, externalPermBtn);
            }
        });

        notifiPermBtn.setOnClickListener((View view) -> {
            if (!Util.checkNotificationListenerPermission(this)) {
                try {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(intent);
                } catch (Exception e) {
                    findNotify.setTextColor(warningColor);
                    findNotify.setText(R.string.find_notifications_settings);
                    Log.e(TAG,e.toString());
                }
            } else {
                colorPermission(true, notifiPermBtn);
            }
        });

        batteryPermBtn.setOnClickListener((View view) -> {
            if (!Util.checkBatteryOptimizationsPermission(this)) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    findBattery.setTextColor(warningColor);
                    findBattery.setText(R.string.find_battery_settings);
                    Log.e(TAG,e.toString());
                }
            } else {
                colorPermission(true, batteryPermBtn);
            }
        });

        batteryBasicPermBtn.setOnClickListener((View view) -> {
            if (!Util.checkBatteryOptimizationBasicPermission(this)) {
                try {
                    ActivityCompat.requestPermissions(PermissionsActivity.this, new String[]{Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS}, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                } catch (Exception e) {
                    Log.e(TAG,e.toString());
                }
            } else {
                colorPermission(true, batteryBasicPermBtn);
            }
        });
    }

    public void colorPermission(boolean enabled, Button button){
        if (enabled){
            button.setBackgroundColor(enabledColor);
            return;
        }
        button.setBackgroundColor(disabledColor);
    }

    private void permsCheck() {
        //PERMISSION CHECK
        boolean allPermissionsGo = true;
        allPermissionsGo = allPermissionsGo && Util.checkNotificationListenerPermission(this);
        allPermissionsGo = allPermissionsGo && Util.checkExternalPermission(this);
        allPermissionsGo = allPermissionsGo && Util.checkBatteryOptimizationsPermission(this);
        allPermissionsGo = allPermissionsGo && Util.checkBatteryOptimizationBasicPermission(this);
        Log.d(TAG,"All Permissions Go: " + allPermissionsGo);
        if (allPermissionsGo) {
            btnContinue.setBackgroundColor(enabledColor);
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity(intent);
        } else {
            btnContinue.setBackgroundColor(disabledColor);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            permsCheck();
        } catch (Exception e) {
            Log.e(TAG, "READ_EXTERNAL_STORAGE. " + e);
        }
    }
}
