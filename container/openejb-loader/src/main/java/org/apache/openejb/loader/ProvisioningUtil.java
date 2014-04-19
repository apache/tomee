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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
import java.util.logging.Logger;

public class ProvisioningUtil {
    private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();
    static {
        FACTORY.setNamespaceAware(false);
        FACTORY.setValidating(false);
    }

    public static final String OPENEJB_DEPLOYER_CACHE_FOLDER = "openejb.deployer.cache.folder";
    public static final String HTTP_PREFIX = "http";
    public static final String MVN_PREFIX = "mvn:";
    public static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    private static final int CONNECT_TIMEOUT = 10000;

    private static final String ADDITIONAL_LIB_CONFIG = "provisioning.properties";
    private static final String REPO1 = "http://repo1.maven.org/maven2/";
    private static final String APACHE_SNAPSHOT = "https://repository.apache.org/snapshots/";
    private static final String ZIP_KEY = "zip";
    private static final String DESTINATION_KEY = "destination";
    private static final String JAR_KEY = "jar";

    public static final String TEMP_DIR = "temp";

    private ProvisioningUtil() {
        // no-op
    }

    public static String cache() {
        return System.getProperty(OPENEJB_DEPLOYER_CACHE_FOLDER, TEMP_DIR);
    }

    public static File cacheFile(final String path) {
        return new File(SystemInstance.get().getBase().getDirectory(), cache() + File.separator + path);
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
        for (final Proxy proxy : ProxySelector.getDefault().select(source)) {
            // try to connect
            try {
                final URLConnection urlConnection = url.openConnection(proxy);
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                return new BufferedInputStream(urlConnection.getInputStream());
            } catch (final IOException e) {
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

    public static String realLocation(final String rawLocation) {
        if (rawLocation.startsWith(HTTP_PREFIX)) {
            final File file = cacheFile(lastPart(rawLocation));
            if (file.exists()) {
                return file.getAbsolutePath();
            }

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
        try { // full maven resolution
            final Class<?> clazz = ProvisioningUtil.class.getClassLoader().loadClass("org.apache.openejb.resolver.Resolver");
            final LocationResolver instance = (LocationResolver) clazz.newInstance();
            return instance.resolve(rawLocation);
        } catch (final Throwable e) { // NoClassDefFoundError is not an exception
            return fallback(rawLocation);
        }
    }

    private static String fallback(final String rawLocation) {
        if (rawLocation.startsWith(MVN_PREFIX)) {
            try {
                final String repo1Url = quickMvnUrl(rawLocation.substring(MVN_PREFIX.length()).replace(":", "/"));
                return realLocation(repo1Url);
            } catch (MalformedURLException e1) {
                Logger.getLogger(ProvisioningUtil.class.getName()).severe("Can't find " + rawLocation);
            }
        } else { // try url
            try {
                final File file = cacheFile(lastPart(rawLocation));
                final URL url = new URL(rawLocation);
                InputStream is = null;
                try {
                    is = new BufferedInputStream(url.openStream());
                    IO.copy(is, file);
                    return file.getAbsolutePath();
                } finally {
                    IO.close(is);
                }
            } catch (final Exception e1) {
                // no-op
            }
        }

        // if it was not an url that's just a file path
        return rawLocation;
    }

    public static String quickMvnUrl(final String raw) throws MalformedURLException {
        final String base;
        if (raw.contains(SNAPSHOT_SUFFIX) && raw.contains("apache")) {
            base = APACHE_SNAPSHOT;
        } else {
            base = REPO1;
        }

        StringBuilder builder = new StringBuilder();
        final String toParse;
        if (!raw.contains("!")) {
            // try first local file with default maven settings
            final File file = new File(m2Home() + mvnArtifactPath(raw, null));
            if (file.exists()) {
                return file.getAbsolutePath();
            }

            // else use repo1
            builder = new StringBuilder();
            builder.append(base);
            toParse = raw;

            // try first locally
        } else {
            final int repoIdx = raw.lastIndexOf("!");
            toParse = raw.substring(repoIdx + 1);
            final String repo = raw.substring(0, repoIdx);
            builder.append(repo);
            if (!repo.endsWith("/")) {
                builder.append("/");
            }
        }

        builder.append(mvnArtifactPath(toParse, base));

        return builder.toString();
    }

    private static String m2Home() {
        return SystemInstance.get().getProperty("openejb.m2.home", System.getProperty("user.home") + "/.m2/repository/");
    }

    private static String mvnArtifactPath(final String toParse, final String snapshotBase) throws MalformedURLException {
        final StringBuilder builder = new StringBuilder();
        final String[] segments = toParse.split("/");
        if (segments.length < 3) {
            throw new MalformedURLException("Invalid path. " + toParse);
        }

        final String group = segments[0];
        if (group.trim().isEmpty()) {
            throw new MalformedURLException("Invalid groupId. " + toParse);
        }
        builder.append(group.replace('.', '/')).append("/");

        final String artifact = segments[1];
        if (artifact.trim().isEmpty()) {
            throw new MalformedURLException("Invalid artifactId. " + toParse);
        }
        builder.append(artifact).append("/");

        final String version = segments[2];
        if (version.trim().isEmpty()) {
            throw new MalformedURLException("Invalid artifactId. " + toParse);
        }

        builder.append(version).append("/");

        String artifactVersion;
        if (snapshotBase != null && snapshotBase.startsWith(HTTP_PREFIX) && version.endsWith(SNAPSHOT_SUFFIX)) {
            final String meta = new StringBuilder(snapshotBase).append(builder.toString()).append("maven-metadata.xml").toString();
            final URL url = new URL(meta);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream is = null;
            try {
                is = inputStreamTryingProxies(url.toURI());
                IO.copy(is, out);
                artifactVersion = extractLastSnapshotVersion(version, new ByteArrayInputStream(out.toByteArray()));
            } catch (final Exception e) {
                artifactVersion = version;
            } finally {
                IO.close(is);
            }
        } else {
            artifactVersion = version;
        }

        String type = "jar";
        if (segments.length >= 4 && segments[3].trim().length() > 0) {
            type = segments[3];
        }

        String fullClassifier = null;
        if (segments.length >= 5 && segments[4].trim().length() > 0) {
            fullClassifier = "-" + segments[4];
        }

        builder.append(artifact).append("-").append(artifactVersion);

        if (fullClassifier != null) {
            builder.append(fullClassifier);
        }

        return builder.append(".").append(type).toString();
    }

    private static String extractLastSnapshotVersion(final String defaultVersion, final InputStream metadata) {
        final QuickMvnMetadataParser handler = new QuickMvnMetadataParser();
        try {
            final SAXParser parser = FACTORY.newSAXParser();
            parser.parse(metadata, handler);
            if (handler.timestamp != null && handler.buildNumber != null) {
                return defaultVersion.substring(0, defaultVersion.length() - SNAPSHOT_SUFFIX.length()) + "-" + handler.timestamp.toString() + "-" + handler.buildNumber.toString();
            }
        } catch (final Exception e) {
            // no-op: not parseable so ignoring
        }
        return defaultVersion;
    }

    public static Collection<File> addAdditionalLibraries() throws IOException {
        final File conf = SystemInstance.get().getConf(ADDITIONAL_LIB_CONFIG);
        if (conf == null || !conf.exists()) {
            return Collections.emptyList();
        }

        final Properties additionalLibProperties = IO.readProperties(conf);

        final List<String> libToCopy = new ArrayList<String>();
        final String toCopy = additionalLibProperties.getProperty(JAR_KEY);
        if (toCopy != null) {
            for (final String lib : toCopy.split(",")) {
                libToCopy.add(realLocation(lib.trim()));
            }
        }
        final String toExtract = additionalLibProperties.getProperty(ZIP_KEY);
        if (toExtract != null) {
            for (final String zip : toExtract.split(",")) {
                libToCopy.addAll(extract(realLocation(zip)));
            }
        }

        final File destination;
        if (additionalLibProperties.containsKey(DESTINATION_KEY)) {
            destination = new File(additionalLibProperties.getProperty(DESTINATION_KEY));
        } else {
            destination = new File(SystemInstance.get().getBase().getDirectory(), Embedder.ADDITIONAL_LIB_FOLDER);
        }
        if (!destination.exists()) {
            Files.mkdirs(destination);
        }

        final Collection<File> newFiles = new ArrayList<File>(libToCopy.size());
        for (final String lib : libToCopy) {
            newFiles.add(copy(new File(lib), destination));
        }
        return newFiles;
    }

    private static File copy(final File file, final File lib) throws IOException {
        final File dest = new File(lib, file.getName());
        if (dest.exists()) {
            return null;
        }
        IO.copy(file, dest);
        return dest;
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

        for (final File file : files) {
            if (file.isDirectory()) {
                libs.addAll(list(file));
            } else {
                libs.add(file.getAbsolutePath());
            }
        }
        return libs;
    }

    private static class QuickMvnMetadataParser extends DefaultHandler {
        private boolean readTs = false;
        private boolean readBn = false;
        private StringBuilder timestamp = null;
        private StringBuilder buildNumber = null;

        @Override
        public void startElement(final String uri, final String localName,
                                 final String qName, final Attributes attributes) throws SAXException {
            if ("timestamp".equalsIgnoreCase(qName)) {
                readTs = true;
                timestamp = new StringBuilder();
            } else if ("buildNumber".equalsIgnoreCase(qName)) {
                readBn = true;
                buildNumber = new StringBuilder();
            }
        }

        @Override
        public void characters(final char ch[], final int start, final int length) throws SAXException {
            if (readBn && buildNumber != null) {
                buildNumber.append(new String(ch, start, length));
            } else if (readTs && timestamp != null) {
                timestamp.append(new String(ch, start, length));
            }
        }

        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if ("timestamp".equalsIgnoreCase(qName)) {
                readTs = false;
            } else if ("buildNumber".equalsIgnoreCase(qName)) {
                readBn = false;
            }
        }
    }
}
