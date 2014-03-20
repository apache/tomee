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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Arrays.asList;

public class ExecRunner {
    private static final String SH_BAT_AUTO = "[.sh|.bat]";

    public static void main(final String[] args) throws Exception {
        final Properties config = new Properties();

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final InputStream is = contextClassLoader.getResourceAsStream("configuration.properties");
        if (is != null) {
            config.load(is);
            is.close();
        } else {
            throw new IllegalArgumentException("Config not found");
        }

        final String distrib = config.getProperty("distribution");
        final String workingDir = config.getProperty("workingDir");
        final InputStream distribIs = contextClassLoader.getResourceAsStream(distrib);
        File distribOutput = new File(workingDir);
        extract(distribIs, distribOutput);

        final File[] extracted = distribOutput.listFiles();
        if (extracted != null && extracted.length == 1) {
            distribOutput = extracted[0];
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
        params.add(cmd);
        if (args != null) {
            params.addAll(asList(args));
        }

        final ProcessBuilder builder = new ProcessBuilder(params.toArray(new String[params.size()])).directory(distribOutput);

        final String catalinaOpts = config.getProperty("catalinaOpts");
        if (catalinaOpts != null) { // inherit from existing env
            builder.environment().put("CATALINA_OPTS", catalinaOpts);
        }

        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        final Process process = builder.start();
        process.waitFor();
        System.out.flush();
        System.err.flush();
        System.out.println("Exit status: " + process.exitValue());
    }

    // duplicated to avoid deps, if this class has any dep then it can't be run
    private static void extract(final InputStream distrib, final File output) throws IOException {
        mkdirs(output);

        final ZipInputStream in = new ZipInputStream(distrib);

        ZipEntry entry;
        while ((entry = in.getNextEntry()) != null) {
            final String path = entry.getName();
            final File file = new File(output, path);

            if (entry.isDirectory()) {
                mkdirs(file);
                continue;
            }

            mkdirs(file.getParentFile());
            copy(in, file);

            final long lastModified = entry.getTime();
            if (lastModified > 0) {
                file.setLastModified(lastModified);
            }

        }

        in.close();
    }

    private static void copy(final ZipInputStream in, final File file) throws IOException {
        final FileOutputStream to = new FileOutputStream(file);
        try {
            final byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                to.write(buffer, 0, length);
            }
            to.flush();
        } finally {
            to.close();
        }
    }

    private static void mkdirs(File output) {
        if (!output.exists() && !output.mkdirs()) {
            throw new IllegalArgumentException("Can't create " + output.getAbsolutePath());
        }
    }

    private ExecRunner() {
        // no-op
    }
}
