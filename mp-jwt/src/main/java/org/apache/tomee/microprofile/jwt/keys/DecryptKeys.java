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
package org.apache.tomee.microprofile.jwt.keys;

import java.security.Key;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class DecryptKeys implements Supplier<Map<String, Key>> {
    final Optional<String> contents;
    final Optional<String> location;

    public DecryptKeys(final Optional<String> contents, final Optional<String> location) {
        this.contents = contents;
        this.location = location;
    }

    @Override
    public Map<String, Key> get() {
        final KeyResolver resolver = new KeyResolver();
        return resolver.resolveDecryptKey(contents, location).orElse(Collections.EMPTY_MAP);
    }
}
