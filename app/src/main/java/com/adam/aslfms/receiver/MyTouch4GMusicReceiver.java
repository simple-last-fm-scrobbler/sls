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
 

package com.adam.aslfms.receiver;

/**
 * A BroadcastReceiver for intents sent by the myTouch 4G Music Player.
 * 
 * @see BuiltInMusicAppReceiver
 * 
 * @author tgwizard
 * @since 1.3.2
 */
public class MyTouch4GMusicReceiver extends BuiltInMusicAppReceiver {
	// these first two are untested
	public static final String ACTION_MYTOUCH4G_PLAYSTATECHANGED = "com.real.IMP.playstatechanged";
	public static final String ACTION_MYTOUCH4G_STOP = "com.real.IMP.playbackcomplete";
	// should work
	public static final String ACTION_MYTOUCH4G_METACHANGED = "com.real.IMP.metachanged";

	public MyTouch4GMusicReceiver() {
		super(ACTION_MYTOUCH4G_STOP, "com.real.IMP", "myTouch 4G Music Player");
	}
}
