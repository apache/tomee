/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.maven.plugin.runner;

import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Zips;
import org.apache.tomee.util.QuickServerXmlParser;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static java.util.Arrays.asList;

/**
 * The type Exec runner with the main function to run the plugin.
 */
public class ExecRunner {
    private static final String SH_BAT_AUTO = "[.sh|.bat]";

    /**
     * Main function to run the plugin.
     *
     * @param rawArgs the raw args
     * @throws Exception the exception
     */
    public static void main(final String[] rawArgs) throws Exception {
        final String[] args;
        if (rawArgs == null || rawArgs.length == 0) {
            args = new String[]{"run"};
        } else {
            args = rawArgs;
        }

        final Properties config = new Properties();

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final InputStream is = contextClassLoader.getResourceAsStream("configuration.properties");
        if (is != null) {
            config.load(new InputStreamReader(is, "UTF-8"));
            is.close();
        } else {
            throw new IllegalArgumentException("Config not found");
        }

        final String distrib = config.getProperty("distribution");
        final String workingDir = config.getProperty("workingDir");
        final InputStream distribIs = contextClassLoader.getResourceAsStream(distrib);
        final File distribOutput = new File(workingDir);
        final File timestampFile = new File(distribOutput, "timestamp.txt");
        final boolean forceDelete = Boolean.getBoolean("tomee.runner.force-delete");
        if (forceDelete
                || !timestampFile.exists()
                || Long.parseLong(IO.slurp(timestampFile).replace(System.getProperty("line.separator"), "")) < Long.parseLong(config.getProperty("timestamp"))) {
            if (forceDelete || timestampFile.exists()) {
                System.out.println("Deleting " + distribOutput.getAbsolutePath());
                Files.delete(distribOutput);
            }
            System.out.println("Extracting tomee to " + distribOutput.getAbsolutePath());
            Zips.unzip(distribIs, distribOutput, false);
            IO.writeString(timestampFile, config.getProperty("timestamp", Long.toString(System.currentTimeMillis())));
        }

        final File[] scripts = new File(distribOutput, "bin").listFiles();
        if (scripts != null) { // dont use filefilter to avoid dependency issue
            for (final File f : scripts) {
                setExecutable(f);
            }
        }

        String cmd = config.getProperty("command");
        if (cmd.endsWith(SH_BAT_AUTO)) {
            final int lastSlash = cmd.lastIndexOf('/');
            if (lastSlash > 0) {
                final String dir = cmd.substring(0, lastSlash);
                final boolean isWin = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
                final String script = cmd.substring(lastSlash + 1, cmd.length() - SH_BAT_AUTO.length()).replace('/', isWin ? '\\' : '/')
                        + (isWin ? ".bat" : ".sh");
                final File scriptFile = new File(distribOutput, dir + File.separator + script);
                if (!scriptFile.exists()) {
                    throw new IllegalArgumentException("Can't find  " + cmd);
                }
                cmd = scriptFile.getAbsolutePath();
                setExecutable(scriptFile); // in case it is not in bin/
            }
        }

        final String additionalArgs = System.getProperty("additionalSystemProperties");

        // build also post here to avoid surprises
        final Map<?, ?> map = new HashMap<>();
        final Collection<Runnable> preTasks = buildRunnables(config.getProperty("preTasks"), map);
        final Collection<Runnable> postTasks = buildRunnables(config.getProperty("postTasks"), map);
        final boolean doWait = Boolean.parseBoolean(config.getProperty("waitFor"));
        if (!doWait && !postTasks.isEmpty()) {
            throw new IllegalArgumentException("You can't use post task if you dont wait for the process.");
        }

        for (final Runnable r : preTasks) {
            r.run();
        }

        try {
            final Collection<String> params = new ArrayList<>();
            if ("java".equals(cmd)) {
                final File base = findBase(distribOutput);

                final QuickServerXmlParser parser = QuickServerXmlParser.parse(new File(base, "conf/server.xml"));

                System.setProperty("openejb.home", base.getAbsolutePath());
                System.setProperty("server.shutdown.port", parser.stop());
                System.setProperty("server.shutdown.command", config.getProperty("shutdownCommand"));

                final RemoteServer server = new RemoteServer();
                server.setPortStartup(Integer.parseInt(parser.http()));

                if (config.containsKey("additionalClasspath")) {
                    server.setAdditionalClasspath(config.getProperty("additionalClasspath"));
                }

                final List<String> jvmArgs = new LinkedList<>();
                if (additionalArgs != null) {
                    Collections.addAll(jvmArgs, additionalArgs.split(" "));
                }
                for (final String k : config.stringPropertyNames()) {
                    if (k.startsWith("jvmArg.")) {
                        jvmArgs.add(config.getProperty(k));
                    }
                }
                final String userProps = String.class.cast(map.get("jvmArgs"));
                if (userProps != null) {
                    Collections.addAll(jvmArgs, userProps.split(" "));
                }

                if ("run".equals(args[0])) {
                    args[0] = "start";
                }

                try {
                    server.start(jvmArgs, args[0], true);
                } catch (final Exception e) {
                    server.destroy();
                    throw e;
                }

                if (doWait) {
                    server.getServer().waitFor();
                }
            } else {
                // TODO: split cmd correctly to support multiple inlined segments in cmd
                if (cmd.endsWith(".bat") && !cmd.startsWith("cmd.exe")) {
                    params.add("cmd.exe");
                    params.add("/c");
                } // else suppose the user knows what he does
                params.add(cmd);
                params.addAll(asList(args));

                final ProcessBuilder builder = new ProcessBuilder(params.toArray(new String[params.size()]))
                        .inheritIO()
                        .directory(findBase(distribOutput));

                final String existingOpts = System.getenv("CATALINA_OPTS");
                final String catalinaOpts = config.getProperty("catalinaOpts");
                if (catalinaOpts != null || existingOpts != null || additionalArgs != null) { // inherit from existing env
                    builder.environment()
                            .put("CATALINA_OPTS",
                                    identityOrEmpty(catalinaOpts) + " " +
                                            identityOrEmpty(existingOpts) + " " +
                                            identityOrEmpty(additionalArgs) + " " +
                                            identityOrEmpty(String.class.cast(map.get("jvmArgs"))));
                }

                if (doWait) {
                    builder.start().waitFor();
                }
            }

            System.out.flush();
            System.err.flush();
            System.out.println("Exited Successfully!");
        } finally {
            for (final Runnable r : postTasks) {
                r.run();
            }
        }
    }

    private static Collection<Runnable> buildRunnables(final String classes, final Map<?, ?> state) {
        final Collection<Runnable> tasks = new ArrayList<>();
        if (classes == null || classes.trim().isEmpty()) {
            return tasks;
        }
        for (final String className : classes.split(" *, *")) {
            try {
                final Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                try {
                    final Constructor<?> cons = clazz.getConstructor(Map.class);
                    tasks.add(Runnable.class.cast(cons.newInstance(state)));
                } catch (final Throwable th) {
                    tasks.add(Runnable.class.cast(clazz.newInstance()));
                }
            } catch (final Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        return tasks;
    }

    private static void setExecutable(final File f) {
        if (f.getName().endsWith(".sh") && !f.canExecute()) {
            if (!f.setExecutable(true, true)) {
                System.err.println("Failed make file executable: " + f);
            }
        }
    }

    private static File findBase(final File distribOutput) {
        final File[] extracted = distribOutput.listFiles();
        if (extracted != null) {
            File newRoot = null;
            for (final File e : extracted) {
                if (e.isDirectory()) {
                    if (newRoot == null) {
                        newRoot = e;
                    } else {
                        newRoot = null;
                        break;
                    }
                }
            }
            if (newRoot != null) {
                return newRoot;
            }
        }
        return distribOutput;
    }

    private static String identityOrEmpty(final String value) {
        return (value != null ? value : "");
    }

    private ExecRunner() {
        // no-op
    }
}
