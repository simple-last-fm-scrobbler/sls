package com.adam.aslfms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.widget.EditText;

import com.adam.aslfms.util.SplitsDatabase;

public class SplitAddDialog {
  @SuppressWarnings("unused")
  private static final String TAG = "SplitAddDialog";

  private final Context mCtx;
  private final SplitsDatabase mDb;
  private final Cursor mParentCursor;

  public SplitAddDialog(Context context, SplitsDatabase db, Cursor cursor) {
    this.mCtx = context;
    this.mDb = db;
    this.mParentCursor = cursor;
  }

  public void show() {
    AlertDialog.Builder alert = new AlertDialog.Builder(mCtx);

    alert.setTitle(R.string.splits_add_title);
    alert.setMessage(R.string.splits_add_message);

    final EditText input = new EditText(mCtx);

    alert.setView(input);
    alert.setNegativeButton(R.string.add, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        mDb.insertTitleSplit(input.getText().toString());
        mParentCursor.requery();
      }
    });

    alert.setPositiveButton(R.string.close, null);
    alert.show();
  }
}
