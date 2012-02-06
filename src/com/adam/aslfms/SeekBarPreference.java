/**
 *  This file is part of Simple Last.fm Scrobbler.
 *
 *  Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class SeekBarPreference extends DialogPreference {

	private Context mContext;
	private Saver mSaver;

	private SeekBar mSeekBar;

	private int mStartMax;
	private int mStartProgress;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mStartMax = 100;
		mStartProgress = 32;
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		// we "have" to create new views each time
		LinearLayout layout = new LinearLayout(mContext);
		layout.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		// layout.setMinimumWidth(400);
		layout.setPadding(20, 20, 20, 20);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		mSeekBar.setMax(mStartMax);
		mSeekBar.setProgress(mStartProgress);
		layout.addView(mSeekBar);

		builder.setView(layout);
		super.onPrepareDialogBuilder(builder);
	}

	public void setDefaults(int progress, int max) {
		mStartMax = max;
		mStartProgress = progress;
	}

	public void setSaver(Saver saver) {
		mSaver = saver;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			mSaver.save(mSeekBar.getProgress());
		}
	}

	public static abstract class Saver {
		public abstract void save(int value);
	}
}
