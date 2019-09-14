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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.InternalTrackTransmitter;
import com.adam.aslfms.util.MyContextWrapper;
import com.adam.aslfms.util.Util;
/**
 * @author a93h
 * @since 1.5.8
 */
public class PermissionsActivity extends AppCompatActivity {

    private static final String TAG = "PermissionsActivity";

    int WRITE_EXTERNAL_STORAGE;
    int disabledColor = Color.argb(25, 0,0,0);
    int enabledColor = Color.argb(75, 0,255,0);
    int warningColor = Color.argb(80,255,0,0);

    AppSettings settings = null;
    Button skipBtn = null;
    Button continueBtn = null;
    Button externalPermBtn = null;
    Button notifiPermBtn = null;
    Button batteryPermBtn = null;
    Context ctx = this;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase));
    }

    @Override
    public Resources.Theme getTheme() {
        settings = new AppSettings(this);
        Resources.Theme theme = super.getTheme();
        theme.applyStyle(settings.getAppTheme(), true);
        //Log.d(TAG, getResources().getResourceName(settings.getAppTheme()));
        // you could also use a switch if you have many themes that could apply
        return theme;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndSetColors();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        settings = new AppSettings(this);
        setTheme(settings.getAppTheme());

        checkCurrrentPermissions();
    }

    public void checkCurrrentPermissions(){
        skipBtn = findViewById(R.id.button_skip);
        skipBtn.setBackgroundColor(enabledColor);
        continueBtn = findViewById(R.id.button_continue);
        externalPermBtn = findViewById(R.id.button_permission_external_storage);
        notifiPermBtn = findViewById(R.id.button_permission_notification_listener);
        batteryPermBtn = findViewById(R.id.button_permission_battery_optimizations);

        TextView findBattery = findViewById(R.id.text_find_battery_optimization_setting);
        TextView findNotify = findViewById(R.id.text_find_notification_setting);

        checkAndSetColors();

        externalPermBtn.setOnClickListener((View view) -> {
            try {
                if (Util.checkExternalPermission(this)){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(PermissionsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
                }
            } catch (Exception e) {
                Log.e(TAG,e.toString());
            }
        });

        notifiPermBtn.setOnClickListener((View view) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    startActivity(intent);
                } catch (Exception e) {
                    findNotify.setTextColor(warningColor);
                    findNotify.setText(R.string.find_notifications_settings);
                    Log.e(TAG, e.toString());
                }
            }
        });

        batteryPermBtn.setOnClickListener((View view) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    Intent intent = new Intent();
                    String packageName = this.getPackageName();
                    PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                    if (pm.isIgnoringBatteryOptimizations(packageName))
                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    else {
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                    }
                    this.startActivity(intent);
                } catch (Exception e) {
                    findBattery.setTextColor(warningColor);
                    findBattery.setText(R.string.find_battery_settings);
                    Log.e(TAG,e.toString());
                }
            }
        });

        skipBtn.setOnClickListener((View view) -> {
            if(!allPermsCheck()) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                settings.setKeyBypassNewPermissions(1);
                                Intent intent = new Intent(ctx, SettingsActivity.class);
                                ctx.startActivity(intent);
                                Util.runServices(ctx);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                String message = ctx.getResources().getString(R.string.are_you_sure);
                if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT && !Util.checkNotificationListenerPermission(ctx)){
                    message += " " + ctx.getResources().getString(R.string.warning_will_not_scrobble);
                    message += "/" + ctx.getResources().getString(R.string.permission_notification_listener);
                }
                builder.setMessage(message).setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();
            }
        });
        continueBtn.setOnClickListener((View v) -> {
            if(allPermsCheck()){
                settings.setKeyBypassNewPermissions(0);
                Intent intent = new Intent(ctx, SettingsActivity.class);
                ctx.startActivity(intent);
                Util.runServices(ctx);
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

    private void checkAndSetColors(){
        colorPermission(Util.checkExternalPermission(this), externalPermBtn);
        colorPermission(Util.checkNotificationListenerPermission(this), notifiPermBtn);
        colorPermission(Util.checkBatteryOptimizationsPermission(this), batteryPermBtn);
        colorPermission(allPermsCheck(), continueBtn);
        colorPermission(!allPermsCheck(), skipBtn);
    }

    private boolean allPermsCheck() {
        return Util.checkNotificationListenerPermission(this)
                && Util.checkExternalPermission(this)
                && Util.checkBatteryOptimizationsPermission(this);
    }
}
