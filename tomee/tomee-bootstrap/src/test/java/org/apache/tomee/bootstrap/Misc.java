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
package org.apache.tomee.bootstrap;

import lombok.Getter;
import lombok.ToString;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.Zips;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Misc {

    public static void main(String[] args) throws IOException {

        final Repository repository = Repository.build();

        final File file = new File("/Users/dblevins/work/apache/downloads/apache-tomee-8.0.0-webprofile.zip");
        final File tmpdir = Files.tmpdir();
        Zips.unzip(file, tmpdir);

        final List<File> jars = Files.collect(tmpdir, ".*.jar");

        final List<Artifact> collect = jars.stream()
                .filter(jar -> !jar.getName().equals("bootstrap.jar"))
                .filter(jar -> !jar.getName().equals("catalina-ant.jar"))
                .filter(jar -> !jar.getName().startsWith("tomcat-i18n"))
                .map(repository::from)
                .sorted()
                .peek(artifact -> System.out.print(artifact.asDep()))
                .collect(Collectors.toList());


        collect.stream().forEach(System.out::println);


        System.out.println(tmpdir.getAbsolutePath());
    }

    public static class Repository {
        private final Map<String, File> artifacts = new HashMap<>();
        private final File path = new File("/Users/dblevins/.m2/repository/");

        public static Repository build() {
            final Repository repository = new Repository();

            final List<File> jars = Files.collect(repository.path, ".*\\.jar");
            for (final File jar : jars) {
                repository.artifacts.put(jar.getName(), jar);
            }

            return repository;
        }

        public Artifact from(final File jar) {
            if (jar.getName().equals("commons-daemon.jar")) {
                return new Artifact("commons-daemon", "commons-daemon", "1.1.0");
            }

            if (jar.getName().equals("tomcat-juli.jar")) {
                return new Artifact("org.apache.tomee", "tomee-juli", "${project.version}");
            }

            if (jar.getName().equals("catalina-ha.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-catalina-ha", "9.0.22");
            }

            if (jar.getName().equals("catalina-storeconfig.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-storeconfig", "9.0.22");
            }

            if (jar.getName().equals("catalina-tribes.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-tribes", "9.0.22");
            }

            if (jar.getName().equals("catalina.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-catalina", "9.0.22");
            }

            if (jar.getName().equals("el-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-el-api", "9.0.22");
            }

            if (jar.getName().equals("jasper-el.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jasper-el", "9.0.22");
            }

            if (jar.getName().equals("jasper.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jasper", "9.0.22");
            }

            if (jar.getName().equals("jaspic-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jaspic-api", "9.0.22");
            }

            if (jar.getName().equals("servlet-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-servlet-api", "9.0.22");
            }
            if (jar.getName().equals("websocket-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-websocket-api", "9.0.22");
            }
            if (jar.getName().equals("tomcat-coyote.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-coyote", "9.0.22");
            }
            if (jar.getName().equals("tomcat-dbcp.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-dbcp", "9.0.22");
            }
            if (jar.getName().equals("tomcat-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-api", "9.0.22");
            }
            if (jar.getName().equals("tomcat-websocket.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-websocket", "9.0.22");
            }
            if (jar.getName().equals("tomcat-util.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-util", "9.0.22");
            }
            if (jar.getName().equals("tomcat-util-scan.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-util-scan", "9.0.22");
            }
            if (jar.getName().equals("tomcat-jni.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jni", "9.0.22");
            }
            if (jar.getName().equals("tomcat-jdbc.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jdbc", "9.0.22");
            }
            if (jar.getName().equals("jsp-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jsp-api", "9.0.22");
            }

            if (jar.getName().equals("ecj-4.12.jar")) {
                return new Artifact("org.eclipse.jdt", "ecj", "3.18.0");
            }

            if (jar.getName().startsWith("openejb-")) {
                final String artifact = jar.getName().replaceAll("-8.0.0.*", "");
                return new Artifact("org.apache.tomee", artifact, "${project.version}");
            }
            if (jar.getName().startsWith("tomee-")) {
                final String artifact = jar.getName().replaceAll("-8.0.0.*", "");
                return new Artifact("org.apache.tomee", artifact, "${project.version}");
            }


            // /Users/dblevins/.m2/repository//org/apache/xbean/xbean-naming/4.17/xbean-naming-4.17.jar
            final File file = getFile(jar);
            final File versionDir = file.getParentFile();
            final File artifactDir = versionDir.getParentFile();

            final String groupId = artifactDir.getParentFile()
                    .getAbsolutePath()
                    .substring(path.getAbsolutePath().length() + 1)
                    .replace("/", ".");

            return Artifact.builder()
                    .artifactId(artifactDir.getName())
                    .version(versionDir.getName())
                    .groupId(groupId)
                    .build();
        }

        private File getFile(final File jar) {
            {
                final File file = artifacts.get(jar.getName());
                if (file != null) return file;
            }
            {
                final String name = jar.getName();
                final String relativeName = name.substring(name.length() - 4);

                for (final Map.Entry<String, File> entry : artifacts.entrySet()) {
                    if (entry.getKey().startsWith(relativeName)) return entry.getValue();
                }
            }

            throw new IllegalStateException(jar.getName());
        }
    }

    @lombok.Builder
    @Getter
    @ToString
    public static class Artifact implements Comparable<Artifact> {
        private final String groupId;
        private final String artifactId;
        private final String version;

        @Override
        public int compareTo(final Artifact o) {
            final String a = this.getGroupId() + ":" + this.artifactId;
            final String b = o.getGroupId() + ":" + o.artifactId;
            return a.compareTo(b);
        }

        public String asDep() {
            final String g = groupId;
            final String a = artifactId;
            final String v = version;
            return "" +
                    "    <dependency>\n" +
                    "      <groupId>" + g + "</groupId>\n" +
                    "      <artifactId>" + a + "</artifactId>\n" +
                    "      <version>" + v + "</version>\n" +
                    "      <exclusions>\n" +
                    "        <exclusion>\n" +
                    "          <artifactId>*</artifactId>\n" +
                    "          <groupId>*</groupId>\n" +
                    "        </exclusion>\n" +
                    "      </exclusions>\n" +
                    "    </dependency>\n";
        }
    }

}
