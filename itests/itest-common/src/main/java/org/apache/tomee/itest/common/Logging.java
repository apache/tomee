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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.itest.common;

import org.apache.tomee.server.composer.TomEE.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Logging {

    private final List<String> output = new ArrayList<>();


    public void install(final Builder builder) {
        builder.watch("", "\n", output::add);
    }

    public Logging assertPresent(final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final long count = output.stream()
                .filter(line -> pattern.matcher(line).find())
                .count();

        assertTrue(count > 0);
        return this;
    }

    public Logging assertPresent(final int count, final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final long actual = output.stream()
                .filter(line -> pattern.matcher(line).find())
                .count();

        assertEquals(count, actual);
        return this;
    }

    public Logging assertNotPresent(final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(actual.isEmpty());
        return this;
    }
}
