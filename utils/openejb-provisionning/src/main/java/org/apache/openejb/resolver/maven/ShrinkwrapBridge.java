/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resolver.maven;

import org.apache.openejb.util.reflection.Reflections;

import java.io.File;

public class ShrinkwrapBridge {
    private static final Class<?>[] NO_PARAM = new Class[0];
    private static final Class<?>[] STRING_PARAM = new Class[]{String.class};
    private static final Object[] NO_ARG = new Object[0];

    public static File resolve(final String rawLocation) throws Exception {
        final Class<?> mvn = Thread.currentThread().getContextClassLoader().loadClass("org.jboss.shrinkwrap.resolver.api.maven.Maven");
        /*
        return Maven.configureResolver().workOffline()
            .resolve(toSwFormat(rawLocation))
            .withoutTransitivity()
            .asSingleFile()
            .getAbsolutePath()
         */
        return File.class.cast(Reflections.invokeByReflection(
            Reflections.invokeByReflection(
                Reflections.invokeByReflection(
                    Reflections.invokeByReflection(
                        mvn.getMethod("configureResolver").invoke(null),
                        "workOffline", NO_PARAM, NO_ARG),
                    "resolve", STRING_PARAM, new Object[]{toSwFormat(rawLocation)}),
                "withoutTransitivity", NO_PARAM, NO_ARG),
            "asSingleFile", NO_PARAM, NO_ARG));
    }

    private static String toSwFormat(final String rawLocation) {
        final String[] segments = rawLocation.split(":");
        if (!"mvn".equals(segments[0])) {
            throw new IllegalArgumentException("Only mvn prefix is supported: " + rawLocation);
        }

        if (segments.length == 5) {
            return segments[1] + ':' + segments[2] + ':' + segments[4] + ':' + segments[3];
        } else if (segments.length == 4) {
            return segments[1] + ':' + segments[2] + ':' + segments[3];
        } else if (segments.length == 3) {
            return segments[1] + ':' + segments[2];
        }
        throw new IllegalArgumentException("Unknown mvn format: " + rawLocation);
    }
}
