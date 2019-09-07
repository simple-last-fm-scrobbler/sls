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

public enum SubmissionType {
    SCROBBLE(
            "status_last_scrobble", "status_nscrobbles"),
    NP(
            "status_last_np", "status_nnps");

    private final String lastPrefix;
    private final String numberOfPrefix;

    SubmissionType(String lastPrefix, String numberOfPrefix) {
        this.lastPrefix = lastPrefix;
        this.numberOfPrefix = numberOfPrefix;
    }

    public String getLastPrefix() {
        return lastPrefix;
    }

    public String getNumberOfPrefix() {
        return numberOfPrefix;
    }
}