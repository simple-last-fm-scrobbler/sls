package com.adam.aslfms.receiver;

/**
 * A BroadcastReceiver for intents sent by the trial version of Player Pro.
 *
 * @see BuiltInMusicAppReceiver
 *
 * @author metanota <metanota@gmail.com>
 * @since 1.4.7
 */
public class PlayerProTrialReceiver extends BuiltInMusicAppReceiver {

    public static final String ACTION_PLAYER_PRO_TRIAL_STOP = "com.tbig.playerprotrial.playbackcomplete";
    public static final String ACTION_PLAYER_PRO_TRIAL_PLAYSTATECHANGED = "com.tbig.playerprotrial.playstatechanged";
    public static final String ACTION_PLAYER_PRO_TRIAL_METACHANGED = "com.tbig.playerprotrial.metachanged";

    public PlayerProTrialReceiver() {
        super(ACTION_PLAYER_PRO_TRIAL_STOP, "com.tbig.playerprotrial", "Player Pro Trial");
    }
}
