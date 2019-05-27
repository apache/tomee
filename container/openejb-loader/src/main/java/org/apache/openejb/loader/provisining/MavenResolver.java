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
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class MavenResolver implements ArchiveResolver, ProvisioningResolverAware {
    private static final String REPO1 = "http://repo1.maven.org/maven2/";
    private static final String APACHE_SNAPSHOT = "https://repository.apache.org/snapshots/";
    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";
    private static final AtomicReference<DocumentBuilder> BUILDER = new AtomicReference<>(null);

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
        final int sep = url.indexOf('!') + 1;
        final String value = url.substring(prefix().length() + 1);
        return value.substring(0, sep) + value.substring(sep).replace(":", "/");
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
        return Objects.requireNonNull(SystemInstance.get().getComponent(ProvisioningResolver.class)).resolveStream(repo1Url);
    }

    public String quickMvnUrl(final String raw) throws MalformedURLException {
        final String base;
        if (raw.contains(SNAPSHOT_SUFFIX) && raw.contains("apache")) {
            base = System.getProperty("openejb.deployer.snapshot.repository", APACHE_SNAPSHOT);
        } else {
            base = System.getProperty("openejb.deployer.repository", REPO1);
        }

        final StringBuilder builder = new StringBuilder();
        final String toParse;
        if (!raw.contains("!")) {
            // try first local file with default maven settings
            final File file = new File(m2Home() + mvnArtifactPath(raw, null));
            if (file.exists()) {
                return file.getAbsolutePath();
            }

            // else use repo1
            toParse = raw;

            // try first locally
        } else {
            final int repoIdx = raw.lastIndexOf('!');
            toParse = raw.substring(repoIdx + 1);
        }

        builder.append(mvnArtifactPath(toParse, base));

        return builder.toString();
    }

    /**
     * Locate the .m2 repository
     * First look for the settings.xml value, else assume it is in the .m2 directory
     *
     * @return String path to repository
     */
    private static String m2Home() {

        final Properties properties;
        if (SystemInstance.isInitialized()) {
            properties = SystemInstance.get().getProperties();
        } else {
            properties = System.getProperties();
        }

        String home = "";

        File f = new File(properties.getProperty("openejb.m2.settings", System.getProperty("user.home") + "/.m2/settings.xml"));
        if (f.exists()) {
            try {

                DocumentBuilder builder = BUILDER.get();
                if (null == builder) {
                    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    builder = factory.newDocumentBuilder();
                    BUILDER.set(builder);
                }

                final Document document = builder.parse(f);
                final XPathFactory xpf = XPathFactory.newInstance();
                final XPath xp = xpf.newXPath();
                home = xp.evaluate("//settings/localRepository/text()", document.getDocumentElement());
            } catch (final Exception ignore) {
                //no-op
            }
        }

        if (home.isEmpty()) {
            f = new File(properties.getProperty("openejb.m2.home", System.getProperty("user.home") + "/.m2/repository/"));

            if (f.exists()) {
                home = f.getAbsolutePath();
            }
        }

        return (home.endsWith("/") ? home : home + "/");
    }

    private String mvnArtifactPath(final String toParse, final String snapshotBase) throws MalformedURLException {
        final String[] segments = toParse.split("/");
        if (segments.length < 3) {
            throw new MalformedURLException("Invalid path. " + toParse);
        }

        final String group = segments[0];
        if (group.trim().isEmpty()) {
            throw new MalformedURLException("Invalid groupId. " + toParse);
        }

        final String artifact = segments[1];
        if (artifact.trim().isEmpty()) {
            throw new MalformedURLException("Invalid artifactId. " + toParse);
        }

        String version = segments[2];
        if (version.trim().isEmpty()) {
            throw new MalformedURLException("Invalid artifactId. " + toParse);
        }

        final String base = snapshotBase == null || snapshotBase.isEmpty() ? "" : (snapshotBase + (!snapshotBase.endsWith("/") ? "/" : ""));

        if (("LATEST".equals(version) || "LATEST-SNAPSHOT".equals(version)) && base.startsWith("http")) {
            final String meta = base + group.replace('.', '/') + "/" + artifact + "/maven-metadata.xml";
            final URL url = new URL(meta);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream is = null;
            try {
                is = resolveStream(url.toExternalForm());
                if (is == null) {
                    throw new IllegalArgumentException("can't resolve " + url);
                }
                IO.copy(is, out);
                version = extractRealVersion(version, out);
            } catch (final Exception e) {
                // no-op
            } finally {
                IO.close(is);
            }
        }

        String artifactVersion;
        if (version.endsWith("-SNAPSHOT") && base.startsWith("http")) {
            final String meta = base + group.replace('.', '/') + "/" + artifact + "/" + version + "/maven-metadata.xml";
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

        final StringBuilder builder = new StringBuilder(base);
        builder.append(group.replace('.', '/')).append("/");
        builder.append(artifact).append("/");
        builder.append(version).append("/");
        builder.append(artifact).append("-").append(artifactVersion);
        if (fullClassifier != null) {
            builder.append(fullClassifier);
        }

        return builder.append(".").append(type).toString();
    }

    private static String extractRealVersion(String version, final ByteArrayOutputStream out) {
        final QuickMvnMetadataParser handler = new QuickMvnMetadataParser();
        try {
            final SAXParser parser = FACTORY.newSAXParser();
            parser.parse(new ByteArrayInputStream(out.toByteArray()), handler);
            if (!version.endsWith(SNAPSHOT_SUFFIX) && handler.release != null) {
                version = handler.release.toString();
            } else if (handler.latest != null) {
                version = handler.latest.toString();
            }
        } catch (final Exception e) {
            // no-op: not parseable so ignoring
        }
        return version;
    }

    private static String extractLastSnapshotVersion(final String defaultVersion, final InputStream metadata) {
        final QuickMvnMetadataParser handler = new QuickMvnMetadataParser();
        try {
            final SAXParser parser = FACTORY.newSAXParser();
            parser.parse(metadata, handler);
            if (handler.timestamp != null && handler.buildNumber != null) {
                return defaultVersion.substring(0, defaultVersion.length() - SNAPSHOT_SUFFIX.length())
                        + "-" + handler.timestamp + "-" + handler.buildNumber;
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
        private StringBuilder timestamp;
        private StringBuilder buildNumber;
        private StringBuilder latest;
        private StringBuilder release;
        private StringBuilder text;

        @Override
        public void startElement(final String uri, final String localName,
                                 final String qName, final Attributes attributes) throws SAXException {
            if ("timestamp".equalsIgnoreCase(qName)) {
                timestamp = new StringBuilder();
                text = timestamp;
            } else if ("buildNumber".equalsIgnoreCase(qName)) {
                buildNumber = new StringBuilder();
                text = buildNumber;
            } else if ("latest".equalsIgnoreCase(qName)) {
                latest = new StringBuilder();
                text = latest;
            } else if ("release".equalsIgnoreCase(qName)) {
                release = new StringBuilder();
                text = release;
            }
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) {
            if (text != null) {
                text.append(new String(ch, start, length));
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {
            text = null;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
