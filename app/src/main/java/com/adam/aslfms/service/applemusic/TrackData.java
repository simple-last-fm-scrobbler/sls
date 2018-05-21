package com.adam.aslfms.service.applemusic;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 4-Eyes on 16/3/2017.
 *
 */

class TrackData {

    private String artist;
    private String title;
    private String album;
    private Date startTime;
    private PlayingState currentState = PlayingState.UNKNOWN;
    private ArrayList<Long> playTimes = new ArrayList<>();
    private long lastStateChangedTime;
    private boolean isRepeat = false;

    void setContentType(String contentType) {
        switch (contentType) {
            case "Pause":
                currentState = PlayingState.PLAYING;
                break;
            case "Play":
                currentState = PlayingState.PAUSED;
                break;
            default:
                currentState = PlayingState.UNKNOWN;
                break;
        }
        this.lastStateChangedTime = System.currentTimeMillis();
    }

    boolean mergeSame(TrackData data) {
        if (this.currentState.equals(data.currentState)) return false;
        if (this.currentState.equals(PlayingState.PLAYING)
                && data.currentState.equals(PlayingState.PAUSED)) {
            playTimes.add(data.startTime.getTime() - this.lastStateChangedTime);
        }
        this.currentState = data.currentState;
        lastStateChangedTime = System.currentTimeMillis();
        return true;
    }

    long recordedTotalPlayTime() {
        long total = 0;
        for (long playtime : playTimes) {
            total += playtime;
        }
        return total;
    }

    long currentTotalPlayTime() {
        long recordedTime = this.recordedTotalPlayTime();
        if (currentState.equals(PlayingState.PLAYING)) {
            recordedTime += System.currentTimeMillis() - lastStateChangedTime;
        }
        return recordedTime;
    }

    long finalisePlayTime(long currentTrackDuration) {
        if (currentState.equals(PlayingState.PLAYING)) {
            long lastPlayTime = System.currentTimeMillis() - lastStateChangedTime;
            long totalPlayTimes = recordedTotalPlayTime();
            long overlapTime = (totalPlayTimes + lastPlayTime - currentTrackDuration);
            if (totalPlayTimes + lastPlayTime > currentTrackDuration + 5000 &&
                    overlapTime < currentTrackDuration) { // check overlap time is not ridiculous
                playTimes.add(lastPlayTime - overlapTime);
                return overlapTime;
            }
            playTimes.add(lastPlayTime);
        }
        return 0;
    }

    boolean sameTrack(TrackData other) {
        return other != null & this.title != null && this.artist != null && this.album != null &&
                this.title.equals(other.title) && this.artist.equals(other.artist) && this.album.equals(other.album);
    }

    boolean isComplete(long trackDuration) {
        long playTime = recordedTotalPlayTime();
        return playTime >= (trackDuration - 5000); // Has been played for within 5 seconds of the actual song length.
    }

    void setArtist(String artist) {
        this.artist = artist;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setAlbum(String album) {
        this.album = album;
    }

    void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    String getTitle() {
        return title;
    }

    String getArtist() {
        return artist;
    }

    String getAlbum() {
        return album;
    }

    PlayingState getCurrentState() {
        return currentState;
    }

    void addPlayTime(long playtime) {
        playTimes.add(playtime);
    }

    String getContentType() {
        switch (this.currentState) {
            case PLAYING:
                return "Pause";
            case PAUSED:
                return "Play";
            case UNKNOWN:
            default:
                return "";
        }
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }
}
