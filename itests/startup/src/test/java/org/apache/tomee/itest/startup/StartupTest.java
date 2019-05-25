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
package org.apache.tomee.itest.startup;

import com.github.tomaslanger.chalk.Ansi;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.math.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.openejb.util.Join;
import org.apache.tomee.server.composer.TomEE;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StartupTest {

    private static final File downloads = new File("/Users/dblevins/work/apache/downloads");

    public StartupTest() {
        if (!downloads.exists()) assertTrue(downloads.mkdir());
    }

    public static class Binary {
        final AtomicInteger nameWidth;
        final AtomicInteger count = new AtomicInteger(0);
        final SynchronizedDescriptiveStatistics extraction = new SynchronizedDescriptiveStatistics(2000);
        final SynchronizedDescriptiveStatistics startup = new SynchronizedDescriptiveStatistics(2000);
        final SynchronizedDescriptiveStatistics other = new SynchronizedDescriptiveStatistics(2000);
        final File file;
        final String name;

        public Binary(final AtomicInteger nameWidth, final File file) {
            this.file = file;
            this.name = this.file.getName().replace(".tar.gz", "");
            this.nameWidth = nameWidth;
            this.nameWidth.set(Math.max(this.nameWidth.get(), name.length()));
            System.out.println("Max width: "+nameWidth.get());
        }

        public String run() {
            final String namePadded = String.format("%-" + nameWidth.get() + "s", name);
            final String nameColored = namePadded.replaceAll("(apache-tomee-)(.+)-(.+)", "$1\033[38;5;231m$2\033[0m-\033[38;5;186m$3\033[0m");
            try {
                final long start = System.nanoTime();
                final TomEE tomee = TomEE.from(file)
                        .out(INGORED())
                        .err(INGORED())
                        .add("webapps/speed.war", new File(StartupTest.downloads, "speed.war"))
                        .build();

                final String address = tomee.toURI().resolve("/speed").toURL().toExternalForm();
                final WebClient webClient = WebClient.create(address);

                {// valid token
                    final Response response = webClient.reset()
                            .path("/color/")
                            .header("Content-Type", "application/json")
                            .get();
                    assertEquals(200, response.getStatus());
                }

                final long e = toMillis(tomee.getStats().getExtracted());
                final long s = toMillis(tomee.getStats().getStartup());
                final long o = toMillis(System.nanoTime() - start);
                extraction.addValue(e);
                startup.addValue(s);
                other.addValue(o);

                final int i = count.incrementAndGet();
                final long extractedTime = (long) extraction.getPercentile(90.0);
                final long startupTime = (long) startup.getPercentile(90.0);
                final long executionTime = (long) other.getPercentile(90.0);

                final int unit = 100;
                final int n = (int) (executionTime / unit);
                final char[] bar = new char[n];

                int j = 0;
                for (int k = 0; k * unit < extractedTime && j < bar.length; k++, j++) bar[j] = 'x';
                for (int k = 0; k * unit < startupTime && j < bar.length; k++, j++) bar[j] = 's';
                for (; j < bar.length; j++) bar[j] = 'o';

                final String executionBar = new String(bar)
                        .replaceFirst("(x+)(s+)?(o+)?", "\033[38;5;060m$1\033[38;5;088m$2\033[38;5;071m$3\033[0m");

                tomee.shutdown();

                return String.format("%s %4s %6s %6s %6s %s",
                        nameColored,
                        i,
                        extractedTime,
                        startupTime,
                        executionTime,
                        executionBar
                );
            } catch (Exception e1) {
                return String.format("%-" + nameWidth.get() + "s %s: %s", nameColored, e1.getClass().getSimpleName(), e1.getMessage());
            }
        }
    }

    @Test
    public void runFive() throws Exception {
        final AtomicInteger nameWidth = new AtomicInteger(30);
        run(() -> true,
                new Binary(nameWidth, new File(downloads, "apache-tomee-1.0.0-plus.tar.gz"))::run,
                new Binary(nameWidth, new File(downloads, "apache-tomee-1.7.0-plus.tar.gz"))::run,
                new Binary(nameWidth, new File(downloads, "apache-tomee-7.0.0-plus.tar.gz"))::run,
                new Binary(nameWidth, new File(downloads, "apache-tomee-8.0.0-M1-plus.tar.gz"))::run
        );
    }

    public void runFour() throws Exception {
        final AtomicInteger count = new AtomicInteger(0);
        final SynchronizedDescriptiveStatistics extraction = new SynchronizedDescriptiveStatistics(2000);
        final SynchronizedDescriptiveStatistics startup = new SynchronizedDescriptiveStatistics(2000);
        final SynchronizedDescriptiveStatistics other = new SynchronizedDescriptiveStatistics(2000);

        int nameWidth = 20;

        final File file = new File("/tmp/downloads/apache-tomee-8.0.0-M1-plus.tar.gz");
        final String name = file.getName().replace(".tar.gz", "");
        nameWidth = Math.max(nameWidth, name.length());

        final String line;
        {
            final long start = System.nanoTime();
            final TomEE tomee = TomEE.from(file)
                    .out(INGORED())
                    .err(INGORED())
                    .add("webapps/speed.war", new File(downloads, "speed.war"))
                    .build();

            final String address = tomee.toURI().resolve("/speed").toURL().toExternalForm();
            final WebClient webClient = WebClient.create(address);

            {// valid token
                final Response response = webClient.reset()
                        .path("/color/")
                        .header("Content-Type", "application/json")
                        .get();
                assertEquals(200, response.getStatus());
            }

            final long e = toMillis(tomee.getStats().getExtracted());
            final long s = toMillis(tomee.getStats().getStartup());
            final long o = toMillis(System.nanoTime() - start);
            extraction.addValue(e);
            startup.addValue(s);
            other.addValue(o);

            final int i = count.incrementAndGet();
            final long extractedTime = (long) extraction.getPercentile(90.0);
            final long startupTime = (long) startup.getPercentile(90.0);
            final long executionTime = (long) other.getPercentile(90.0);

            final int unit = 100;
            final int n = (int) (executionTime / unit);
            final char[] bar = new char[n];

            int j = 0;
            for (int k = 0; k * unit < extractedTime && j < bar.length; k++, j++) bar[j] = 'x';
            for (int k = 0; k * unit < startupTime && j < bar.length; k++, j++) bar[j] = 's';
            for (; j < bar.length; j++) bar[j] = 'o';

            final String executionBar = new String(bar)
                    .replaceFirst("(x+)(s+)(o+)", "\033[38;5;060m$1\033[38;5;088m$2\033[38;5;071m$3\033[0m");

            final String nameColored = name
                    .replaceAll("(apache-tomee-)(.+)-(.+)", "$1\033[38;5;231m$2\033[0m-\033[38;5;186m$3\033[0m");

            line = String.format("%-" + nameWidth + "s %4s %6s %6s %6s %s%n",
                    nameColored,
                    i,
                    extractedTime,
                    startupTime,
                    executionTime,
                    executionBar
            );

            tomee.shutdown();
        }

        System.out.println(line);
    }


    private static long toMillis(final long time) {
        return TimeUnit.NANOSECONDS.toMillis(time);
    }

    private static PrintStream INGORED() {
        return new PrintStream(new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
            }
        });
    }

    public void progress2() throws Exception {
        run(() -> true,
                new Line("one", 1),
                new Line("three", 3),
                new Line("five", 5),
                new Line("seven", 7),
                new Line("eleven", 11)
        );
    }

    private static void run(final Supplier<Boolean> proceed, Supplier<String>... items) {
        run(proceed, Arrays.asList(items));
    }

    private static void run(final Supplier<Boolean> proceed, final List<Supplier<String>> items) {
//        System.out.print(Ansi.eraseScreenDown());
        System.out.print("\014");
        System.out.print("\f");
        for (final Supplier<String> item : items) System.out.println();

        while (proceed.get()) {

            final long start = System.currentTimeMillis();

            System.out.print(Ansi.cursorUp(items.size() + 1));

            for (final Supplier<String> item : items) {
                System.out.print("\r");
                System.out.print(Ansi.cursorDown());
                final String s = item.get();
                System.out.print(Ansi.eraseLine());
                System.out.print(s);
            }

            System.out.print(Ansi.cursorDown());
            System.out.printf("\r");

            final long elapsed = System.currentTimeMillis() - start;
            if (elapsed < 500) {
                try {
                    Thread.sleep(500 - elapsed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.interrupted();
                }
            }
        }
    }

    //    @Test
    public void get() throws Exception {
        getCachedTomeeTarGzs()
                .stream()
                .map(this::getDownloaded)
                .map(File::getAbsolutePath)
                .forEach(System.out::println);

    }

    private List<URI> getCachedTomeeTarGzs() {
        final File index = new File(downloads, "index.txt");
        if (index.exists()) {
            return Stream.of(slurp(index).split("\n"))
                    .map(URI::create)
                    .collect(Collectors.toList());
        }

        final List<URI> uris = getAllTomeeTarGzs();

        try {
            IO.copy(IO.read(Join.join("\n", uris)), index);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return uris;
    }

    private File getDownloaded(final URI uri) {
        final File file = getFile(uri);

        if (file.exists()) return file;

        return download(uri);
    }

    private File download(final URI uri) {
        final WebClient client = WebClient.create(uri.toASCIIString());
        final Response response = client.get();
        assertEquals(200, response.getStatus());

        final InputStream entity = (InputStream) response.getEntity();
        final BufferedInputStream buffer = new BufferedInputStream(entity);

        Files.mkdir(downloads);

        final File file = getFile(uri);

        System.out.printf("Downloading: %s%n", file.getAbsolutePath());

        try {
            IO.copy(buffer, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return file;
    }

    private File getFile(final URI uri) {
        return new File(downloads, new File(uri.getPath()).getName());
    }

    private List<URI> getAllTomeeTarGzs() {
        final URI uri = URI.create("http://archive.apache.org/dist/");
        return (List<URI>) Stream.of("openejb/", "tomee/")
                .map(uri::resolve)
                .map(this::getVersions)
                .flatMap(Collection::stream)
                .map(this::getTarGzs)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private String slurp(final File index) {
        try {
            return IO.slurp(index);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<URI> getVersions(final URI uri) {
        final WebClient client = WebClient.create(uri.toASCIIString());
        final String html = client.get(String.class);
        return (List<URI>) Stream.of(html.split("\n"))
                .filter(s -> s.contains("alt=\"[DIR]\""))
                .map(s -> s.replaceAll(".*a href=\"([^\"]+)\".*", "$1"))
                .map(uri::resolve)
                .collect(Collectors.toList());
    }

    private List<URI> getTarGzs(final URI uri) {
        final WebClient client = WebClient.create(uri.toASCIIString());
        final String html = client.get(String.class);

        return (List<URI>) Stream.of(html.split("[<>]"))
                .filter(s -> s.contains("tar.gz\""))
                .filter(s -> s.contains("apache-tomee"))
                .map(s -> s.replace("a href=\"", ""))
                .map(s -> s.replace("\"", ""))
                .map(uri::resolve)
                .collect(Collectors.toList());
    }

    private static class Line implements Supplier<String> {
        private final String prefix;
        private final int mod;
        private int i;

        public Line(final String prefix, final int mod) {
            this.prefix = prefix;
            this.mod = mod;
        }

        @Override
        public String get() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            i += mod;
            return prefix + " " + i;
        }
    }
}
