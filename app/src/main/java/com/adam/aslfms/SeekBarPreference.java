/**
 * This file is part of Simple Last.fm Scrobbler.
 * 
 *     https://github.com/tgwizard/sls
 * 
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
