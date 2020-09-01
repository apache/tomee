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

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.loader.Zips;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Strings;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateBoms {

    private final File project;
    private final File boms;
    private final File dists;
    private final Repository repository;

    public GenerateBoms() {
        final File testClasses = JarLocation.jarLocation(GenerateBoms.class);
        final File target = testClasses.getParentFile();
        final File tomeeBootstrap = target.getParentFile();
        final File tomee = tomeeBootstrap.getParentFile();

        this.project = tomee.getParentFile();
        this.boms = new File(project, "boms");
        this.dists = new File(tomee, "apache-tomee/target");

        { // Find the ~/.m2/repository directory
            final File junitJar = JarLocation.jarLocation(Test.class);
            final File version = junitJar.getParentFile();
            final File artifact = version.getParentFile();
            final File group = artifact.getParentFile();
            final File repository = group.getParentFile();
            this.repository = new Repository(repository);
        }

        Files.dir(project);
        Files.dir(boms);
        Files.dir(dists);
    }

    public static void main(String[] args) throws Exception {
        new GenerateBoms().run();
    }

    public void run() throws Exception {
        final List<Distribution> distributions = Stream.of(dists.listFiles())
                .filter(file -> file.getName().endsWith(".zip"))
                .filter(file -> file.getName().startsWith("apache-tomee-"))
                .map(this::asDistribution)
                .collect(Collectors.toList());

        distributions.forEach(this::toBom);
    }

    private void toBom(final Distribution distribution) {
        try {
            final URL url = this.getClass().getClassLoader().getResource("pom-template.xml");
            final String template = IO.slurp(url);

            final String dependencies = Join.join("", Artifact::asBomDep, distribution.getArtifacts());

            final String pom = template.replace("TomEE MicroProfile", distribution.getDisplayName())
                    .replace("tomee-microprofile", distribution.getName())
                    .replace("<!--dependencies-->", dependencies);

            final File dist = Files.mkdir(boms, distribution.getName());

            final File pomFile = new File(dist, "pom.xml");

            IO.copy(IO.read(pom), pomFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Data
    public static class Distribution {
        private final List<Artifact> artifacts = new ArrayList<>();
        private final List<File> missing = new ArrayList<>();
        private final File zip;
        private final String name;
        private final String displayName;

        public Distribution(final File zip) {
            this.zip = zip;
            name = zip.getName()
                    .replaceFirst("-[0-9].*", "")
                    .replace("apache-", "");

            this.displayName = Stream.of(name.split("-"))
                    .map(Strings::ucfirst)
                    .map(s -> s.replace("ee", "EE"))
                    .map(s -> s.replace("profile", "Profile"))
                    .reduce((s, s2) -> s + " " + s2)
                    .get();
        }

        @Override
        public String toString() {
            return "Distribution{" +
                    "displayName=" + displayName +
                    ", name=" + name +
                    ", artifacts=" + artifacts.size() +
                    ", missing=" + missing.size() +
                    '}';
        }
    }

    private Distribution asDistribution(final File file) {
        final File tmpdir = Files.tmpdir();
        try {
            Zips.unzip(file, tmpdir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot unzip " + file.getAbsolutePath(), e);
        }

        final Distribution distribution = new Distribution(file);

        final List<File> jars = Files.collect(tmpdir, ".*.jar");

        final Function<File, Artifact> from = file1 -> {
            try {
                return repository.from(file1);
            } catch (IllegalStateException e) {
                distribution.missing.add(file1);
                return null;
            }
        };

        jars.stream()
                .filter(jar -> !jar.getName().equals("bootstrap.jar"))
                .filter(jar -> !jar.getName().equals("catalina-ant.jar"))
                .filter(jar -> !jar.getName().startsWith("tomcat-i18n"))
                .map(from)
                .filter(Objects::nonNull)
                .sorted()
                .forEach(distribution.artifacts::add);

        return distribution;
    }

    public static class Repository {
        private final Map<String, File> artifacts = new HashMap<>();
        private final File path;

        public Repository(final File path) {
            this.path = path;

            final List<File> jars = Files.collect(this.path, ".*\\.jar");
            for (final File jar : jars) {
                this.artifacts.put(jar.getName(), jar);
            }
        }

        public Artifact from(final File jar) {
            if (jar.getName().equals("commons-daemon.jar")) {
                return new Artifact("commons-daemon", "commons-daemon", "1.1.0");
            }

            if (jar.getName().equals("tomcat-juli.jar")) {
                return new Artifact("org.apache.tomee", "tomee-juli", "${project.version}");
            }

            if (jar.getName().equals("catalina-ha.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-catalina-ha", "${tomcat.version}");
            }

            if (jar.getName().equals("catalina-storeconfig.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-storeconfig", "${tomcat.version}");
            }

            if (jar.getName().equals("catalina-tribes.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-tribes", "${tomcat.version}");
            }

            if (jar.getName().equals("catalina-ssi.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-ssi", "${tomcat.version}");
            }

            if (jar.getName().equals("catalina.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-catalina", "${tomcat.version}");
            }

            if (jar.getName().equals("el-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-el-api", "${tomcat.version}");
            }

            if (jar.getName().equals("jasper-el.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jasper-el", "${tomcat.version}");
            }

            if (jar.getName().equals("jasper.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jasper", "${tomcat.version}");
            }

            if (jar.getName().equals("jaspic-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jaspic-api", "${tomcat.version}");
            }

            if (jar.getName().equals("servlet-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-servlet-api", "${tomcat.version}");
            }
            if (jar.getName().equals("websocket-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-websocket-api", "${tomcat.version}");
            }
            if (jar.getName().equals("tomcat-coyote.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-coyote", "${tomcat.version}");
            }
            if (jar.getName().equals("tomcat-dbcp.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-dbcp", "${tomcat.version}");
            }
            if (jar.getName().equals("tomcat-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-api", "${tomcat.version}");
            }
            if (jar.getName().equals("tomcat-websocket.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-websocket", "${tomcat.version}");
            }
            if (jar.getName().equals("tomcat-util.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-util", "${tomcat.version}");
            }
            if (jar.getName().equals("tomcat-util-scan.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-util-scan", "${tomcat.version}");
            }
            if (jar.getName().equals("tomcat-jni.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jni", "${tomcat.version}");
            }
            if (jar.getName().equals("tomcat-jdbc.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jdbc", "${tomcat.version}");
            }
            if (jar.getName().equals("jsp-api.jar")) {
                return new Artifact("org.apache.tomcat", "tomcat-jsp-api", "${tomcat.version}");
            }

            if (jar.getName().startsWith("ecj-")) {
                return new Artifact("org.eclipse.jdt", "ecj", "3.22.0");
            }

            if (jar.getName().startsWith("openejb-")) {
                final String artifact = jar.getName().replaceAll("-8.0.*", "");
                return new Artifact("org.apache.tomee", artifact, "${project.version}");
            }

            if (jar.getName().startsWith("tomee-")) {
                final String artifact = jar.getName().replaceAll("-8.0.*", "");
                return new Artifact("org.apache.tomee", artifact, "${project.version}");
            }


            // /Users/dblevins/.m2/repository//org/apache/xbean/xbean-naming/4.14/xbean-naming-4.14.jar
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

    @lombok.Builder(toBuilder = true)
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

        public String asBomDep() {
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

        public String asManagedDep() {
            final String g = groupId;
            final String a = artifactId;
            final String v = version;
            return "" +
                    "    <dependency>\n" +
                    "      <groupId>" + g + "</groupId>\n" +
                    "      <artifactId>" + a + "</artifactId>\n" +
                    "      <version>" + v + "</version>\n" +
                    "    </dependency>\n";
        }
    }

}
