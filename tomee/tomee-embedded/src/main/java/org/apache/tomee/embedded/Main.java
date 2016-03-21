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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.embedded;

import java.io.File;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.loader.ProvisioningUtil;

import static org.apache.openejb.loader.JarLocation.jarLocation;

public class Main {
    public static final String PORT = "port";
    public static final String SHUTDOWN = "shutdown";
    public static final String PATH = "path";
    public static final String CONTEXT = "context";
    public static final String DIRECTORY = "directory";
    public static final String DOC_BASE = "doc-base";
    public static final String AS_WAR = "as-war";
    public static final String RENAMING = "renaming";
    public static final String SERVER_XML = "serverxml";
    public static final String TOMEE_XML = "tomeexml";
    public static final String PROPERTY = "property";

    public static void main(final String[] args) {
        final CommandLineParser parser = new PosixParser();
        final Options options = createOptions();

        // parse command line
        final CommandLine line;
        try {
            line = parser.parse(options, args, true);
        } catch (final ParseException exp) {
            new HelpFormatter().printHelp("java -jar tomee-embedded-user.jar", options);
            return;
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

            boolean autoWar = true;
            int i = 0;
            if (line.hasOption(PATH)) {
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
            }
            if (line.hasOption(AS_WAR)) {
                container.deployClasspathAsWebApp(contexts == null || i == contexts.length ? "" : contexts[i],
                        line.hasOption(DOC_BASE) ? new File(line.getOptionValue(DOC_BASE)) : null);
                autoWar = false;
            }
            if (autoWar) { // nothing deployed check if we are a war and deploy ourself then
                final File me = jarLocation(Main.class);
                if (me.getName().endsWith(".war")) {
                    container.deploy(contexts == null || i == contexts.length ? "" : contexts[i], me, line.hasOption(RENAMING));
                }
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        container.stop();
                    } catch (final Exception e) {
                        e.printStackTrace(); // just log the exception
                    }
                }
            });
            container.await();
        } catch (final Exception e) {
            e.printStackTrace();
        }
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
        return options;
    }

    private static Configuration createConfiguration(final CommandLine args) {
        final Configuration config = new Configuration();
        config.setHttpPort(Integer.parseInt(args.getOptionValue(PORT, "8080")));
        config.setStopPort(Integer.parseInt(args.getOptionValue(SHUTDOWN, "8005")));
        config.setDir(args.getOptionValue(DIRECTORY, new File(new File("."), "apache-tomee").getAbsolutePath()));
        if (args.hasOption(SERVER_XML)) {
            config.setServerXml(args.getOptionValue(SERVER_XML));
        }
        if (args.hasOption(TOMEE_XML)) {
            config.property("openejb.conf.file", args.getOptionValue(TOMEE_XML));
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
        return config;
    }

}
