/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.embedded;

import org.apache.catalina.realm.JAASRealm;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.embedded.component.TomEEEmbeddedArgs;
import org.apache.xbean.finder.filter.Filter;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.Set;

import static org.apache.openejb.loader.JarLocation.jarLocation;
import static org.apache.openejb.util.PropertyPlaceHolderHelper.simpleValue;

public class Main {
    public static final String PORT = "port";
    public static final String SHUTDOWN = "shutdown";
    public static final String HOST = "host";
    public static final String PATH = "path";
    public static final String CONTEXT = "context";
    public static final String DIRECTORY = "directory";
    public static final String DOC_BASE = "doc-base";
    public static final String AS_WAR = "as-war";
    public static final String RENAMING = "renaming";
    public static final String SERVER_XML = "serverxml";
    public static final String TOMEE_XML = "tomeexml";
    public static final String PROPERTY = "property";
    public static final String SINGLE_CLASSLOADER = "single-classloader";
    public static final String QUICK_SESSION = "quick-session";
    public static final String SKIP_HTTP = "skip-http";
    public static final String HTTPS_PORT = "https-port";
    public static final String SSL = "ssl";
    public static final String KEYSTORE_FILE = "keystore";
    public static final String KEYSTORE_PASS = "keystore-pass";
    public static final String KEYSTORE_TYPE = "keystore-type";
    public static final String CLIENT_AUTH = "client-auth";
    public static final String KEY_ALIAS = "key-alias";
    public static final String SSL_PROTOCOL = "ssl-protocol";
    public static final String WEB_XML = "web-xml";
    public static final String JAAS_CONFIG = "jaas";
    public static final String CACHE_WEB_RESOURCES = "cache-web-resources";
    public static final String BASIC = "basic";
    public static final String SIMPLE_LOG = "simple-log";
    public static final String PRE_TASK = "pre-task";
    public static final String INTERACTIVE = "interactive";
    public static final String CONFIGURATION = "configuration-location";
    public static final String CLASSES_FILTER = "classes-filter";
    public static final String HELP = "help";

    public static void main(final String[] args) {
        final CommandLineParser parser = new PosixParser();
        final Options options = createOptions();

        // parse command line
        final CommandLine line;
        try {
            line = parser.parse(options, args, true);
        } catch (final ParseException exp) {
            help(options);
            return;
        }

        if (line.hasOption(HELP)) {
            help(options);
            return;
        }

        final Collection<Closeable> post = new ArrayList<>();
        for (final LifecycleTask task : ServiceLoader.load(LifecycleTask.class)) {
            final Closeable closeable = task.beforeContainerStartup();
            if (closeable != null) {
                post.add(closeable);
            }
        }
        if (line.hasOption(PRE_TASK)) {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            for (final String type : line.getOptionValues(PRE_TASK)) {
                final Object task;
                try {
                    task = loader.loadClass(type).newInstance();
                } catch (final Exception e) {
                    throw new IllegalArgumentException(e);
                }
                if (Runnable.class.isInstance(task)) {
                    Runnable.class.cast(task).run();
                } else if (LifecycleTask.class.isInstance(task)) {
                    final Closeable closeable = LifecycleTask.class.cast(task).beforeContainerStartup();
                    if (closeable != null) {
                        post.add(closeable);
                    }
                } else {
                    throw new IllegalArgumentException(task + " can't be executed");
                }
            }
        }

        // run TomEE
        try {
            final Container container = new Container(createConfiguration(line));
            final String[] contexts;
            if (line.hasOption(CONTEXT)) {
                contexts = line.getOptionValues(CONTEXT);
            } else {
                contexts = null;
            }

            SystemInstance.get().setComponent(TomEEEmbeddedArgs.class, new TomEEEmbeddedArgs(args, line));

            boolean autoWar;
            if (line.hasOption(PATH)) {
                int i = 0;
                for (final String path : line.getOptionValues(PATH)) {
                    final Set<String> locations = ProvisioningUtil.realLocation(path);
                    for (final String location : locations) {
                        final File file = new File(location);
                        if (!file.exists()) {
                            System.err.println(file.getAbsolutePath() + " does not exist, skipping");
                            continue;
                        }

                        String name = file.getName().replaceAll("\\.[A-Za-z]+$", "");
                        if (contexts != null) {
                            name = contexts[i++];
                        }
                        container.deploy(name, file, true);
                    }
                }
                autoWar = false;
            } else if (line.hasOption(AS_WAR)) {
                deployClasspath(line, container, contexts);
                autoWar = false;
            } else { // nothing to deploy
                autoWar = true;
            }
            if (autoWar) { // nothing deployed check if we are a war and deploy ourself then
                final File me = jarLocation(Main.class);
                if (me.getName().endsWith(".war")) {
                    container.deploy(contexts == null || 0 == contexts.length ? "" : contexts[0], me, line.hasOption(RENAMING));
                } else {
                    deployClasspath(line, container, contexts);
                }
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        container.stop();
                    } catch (final Exception e) {
                        e.printStackTrace(); // just log the exception
                    } finally {
                        close(post);
                    }
                }
            });
            if (line.hasOption(INTERACTIVE)) {
                String l;
                final Scanner scanner = new Scanner(System.in);
                while ((l = scanner.nextLine()) != null) {
                    switch (l.trim()) {
                        case "quit":
                        case "exit":
                            return;
                        default:
                            System.out.println("Unknown command '" + l + "', supported commands: 'quit', 'exit'");
                    }
                }
            }
            container.await();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            close(post);
        }
    }

    private static void help(Options options) {
        new HelpFormatter().printHelp("java -jar tomee-embedded-user.jar", options);
    }

    private static void close(final Collection<Closeable> post) {
        synchronized (post) {
            for (final Closeable p : post) {
                try {
                    p.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            post.clear();
        }
    }

    private static void deployClasspath(final CommandLine line, final Container container, final String[] contexts) {
        container.deployClasspathAsWebApp(
                contexts == null || 0 == contexts.length ? "" : contexts[0],
                line.hasOption(DOC_BASE) ? new File(line.getOptionValue(DOC_BASE)) : null,
                line.hasOption(SINGLE_CLASSLOADER));
    }

    private static Options createOptions() {
        final Options options = new Options();
        options.addOption(null, PATH, true, "");
        options.addOption(null, CONTEXT, true, "Context name for applications (same order than paths)");
        options.addOption("p", PORT, true, "TomEE http port");
        options.addOption("s", SHUTDOWN, true, "TomEE shutdown port");
        options.addOption("d", DIRECTORY, true, "TomEE directory");
        options.addOption("c", AS_WAR, false, "deploy classpath as war");
        options.addOption("b", DOC_BASE, true, "when deploy classpath as war, the doc base");
        options.addOption(null, RENAMING, true, "for fat war only, is renaming of the context supported");
        options.addOption(null, SERVER_XML, true, "the server.xml path");
        options.addOption(null, TOMEE_XML, true, "the tomee.xml path");
        options.addOption(null, PROPERTY, true, "some container properties");
        options.addOption(null, HOST, true, "server host");
        options.addOption(null, QUICK_SESSION, true, "use a quick session - it uses Random instead of SecureRandom");
        options.addOption(null, SKIP_HTTP, true, "should http connector be ignored");
        options.addOption(null, HTTPS_PORT, true, "the https port if needed");
        options.addOption(null, SSL, true, "Is https enabled");
        options.addOption(null, KEYSTORE_FILE, true, "the https keystore");
        options.addOption(null, KEYSTORE_PASS, true, "the https keystore password (can use cipher:xxx)");
        options.addOption(null, KEYSTORE_TYPE, true, "the https keystore type");
        options.addOption(null, CLIENT_AUTH, true, "is client_auth used");
        options.addOption(null, KEY_ALIAS, true, "the https key alias");
        options.addOption(null, SSL_PROTOCOL, true, "the https SSL protocols");
        options.addOption(null, WEB_XML, true, "override global web.xml");
        options.addOption(null, JAAS_CONFIG, true, "forces tomee to use JAAS with the set config");
        options.addOption(null, CACHE_WEB_RESOURCES, true, "should web resources be cached");
        options.addOption(null, BASIC, true, "basic authentication if set");
        options.addOption(null, SIMPLE_LOG, false, "should tomee use simple log format (level - message) - demo intended");
        options.addOption("i", INTERACTIVE, false, "should tomee start and wait for SIGTERM signal or wait for 'exit' to be entered");
        options.addOption(null, CONFIGURATION, true, "a properties file containing the configuration to load");
        options.addOption(null, SINGLE_CLASSLOADER, false, "if the application should use the same classloader as the boot one");
        options.addOption(null, CLASSES_FILTER, true, "A custom implementation of a xbean filter to exclude classes to not scan");
        options.addOption("h", HELP, false, "show help");
        return options;
    }

    private static Configuration createConfiguration(final CommandLine args) {
        final Configuration config = new Configuration();
        if (args.hasOption(CONFIGURATION)) {
            config.loadFrom(args.getOptionValue(CONFIGURATION));
        }
        config.setHttpPort(Integer.parseInt(args.getOptionValue(PORT, Integer.toString(config.getHttpPort()))));
        config.setStopPort(Integer.parseInt(args.getOptionValue(SHUTDOWN, Integer.toString(config.getHttpsPort()))));
        config.setDir(args.getOptionValue(DIRECTORY, config.getDir() == null ? new File(new File("."), "apache-tomee").getAbsolutePath() : config.getDir()));
        if (args.hasOption(SERVER_XML)) {
            config.setServerXml(args.getOptionValue(SERVER_XML));
        }
        if (args.hasOption(WEB_XML)) {
            config.setWebXml(args.getOptionValue(WEB_XML));
        }
        if (args.hasOption(TOMEE_XML)) {
            config.property("openejb.conf.file", args.getOptionValue(TOMEE_XML));
        }
        if (args.hasOption(SIMPLE_LOG)) {
            config.property("openejb.jul.forceReload", "true");
        }
        if (args.hasOption(CLASSES_FILTER)) {
            try {
                config.classesFilter(Filter.class.cast(Thread.currentThread().getContextClassLoader().loadClass(args.getOptionValue(CLASSES_FILTER)).newInstance()));
            } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }
        if (args.hasOption(PROPERTY)) {
            for (final String opt : args.getOptionValues(PROPERTY)) {
                final int sep = opt.indexOf('=');
                if (sep > 0) {
                    config.property(opt.substring(0, sep), opt.substring(sep + 1));
                } else {
                    config.property(opt, "true");
                }
            }
        }
        if (args.hasOption(JAAS_CONFIG)) {
            final String jaas = args.getOptionValue(JAAS_CONFIG);
            final File file = new File(jaas);
            System.setProperty("java.security.auth.login.config", file.getAbsolutePath());
            final JAASRealm realm = new JAASRealm() {
                @Override
                protected javax.security.auth.login.Configuration getConfig() {
                    try {
                        if (jaasConfigurationLoaded) {
                            return jaasConfiguration;
                        }
                        synchronized (this) {
                            if (configFile == null) {
                                jaasConfigurationLoaded = true;
                                return null;
                            }
                            configFile = file.getAbsolutePath();
                            final Class<?> sunConfigFile = Class.forName("com.sun.security.auth.login.ConfigFile");
                            final Constructor<?> constructor = sunConfigFile.getConstructor(URI.class);
                            javax.security.auth.login.Configuration config = javax.security.auth.login.Configuration.class.cast(constructor.newInstance(file.toURI()));
                            this.jaasConfiguration = config;
                            this.jaasConfigurationLoaded = true;
                            return this.jaasConfiguration;
                        }
                    } catch (final NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException |
                            InstantiationException | InvocationTargetException | ClassNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            };
            realm.setAppName("application");
            realm.setConfigFile(new File(jaas).getAbsolutePath());
            config.setRealm(realm);
        }
        if (args.hasOption(BASIC)) {
            config.loginConfig(new LoginConfigBuilder().basic()
                    .realmName(System.getProperty("tomee.embedded.main.basic.realm", "Security")));
            config.securityConstaint(new SecurityConstaintBuilder().authConstraint(true)
                    .addAuthRole(System.getProperty("tomee.embedded.main.basic.role", "*"))
                    .addCollection("Basic", System.getProperty("tomee.embedded.main.basic.pattern", "/*"))
                    .displayName(System.getProperty("tomee.embedded.main.basic.display-name", "Basic security")));
        }
        if (args.hasOption(CACHE_WEB_RESOURCES)) {
            config.setWebResourceCached(Boolean.parseBoolean(args.getOptionValue(CACHE_WEB_RESOURCES)));
        }
        if (args.hasOption(SSL_PROTOCOL)) {
            config.setSslProtocol(args.getOptionValue(SSL_PROTOCOL));
        }
        if (args.hasOption(KEY_ALIAS)) {
            config.setKeyAlias(args.getOptionValue(KEY_ALIAS));
        }
        if (args.hasOption(KEYSTORE_TYPE)) {
            config.setKeystoreType(args.getOptionValue(KEYSTORE_TYPE));
        }
        if (args.hasOption(KEYSTORE_PASS)) {
            config.setKeystorePass(simpleValue(args.getOptionValue(KEYSTORE_PASS)));
        }
        if (args.hasOption(KEYSTORE_FILE)) {
            config.setKeystoreFile(args.getOptionValue(KEYSTORE_FILE));
        }
        if (args.hasOption(SSL)) {
            config.setSsl(Boolean.parseBoolean(args.getOptionValue(SSL)));
        }
        if (args.hasOption(HTTPS_PORT)) {
            config.setHttpsPort(Integer.parseInt(args.getOptionValue(HTTPS_PORT)));
        }
        if (args.hasOption(SKIP_HTTP)) {
            config.setSkipHttp(Boolean.parseBoolean(args.getOptionValue(SKIP_HTTP)));
        }
        if (args.hasOption(QUICK_SESSION)) {
            config.setQuickSession(Boolean.parseBoolean(args.getOptionValue(QUICK_SESSION)));
        }
        return config;
    }

}
