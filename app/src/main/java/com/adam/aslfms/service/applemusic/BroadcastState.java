package com.adam.aslfms.service.applemusic;

/**
 * Created by 4-Eyes on 16/3/2017.
 *
 */

enum BroadcastState {
    START(0),
    RESUME(1),
    PAUSE(2),
    COMPLETE(3);

    private int value;

    BroadcastState(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }
}
