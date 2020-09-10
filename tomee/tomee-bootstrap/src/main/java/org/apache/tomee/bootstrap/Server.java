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

import org.apache.catalina.startup.Catalina;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Server {

    private static final Log log = LogFactory.getLog(Server.class);

    private final File home;
    private final URI uri;

    public Server(final File home, final int port) {
        this.home = home;
        this.uri = URI.create("http://localhost:" + port);
    }

    public URI getURI() {
        return uri;
    }

    public File getHome() {
        return home;
    }

    private static void cp(final File conf, final String resource) {
        try {
            final URL url = resolve(resource);

            IO.copy(IO.read(url), new File(conf, resource));

        } catch (IOException e) {
            // todo add more detail
            throw new UncheckedIOException(e);
        }
    }

    private static URL resolve(final String resource) throws IOException {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Enumeration<URL> resources = loader.getResources("tomee/conf/" + resource);
        final List<URL> list = Collections.list(resources);

        if (list.size() == 0) {
            throw new MissingResourceException(resource);
        }

        if (list.size() == 1) {
            return list.get(0);
        }

        sort(list);
        return list.get(0);
    }

    public static void sort(final List<URL> list) {
        Collections.sort(list, Server::compare);
    }

    private static int compare(final URL o1, final URL o2) {
        final String a = o1.toExternalForm();
        final String b = o2.toExternalForm();

        int modifier = 0;
        if (a.contains("tomee-bootstrap")) modifier += 1000;
        if (b.contains("tomee-bootstrap")) modifier -= 1000;

        return a.compareTo(b) + modifier;
    }

    public static class MissingResourceException extends RuntimeException {
        public MissingResourceException(final String message) {
            super(message);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int httpPort;
        private int shutdownPort;
        private int ajpPort;
        protected final ArrayList<Consumer<File>> homeConsumers = new ArrayList<>();
        protected final ArrayList<Consumer<Builder>> builderConsumers = new ArrayList<>();
        protected final Archive modifications = Archive.archive();

        public Builder httpPort(final int port) {
            this.httpPort = port;
            return this;
        }

        public Builder ajpPort(final int port) {
            this.ajpPort = port;
            return this;
        }

        public Builder shutdownPort(final int port) {
            this.shutdownPort = port;
            return this;
        }

        public Builder add(final String destinationPath, final byte[] bytes) {
            modifications.add(destinationPath, bytes);
            return this;
        }

        public Builder add(final String destinationPath, final Supplier<byte[]> content) {
            modifications.add(destinationPath, content);
            return this;
        }

        public Builder add(final String destinationPath, final String content) {
            modifications.add(destinationPath, content);
            return this;
        }

        public Builder add(final String destinationPath, final File content) {
            modifications.add(destinationPath, content);
            return this;
        }

        public Builder add(final String name, final Archive contents) {
            modifications.add(name, contents);
            return this;
        }

        public Builder home(final Consumer<File> customization) {
            homeConsumers.add(customization);
            return this;
        }

        public Builder and(final Consumer<Builder> consumer) {
            this.builderConsumers.add(consumer);
            return this;
        }

        protected void applyHomeConsumers(final File home) {
            // run any customization logic that's been added
            for (final Consumer<File> customization : homeConsumers) {
                customization.accept(home);
            }
        }

        protected void applyModifications(final File home) {
            // copy user files
            try {
                modifications.toDir(home);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to apply home modifications to " + home.getAbsolutePath(), e);
            }
        }

        protected void applyBuilderConsumers() {
            for (final Consumer<Builder> consumer : builderConsumers) {
                consumer.accept((Builder) this);
            }
        }

        public Server build() {
            final long start = System.currentTimeMillis();

            applyBuilderConsumers();

            final File home = Files.mkdir(Files.tmpdir(), "apache-tomee");
            final File conf = Files.mkdir(home, "conf");
            final File logs = Files.mkdir(home, "logs");
            final File webapps = Files.mkdir(home, "webapps");

            cp(conf, "catalina.policy");
            cp(conf, "catalina.properties");
            cp(conf, "context.xml");
            cp(conf, "jaspic-providers.xml");
            cp(conf, "jaspic-providers.xsd");
            cp(conf, "logging.properties");
            cp(conf, "server.xml");
            cp(conf, "system.properties");
            cp(conf, "tomcat-users.xml");
            cp(conf, "tomcat-users.xsd");
            cp(conf, "tomee.xml");
            cp(conf, "web.xml");

            applyModifications(home);

            final Iterator<Integer> ports = Ports.allocate(3).iterator();

            final int http = httpPort > 0 ? httpPort : ports.next();
            final int shutdown = shutdownPort > 0 ? shutdownPort : ports.next();
            final int ajp = ajpPort > 0 ? ajpPort : ports.next();

            try { // apply modifications to server.xml
                final File serverxml = new File(conf, "server.xml");
                final String content = setPorts(http, shutdown, ajp)
                        .andThen(this::addServerListener)
                        .andThen(this::setUtilityThreadsAsDaemon)
                        .apply(IO.slurp(serverxml));
                IO.copy(IO.read(content), serverxml);
            } catch (final IOException e) {
                throw new UncheckedIOException("Unable to modify server.xml", e);
            }

            applyHomeConsumers(home);

            System.setProperty("catalina.home", home.getAbsolutePath());
            System.setProperty("catalina.base", home.getAbsolutePath());
            final URLClassLoader loader = new URLClassLoader(new URL[0], Server.class.getClassLoader());

            final Catalina catalina = new Catalina();
            catalina.setParentClassLoader(loader);
            catalina.setAwait(false);
            catalina.load();
            catalina.start();
            final long elapsed = System.currentTimeMillis() - start;
            final String message = "Full bootstrap in [" + elapsed + "] milliseconds";
            log.info(message);

            return new Server(home, http);
        }

        private Function<String, String> setPorts(final int http, final int shutdown, final int ajp) {
            return s -> s.replace("8080", http + "")
                    .replace("8005", shutdown + "")
                    .replace("8009", ajp + "");
        }

        private String setUtilityThreadsAsDaemon(final String serverXml) {
            // Normalize by removing any setting of utilityThreadsAsDaemon
            // Then explicitly set utilityThreadsAsDaemon to true
            return serverXml
                    .replace("utilityThreadsAsDaemon=\"true\"", "")
                    .replace("utilityThreadsAsDaemon=\"false\"", "")
                    .replace("shutdown=\"SHUTDOWN\"", "shutdown=\"SHUTDOWN\" utilityThreadsAsDaemon=\"true\"");
        }

        private String addServerListener(final String serverXml) {
            if (serverXml.contains("<Listener className=\"org.apache.tomee.catalina.ServerListener\"")) return serverXml;
            return serverXml.replaceFirst("<Listener ",
                    "<Listener className=\"org.apache.tomee.catalina.ServerListener\"/>\n  <Listener ");
        }
    }
}
