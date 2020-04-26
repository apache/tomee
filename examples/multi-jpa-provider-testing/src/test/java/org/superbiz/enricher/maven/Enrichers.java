/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.enricher.maven;

import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import jakarta.enterprise.inject.ResolutionException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class Enrichers {

    private static final Map<String, File[]> CACHE = new HashMap<String, File[]>();

    private Enrichers() {
        // no-op
    }

    public static File[] resolve(final String pom) {
        if (!CACHE.containsKey(pom)) {
            try {

                // try offline first since it is generally faster
                CACHE.put(pom, Maven.resolver()
                        .offline(true)
                        .loadPomFromFile(pom)
                        .importCompileAndRuntimeDependencies().resolve().withTransitivity()
                        .asFile());
            } catch (ResolutionException re) { // try on central
                CACHE.put(pom, Maven.resolver()
                        .loadPomFromFile(pom)
                        .importCompileAndRuntimeDependencies().resolve().withTransitivity()
                        .asFile());
            }
        }
        return CACHE.get(pom);
    }

    public static LibraryContainer wrap(final org.jboss.shrinkwrap.api.Archive<?> archive) {
        if (!(LibraryContainer.class.isInstance(archive))) {
            throw new IllegalArgumentException("Unsupported archive type: " + archive.getClass().getName());
        }
        return (LibraryContainer) archive;
    }
}
