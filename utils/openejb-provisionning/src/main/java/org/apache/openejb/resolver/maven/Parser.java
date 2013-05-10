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

import org.ops4j.pax.url.maven.commons.MavenRepositoryURL;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class respects both Maven Coordinates
 *
 *  - groupId:artifactId:packaging:classifier:version
 *  - http://maven.apache.org/pom.html#Maven_Coordinates
 *
 * And OPS4j coordinates
 *
 *  - groupId/artifactId/version/packaging/classifier
 *  - https://ops4j1.jira.com/wiki/display/paxurl/Mvn+Protocol
 *
 */
public class Parser {
    private static final String SYNTAX = "mvn:[repository_url!]groupId/artifactId[/[version]/[type]]";
    private static final String REPOSITORY_SEPARATOR = "!";
    private static final String ARTIFACT_SEPARATOR = "/";
    private static final String VERSION_SNAPSHOT = "SNAPSHOT";
    private static final String FILE_SEPARATOR = "/";
    private static final String GROUP_SEPARATOR = "\\.";
    private static final String VERSION_SEPARATOR = "-";
    private static final String TYPE_SEPARATOR = ".";
    private static final String CLASSIFIER_SEPARATOR = "-";
    private static final String METADATA_FILE = "maven-metadata.xml";
    private static final String METADATA_FILE_LOCAL = "maven-metadata-local.xml";

    private final String group;
    private final String artifact;
    private final String version;
    private final String type;
    private final String classifier;
    private final String fullClassifier;

    private MavenRepositoryURL repositoryURL;

    public Parser(final String rawPath) throws MalformedURLException {

        if (rawPath == null) throw new MalformedURLException("Path cannot be null. Syntax " + SYNTAX);

        final boolean possibleMavenCoordinates = rawPath.contains(":");

        final String path = rawPath.replace(":", "/"); // mvn:G:A:V = mvn:G/A/V

        if (path.startsWith(REPOSITORY_SEPARATOR) || path.endsWith(REPOSITORY_SEPARATOR)) {
            throw new MalformedURLException("Path cannot start or end with " + REPOSITORY_SEPARATOR + ". Syntax " + SYNTAX);
        }

        final String part;

        if (path.contains(REPOSITORY_SEPARATOR)) {
            int pos = path.lastIndexOf(REPOSITORY_SEPARATOR);
            part = path.substring(pos + 1);
            repositoryURL = new MavenRepositoryURL(path.substring(0, pos) + "@snapshots");
        } else {
            part = path;
        }

        final List<String> segments = new ArrayList<String>(Arrays.asList(part.split(ARTIFACT_SEPARATOR)));

        if (segments.size() < 2 || segments.size() > 5) {
            throw new MalformedURLException("Invalid path. Syntax " + SYNTAX);
        }

        // If Maven Coordinates were used, rearrange the segments to the OPS4j format
        if (possibleMavenCoordinates && segments.get(segments.size() - 1).matches("[0-9].*")) {
            // position the version after the artifactId
            final String version = segments.remove(segments.size() - 1);
            segments.add(2, version);
        }

        final String[] coordinates = {null, null, "LATEST", "jar", null};

        for (int i = 0; i < segments.size(); i++) {
            final String value = segments.get(i).trim();
            if (value.length() != 0) {
                coordinates[i] = value;
            }
        }

        this.group = coordinates[0];
        this.artifact = coordinates[1];
        this.version = coordinates[2];
        this.type = coordinates[3];
        this.classifier = coordinates[4];
        this.fullClassifier = (this.classifier != null) ? CLASSIFIER_SEPARATOR + classifier : null;

        if (group == null) {
            throw new MalformedURLException("Invalid groupId. Syntax " + SYNTAX);
        }

        if (artifact == null) {
            throw new MalformedURLException("Invalid artifactId. Syntax " + SYNTAX);
        }
    }

    public String getGroup() {
        return group;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getArtifactPath() {
        return getArtifactPath(version);
    }

    public String getArtifactPath(final String version) {
        return new StringBuilder()
                .append(group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(artifact)
                .append(FILE_SEPARATOR)
                .append(version)
                .append(FILE_SEPARATOR)
                .append(artifact)
                .append(VERSION_SEPARATOR)
                .append(version)
                .append(fullClassifier)
                .append(TYPE_SEPARATOR)
                .append(type)
                .toString();
    }

    public String getSnapshotVersion(final String version, final String timestamp, final String buildnumber) {
        return version.replace(VERSION_SNAPSHOT, timestamp) + VERSION_SEPARATOR + buildnumber;
    }

    public String getSnapshotPath(final String version, final String timestamp, final String buildnumber) {
        return new StringBuilder()
                .append(group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(artifact)
                .append(FILE_SEPARATOR)
                .append(version)
                .append(FILE_SEPARATOR)
                .append(artifact)
                .append(VERSION_SEPARATOR)
                .append(getSnapshotVersion(version, timestamp, buildnumber))
                .append(fullClassifier)
                .append(TYPE_SEPARATOR)
                .append(type)
                .toString();
    }

    public String getVersionMetadataPath(final String version) {
        return new StringBuilder()
                .append(group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(artifact)
                .append(FILE_SEPARATOR)
                .append(version)
                .append(FILE_SEPARATOR)
                .append(METADATA_FILE)
                .toString();
    }

    public String getVersionLocalMetadataPath(final String version) {
        return new StringBuilder()
                .append(group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(artifact)
                .append(FILE_SEPARATOR)
                .append(version)
                .append(FILE_SEPARATOR)
                .append(METADATA_FILE_LOCAL)
                .toString();
    }

    public String getArtifactLocalMetdataPath() {
        return new StringBuilder()
                .append(group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(artifact)
                .append(FILE_SEPARATOR)
                .append(METADATA_FILE_LOCAL)
                .toString();
    }

    public String getArtifactMetdataPath() {
        return new StringBuilder()
                .append(group.replaceAll(GROUP_SEPARATOR, FILE_SEPARATOR))
                .append(FILE_SEPARATOR)
                .append(artifact)
                .append(FILE_SEPARATOR)
                .append(METADATA_FILE)
                .toString();
    }

    public MavenRepositoryURL getRepositoryURL() {
        return repositoryURL;
    }
}
