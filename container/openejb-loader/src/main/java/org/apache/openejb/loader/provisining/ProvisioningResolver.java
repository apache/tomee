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
package org.apache.openejb.loader.provisining;

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static java.util.Collections.singleton;

public class ProvisioningResolver {
    public static final String TEMP_DIR = "temp";
    public static final String OPENEJB_DEPLOYER_CACHE_FOLDER = "openejb.deployer.cache.folder";

    private final Map<String, ArchiveResolver> resolvers = new ConcurrentHashMap<>();
    private final ArchiveResolver fallbackResolver = new SimpleUrlResolver();

    public ProvisioningResolver() {
        resolvers.put("mvn", new MavenResolver());
        resolvers.put("http", new HttpResolver());
        resolvers.put("https", new HttpResolver());

        if (SystemInstance.isInitialized()) {
            final String userOnes = SystemInstance.get().getProperty("openejb.provisinig.archive-resolvers");
            if (userOnes != null) {
                for (final String u : userOnes.split(" *, *")) {
                    final String c = u.trim();
                    if (!c.isEmpty()) {
                        continue;
                    }
                    try {
                        final ArchiveResolver instance = ArchiveResolver.class.cast(ProvisioningResolver.class.getClassLoader().loadClass(c));
                        addResolver(instance);
                    } catch (final ClassNotFoundException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }

            SystemInstance.get().fireEvent(new ProvisiningResolverCreated(this));
        }
        for (final ArchiveResolver ar : resolvers.values()) {
            if (ProvisioningResolverAware.class.isInstance(ar)) {
                ProvisioningResolverAware.class.cast(ar).setResolver(this);
            }
        }
    }

    public void addResolver(final ArchiveResolver resolver) {
        if (resolvers.put(resolver.prefix(), resolver) != null) {
            Logger.getLogger(ProvisioningResolver.class.getName()).warning("Overriding resolver " + resolver.prefix() + " with " + resolver);
        }
    }

    public void removeResolver(final ArchiveResolver resolver) {
        resolvers.remove(resolver.prefix());
    }

    public Set<String> realLocation(final String rawLocation) {
        // if direct file path then use it
        final File file = new File(rawLocation);
        if (file.exists()) {
            return singleton(file.getAbsolutePath());
        }
        if (rawLocation.endsWith("*.jar")) {
            final File dir = new File(rawLocation.substring(0, rawLocation.length() - "*.jar".length()));
            if (dir.exists()) {
                final File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(final File dir, final String name) {
                        return name.endsWith(".jar") || name.endsWith(".zip");
                    }
                });
                final Set<String> paths = new HashSet<>(files == null ? 1 : files.length + 1);
                if (files != null) {
                    for (final File f : files) {
                        paths.add(f.getAbsolutePath());
                    }
                }
                paths.add(dir.getAbsolutePath()); // for config it is nice
                return paths;
            } else {
                throw new IllegalArgumentException(dir.getAbsolutePath() + " doesn't exist, *.jar is only supported locally");
            }
        }

        final String protocol = protocol(rawLocation);
        ArchiveResolver resolver = resolvers.get(protocol);
        if (resolver == null) {
            resolver = fallbackResolver;
        }

        final File destination = cacheFile(resolver.name(rawLocation));
        if (destination.exists()) {
            return singleton(destination.getAbsolutePath());
        }

        return singleton(doResolve(rawLocation, destination, resolver));
    }

    // don't use new URL() since that's really just a prefix
    private String protocol(final String rawLocation) {
        final int sep = rawLocation.indexOf(':');
        if (sep < 1) {
            throw new IllegalArgumentException("Not a local file but no prefix specified: " + rawLocation + ". So can't resolve");
        }
        return rawLocation.substring(0, sep);
    }

    private String doResolve(final String rawLocation, final File file, final ArchiveResolver resolver) {
        final InputStream resolverStream = resolver.resolve(rawLocation);
        if (LocalInputStream.class.isInstance(resolverStream)) {
            return LocalInputStream.class.cast(resolverStream).path;
        }

        if (resolverStream != null) {
            BufferedInputStream is = null;
            try {
                is = new BufferedInputStream(resolverStream);
                Files.mkdirs(file.getParentFile());
                IO.copy(is, file);
                return file.getAbsolutePath();
            } catch (final IOException ioe) {
                throw new IllegalArgumentException(ioe);
            } finally {
                IO.close(is);
            }
        } else {
            throw new IllegalArgumentException("Could not resolve (" + rawLocation + ')');
        }
    }

    public InputStream resolveStream(final String rawLocation) throws MalformedURLException {
        final File file = new File(rawLocation);
        if (file.exists()) {
            return new LocalInputStream(file.getAbsolutePath());
        }

        final String protocol = protocol(rawLocation);
        final ArchiveResolver resolver = resolvers.get(protocol);
        if (resolver != null) {
            return resolver.resolve(rawLocation);
        }
        return null;
    }

    public static String cache() {
        return System.getProperty(OPENEJB_DEPLOYER_CACHE_FOLDER, TEMP_DIR);
    }

    public static File cacheFile(final String path) {
        final String cache = cache();
        if (new File(cache).isAbsolute()) {
            return new File(cache, path);
        }
        return new File(SystemInstance.get().getBase().getDirectory(), cache + File.separator + path);
    }

    @Override
    public String toString() {
        return "ProvisioningResolver{" +
                "resolvers=" + resolvers +
                ", fallbackResolver=" + fallbackResolver +
                '}';
    }

    // used when a resolver wants to use a local file by calling back this facade resolver
    public static final class LocalInputStream extends InputStream {
        private final String path;
        private FileInputStream stream;

        private LocalInputStream(final String path) {
            this.path = path;
        }

        private FileInputStream stream() {
            if (stream == null) {
                try {
                    stream = new FileInputStream(path);
                } catch (final FileNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
            return stream;
        }

        @Override
        public int read() throws IOException {
            return stream().read();
        }

        @Override
        public int read(final byte[] b) throws IOException {
            return stream().read(b);
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            return stream().read(b, off, len);
        }

        @Override
        public synchronized void mark(final int readlimit) {
            stream().mark(readlimit);
        }

        @Override
        public int available() throws IOException {
            return stream().available();
        }

        @Override
        public long skip(final long n) throws IOException {
            return stream().skip(n);
        }

        @Override
        public synchronized void reset() throws IOException {
            stream().reset();
        }

        @Override
        public boolean markSupported() {
            return stream().markSupported();
        }

        @Override
        public void close() throws IOException {
            if (stream != null) {
                stream.close();
            }
            super.close();
        }
    }
}
