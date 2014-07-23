package com.adam.aslfms.receiver;

/**
 * A BroadcastReceiver for intents sent by the Player Pro.
 *
 * @see BuiltInMusicAppReceiver
 *
 * @author metanota <metanota@gmail.com>
 * @since 1.4.7
 */
public class PlayerProReceiver extends BuiltInMusicAppReceiver {

    public static final String ACTION_PLAYER_PRO_STOP = "com.tbig.playerpro.playbackcomplete";
    public static final String ACTION_PLAYER_PRO_PLAYSTATECHANGED = "com.tbig.playerpro.playstatechanged";
    public static final String ACTION_PLAYER_PRO_METACHANGED = "com.tbig.playerpro.metachanged";

    public PlayerProReceiver() {
        super(ACTION_PLAYER_PRO_STOP, "com.tbig.playerpro", "Player Pro");
    }
}
