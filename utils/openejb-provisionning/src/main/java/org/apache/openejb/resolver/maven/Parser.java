/**
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
package org.apache.openejb.resolver.maven;

import java.net.MalformedURLException;
import org.ops4j.pax.url.maven.commons.MavenRepositoryURL;

public class Parser {
    public static final String VERSION_LATEST = "LATEST";
    private static final String SYNTAX = "mvn:[repository_url!]groupId/artifactId[/[version]/[type]]";
    private static final String REPOSITORY_SEPARATOR = "!";
    private static final String ARTIFACT_SEPARATOR = "/";
    private static final String VERSION_SNAPSHOT = "SNAPSHOT";
    private static final String TYPE_JAR = "jar";
    private static final String FILE_SEPARATOR = "/";
    private static final String GROUP_SEPARATOR = "\\.";
    private static final String VERSION_SEPARATOR = "-";
    private static final String TYPE_SEPARATOR = ".";
    private static final String CLASSIFIER_SEPARATOR = "-";
    private static final String METADATA_FILE = "maven-metadata.xml";
    private static final String METADATA_FILE_LOCAL = "maven-metadata-local.xml";
    private String m_group;
    private String m_artifact;
    private String m_version;
    private String m_type;
    private String m_classifier;
    private String m_fullClassifier;
    private MavenRepositoryURL m_repositoryURL;

    public Parser(final String rawPath)
            throws MalformedURLException {
        if (rawPath == null) {
            throw new MalformedURLException("Path cannot be null. Syntax " + SYNTAX);
        }
        final String path = rawPath.replace(":", "/"); // mvn:G:A:V = mvn:G/A/V
        if (path.startsWith(REPOSITORY_SEPARATOR) || path.endsWith(REPOSITORY_SEPARATOR)) {
            throw new MalformedURLException(
                    "Path cannot start or end with " + REPOSITORY_SEPARATOR + ". Syntax " + SYNTAX
            );
        }
        if (path.contains(REPOSITORY_SEPARATOR)) {
            int pos = path.lastIndexOf(REPOSITORY_SEPARATOR);
            parseArtifactPart(path.substring(pos + 1));
            m_repositoryURL = new MavenRepositoryURL(path.substring(0, pos) + "@snapshots");
        } else {
            parseArtifactPart(path);
        }
    }

    private void parseArtifactPart(final String part)
            throws MalformedURLException {
        String[] segments = part.split(ARTIFACT_SEPARATOR);
        if (segments.length < 2) {
            throw new MalformedURLException("Invalid path. Syntax " + SYNTAX);
        }
        // we must have a valid group
        m_group = segments[0];
        if (m_group.trim().length() == 0) {
            throw new MalformedURLException("Invalid groupId. Syntax " + SYNTAX);
        }
        // valid artifact
        m_artifact = segments[1];
        if (m_artifact.trim().length() == 0) {
            throw new MalformedURLException("Invalid artifactId. Syntax " + SYNTAX);
        }
        // version is optional but we have a default value
        m_version = VERSION_LATEST;
        if (segments.length >= 3 && segments[2].trim().length() > 0) {
            m_version = segments[2];
        }
        // type is optional but we have a default value
        m_type = TYPE_JAR;
        if (segments.length >= 4 && segments[3].trim().length() > 0) {
            m_type = segments[3];
        }
        // classifier is optional (if not pressent or empty we will have a null classsifier
        m_fullClassifier = "";
        if (segments.length >= 5 && segments[4].trim().length() > 0) {
            m_classifier = segments[4];
            m_fullClassifier = CLASSIFIER_SEPARATOR + m_classifier;
        }
    }

    public String getGroup() {
        return m_group;
    }

    public String getArtifact() {
        return m_artifact;
    }

    public String getVersion() {
        return m_version;
    }

    public String getType() {
        return m_type;
    }

    public String getClassifier() {
        return m_classifier;
    }

    public String getArtifactPath() {
        return getArtifactPath(m_version);
    }

    public String getArtifactPath(final String version) {
        return new StringBuilder()
                .append(m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(m_artifact)
                .append(FILE_SEPARATOR)
                .append(version)
                .append(FILE_SEPARATOR)
                .append(m_artifact)
                .append(VERSION_SEPARATOR)
                .append(version)
                .append(m_fullClassifier)
                .append(TYPE_SEPARATOR)
                .append(m_type)
                .toString();
    }

    public String getSnapshotVersion(final String version, final String timestamp, final String buildnumber) {
        return version.replace(VERSION_SNAPSHOT, timestamp) + VERSION_SEPARATOR + buildnumber;
    }

    public String getSnapshotPath(final String version, final String timestamp, final String buildnumber) {
        return new StringBuilder()
                .append(m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(m_artifact)
                .append(FILE_SEPARATOR)
                .append(version)
                .append(FILE_SEPARATOR)
                .append(m_artifact)
                .append(VERSION_SEPARATOR)
                .append(getSnapshotVersion(version, timestamp, buildnumber))
                .append(m_fullClassifier)
                .append(TYPE_SEPARATOR)
                .append(m_type)
                .toString();
    }

    public String getVersionMetadataPath(final String version) {
        return new StringBuilder()
                .append(m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(m_artifact)
                .append(FILE_SEPARATOR)
                .append(version)
                .append(FILE_SEPARATOR)
                .append(METADATA_FILE)
                .toString();
    }

    public String getVersionLocalMetadataPath(final String version) {
        return new StringBuilder()
                .append(m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(m_artifact)
                .append(FILE_SEPARATOR)
                .append(version)
                .append(FILE_SEPARATOR)
                .append(METADATA_FILE_LOCAL)
                .toString();
    }

    public String getArtifactLocalMetdataPath() {
        return new StringBuilder()
                .append(m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(m_artifact)
                .append(FILE_SEPARATOR)
                .append(METADATA_FILE_LOCAL)
                .toString();
    }

    public String getArtifactMetdataPath() {
        return new StringBuilder()
                .append(m_group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(m_artifact)
                .append(FILE_SEPARATOR)
                .append(METADATA_FILE)
                .toString();
    }

    public MavenRepositoryURL getRepositoryURL() {
        return m_repositoryURL;
    }
}
