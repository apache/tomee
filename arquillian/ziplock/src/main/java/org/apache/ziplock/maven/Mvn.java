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
package org.apache.ziplock.maven;

import org.apache.ziplock.IO;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public final class Mvn {
    /**
     * Client war.
     *
     * @return create a war with src/main/resources, src/main/webapp and all compile and runtime dependencies
     */
    public static Archive<?> war() {
        return war("test.war", null);
    }

    /**
     * Server war without tests clases.
     *
     * @return create a war with src/main/resources, src/main/webapp and all compile, runtime and test dependencies
     */
    public static Archive<?> testWar() {
        return war("test.war", null, ScopeType.COMPILE, ScopeType.RUNTIME, ScopeType.TEST);
    }

    public static Archive<?> war(final String name, final String basePackage, final ScopeType... scopes) {
        final File basedir = basedir();

        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, name);

        if (basePackage == null) {
            final File classes = new File(basedir, "target/classes/");
            add(webArchive, classes, "/WEB-INF/classes/");
        } else {
            webArchive.addPackages(true, basePackage);
        }

        final File webapp = new File(basedir, "src/main/webapp");
        add(webArchive, webapp, "/");
        if (basePackage != null) {
            final File resources = new File(basedir, "src/main/resources");
            add(webArchive, resources, "/WEB-INF/classes/");
        }

        final ScopeType[] types;
        if (scopes == null || scopes.length == 0) {
            types = new ScopeType[]{ ScopeType.COMPILE, ScopeType.RUNTIME };
        } else {
            types = scopes;
        }
        try {
            final File[] deps = Maven.resolver().offline().loadPomFromFile(new File(basedir, "pom.xml"))
                .importDependencies(types).resolve().withTransitivity().asFile();
            if (deps.length > 0) {
                webArchive.addAsLibraries(deps);
            }
        } catch (final Exception e) {
            // no-op
        }

        return webArchive;
    }

    private static void add(final WebArchive webArchive, final File classes, final String root) {
        if (!classes.exists()) {
            return;
        }

        final KnownResourcesFilter filter = new KnownResourcesFilter(classes, root);
        filter.update(
            webArchive.merge(
                ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)
                    .importDirectory(classes).as(GenericArchive.class), root, filter));
    }

    private static File basedir() {
        {
            final File file = new File("pom.xml");
            if (file.exists()) {
                return new File(".");
            }
        }
        {
            final File file = new File("../pom.xml");
            if (file.exists()) {
                return new File("..");
            }
        }
        throw new IllegalStateException("basedir not found");
    }

    private Mvn() {
        // no-op
    }

    public static class KnownResourcesFilter implements Filter<ArchivePath> {
        private final File base;
        private final String prefix;
        private final Map<ArchivePath, Asset> paths = new HashMap<ArchivePath, Asset>();

        public KnownResourcesFilter(final File base, final String prefix) {
            this.base = base;

            if (prefix.startsWith("/")) {
                this.prefix = prefix.substring(1);
            } else {
                this.prefix = prefix;
            }
        }

        @Override
        public boolean include(final ArchivePath archivePath) {
            if (archivePath.get().contains("shiro.ini")) {
                paths.put(archivePath, addArquillianServletInUrls(archivePath));
                return false;
            }
            return true;
        }

        private Asset addArquillianServletInUrls(final ArchivePath archivePath) {
            final File f = new File(base, archivePath.get());
            if (!f.exists()) {
                throw new IllegalArgumentException("Can't find " + archivePath.get());
            }

            final String ln = System.getProperty("line.separator");
            final StringWriter out = new StringWriter();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(f));
                String line;
                while ((line = reader.readLine()) != null) {
                    out.write(line + ln);
                    if ("[urls]".equals(line)) {
                        out.write("/ArquillianServletRunner = anon" + ln);
                    }
                }
            } catch (final IOException e) {
                throw new IllegalArgumentException(e);
            } finally {
                try {
                    IO.close(reader);
                } catch (final IOException e) {
                    // no-op
                }
            }
            return new StringAsset(out.toString());
        }

        public void update(final Archive<?> archive) {
            for (final Map.Entry<ArchivePath, Asset> r : paths.entrySet()) {
                archive.add(r.getValue(), prefix + r.getKey());
            }
        }
    }
}
