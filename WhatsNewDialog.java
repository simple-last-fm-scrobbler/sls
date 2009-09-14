package com.adam.aslfms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
				R.string.thats_new).setIcon(android.R.drawable.ic_dialog_info)
				.setView(dialogView).setNegativeButton(R.string.close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});

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
