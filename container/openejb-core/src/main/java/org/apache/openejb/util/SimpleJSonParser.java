/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// only maps json objects, doesn't handle array, number, true/false/null
public final class SimpleJSonParser {
    public static Object read(final InputStream is) throws IOException {
        Map<String, Object> json = null;
        Collection<Object> array = null;

        int read;
        char current;
        while ((read = is.read()) != -1) {
            current = (char) read; // cast after otherwise -1 test will likely fail if the input file is not correct

            if (current == '{') {
                json = new HashMap<>();
            } else if (current == '}') {
                return json;
            } else if (current == '[') {
                array = new ArrayList<>();
            } else if (current == ']') {
                return array;
            } else if (current == '"') {
                final StringBuilder b = new StringBuilder();
                do {
                    read = is.read();
                    current = (char) read;

                    b.append(current);
                } while (read != -1 && current != '\"');

                if (read == -1) {
                    throw new IllegalArgumentException("String should be between \"");
                }

                final String value = PropertyPlaceHolderHelper.simpleValue(b.substring(0, b.length() - 1)); // remove last "
                if (valueRead(is, json, array, value)) {
                    return value;
                }
            } else if (current != ':' && current != '=' && current != ',' && !isWhiteSpace(current)) {
                final StringBuilder b = new StringBuilder().append(current);
                do {
                    read = is.read();
                    current = (char) read;

                    b.append(current);
                } while (read != -1 && !isWhiteSpace(current) && current != ',');

                final String value = PropertyPlaceHolderHelper.simpleValue(b.substring(0, b.length() - 1)); // remove last character
                if (valueRead(is, json, array, value)) {
                    return value;
                }
            }
            // else skip
        }

        throw new IllegalArgumentException("Please check input, a } is probably missing");
    }

    private static boolean valueRead(final InputStream is, final Map<String, Object> json, final Collection<Object> array, final String value) throws IOException {
        int read;
        char c;
        do {
            read = is.read();
            c = (char) read;
        } while (read != -1 && c != ':' && c != '=' && (c == ',' || c == '\n' || c == '\r'));

        if (json != null) {
            json.put(value, read(is));
        } else if (array != null) {
            array.add(value);
        } else {
            return true;
        }
        return false;
    }

    private static boolean isWhiteSpace(final char current) {
        return current == ' ' || current == '\n' || current == '\r' || current == '\t';
    }

    private SimpleJSonParser() {
        // no-op
    }
}
