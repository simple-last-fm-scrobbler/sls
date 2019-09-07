/**
 * This file is part of Simple Scrobbler.
 * <p/>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p/>
 * Copyright 2011 Simple Scrobbler Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.adam.aslfms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.adam.aslfms.service.NetApp;
import com.adam.aslfms.util.CorrectionRule;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;

public class ViewScrobbleInfoDialog {
    @SuppressWarnings("unused")
    private static final String TAG = "WhatsNewDialog";
    private final Context mCtx;
    private final ScrobblesDatabase mDb;
    private final Cursor mParentCursor;
    private final Track mTrack;

    private final NetApp mNetApp;
    private NetApp[] mNetApps;

    public ViewScrobbleInfoDialog(Context mCtx, ScrobblesDatabase mDb,
                                  NetApp mNetApp, Cursor mParentCursor, Track mTrack) {
        super();
        this.mCtx = mCtx;
        this.mDb = mDb;
        this.mNetApp = mNetApp;
        this.mParentCursor = mParentCursor;
        this.mTrack = mTrack;

        if (mNetApp == null) {
            mNetApps = mDb.fetchNetAppsForScrobble(mTrack.getRowId());
        } else {
            mNetApps = null;
        }
    }

    public void show() {

        AlertDialog.Builder adBuilder = new AlertDialog.Builder(mCtx);

        // alertdialog editor

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.scrobble_info_row, null);

        final TextView unEditable = (TextView) view.findViewById(R.id.text0);

        StringBuilder builder = new StringBuilder();

        String time = Util.timeFromUTCSecs(mCtx, mTrack.getWhen());
        builder.append(time);
        builder.append("\n");
        builder.append(mTrack.getMusicAPI().getName());
        builder.append("\n");
        if (mNetApps != null) {
            StringBuilder sb = new StringBuilder();
            for (NetApp napp : mNetApps) {
                sb.append(napp.getName());
                sb.append(", ");
            }
            if (sb.length() > 2)
                sb.setLength(sb.length() - 2);
            builder.append(sb.toString());
            builder.append("\n");
        }
        unEditable.setText(builder.toString());

        final EditText edTrack = (EditText) view.findViewById(R.id.trackEd);
        edTrack.setText(mTrack.getTrack());

        final EditText edArtist = (EditText) view.findViewById(R.id.artistEd);
        edArtist.setText(mTrack.getArtist());

        final EditText edAlbum = (EditText) view.findViewById(R.id.albumEd);
        edAlbum.setText(mTrack.getAlbum());

        adBuilder.setView(view);

        adBuilder.setTitle(
                R.string.track_info)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(R.string.remove,
                        (dialog, which) -> {
                            if (mNetApp == null) {
                                Util.deleteScrobbleFromAllCaches(mCtx, mDb,
                                        mParentCursor, mTrack.getRowId());
                            } else {
                                Util.deleteScrobbleFromCache(mCtx, mDb, mNetApp,
                                        mParentCursor, mTrack.getRowId());
                            }

                        })
                .setNegativeButton(R.string.close, (dialogInterface, i) -> {
                    int rid = mTrack.getRowId();
                    // if (rid != -1) TODO song gets scrobbled mid typing

                    CheckBox saveAsRule = (CheckBox) view.findViewById(R.id.save_as_rule);
                    if (saveAsRule.isChecked()) {
                        CorrectionRule rule = new CorrectionRule();
                        rule.setTrackToChange(mTrack.getTrack());
                        rule.setAlbumToChange(mTrack.getAlbum());
                        rule.setArtistToChange(mTrack.getArtist());
                        rule.setTrackCorrection(edTrack.getText().toString());
                        rule.setAlbumCorrection(edAlbum.getText().toString());
                        rule.setArtistCorrection(edArtist.getText().toString());
                        mDb.insertCorrectionRule(rule);
                    }
                    mDb.setTrack(edTrack.getText().toString(), rid);
                    mDb.setArtist(edArtist.getText().toString(), rid);
                    mDb.setAlbum(edAlbum.getText().toString(), rid);

                    dialogInterface.cancel();
                    Intent intent = new Intent(mCtx, ViewScrobbleCacheActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("viewall", true);
                    mCtx.startActivity(intent);
                })
                .setNeutralButton(R.string.copy_title, (dialogInterface, i) -> {
                    int rid = mTrack.getRowId();

                    String track = edTrack.getText().toString();
                    String artist = edArtist.getText().toString();
                    String album = edAlbum.getText().toString();

                    mDb.setTrack(track, rid);
                    mDb.setArtist(artist, rid);
                    mDb.setAlbum(album, rid);

                    int sdk = Build.VERSION.SDK_INT;
                    if (sdk < Build.VERSION_CODES.HONEYCOMB) {
                        @SuppressWarnings("deprecation")
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(track + " by " + artist + ", " + album);
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Track", track + " by " + artist + ", " + album);
                        clipboard.setPrimaryClip(clip);
                    }
                    Log.d(TAG, "Copy Track!");

                    dialogInterface.cancel();
                    Intent intent = new Intent(mCtx, ViewScrobbleCacheActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("viewall", true);
                    mCtx.startActivity(intent);
                });

        adBuilder.show();
    }
}