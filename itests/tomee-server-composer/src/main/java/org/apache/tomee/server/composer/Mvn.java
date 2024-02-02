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
package org.apache.tomee.server.composer;

import org.tomitribe.util.Files;
import org.tomitribe.util.JarLocation;

import java.io.File;
import java.util.logging.Logger;

public class Mvn {

    private static final Logger LOGGER = Logger.getLogger(Mvn.class.getName());

    private Mvn() {
        // no-op
    }

    /**
     * Expected format
     *
     * org.apache.tomee:apache-tomee:zip:plus:10.0.0-M1-SNAPSHOT
     */
    public static File mvn(final String coordinates) {
        final String[] parts = coordinates.split(":");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Expected format with 5 parts such as 'org.apache.tomee:apache-tomee:zip:plus:10.0.0-M1-SNAPSHOT'.  Found " + coordinates);
        }

        final String group = parts[0];
        final String artifact = parts[1];
        final String version = parts[4];
        final String packaging = parts[2];
        final String classifier = parts[3];
        return mvn(group, artifact, version, packaging, classifier);
    }

    public static File mvn(final String group, final String artifact, final String version, final String packaging, final String classifier) {

        File file = JarLocation.get();
        while (!file.getName().equals("org")) {
            file = file.getParentFile();
        }
        file = file.getParentFile();

        final File archive = Files.file(file, group.replace('.', '/'), artifact, version, String.format("%s-%s-%s.%s", artifact, version, classifier, packaging));

        if (!archive.exists()) {
            final String s = "Declare a dependency in your pom.xml on " + group + ":" + artifact + ":" + version + ":tar.gz:" + classifier + ".  For example: \n    <dependency>\n" +
                    "      <groupId>" + group + "</groupId>\n" +
                    "      <artifactId>" + artifact + "</artifactId>\n" +
                    "      <version>" + version + "</version>\n" +
                    "      <type>" + packaging + "</type>\n" +
                    "      <classifier>" + classifier + "</classifier>\n" +
                    "      <exclusions>\n" +
                    "        <exclusion>\n" +
                    "          <groupId>*</groupId>\n" +
                    "          <artifactId>*</artifactId>\n" +
                    "        </exclusion>\n" +
                    "      </exclusions>\n" +
                    "    </dependency>\n";
            LOGGER.severe(s);
        }
        Files.exists(archive);
        Files.file(archive);
        Files.readable(archive);
        return archive;
    }

}
