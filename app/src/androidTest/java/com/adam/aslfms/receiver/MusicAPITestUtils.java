/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * http://code.google.com/p/a-simple-lastfm-scrobbler/
 * <p>
 * Copyright 2011 Simple Last.fm Scrobbler Team
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

package com.adam.aslfms.receiver;

import android.content.Context;

public class MusicAPITestUtils {

    public static MusicAPI getDummyMusicAPI() {
        return new MusicAPI(0, "SLS Test", "com.testing.sls", null, false, true);
    }

    public static void deleteDatabase(Context ctx) {
        ctx.deleteDatabase(MusicAPI.DATABASE_NAME);
    }
}
