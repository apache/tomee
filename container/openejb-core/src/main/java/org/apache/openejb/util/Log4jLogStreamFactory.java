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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class Log4jLogStreamFactory implements LogStreamFactory {

    private static final String LOGGING_PROPERTIES_FILE = "logging.properties";
    private static final String STANDALONE_PROPERTIES_FILE = "log4j.standalone.";
    private static final String EMBEDDED_PROPERTIES_FILE = "log4j.embedded.logging.properties";

    @Override
    public LogStream createLogStream(final LogCategory logCategory) {
        return new Log4jLogStream(logCategory);
    }

    public Log4jLogStreamFactory() {
        try {
            final String prop = SystemInstance.get().getProperty("openejb.logger.external", "false");
            final boolean externalLogging = Boolean.parseBoolean(prop);

            if (!externalLogging) configureInternal();
        } catch (Exception e) {
            // The fall back here is that if log4j.configuration system property is set, then that configuration file will be used.
            e.printStackTrace();
        }
    }

    private void configureInternal() throws IOException {
        // OpenJPA should use Log4j also
        System.setProperty("openjpa.Log", "log4j");
        System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");

        final boolean embedded = SystemInstance.get().getProperty("openejb.logging.embedded", "false").equalsIgnoreCase("true");

        final File confDir = SystemInstance.get().getBase().getDirectory("conf");

        //Use the old file name first
        File loggingPropertiesFile = new File(confDir, LOGGING_PROPERTIES_FILE);

        if ((!embedded && confDir.exists()) || (embedded && loggingPropertiesFile.exists())) {

            if (!loggingPropertiesFile.exists()) {
                //Use the new file name
                loggingPropertiesFile = new File(confDir, STANDALONE_PROPERTIES_FILE + LOGGING_PROPERTIES_FILE);
            }

            if (loggingPropertiesFile.exists()) {
                // load logging.properties file
                final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(loggingPropertiesFile));
                final Properties props = new Properties();
                props.load(bis);
                applyOverrides(props);
                preprocessProperties(props);
                PropertyConfigurator.configure(props);
                IO.close(bis);
            } else {
                // install our logging.properties file into the conf dir
                installLoggingPropertiesFile(loggingPropertiesFile);
            }
        } else {
            // Embedded and no logging.properties so configure log4j directly
            configureEmbedded();
        }
    }

    private static void applyOverrides(final Properties properties) {
        final Properties system = SystemInstance.get().getProperties();
        for (final Map.Entry<Object, Object> entry : system.entrySet()) {
            final String key = entry.getKey().toString();
            if (key.startsWith("log4j.") && !key.equals("log4j.configuration")) {
                properties.put(key, entry.getValue());
            }
        }
    }

    private void preprocessProperties(final Properties properties) {
        final FileUtils base = SystemInstance.get().getBase();
        final File confDir = new File(base.getDirectory(), "conf");
        final File baseDir = base.getDirectory();
        final File userDir = new File("foo").getParentFile();

        final File[] paths = {confDir, baseDir, userDir};

        final List<File> missing = new ArrayList<File>();

        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String key = (String) entry.getKey();
            final String value = (String) entry.getValue();

            if (key.endsWith(".File")) {
                boolean found = false;
                for (int i = 0; i < paths.length && !found; i++) {
                    final File path = paths[i];
                    final File logfile = new File(path, value);
                    if (logfile.getParentFile().exists()) {
                        properties.setProperty(key, logfile.getAbsolutePath());
                        found = true;
                    }
                }

                if (!found) {
                    final File logfile = new File(paths[0], value);
                    missing.add(logfile);
                }
            }
        }

        if (missing.size() > 0) {
            final org.apache.log4j.Logger logger = getFallabckLogger();

            logger.error("Logging may not operate as expected.  The directories for the following files do not exist so no file can be created.  See the list below.");
            for (int i = 0; i < missing.size(); i++) {
                final File file = missing.get(i);
                logger.error("[" + i + "] " + file.getAbsolutePath());
            }
        }
    }

    private org.apache.log4j.Logger getFallabckLogger() {
        final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("OpenEJB.logging");

        final SimpleLayout simpleLayout = new SimpleLayout();
        final ConsoleAppender newAppender = new ConsoleAppender(simpleLayout);
        logger.addAppender(newAppender);
        return logger;
    }

    private void configureEmbedded() {
        final URL resource = ConfUtils.getResource(EMBEDDED_PROPERTIES_FILE);
        if (resource == null) {
            System.err.println("FATAL ERROR WHILE CONFIGURING LOGGING!!!. MISSING embedded.logging.properties FILE ");

        } else {
            final Properties properties = asProperies(resource);
            applyOverrides(properties);
            PropertyConfigurator.configure(properties);

            // TODO Has to be a better way to set the log level
            final Logger logger = Logger.getLogger("org.apache");
            final Logger parent = logger.getParent();
            parent.setLevel(java.util.logging.Level.WARNING);

        }
    }

    private static Properties asProperies(final URL resource) {
        final Properties properties = new Properties();
        InputStream in = null;
        try {
            in = resource.openStream();
            properties.load(in);
        } catch (Throwable e) {
            //Ignore
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }
        return properties;
    }

    private void installLoggingPropertiesFile(final File loggingPropertiesFile) throws IOException {

        final String name = STANDALONE_PROPERTIES_FILE + LOGGING_PROPERTIES_FILE;
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(name);

        if (resource == null) {
            System.err.println("FATAL ERROR WHILE CONFIGURING LOGGING!!!. MISSING RESOURCE " + name);
            return;
        }

        InputStream in = null;
        FileOutputStream out = null;

        try {
            in = resource.openStream();
            final Properties props = new Properties();
            props.load(in);

            preprocessProperties(props);

            out = new FileOutputStream(loggingPropertiesFile);
            props.store(out, "OpenEJB Default Log4j Configuration");

            PropertyConfigurator.configure(props);
        } finally {

            if (null != in) {
                try {
                    in.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }

            if (null != out) {
                try {
                    out.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }

    }
}
