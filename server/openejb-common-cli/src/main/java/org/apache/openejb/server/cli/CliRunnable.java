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
package org.apache.openejb.server.cli;

import jline.ConsoleReader;
import jline.FileNameCompletor;
import jline.SimpleCompletor;
import org.apache.openejb.config.ConfigurableClasspathArchive;
import org.apache.openejb.server.cli.command.AbstractCommand;
import org.apache.openejb.server.cli.command.Command;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CliRunnable implements Runnable {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER, CliRunnable.class);

    public static final String SERVICE_FOLDER = "META-INF/org/apache/openejb/server";
    public static final String SERVICE_FILE = "commands.xml";

    private static final String BRANDING_FILE = "branding.properties";
    private static final String WELCOME_KEY_PREFIX = "welcome_";
    private static final String WELCOME_COMMON_KEY = WELCOME_KEY_PREFIX + "common";
    private static final String WELCOME_OPENEJB_KEY = WELCOME_KEY_PREFIX + "openejb";
    private static final String WELCOME_TOMEE_KEY = WELCOME_KEY_PREFIX + "tomee";

    public static final String TOMEE_NAME = "TomEE";
    public static final String OPENEJB_NAME = "OpenEJB";

    public static final String EXIT_COMMAND = "exit";
    private static final String OS_LINE_SEP = System.getProperty("line.separator");
    private static final String NAME;
    private static final String PROMPT;
    private static final String PROMPT_SUFFIX = "> ";

    private static final Properties PROPERTIES = new Properties();
    private static final boolean tomee;
    private static Map<String, Class<?>> COMMANDS = new HashMap<String, Class<?>>();
    private static final OpenEJBScripter scripter = new OpenEJBScripter();

    static {
        String name = OPENEJB_NAME;
        try {
            CliRunnable.class.getClassLoader().loadClass("org.apache.tomee.loader.TomcatHook");
            name = TOMEE_NAME;
        } catch (ClassNotFoundException cnfe) {
            // ignored, we are using a simple OpenEJB server
        }
        tomee = TOMEE_NAME.equals(name);
        NAME = name;
        PROMPT = NAME.toLowerCase() + PROMPT_SUFFIX;

        try {
            PROPERTIES.load(CliRunnable.class.getClassLoader().getResourceAsStream(BRANDING_FILE));
        } catch (IOException e) {
            // no-op
        }

        final ClassLoader loader = CliRunnable.class.getClassLoader();
        try {
            UrlSet urlSet = new UrlSet(loader);
            urlSet = urlSet.exclude(loader.getParent());

            final IAnnotationFinder finder = new AnnotationFinder(new ConfigurableClasspathArchive(loader, true, urlSet.getUrls()));
            for (Annotated<Class<?>> cmd : finder.findMetaAnnotatedClasses(Command.class)) {
                try {
                    final Command annotation = cmd.getAnnotation(Command.class);
                    final String key = annotation.name();
                    if (!COMMANDS.containsKey(key)) {
                        COMMANDS.put(key, cmd.get());
                    } else {
                        LOGGER.warning("command " + key + " already exists, this one will be ignored ( " + annotation.description() + ")");
                    }
                } catch (Exception e) {
                    // command ignored
                }
            }
        } catch (RuntimeException e) {
            LOGGER.error("an error occured while getting commands", e);
        } catch (IOException e) {
            LOGGER.error("can't get commands");
        }
    }

    public String lineSep;

    private OutputStream err;
    private OutputStream out;
    private InputStream sin;
    private String username;
    private String bind;
    private int port;

    public CliRunnable(String bind, int port) {
        this(bind, port, PROMPT, null);
    }

    public CliRunnable(String bind, int port, String username, String sep) {
        this.bind = bind;
        this.port = port;
        this.username = username;

        if (sep != null) { // workaround to force ConsoleReader to use another line.separator
            lineSep = sep;
            System.setProperty("line.separator", sep);
            try {
                // just to force the loading of this class with the set line.separator
                // because ConsoleReader.CR is a constant and we need sometimes another value
                // not a big issue but keeping this as a workaround
                new ConsoleReader();
            } catch (IOException ignored) {
                // no-op
            } finally {
                System.setProperty("line.separator", OS_LINE_SEP);
            }
        } else {
            lineSep = OS_LINE_SEP;
        }
    }

    public void setInputStream(InputStream in) {
        sin = in;
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    public void start() throws IOException {
        new Thread(this, "OpenEJB Cli").start();
    }

    public void destroy() {
        // no-op
    }

    public void clean() {
        scripter.clearEngines();
    }

    public void run() {
        clean();

        try {
            final StreamManager streamManager = new StreamManager(out, err, lineSep);

            final ConsoleReader reader = new ConsoleReader(sin, streamManager.getSout());
            reader.addCompletor(new FileNameCompletor());
            reader.addCompletor(new SimpleCompletor(COMMANDS.keySet().toArray(new String[COMMANDS.size()])));
            // TODO : add completers

            String line;
            StringBuilder builtWelcome = new StringBuilder("Apache OpenEJB ")
                    .append(OpenEjbVersion.get().getVersion())
                    .append("    build: ")
                    .append(OpenEjbVersion.get().getDate())
                    .append("-")
                    .append(OpenEjbVersion.get().getTime())
                    .append(lineSep);
            if (tomee) {
                builtWelcome.append(OS_LINE_SEP).append(PROPERTIES.getProperty(WELCOME_TOMEE_KEY));
            } else {
                builtWelcome.append(OS_LINE_SEP).append(PROPERTIES.getProperty(WELCOME_OPENEJB_KEY));
            }
            builtWelcome.append(lineSep).append(PROPERTIES.getProperty(WELCOME_COMMON_KEY));

            streamManager.writeOut(OpenEjbVersion.get().getUrl());
            streamManager.writeOut(builtWelcome.toString()
                    .replace("$bind", bind)
                    .replace("$port", Integer.toString(port))
                    .replace("$name", NAME)
                    .replace(OS_LINE_SEP, lineSep));

            while ((line = reader.readLine(prompt())) != null) {
                // exit simply let us go out of the loop
                // do we need a command for it?
                if (EXIT_COMMAND.equals(line)) {
                    break;
                }

                Class<?> cmdClass = null;
                for (Map.Entry<String, Class<?>> cmd : COMMANDS.entrySet()) {
                    if (line.startsWith(cmd.getKey())) {
                        cmdClass = cmd.getValue();
                        break;
                    }
                }

                if (cmdClass != null) {
                    ObjectRecipe recipe = new ObjectRecipe(cmdClass);
                    recipe.setProperty("streamManager", streamManager);
                    recipe.setProperty("command", line);
                    recipe.setProperty("scripter", scripter);
                    recipe.setProperty("commands", COMMANDS);

                    recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
                    recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
                    recipe.allow(Option.NAMED_PARAMETERS);

                    try {
                        final AbstractCommand cmdInstance = (AbstractCommand) recipe.create();
                        cmdInstance.execute(line);
                    } catch (Exception e) {
                        streamManager.writeErr(e);
                    }
                } else {
                    streamManager.writeErr("sorry i don't understand '" + line + "'");
                }
            }

            clean();
        } catch (IOException e) {
            clean();
            throw new CliRuntimeException(e);
        }
    }

    private String prompt() {
        final StringBuilder prompt = new StringBuilder("");
        if (username != null) {
            prompt.append(username);
        } else {
            prompt.append(PROMPT);
        }
        prompt.append(" @ ")
            .append(bind).append(":").append(port)
            .append(PROMPT_SUFFIX);
        return prompt.toString();
    }
}
