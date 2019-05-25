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
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.math.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.openejb.monitoring.Managed;
import org.apache.openejb.util.Join;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.junit.Test;

import javax.ejb.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StartupTest {

    private final File downloads = new File("/tmp/downloads");

    public StartupTest() {
        if (!downloads.exists()) assertTrue(downloads.mkdir());
    }

    @Test
    public void runFour() throws Exception {
        int nameWidth = 20;

        final File file = new File("/tmp/downloads/apache-tomee-8.0.0-M1-plus.tar.gz");
        final String name = file.getName().replace(".tar.gz", "");
        nameWidth = Math.max(nameWidth, name.length());

        final long start = System.nanoTime();
        final TomEE tomee = TomEE.from(file)
                .out(INGORED())
                .err(INGORED())
                .add("webapps/speed.war", new File("/tmp/downloads/speed.war"))
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

        final int i = 1;
        final long extractedTime = toMillis(tomee.getStats().getExtracted());
        final long startupTime = toMillis(tomee.getStats().getStartup());
        final long executionTime = toMillis(System.nanoTime() - start);

        final int unit = 100;
        final int n = (int) (executionTime / unit);
        final char[] bar = new char[n];

        int j = 0;

        for (int k = 0; k * unit < extractedTime && j < bar.length; k++, j++) bar[j] = 'x';
        for (int k = 0; k * unit < startupTime && j < bar.length; k++, j++) bar[j] = 's';
        for (; j < bar.length; j++) bar[j] = 'o';

        final String executionBar = new String(bar).replace("\0", "x");

        System.out.printf("%-" + nameWidth + "s %4s %6s %6s %6s %s%n",
                name,
                i,
                extractedTime,
                startupTime,
                executionTime,
                executionBar
        );
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
        for (final Supplier<String> item : items) System.out.println();

        while (proceed.get()) {

            final long start = System.currentTimeMillis();

            System.out.print(Ansi.cursorUp(items.size() + 1));

            for (final Supplier<String> item : items) {
                System.out.print(Ansi.cursorDown());
                System.out.print(Ansi.eraseLine());
                System.out.printf("\r%s", item.get());
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
    public void progress() throws Exception {
//        System.out.println();

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        for (int i = 0; i <= 100; i++) {
            System.out.print(Ansi.cursorUp(5));

            int v = i;
            if (i % 3 == 0) v *= 100;
            if (i % 10 == 0) v *= 1000;
            if (i % 21 == 0) v *= 1000000;
            System.out.print(Ansi.cursorDown());
            System.out.print(Ansi.eraseLine());
            System.out.printf("\rone %s", v + 5);

            System.out.print(Ansi.cursorDown());
            System.out.print(Ansi.eraseLine());
            System.out.printf("\rtwo %s", v + 1);

            System.out.print(Ansi.cursorDown());
            System.out.print(Ansi.eraseLine());
            System.out.printf("\rthree %s", v + 9);

            System.out.print(Ansi.cursorDown());
            System.out.print(Ansi.eraseLine());
            System.out.printf("\rfour %s", v + 15);

            System.out.print(Ansi.cursorDown());
            System.out.printf("\r");

            Thread.sleep(500);
        }
    }

    @Test
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

    public void test1024() throws Exception {
        final File appJar = Archive.archive()
                .add(StartupTest.class)
                .add(ColorService.class)
                .asJar();

        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .build();

        final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());

        {// valid token
            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .get();
            assertEquals(200, response.getStatus());
        }
    }

    private static WebClient createWebClient(final URL base) {
        return WebClient.create(base.toExternalForm(), singletonList(new JohnzonProvider<>()),
                singletonList(new LoggingFeature()), null);
    }

    @Path("/movies")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Singleton
    public static class ColorService {

        @GET
        public String getAllMovies() {
            return "good";
        }
    }

    public static class Dist {

    }

    public static class Stat {
        private final AtomicLong count = new AtomicLong();
        private final SynchronizedDescriptiveStatistics samples;
        private final String name;

        public Stat(final String name) {
            this.samples = new SynchronizedDescriptiveStatistics(2000);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Managed
        public void setSampleSize(final int i) {
            samples.setWindowSize(i);
        }

        @Managed
        public int getSampleSize() {
            return samples.getWindowSize();
        }

        @Managed
        public long getCount() {
            return count.get();
        }

        @Managed
        public double getPercentile99() {
            return samples.getPercentile(99.0);
        }

        @Managed
        public double getPercentile90() {
            return samples.getPercentile(90.0);
        }

        @Managed
        public double getPercentile75() {
            return samples.getPercentile(75.0);
        }

        @Managed
        public double getPercentile50() {
            return samples.getPercentile(50.0);
        }

        @Managed
        public double getPercentile25() {
            return samples.getPercentile(25.0);
        }

        @Managed
        public double getPercentile10() {
            return samples.getPercentile(10.0);
        }

        @Managed
        public double getPercentile01() {
            return samples.getPercentile(1.0);
        }

        @Managed
        public double getStandardDeviation() {
            return samples.getStandardDeviation();
        }

        @Managed
        public double getMean() {
            return samples.getMean();
        }

        @Managed
        public double getVariance() {
            return samples.getVariance();
        }

        @Managed
        public double getGeometricMean() {
            return samples.getGeometricMean();
        }

        @Managed
        public double getSkewness() {
            return samples.getSkewness();
        }

        @Managed
        public double getKurtosis() {
            return samples.getKurtosis();
        }

        @Managed
        public double getMax() {
            return samples.getMax();
        }

        @Managed
        public double getMin() {
            return samples.getMin();
        }

        @Managed
        public double getSum() {
            return samples.getSum();
        }

        @Managed
        public double getSumsq() {
            return samples.getSumsq();
        }

        @Managed
        public double[] sortedValues() {
            return samples.getSortedValues();
        }

        @Managed
        public double[] values() {
            return samples.getValues();
        }

        public void record(final long time) {
            count.incrementAndGet();
            samples.addValue(time);
        }

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
            i += mod;
            return prefix + " " + i;
        }
    }
}
