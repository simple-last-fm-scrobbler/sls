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
package com.adam.aslfms.util.enums;

import android.content.Context;

import com.adam.aslfms.R;

public enum SortField {
    WHEN_ASC(
            "whenplayed", SortOrder.ASCENDING, R.string.sc_sort_when_asc),
    WHEN_DESC(
            "whenplayed", SortOrder.DESCENDING, R.string.sc_sort_when_desc),
    ARTIST_ASC(
            "artist", SortOrder.ASCENDING, R.string.sc_sort_artist_asc),
    ARTIST_DESC(
            "artist", SortOrder.DESCENDING, R.string.sc_sort_artist_desc),
    ALBUM_ASC(
            "album", SortOrder.ASCENDING, R.string.sc_sort_album_asc),
    ALBUM_DESC(
            "album", SortOrder.DESCENDING, R.string.sc_sort_album_desc),
    TRACK_ASC(
            "track", SortOrder.ASCENDING, R.string.sc_sort_track_asc),
    TRACK_DESC(
            "track", SortOrder.DESCENDING, R.string.sc_sort_track_desc);

    private final String field;
    private final SortOrder sortOrder;
    private final int nameRID;

    SortField(String field, com.adam.aslfms.util.enums.SortOrder sortOrder, int nameRID) {
        this.field = field;
        this.sortOrder = sortOrder;
        this.nameRID = nameRID;
    }

    public String getField() {
        return field;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public int getNameRID() {
        return nameRID;
    }

    public String getName(Context ctx) {
        return ctx.getString(nameRID);
    }

    public String getSql() {
        return field + " " + sortOrder.getSql();
    }

    public static CharSequence[] toCharSequenceArray(Context ctx) {
        return new CharSequence[]{ctx.getString(WHEN_ASC.nameRID), ctx.getString(WHEN_DESC.nameRID),
                ctx.getString(ARTIST_ASC.nameRID), ctx.getString(ARTIST_DESC.nameRID),
                ctx.getString(ALBUM_ASC.nameRID), ctx.getString(ALBUM_DESC.nameRID), ctx.getString(TRACK_ASC.nameRID),
                ctx.getString(TRACK_DESC.nameRID),};
    }
}