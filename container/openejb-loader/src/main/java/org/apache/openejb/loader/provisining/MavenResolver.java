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

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class MavenResolver implements ArchiveResolver, ProvisioningResolverAware {
    private static final String REPO1 = "http://repo1.maven.org/maven2/";
    private static final String APACHE_SNAPSHOT = "https://repository.apache.org/snapshots/";
    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();
    static {
        FACTORY.setNamespaceAware(false);
        FACTORY.setValidating(false);
    }

    private ProvisioningResolver resolver;

    @Override
    public String prefix() {
        return "mvn";
    }

    @Override
    public InputStream resolve(final String url) {
        try {
            final String sanitized = sanitize(url);
            final String repo1Url = quickMvnUrl(sanitized);
            return resolveStream(repo1Url);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String sanitize(final String url) {
        return url.substring(prefix().length() + 1).replace(":", "/");
    }

    @Override
    public String name(final String rawLocation) {
        try {
            final String s = mvnArtifactPath(sanitize(rawLocation), "");
            return s.substring(s.lastIndexOf('/') + 1);
        } catch (final MalformedURLException e) {
            return rawLocation + ".jar";
        }
    }

    private InputStream resolveStream(final String repo1Url) throws MalformedURLException {
        if (resolver != null) {
            return resolver.resolveStream(repo1Url);
        }
        return SystemInstance.get().getComponent(ProvisioningResolver.class).resolveStream(repo1Url);
    }

    public String quickMvnUrl(final String raw) throws MalformedURLException {
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
        final Properties properties;
        if (SystemInstance.isInitialized()) {
            properties = SystemInstance.get().getProperties();
        } else {
            properties = System.getProperties();
        }
        return properties.getProperty("openejb.m2.home", System.getProperty("user.home") + "/.m2/repository/");
    }

    private String mvnArtifactPath(final String toParse, final String snapshotBase) throws MalformedURLException {
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
        if (snapshotBase != null && snapshotBase.startsWith("http") && version.endsWith(SNAPSHOT_SUFFIX)) {
            final String meta = snapshotBase + builder.toString() + "maven-metadata.xml";
            final URL url = new URL(meta);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream is = null;
            try {
                is = resolveStream(url.toExternalForm());
                if (is == null) {
                    throw new IllegalArgumentException("can't resolve " + url);
                }
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

    @Override
    public void setResolver(final ProvisioningResolver resolver) {
        this.resolver = resolver;
    }

    private static class QuickMvnMetadataParser extends DefaultHandler {
        private boolean readTs;
        private boolean readBn;
        private StringBuilder timestamp;
        private StringBuilder buildNumber;

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
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
