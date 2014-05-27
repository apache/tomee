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
package org.apache.ziplock.maven;

import org.apache.ziplock.IO;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.WebContainer;
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
    public static class Builder {
        private File basedir;
        private File resources;
        private File webapp;
        private File classes;
        private String basePackage;
        private String name = "test.war";
        private Map<File, String> additionalResources = new HashMap<File, String>();
        private ScopeType[] scopes = { ScopeType.COMPILE, ScopeType.RUNTIME };
        private Filter<ArchivePath> filter = Filters.includeAll();

        public Builder scopes(final ScopeType... scopes) {
            this.scopes = scopes;
            return this;
        }

        public Builder basedir(final File path) {
            this.basedir = path;
            return this;
        }

        public Builder resources(final File path) {
            this.resources = path;
            return this;
        }

        public Builder webapp(final File path) {
            this.webapp = path;
            return this;
        }

        public Builder classes(final File path) {
            this.classes = path;
            return this;
        }

        public Builder applicationPackage(final String base) {
            this.basePackage = base;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder filter(final Filter<ArchivePath> filter) {
            this.filter = filter;
            return this;
        }

        public Builder additionalResource(final File folder, final String root) {
            additionalResources.put(folder, root);
            return this;
        }

        public Archive<?> build() {
            return build(WebArchive.class);
        }
        public <T extends Archive<?>> T build(final Class<T> type) {
            initDefaults();

            final T webArchive = ShrinkWrap.create(type, name);

            if (basePackage != null) {
                if (ClassContainer.class.isInstance(webArchive)) {
                    final ClassContainer<?> container = ClassContainer.class.cast(webArchive);
                    if (filter != null) {
                        container.addPackages(true, filter, basePackage);
                    } else {
                        container.addPackages(true, basePackage);
                    }
                }
            }

            final String root;
            if (WebContainer.class.isInstance(webArchive)) {
                root = "/WEB-INF/classes";
            } else {
                root = "/";
            }

            add(webArchive, classes, root)
            .add(webArchive, resources, root)
            .add(webArchive, webapp, "/");
            for (final Map.Entry<File, String> additionalResource : additionalResources.entrySet()) {
                add(webArchive, additionalResource.getKey(), additionalResource.getValue());
            }

            if (LibraryContainer.class.isInstance(webArchive)) {
                try {
                    final File[] deps = Maven.configureResolver().workOffline().loadPomFromFile(new File(basedir, "pom.xml"))
                        .importDependencies(scopes).resolve().withTransitivity().asFile();
                    if (deps.length > 0) {
                        LibraryContainer.class.cast(webArchive).addAsLibraries(deps);
                    }
                } catch (final Exception e) {
                    // no-op: no deps
                }
            }

            return webArchive;
        }

        private File basedir() {
            if (basedir != null) {
                return basedir;
            }

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

        private void initDefaults() {
            final File basedir = basedir();
            if (classes == null && basePackage == null) {
                classes = new File(basedir, "target/classes");
            }
            if (resources == null && basePackage != null) {
                resources = new File(basedir, "src/main/resources");
            }
            if (webapp == null) {
                webapp = new File(basedir, "src/main/webapp");
            }
        }

        private Builder add(final Archive<?> webArchive, final File dir, final String root) {
            if (dir == null || !dir.exists()) {
                return this;
            }

            final KnownResourcesFilter filter = new KnownResourcesFilter(dir, root, this.filter);
            filter.update(
                webArchive.merge(
                    ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)
                        .importDirectory(dir).as(GenericArchive.class), root, filter));

            return this;
        }
    }

    /**
     * Client war.
     *
     * @return create a war with src/main/resources, src/main/webapp and all compile and runtime dependencies
     */
    public static Archive<?> war() {
        return new Builder().build();
    }

    /**
     * Server war without tests clases.
     *
     * @return create a war with src/main/resources, src/main/webapp and all compile, runtime and test dependencies
     */
    public static Archive<?> testWar() {
        return new Builder().scopes(ScopeType.COMPILE, ScopeType.RUNTIME, ScopeType.TEST).build();
    }

    private Mvn() {
        // no-op
    }

    public static class KnownResourcesFilter implements Filter<ArchivePath> {
        private final File base;
        private final String prefix;
        private final Map<ArchivePath, Asset> paths = new HashMap<ArchivePath, Asset>();
        private final Filter<ArchivePath> delegate;

        public KnownResourcesFilter(final File base, final String prefix, final Filter<ArchivePath> filter) {
            this.base = base;
            this.delegate = filter;

            if (prefix.startsWith("/")) {
                this.prefix = prefix.substring(1);
            } else {
                this.prefix = prefix;
            }
        }

        @Override
        public boolean include(final ArchivePath archivePath) {
            if (!delegate.include(archivePath)) {
                return false;
            }
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
                archive.add(r.getValue(), prefix + r.getKey().get());
            }
        }
    }
}
