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
import org.apache.openejb.util.Pipe;
import org.apache.tomee.util.QuickServerXmlParser;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static java.util.Arrays.asList;

public class ExecRunner {
    private static final String SH_BAT_AUTO = "[.sh|.bat]";

    public static void main(final String[] rawArgs) throws Exception {
        final String[] args;
        if (rawArgs == null || rawArgs.length == 0) {
            args = new String[] { "run" };
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
        File distribOutput = new File(workingDir);
        final File timestampFile = new File(distribOutput, "timestamp.txt");
        if (!timestampFile.exists()
                || Long.parseLong(IO.slurp(timestampFile).replace(System.getProperty("line.separator"), "")) < Long.parseLong(config.getProperty("timestamp"))) {
            if (timestampFile.exists()) {
                System.out.println("Deleting " + distribOutput.getAbsolutePath());
                Files.delete(distribOutput);
            }
            System.out.println("Extracting tomee to " + distribOutput.getAbsolutePath());
            Zips.unzip(distribIs, distribOutput, false);
            IO.writeString(timestampFile, config.getProperty("timestamp"));
        }

        final File[] extracted = distribOutput.listFiles();
        if (extracted != null && extracted.length == 1) {
            distribOutput = extracted[0];
        }
        final File[] scripts = new File(distribOutput, "conf").listFiles();
        if (scripts != null) { // dont use filefilter to avoid dependency issue
            for (final File f : scripts) {
                if (f.getName().endsWith(".sh") && !f.canExecute()) {
                    f.setExecutable(true, true);
                }
            }
        }

        String cmd = config.getProperty("command");
        if (cmd.endsWith(SH_BAT_AUTO)) {
            final int lastSlash = cmd.lastIndexOf('/');
            if (lastSlash > 0) {
                final String dir = cmd.substring(0, lastSlash);
                final String script = cmd.substring(lastSlash + 1, cmd.length() - SH_BAT_AUTO.length())
                        + (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win") ? ".bat" : ".sh");
                cmd = dir + '/' + script;
                final File scriptFile = new File(distribOutput, cmd);
                if (!scriptFile.exists()) {
                    throw new IllegalArgumentException("Can't find  " + cmd);
                }
                if (cmd.endsWith(".sh")) {
                    scriptFile.setExecutable(true);
                }
            }
        }

        final Collection<String> params = new ArrayList<String>();
        if ("java".equals(cmd)) {
            final QuickServerXmlParser parser = QuickServerXmlParser.parse(new File(distribOutput,"conf/server.xml"));

            System.setProperty("openejb.home", distribOutput.getAbsolutePath());
            System.setProperty("server.shutdown.port", parser.stop());
            System.setProperty("server.shutdown.command", config.getProperty("shutdownCommand"));

            final RemoteServer server = new RemoteServer();
            if (config.containsKey("additionalClasspath")) {
                server.setAdditionalClasspath(config.getProperty("additionalClasspath"));
            }

            final List<String> jvmArgs = new LinkedList<String>();
            for (final String k : config.stringPropertyNames()) {
                if (k.startsWith("jvmArg.")) {
                    jvmArgs.add(config.getProperty(k));
                }
            }

            if ("run".equals(args[0])) {
                args[0] = "start";
            }
            server.start(jvmArgs, args[0], true);
            server.getServer().waitFor();
        } else {
            params.add(cmd);
            params.addAll(asList(args));

            final ProcessBuilder builder = new ProcessBuilder(params.toArray(new String[params.size()])).directory(distribOutput);

            final String additionalArgs = System.getProperty("additionalSystemProperties");
            final String existingOpts = System.getenv("CATALINA_OPTS");
            final String catalinaOpts = config.getProperty("catalinaOpts");
            if (catalinaOpts != null || existingOpts != null || additionalArgs != null) { // inherit from existing env
                builder.environment().put("CATALINA_OPTS", identityOrEmpty(catalinaOpts) + " " + identityOrEmpty(existingOpts) + " " + identityOrEmpty(additionalArgs));
            }

            boolean redirectOut = false;
            try { // java >= 7
                ProcessBuilder.class.getDeclaredMethod("inheritIO").invoke(builder);
            } catch (final Throwable th){ // java 6
                redirectOut = true;
            }

            final Process process = builder.start();
            if (redirectOut) {
                Pipe.pipe(process);
            }

            process.waitFor();
        }

        System.out.flush();
        System.err.flush();
        System.out.println("Exited Successfully!");
    }

    private static String identityOrEmpty(final String value) {
        return (value != null ? value : "");
    }

    private ExecRunner() {
        // no-op
    }
}
