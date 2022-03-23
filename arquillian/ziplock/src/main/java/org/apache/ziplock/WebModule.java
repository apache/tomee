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
package org.apache.ziplock;

import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.NamedModule;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class WebModule {

    private final WebArchive archive;

    public WebModule(final WebArchive archive) {
        this.archive = archive;
    }

    public WebModule(final String name) {
        this(ShrinkWrap.create(WebArchive.class, name));
    }

    /**
     * @param clazz class used to extract the package to include in the webarchive
     * @param excluded list of excluded files (will not be in the webarchive) contained in the selected package
     */
    public WebModule(final Class<?> clazz, final Class<?>... excluded) {
        this(clazz, clazz.getSimpleName() + ".war", excluded);
    }

    public WebModule(final Class<?> clazz, final String appName, final Class<?>... excluded) {
        this(appName);

        final URL archiveURL;
        try {
            final File file = JarLocation.jarLocation(clazz);
            archiveURL = file.toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }

        final String packageName = clazz.getName().substring(0, clazz.getName().lastIndexOf(".") + 1).replace(".", "/");

        final ResourceFinder finder = new ResourceFinder(archiveURL);

        try {
            final Map<String, URL> map = finder.getResourcesMap(packageName);
            for (final Map.Entry<String, URL> entry : map.entrySet()) {
                final URL url = entry.getValue();
                final String name = entry.getKey();


                if (name.endsWith(".xml")) {
                    this.archive.add(new Named("WEB-INF/" + name, new UrlAsset(url)));
                } else {
                    boolean keep = true;
                    if (excluded != null) {
                        for (final Class<?> excludedClazz : excluded) {
                            if (name.equals(excludedClazz.getSimpleName().concat(".class"))) {
                                keep = false;
                            }
                        }
                    }

                    if (keep) {
                        final String path = url.getPath();
                        final String relativePath = path.substring(path.indexOf(packageName));
                        this.archive.add(new Named("WEB-INF/classes/" + relativePath, new UrlAsset(url)));
                    }
                }
            }
        } catch (final IOException e) {
            throw new IllegalStateException("cannot list package contents", e);
        }
    }

    public static class Named implements NamedAsset {

        private final Asset asset;
        private final String name;

        public Named(final String name, final Asset asset) {
            this.asset = asset;
            this.name = name;
        }

        @Override
        public InputStream openStream() {
            return asset.openStream();
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public WebArchive getArchive() {
        return archive;
    }

    public static class Descriptor<D extends NamedModule> implements Asset {

        private final D descriptor;

        public Descriptor(final D descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public InputStream openStream() {
            try {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                JaxbJavaee.marshal(descriptor.getClass(), descriptor, out);
                return new ByteArrayInputStream(out.toByteArray());
            } catch (final JAXBException e) {
                throw new IllegalArgumentException("Unable to marshal descriptor", e);
            }
        }
    }
}
