package com.adam.aslfms;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.CorrectionRule;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Created by 4-Eyes on 21/3/2017.
 *
 */

class ViewCorrectionRuleEditDialog {
    private static final String TAG = "RuleDialog";
    private final Context context;
    private final ScrobblesDatabase database;
    private final Cursor rulesCursor;
    private final CorrectionRule rule;
    private final boolean isNewRule;

    public ViewCorrectionRuleEditDialog(Context context, ScrobblesDatabase database, Cursor updateRulesCursor, CorrectionRule rule, boolean newRule) {

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
        View view = inflater.inflate(R.layout.correction_rule_edit, null);

        ArrayList<SimpleEntry<Integer, SimpleEntry<String, Consumer<String>>>> viewValuePairs = new ArrayList<>();
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_track_to_change, new SimpleEntry<String, Consumer<String>>(rule.getTrackToChange(), rule::setTrackToChange)));
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_track_correction, new SimpleEntry<String, Consumer<String>>(rule.getTrackCorrection(), rule::setTrackCorrection)));
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_artist_to_change, new SimpleEntry<String, Consumer<String>>(rule.getArtistToChange(), rule::setArtistToChange)));
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_artist_correction, new SimpleEntry<String, Consumer<String>>(rule.getArtistCorrection(), rule::setArtistCorrection)));
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_album_to_change, new SimpleEntry<String, Consumer<String>>(rule.getAlbumToChange(), rule::setAlbumToChange)));
        viewValuePairs.add(new SimpleEntry<>(R.id.edit_album_correction, new SimpleEntry<String, Consumer<String>>(rule.getAlbumCorrection(), rule::setAlbumCorrection)));

        TextView textView;
        for (SimpleEntry<Integer, SimpleEntry<String, Consumer<String>>> viewValue: viewValuePairs) {
            textView = (TextView) view.findViewById(viewValue.getKey());
            textView.setText(viewValue.getValue().getKey());
        }
        builder.setView(view)
                .setTitle("Rule Editing")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Save", (dialog, which) -> {
                    TextView tv;
                    for (SimpleEntry<Integer, SimpleEntry<String, Consumer<String>>> viewValue: viewValuePairs) {
                        tv = (TextView) view.findViewById(viewValue.getKey());
                        viewValue.getValue().getValue().accept(tv.getText().toString());
                    }
                    if (isNewRule) {
                        database.insertCorrectionRule(rule);
                    } else {
                        database.updateCorrectionRule(rule);
                    }
                    rulesCursor.requery();
                })
                .setNegativeButton("Cancel", null);

        if (!isNewRule) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                database.deleteCorrectionRule(rule.getId());
                rulesCursor.requery();
            });
        }

        builder.show();
    }
}
