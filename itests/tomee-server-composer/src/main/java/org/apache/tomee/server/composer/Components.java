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
package org.apache.tomee.server.composer;

import org.tomitribe.util.PrintString;
import org.tomitribe.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

public class Components implements Consumer<TomEE.Builder> {

    private final List<Clazz> classes = new ArrayList<>();

    public Components(final Clazz... classes) {
        this.classes.addAll(Arrays.asList(classes));
    }

    public static Components components(final Clazz... classes) {
        return new Components(classes);
    }

    public static Clazz of(final Class<?> clazz) {
        return new Clazz(clazz);
    }

    @Override
    public void accept(final TomEE.Builder builder) {
        final Archive archive = Archive.archive();
        final Properties properties = new Properties();

        for (final Clazz clazz : classes) {
            archive.add(clazz.clazz);
            properties.put(clazz.prefix, "new://" + clazz.clazz.getName());
            for (final Map.Entry<String, String> entry : clazz.map.entrySet()) {
                properties.put(clazz.prefix + "." + entry.getKey(), entry.getValue());
            }
        }

        final PrintString out = new PrintString();
        try {
            properties.store(out, "Components");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create trixie.properties", e);
        }

        builder.add("lib/components.jar", archive.asJar());
        builder.add("conf/trixie.properties", out.toString());
    }

    public static class Clazz implements Consumer<TomEE.Builder> {
        private final String prefix;
        private final Class clazz;
        private final Map<String, String> map = new HashMap<>();

        public Clazz(final Class clazz) {
            this(Strings.lcfirst(clazz.getSimpleName()), clazz);
        }

        public Clazz(final String prefix, final Class clazz) {
            this.prefix = prefix;
            this.clazz = clazz;
        }

        public Clazz with(final String name, final Object value) {
            this.map.put(name, value + "");
            return this;
        }

        @Override
        public void accept(final TomEE.Builder builder) {
            components(this).accept(builder);
        }
    }
}
