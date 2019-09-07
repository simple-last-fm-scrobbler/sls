/**
 * This file is part of Simple Scrobbler.
 * <p>
 * https://github.com/simple-last-fm-scrobbler/sls
 * <p>
 * Copyright 2011 Simple Scrobbler Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.adam.aslfms.util;

import com.adam.aslfms.receiver.MusicAPI;

/**
 * @author 4-Eyes on 21/3/2017.
 * @since 1.5.8
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
