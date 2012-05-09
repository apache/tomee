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
package org.apache.openejb.loader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ProvisioningUtil {
    public static final String OPENEJB_DEPLOYER_CACHE_FOLDER = "openejb.deployer.cache.folder";
    public static final String HTTP_PREFIX = "http";
    private static final int CONNECT_TIMEOUT = 10000;

    private static final String ADDITIONAL_LIB_CONFIG = "provisioning.properties";
    private static final String ZIP_KEY = "zip";
    private static final String DESTINATION_KEY = "destination";
    private static final String JAR_KEY = "jar";
    public static final String TEMP_DIR = "temp";

    private ProvisioningUtil() {
        // no-op
    }

    public static String cache() {
        return System.getProperty(OPENEJB_DEPLOYER_CACHE_FOLDER, "temp");
    }

    public static File cacheFile(final String path) {
        return new File(SystemInstance.get().getBase().getDirectory(),
                cache() + File.separator + path);
    }

    public static String copyTryingProxies(final URI source, final File destination) throws Exception {
        final InputStream is = inputStreamTryingProxies(source);
        if (is == null) {
            return null;
        }

        try {
            IO.copy(is, destination);
        } finally {
            IO.close(is);
        }
        return destination.getAbsolutePath();
    }

    public static InputStream inputStreamTryingProxies(final URI source) throws Exception {
        final URL url = source.toURL();
        for (Proxy proxy : ProxySelector.getDefault().select(source)) {
            // try to connect
            try {
                URLConnection urlConnection = url.openConnection(proxy);
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                return new BufferedInputStream(urlConnection.getInputStream());
            } catch (IOException e) {
                // ignored
            }
        }
        return null;
    }

    public static String lastPart(final String location) {
        final int idx = location.lastIndexOf('/');
        if (idx <= 0) {
            return location;
        }
        return location.substring(idx + 1, location.length());
    }

    public static String realLocation(String rawLocation) {
        if (rawLocation.startsWith(HTTP_PREFIX)) {
            final File file = cacheFile(lastPart(rawLocation));

            String path = null;
            try {
                path = copyTryingProxies(new URI(rawLocation), file);
            } catch (Exception e1) {
                // ignored
            }

            if (path != null) {
                return path;
            }
        }
        try {
            final Class<?> clazz = ProvisioningUtil.class.getClassLoader().loadClass("org.apache.openejb.resolver.Resolver");
            final LocationResolver instance = (LocationResolver) clazz.newInstance();
            return instance.resolve(rawLocation);
        } catch (Exception e) {
            return rawLocation;
        }
    }

    public static void addAdditionalLibraries() throws IOException {
        final File conf = new File(SystemInstance.get().getBase().getDirectory("conf"), ADDITIONAL_LIB_CONFIG);
        if (!conf.exists()) {
            return;
        }

        final Properties additionalLibProperties = IO.readProperties(conf);

        final List<String> libToCopy = new ArrayList<String>();
        final String toCopy = additionalLibProperties.getProperty(JAR_KEY);
        if (toCopy != null) {
            for (String lib : toCopy.split(",")) {
                libToCopy.add(realLocation(lib.trim()));
            }
        }
        final String toExtract = additionalLibProperties.getProperty(ZIP_KEY);
        if (toExtract != null) {
            for (String zip : toExtract.split(",")) {
                libToCopy.addAll(extract(realLocation(zip)));
            }
        }

        File destination;
        if (additionalLibProperties.containsKey(DESTINATION_KEY)) {
            destination = new File(additionalLibProperties.getProperty(DESTINATION_KEY));
        } else {
            destination = new File(SystemInstance.get().getBase().getDirectory(), Embedder.ADDITIONAL_LIB_FOLDER);
        }
        if (!destination.exists()) {
            Files.mkdirs(destination);
        }

        for (String lib : libToCopy) {
            copy(new File(lib), destination);
        }
    }

    private static void copy(final File file, final File lib) throws IOException {
        final File dest = new File(lib, file.getName());
        if (dest.exists()) {
            return;
        }
        IO.copy(file, dest);
    }

    private static Collection<String> extract(final String zip) throws IOException {
        final File tmp = new File(SystemInstance.get().getBase().getDirectory(), TEMP_DIR);
        if (!tmp.exists()) {
            try {
                Files.mkdirs(tmp);
            } catch (Files.FileRuntimeException fre) {
                // ignored
            }
        }

        final File zipFile = new File(realLocation(zip));
        final File extracted = new File(tmp, zipFile.getName().replace(".zip", ""));
        if (extracted.exists()) {
            return list(extracted);
        } else {
            Files.mkdirs(extracted);
        }

        Zips.unzip(zipFile, extracted);
        return list(extracted);
    }

    private static Collection<String> list(final File dir) {
        if (dir == null) {
            return Collections.emptyList();
        }

        final Collection<String> libs = new ArrayList<String>();
        final File[] files = dir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        for (File file : files) {
            if (file.isDirectory()) {
                libs.addAll(list(file));
            } else {
                libs.add(file.getAbsolutePath());
            }
        }
        return libs;
    }
}
