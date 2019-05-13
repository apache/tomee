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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TomEE {

    private final File home;
    private final int port;
    private final Process process;

    private TomEE(final File home, final int port, final Process process) {
        this.home = home;
        this.port = port;
        this.process = process;
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
            final ProcessBuilder builder = new ProcessBuilder()
                    .directory(home)
                    .command(Files.file(home, "bin", "shutdown.sh").getAbsolutePath());

            final Process start = builder.start();
            Pipe.pipe(start.getErrorStream(), System.err);
            Pipe.pipe(start.getInputStream(), System.out);

            process.waitFor();
        } catch (Exception e) {
            throw new IllegalStateException("Shutdown failed", e);
        }
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

    public static Builder of(final String mavenCoordinates) throws Exception {
        return new Builder(mavenCoordinates);
    }

    public static class Builder extends ServerBuilder<Builder> {

        public Builder(final String mavenCoordinates) throws IOException {
            super(mavenCoordinates);
            filter(Excludes::webapps);
        }

        public TomEE build() throws IOException {

            applyBuilderConsumers();

            final CleanOnExit cleanOnExit = new CleanOnExit();
            final File tmpdir = cleanOnExit.clean(Files.tmpdir());

            final File home;
            { // extract the server
                home = new File(tmpdir, "server");
                Files.mkdir(home);
                TarGzs.untargz(archive, home, true, filter);
            }

            { // make scripts executable
                Stream.of(new File(home, "bin").listFiles())
                        .filter(file1 -> file1.getName().endsWith(".sh"))
                        .forEach(file2 -> file2.setExecutable(true));
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

            final File catalinaSh = Files.file(home, "bin", "catalina.sh");

            final ProcessBuilder builder = new ProcessBuilder()
                    .directory(home)
                    .command(catalinaSh.getAbsolutePath(), "run");

            builder.environment().putAll(env);

            if (list) Files.visit(tmpdir, TomEE::print);

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

            final Future<Pipe> stout = Pipe.pipe(inputStream.get(), System.out);
            final Future<Pipe> sterr = Pipe.pipe(errorStream.get(), System.err);

            try {
                if (!startup.await(await.getTime(), await.getUnit())) {
                    throw new StartupFailedException("Waited " + await.toString());
                }
            } catch (InterruptedException e) {
                throw new StartupFailedException(e);
            }

            return new TomEE(home, http, process);
        }
    }

    private static boolean print(final File file) {
        System.out.println(file.getAbsolutePath());
        return true;
    }

}
