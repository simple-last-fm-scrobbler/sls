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

/**
 * A convenient way to send the sort order of a query as a parameter to the
 * method.
 *
 * @author tgwizard
 */
public enum SortOrder {
    ASCENDING(
            "asc"),
    DESCENDING(
            "desc");

    private final String sql;

    SortOrder(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}