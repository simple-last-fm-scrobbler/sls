package com.adam.aslfms.receiver;

public class PlayerProReceiver extends BuiltInMusicAppReceiver {

    public static final String ACTION_PLAYER_PRO_STOP = "com.tbig.playerpro.playbackcomplete";
    public static final String ACTION_PLAYER_PRO_PLAYSTATECHANGED = "com.tbig.playerpro.playstatechanged";
    public static final String ACTION_PLAYER_PRO_METACHANGED = "com.tbig.playerpro.metachanged";

    public PlayerProReceiver() {
        super(ACTION_PLAYER_PRO_STOP, "com.tbig.playerpro", "Player Pro");
    }
}
