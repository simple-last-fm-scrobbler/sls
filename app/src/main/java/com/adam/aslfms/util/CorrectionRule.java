package com.adam.aslfms.util;

import com.adam.aslfms.receiver.MusicAPI;

/**
 * Created by 4-Eyes on 21/3/2017.
 *
 */

public class CorrectionRule {

    private int id;
    private String trackToChange;
    private String albumToChange;
    private String artistToChange;
    private String trackCorrection;
    private String albumCorrection;
    private String artistCorrection;
    private MusicAPI musicApp;

    public String getTrackToChange() {
        return trackToChange;
    }

    public void setTrackToChange(String trackToChange) {
        this.trackToChange = trackToChange;
    }

    public String getAlbumToChange() {
        return albumToChange;
    }

    public void setAlbumToChange(String albumToChange) {
        this.albumToChange = albumToChange;
    }

    public String getArtistToChange() {
        return artistToChange;
    }

    public void setArtistToChange(String artistToChange) {
        this.artistToChange = artistToChange;
    }

    public String getTrackCorrection() {
        return trackCorrection;
    }

    public void setTrackCorrection(String trackCorrection) {
        this.trackCorrection = trackCorrection;
    }

    public String getAlbumCorrection() {
        return albumCorrection;
    }

    public void setAlbumCorrection(String albumCorrection) {
        this.albumCorrection = albumCorrection;
    }

    public String getArtistCorrection() {
        return artistCorrection;
    }

    public void setArtistCorrection(String artistCorrection) {
        this.artistCorrection = artistCorrection;
    }

    public MusicAPI getMusicApp() {
        return musicApp;
    }

    public void setMusicApp(MusicAPI musicApp) {
        this.musicApp = musicApp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
