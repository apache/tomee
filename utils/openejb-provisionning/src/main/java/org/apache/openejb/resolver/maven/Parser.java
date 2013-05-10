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

/**
 * This class doesn't seem compliant with the documented Maven Coordinates of
 *
 *  - groupId:artifactId:packaging:classifier:version
 *
 * http://maven.apache.org/pom.html#Maven_Coordinates
 *
 * For example, per the output of `mvn dependency:tree` TomEE is:
 *
 *  - org.apache.openejb:apache-tomee:zip:webprofile:1.6.0-SNAPSHOT
 *
 * It instead seems to use its own non-maven coordinates of
 *
 *  - groupId:artifactId:version:packaging:classifier
 *
 */
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

    private final String group;
    private final String artifact;
    private final String version;
    private final String type;
    private final String classifier;
    private final String fullClassifier;

    private MavenRepositoryURL repositoryURL;

    public Parser(final String rawPath) throws MalformedURLException {

        if (rawPath == null) throw new MalformedURLException("Path cannot be null. Syntax " + SYNTAX);

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

        String[] segments = part.split(ARTIFACT_SEPARATOR);

        if (segments.length < 2) {
            throw new MalformedURLException("Invalid path. Syntax " + SYNTAX);
        }

        // we must have a valid group
        group = segments[0];

        if (group.trim().length() == 0) {
            throw new MalformedURLException("Invalid groupId. Syntax " + SYNTAX);
        }

        // valid artifact
        artifact = segments[1];

        if (artifact.trim().length() == 0) {
            throw new MalformedURLException("Invalid artifactId. Syntax " + SYNTAX);
        }

        // version is optional but we have a default value
        String version = VERSION_LATEST;

        if (segments.length >= 3 && segments[2].trim().length() > 0) {
            version = segments[2];
        }

        // type is optional but we have a default value
        String type = TYPE_JAR;

        if (segments.length >= 4 && segments[3].trim().length() > 0) {
            type = segments[3];
        }

        // classifier is optional (if not pressent or empty we will have a null classsifier

        String fullClassifier = "";

        if (segments.length >= 5 && segments[4].trim().length() > 0) {
            classifier = segments[4];
            fullClassifier = CLASSIFIER_SEPARATOR + classifier;
        } else {
            classifier = null;
        }

        this.version = version;
        this.type = type;
        this.fullClassifier = fullClassifier;

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
