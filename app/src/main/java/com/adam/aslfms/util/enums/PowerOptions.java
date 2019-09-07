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

public enum PowerOptions {
    BATTERY(
            "", new AdvancedOptions[]{AdvancedOptions.STANDARD, AdvancedOptions.BATTERY_SAVING,
            AdvancedOptions.CUSTOM}),
    PLUGGED_IN(
            "_plugged", new AdvancedOptions[]{AdvancedOptions.SAME_AS_BATTERY, AdvancedOptions.STANDARD,
            AdvancedOptions.CUSTOM});

    private final String settingsPath;
    private final AdvancedOptions[] applicableOptions;

    PowerOptions(String settingsPath, AdvancedOptions[] applicableOptions) {
        this.settingsPath = settingsPath;
        this.applicableOptions = applicableOptions;
    }

    public String getSettingsPath() {
        return settingsPath;
    }

    public AdvancedOptions[] getApplicableOptions() {
        return applicableOptions;
    }

}