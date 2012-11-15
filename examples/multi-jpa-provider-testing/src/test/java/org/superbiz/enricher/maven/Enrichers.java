/**
 *
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
package org.superbiz.enricher.maven;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveProcessor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.jboss.shrinkwrap.resolver.api.maven.filter.ScopeFilter;

import javax.enterprise.inject.ResolutionException;
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
            try { // try offline first since it is generally faster
                CACHE.put(pom, DependencyResolvers.use(MavenDependencyResolver.class)
                        .goOffline()
                        .loadMetadataFromPom(pom)
                        .scope("compile")
                        .resolveAsFiles());
            } catch (ResolutionException re) { // try on central
                CACHE.put(pom, DependencyResolvers.use(MavenDependencyResolver.class)
                        .loadMetadataFromPom(pom)
                        .scope("compile")
                        .resolveAsFiles());
            }
        }
        return CACHE.get(pom);
    }

    public static WebArchive wrap(final Archive<?> archive) {
        if (!(archive instanceof WebArchive)) {
            throw new IllegalArgumentException("not supported kind of archive: " + archive.getClass().getName());
        }
        return (WebArchive) archive;
    }
}
