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

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
