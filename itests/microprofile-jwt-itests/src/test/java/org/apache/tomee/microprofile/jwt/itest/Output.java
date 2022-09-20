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
package org.apache.tomee.microprofile.jwt.itest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class Output {
    private final List<String> output = new ArrayList<>();

    public void add(final String line) {
        this.output.add(line);
    }

    public Output assertPresent(final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(actual.isPresent());
        return this;
    }

    public Output assertNotPresent(final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(!actual.isPresent());
        return this;
    }
}
