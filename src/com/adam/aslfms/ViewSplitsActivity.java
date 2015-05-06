package com.adam.aslfms;

import com.adam.aslfms.util.SplitsDatabase;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ViewSplitsActivity extends ListActivity {
  @SuppressWarnings("unused")
  private static final String TAG = "SplitsActivity";

  private SplitsDatabase mDb;

  private Cursor mSplitsCursor = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTitle(getString(R.string.splits_view_title));
    setContentView(R.layout.splits_list);

    mDb = new SplitsDatabase(this);
    mDb.open();

    fillData();
    registerForContextMenu(getListView());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mDb.close();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (mSplitsCursor != null)
      mSplitsCursor.requery();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.view_splits, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_splits_add:
      new SplitAddDialog(this, mDb, mSplitsCursor).show();
      mSplitsCursor.requery();
      return true;
    case R.id.menu_splits_clear:
      mDb.clearAllSplits();
      mSplitsCursor.requery();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    if (info.id < 0)
      return;

    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.view_splits_context, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    switch (item.getItemId()) {
    case R.id.menu_remove_split:
      mDb.removeSplit(info.id);
      mSplitsCursor.requery();
      return true;
    }
    return super.onContextItemSelected(item);
  }

  private void fillData() {
    mSplitsCursor = mDb.fetchAllSplitsCursor();

    startManagingCursor(mSplitsCursor);
    CursorAdapter adapter = new MyAdapter(this, mSplitsCursor);
    setListAdapter(adapter);
  }

  private class MyAdapter extends CursorAdapter {
    public MyAdapter(Context context, Cursor c) {
      super(context, c);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      String name = cursor.getString(cursor.getColumnIndex("split"));
      TextView nameView = (TextView) view.findViewById(R.id.name);
      nameView.setText(name);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      return LayoutInflater.from(context).inflate(R.layout.split_row, parent, false);
    }
  }
}
