package com.adam.aslfms;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.UpdateRule;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by 4-Eyes on 21/3/2017.
 *
 */

class ViewUpdateRuleEditDialog {
    private static final String TAG = "RuleDialog";
    private final Context context;
    private final ScrobblesDatabase database;
    private final Cursor rulesCursor;
    private final UpdateRule rule;
    private final boolean isNewRule;

    public ViewUpdateRuleEditDialog(Context context, ScrobblesDatabase database, Cursor updateRulesCursor, UpdateRule rule, boolean newRule) {

        this.context = context;
        this.database = database;
        rulesCursor = updateRulesCursor;
        this.rule = rule;
        isNewRule = newRule;
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.update_rule_edit, null);

        ArrayList<SimpleEntry<Integer, String>> viewValuePairs = new ArrayList<>();
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_track_to_change, rule.getTrackToChange()));
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_track_correction, rule.getTrackCorrection()));
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_artist_to_change, rule.getArtistToChange()));
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_artist_correction, rule.getArtistCorrection()));
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_album_to_change, rule.getAlbumToChange()));
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_album_correction, rule.getAlbumCorrection()));

        TextView textView;
        for (SimpleEntry<Integer, String> viewValue: viewValuePairs) {
            textView = (TextView) view.findViewById(viewValue.getKey());
            textView.setText(viewValue.getValue());
        }
        builder.setView(view)
                .setTitle("Rule Editing")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Todo save stuff
                    }
                })
                .setNegativeButton("Cancel", null);

        if (!isNewRule) {
            builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Todo delete stuff
                }
            });
        }

        builder.show();
    }
}
