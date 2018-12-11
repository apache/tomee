/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.maven.plugin.cli;

import java.util.Collection;
import java.util.LinkedList;

/**
 * The class Args is used to parse the supplied attributes.
 */
public final class Args {
    /**
     * Parse collection.
     *
     * @param raw the raw
     * @return the collection
     */
    public static Collection<String> parse(final String raw) {
        final Collection<String> result = new LinkedList<>();

        Character end = null;
        boolean escaped = false;
        final StringBuilder current = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            final char c = raw.charAt(i);
            if (escaped) {
                escaped = false;
                current.append(c);
            } else if ((end != null && end == c) || (c == ' ' && end == null)) {
                if (current.length() > 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
                end = null;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"' || c == '\'') {
                end = c;
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
    }

    private Args() {
        // no-op
    }
}
