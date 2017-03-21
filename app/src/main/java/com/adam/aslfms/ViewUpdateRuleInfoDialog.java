package com.adam.aslfms;

import android.content.Context;
import android.database.Cursor;

import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.UpdateRule;

/**
 * Created by 4-Eyes on 21/3/2017.
 *
 */

class ViewUpdateRuleInfoDialog {
    private static final String TAG = "RuleDialog";
    private final Context context;
    private final ScrobblesDatabase database;
    private final Cursor rulesCursor;
    private final UpdateRule rule;
    private final boolean isNewRule;

    public ViewUpdateRuleInfoDialog(Context context, ScrobblesDatabase database, Cursor updateRulesCursor, UpdateRule rule, boolean newRule) {

        this.context = context;
        this.database = database;
        rulesCursor = updateRulesCursor;
        this.rule = rule;
        isNewRule = newRule;
    }

    public void show() {

    }
}
