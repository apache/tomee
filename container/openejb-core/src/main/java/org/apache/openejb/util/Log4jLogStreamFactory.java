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

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.openejb.cdi.logging.Log4jLoggerFactory;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
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
            final boolean externalLogging = SystemInstance.get().getOptions().get("openejb.logger.external", false);
            if (!externalLogging) {
                configureInternal();
            }
        } catch (final Exception e) {
            // The fall back here is that if log4j.configuration system property is set, then that configuration file will be used.
            e.printStackTrace();
        }
        JavaSecurityManagers.setSystemProperty("openwebbeans.logging.factory", "org.apache.openejb.cdi.logging.Log4jLoggerFactory");
    }

    private void configureInternal() throws IOException {
        // OpenJPA should use Log4j also
        JavaSecurityManagers.setSystemProperty("openjpa.Log", "log4j");
        JavaSecurityManagers.setSystemProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");
        JavaSecurityManagers.setSystemProperty(WebBeansLoggerFacade.OPENWEBBEANS_LOGGING_FACTORY_PROP, Log4jLoggerFactory.class.getName());

        final boolean embedded = SystemInstance.get().getOptions().get("openejb.logging.embedded", false);

        File confDir = SystemInstance.get().getConf(null);
        if (confDir == null) {
            confDir = SystemInstance.get().getBase().getDirectory("conf");
        }

        //Use the old file name first
        File loggingPropertiesFile = new File(confDir, LOGGING_PROPERTIES_FILE);

        if (!embedded && confDir.exists() || embedded && loggingPropertiesFile.exists()) {

            if (!loggingPropertiesFile.exists()) {
                //Use the new file name
                loggingPropertiesFile = new File(confDir, STANDALONE_PROPERTIES_FILE + LOGGING_PROPERTIES_FILE);
            }

            if (loggingPropertiesFile.exists()) {
                // load logging.properties file
                final Properties properties = IO.readProperties(loggingPropertiesFile);
                applyOverrides(properties);
                preprocessProperties(properties);
                PropertyConfigurator.configure(properties);
            } else {
                final File log4jProperties = new File(confDir, "log4j.properties");
                if (log4jProperties.exists()) {
                    PropertyConfigurator.configure(log4jProperties.toURI().toURL());
                } else {
                    final File log4jXml = new File(confDir, "log4j.xml");
                    if (log4jXml.exists()) {
                        DOMConfigurator.configure(log4jXml.toURI().toURL());
                    } else {

                        // needs mvn:org.apache.logging.log4j:log4j-1.2-api:2.0-rc1,
                        // typically container will contain:
                        //
                        // mvn:org.apache.logging.log4j:log4j-api:2.0-rc1
                        // mvn:org.apache.logging.log4j:log4j-core:2.0-rc1
                        // mvn:org.apache.logging.log4j:log4j-1.2-api:2.0-rc1
                        final File log4j2Xml = new File(confDir, "log4j2.xml");
                        if (!log4j2Xml.exists()) {
                            // install our logging.properties file into the conf dir
                            installLoggingPropertiesFile(loggingPropertiesFile);
                        } // else ignore, DOMConfigurator is just a mock doing nothing so don't call it and don't install defaults
                    }
                }
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
        final File confDir = SystemInstance.get().getConf(null);
        final File baseDir = base.getDirectory();
        final File userDir = new File("foo").getParentFile();

        final File[] paths = {confDir, baseDir, userDir};

        final List<File> missing = new ArrayList<>();

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

        /* will break log4j support and not that needed since we'll get the same by default just not with a nice layout
           which is good when something is wrong

        final SimpleLayout simpleLayout = new SimpleLayout();
        final ConsoleAppender newAppender = new ConsoleAppender(simpleLayout);
        logger.addAppender(newAppender);
        */
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
            parent.setLevel(Level.WARNING);

        }
    }

    private static Properties asProperies(final URL resource) {
        final Properties properties = new Properties();
        try {
            IO.readProperties(resource, properties);
        } catch (final Throwable e) {
            //Ignore
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

        final Properties props = IO.readProperties(resource);

        preprocessProperties(props);

        final OutputStream out = IO.write(loggingPropertiesFile);
        try {
            props.store(out, "OpenEJB Default Log4j Configuration");
        } finally {
            IO.close(out);
        }

        PropertyConfigurator.configure(props);

    }
}
