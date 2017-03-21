package com.adam.aslfms;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.UpdateRule;
import com.adam.aslfms.util.Util;

/**
 * Created by 4-Eyes on 21/3/2017.
 *
 */

public class ViewUpdateRulesActivity extends AppCompatActivity {
    private static final String TAG = "ViewUpdateRulesActivity";

    private AppSettings settings;

    private ScrobblesDatabase database;

    private Cursor updateRulesCursor = null;

    private ListView rulesListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        settings = new AppSettings(this);

        setTitle("Rules for fixing scrobbles");
        setContentView(R.layout.update_rules_list);

        rulesListView = (ListView) findViewById(R.id.rules_list);
        rulesListView.setEmptyView(findViewById(R.id.empty_rules_list));

        rulesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewRuleDetails(id);
            }
        });

        database = new ScrobblesDatabase(this);
        database.open();

        fillData();
        registerForContextMenu(rulesListView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (updateRulesCursor != null)
            updateRulesCursor.requery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_update_rules, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear_rules:
                Util.confirmDialog(this,
                        "Are you sure you want to delete all rules?",
                        R.string.remove,
                        android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                database.deleteAllUpdateRules();
                                if (updateRulesCursor != null)
                                    updateRulesCursor.requery();
                            }
                        });
                return true;
            case R.id.menu_add_rule:
                viewRuleDetails(-1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        if (info.id < 0)
            return;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_update_rules_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_edit_rule:
                viewRuleDetails(info.id);
                return true;
            case R.id.menu_delete_rule:
                Util.confirmDialog(this,
                        "Are you sure you want to delete this rule?",
                        R.string.remove,
                        android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                database.deleteUpdateRule((int) info.id);
                                if (updateRulesCursor != null)
                                    updateRulesCursor.requery();
                            }
                        });
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void fillData() {
        updateRulesCursor = database.fetchAllUpdateRulesCursor();
        startManagingCursor(updateRulesCursor);
        CursorAdapter adapter = new UpdateRulesAdapter(this, updateRulesCursor);
        rulesListView.setAdapter(adapter);

    }

    private void viewRuleDetails(long id) {
        boolean newRule = id == -1;
        UpdateRule rule = newRule ? new UpdateRule() : database.fetchUpdateRule((int)id);
        if (rule == null) {
            Log.e(TAG, "Got null update rule with id: " + id);
            return;
        }
        new ViewUpdateRuleInfoDialog(this, database, updateRulesCursor, rule, newRule).show();
    }

    private class UpdateRulesAdapter extends CursorAdapter{

        public UpdateRulesAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.update_rules_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView trackToChangeView = (TextView) findViewById(R.id.track_to_change);
            trackToChangeView.setText(cursor.getString(cursor.getColumnIndex("track_to_change")));

            TextView trackCorrectionView = (TextView) findViewById(R.id.track_correction);
            trackCorrectionView.setText(cursor.getString(cursor.getColumnIndex("track_correction")));

            TextView albumToChangeView = (TextView) findViewById(R.id.album_to_change);
            albumToChangeView.setText(cursor.getString(cursor.getColumnIndex("album_to_change")));

            TextView albumCorrection = (TextView) findViewById(R.id.album_correction);
            albumCorrection.setText(cursor.getString(cursor.getColumnIndex("album_correction")));

            TextView artistToChangeView = (TextView) findViewById(R.id.artist_to_change);
            artistToChangeView.setText(cursor.getString(cursor.getColumnIndex("artist_to_change")));

            TextView artistCorrection = (TextView) findViewById(R.id.artist_correction);
            artistCorrection.setText(cursor.getString(cursor.getColumnIndex("artist_correction")));
        }
    }
}
