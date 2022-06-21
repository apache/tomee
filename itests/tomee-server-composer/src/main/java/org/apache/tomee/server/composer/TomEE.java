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

import org.apache.tomee.server.version.Version;
import org.tomitribe.swizzle.stream.StreamBuilder;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;
import org.tomitribe.util.JarLocation;
import org.tomitribe.util.hash.XxHash64;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TomEE {

    private final Stats stats;
    private final File home;
    private final int port;
    private final Process process;
    private final CleanOnExit cleanOnExit;

    private TomEE(final File home, final int port, final Process process, final Stats stats, final CleanOnExit cleanOnExit) {
        this.home = home;
        this.port = port;
        this.process = process;
        this.stats = stats;
        this.cleanOnExit = cleanOnExit;
    }

    public URI toURI() {
        return URI.create("http://localhost:" + port);
    }

    public File getHome() {
        return home;
    }

    public int getPort() {
        return port;
    }

    public Process getProcess() {
        return process;
    }

    public void shutdown() {
        try {
            process.destroy();
            process.waitFor();
            cleanOnExit.clean();
        } catch (Exception e) {
            throw new IllegalStateException("Shutdown failed", e);
        }
        if (home.exists()) shutdown();
    }

    public static Builder plus() throws Exception {
        return of("org.apache.tomee:apache-tomee:tar.gz:plus:" + Version.VERSION);
    }

    public static Builder microprofile() throws Exception {
        return of("org.apache.tomee:apache-tomee:tar.gz:microprofile:" + Version.VERSION);
    }

    public static Builder webprofile() throws Exception {
        return of("org.apache.tomee:apache-tomee:tar.gz:webprofile:" + Version.VERSION);
    }

    public Stats getStats() {
        return stats;
    }

    public static class Stats {
        private final long extracted;
        private final long startup;

        public Stats(final long extracted, final long startup) {
            this.extracted = extracted;
            this.startup = startup;
        }

        public long getExtracted() {
            return extracted;
        }

        public long getStartup() {
            return startup;
        }
    }

    public static Builder of(final String mavenCoordinates) throws Exception {
        return new Builder(mavenCoordinates);
    }

    public static Builder from(final File mavenCoordinates) throws Exception {
        return new Builder(mavenCoordinates);
    }

    public static class Builder extends ServerBuilder<Builder> {

        private PrintStream err = System.err;
        private PrintStream out = System.out;

        public Builder(final String mavenCoordinates) throws IOException {
            super(mavenCoordinates);
            filter(Excludes::webapps);
        }

        public Builder(final File mavenCoordinates) throws IOException {
            super(mavenCoordinates);
            filter(Excludes::webapps);
        }

        public Builder err(PrintStream err) {
            this.err = err;
            return this;
        }

        public Builder out(PrintStream out) {
            this.out = out;
            return this;
        }

        public Builder update() {
            home(this::update);
            return this;
        }

        private void update(final File home) {
            final File repository = JarLocation.jarLocation(XxHash64.class)
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile();

            final File org = Files.file(repository, "org", "apache");

            final Map<String, File> map = new HashMap<>();

            Files.collect(org, ".*\\.jar").stream()
                    .forEach(file -> map.put(file.getName(), file));

            final File lib = new File(home, "lib");
            for (final File jar : Files.collect(lib, ".*\\.jar")) {
                final File file = map.get(jar.getName());
                if (file != null && file.lastModified() > jar.lastModified()) {
                    try {
                        System.out.printf("Updating %s%n", jar.getName());
                        IO.copy(file, jar);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }

        public Builder debug(final int port, boolean suspend) {
            env("JPDA_ADDRESS", port + "");
            env("JPDA_SUSPEND", suspend ? "y" : "n");

            return debug();
        }

        public Builder debug(final int port) {
            return debug(port, true);
        }

        public TomEE build() throws IOException {

            applyBuilderConsumers();

            final CleanOnExit cleanOnExit = new CleanOnExit();
            final File tmpdir = cleanOnExit.clean(Files.tmpdir());

            final File home;
            final long extracted;
            { // extract the server
                home = new File(tmpdir, "server");
                Files.mkdir(home);

                final long start = System.nanoTime();
                TarGzs.untargz(archive, home, true, filter);
                extracted = System.nanoTime() - start;
            }

            String os = System.getProperty("os.name").toLowerCase();
            String extension = ".sh";
            if (!os.contains("win")) {
                { // make scripts executable
                    Stream.of(new File(home, "bin").listFiles())
                            .filter(file1 -> file1.getName().endsWith(".sh"))
                            .forEach(file2 -> file2.setExecutable(true));
                }
            } else {
                extension = ".bat";
            }
            applyModifications(home);

            final int http;
            { // set random ports
                final Iterator<Integer> ports = Ports.allocate(3).iterator();
                http = ports.next();

                final File serverxml = Files.file(home, "conf", "server.xml");
                final String config = IO.slurp(serverxml)
                        .replace("8080", http + "")
                        .replace("8005", ports.next() + "")
                        .replace("8009", ports.next() + "");
                IO.copy(IO.read(config), serverxml);
            }

            applyHomeConsumers(home);

            final File catalinaSh = Files.file(home, "bin", "catalina" + extension);

            final ProcessBuilder builder = new ProcessBuilder()
                    .directory(home);

            // todo maybe use the list approach to fill in the arguments
            // but this way we are sure about the order  to put them for tomcat
            if (debug) {
                builder.command(catalinaSh.getAbsolutePath(), "jpda", "run");
            } else {
                builder.command(catalinaSh.getAbsolutePath(), "run");
            }

            // make sure to configure the Locale to english otherwise the watch bellow will fail on other countries
            if (env.containsKey("JAVA_OPTS")) {
                env.put("JAVA_OPTS", "-Duser.language=en -Duser.country=US " + env.get("JAVA_OPTS"));

            } else {
                env.put("JAVA_OPTS", "-Duser.language=en -Duser.country=US");

            }

            if (!env.containsKey("JAVA_HOME")) {
                env.put("JAVA_HOME", System.getProperty("java.home"));
            }
            
            builder.environment().putAll(env);

            if (list) Files.visit(tmpdir, TomEE::print);

            final long start = System.nanoTime();
            final Process process = cleanOnExit.clean(builder.start());

            final CountDownLatch startup = new CountDownLatch(1);

            final StreamBuilder inputStream = StreamBuilder.create(process.getInputStream());
            final StreamBuilder errorStream = StreamBuilder.create(process.getErrorStream())
                    .watch("Server startup in ", startup::countDown);

            for (final Consumer<StreamBuilder> watch : watches) {
                watch.accept(inputStream);
            }

            for (final Consumer<StreamBuilder> watch : watches) {
                watch.accept(errorStream);
            }

            final Future<Pipe> stout = Pipe.pipe(inputStream.get(), out);
            final Future<Pipe> sterr = Pipe.pipe(errorStream.get(), err);

            try {
                if (!startup.await(await.getTime(), await.getUnit())) {
                    throw new StartupFailedException("Waited " + await.toString());
                }
            } catch (InterruptedException e) {
                throw new StartupFailedException(e);
            }
            final long startTime = System.nanoTime() - start;

            return new TomEE(home, http, process, new Stats(extracted, startTime), cleanOnExit);
        }
    }

    private static boolean print(final File file) {
        System.out.println(file.getAbsolutePath());
        return true;
    }

}
