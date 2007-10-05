/**
 *
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
package org.apache.openejb.util;

import org.apache.log4j.PropertyConfigurator;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class JuliLogStreamFactory implements LogStreamFactory {
    private static final String LOGGING_PROPERTIES_FILE = "logging.properties";
    private static final String DEFAULT_LOGGING_PROPERTIES_FILE = "juli.properties";
    private static final String EMBEDDED_PROPERTIES_FILE = "embedded.juli.properties";

    public LogStream createLogStream(LogCategory logCategory) {
        return new JuliLogStream(logCategory);
    }

    public JuliLogStreamFactory() {
        try {
            String prop = System.getProperty("openejb.logger.external", "false");
            boolean externalLogging = Boolean.parseBoolean(prop);
            if (!externalLogging) {
                configureInternal();
            }
        } catch (Exception e) {
            // The fall back here is that if log4j.configuration system property is set, then that configuration file will be used.
            e.printStackTrace();
        }
    }

    private void configureInternal() throws IOException {
        File confDir = SystemInstance.get().getBase().getDirectory("conf");
        File loggingPropertiesFile = new File(confDir, LOGGING_PROPERTIES_FILE);
        if (confDir.exists()) {
            if (loggingPropertiesFile.exists()) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(loggingPropertiesFile);
                    LogManager.getLogManager().readConfiguration(in);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                }
            } else {
                // install our logging.properties file into the conf dir
                installLoggingPropertiesFile(loggingPropertiesFile);
            }
        } else {
            // no conf directory, so we assume we are embedded
            // configure log4j directly
            configureEmbedded();
        }
    }

    private void preprocessProperties(Properties properties) {
        FileUtils base = SystemInstance.get().getBase();
        File confDir = new File(base.getDirectory(), "conf");
        File baseDir = base.getDirectory();
        File userDir = new File("foo").getParentFile();

        File[] paths = {confDir, baseDir, userDir};

        List<File> missing = new ArrayList<File>();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (key.endsWith(".File")) {
                boolean found = false;
                for (int i = 0; i < paths.length && !found; i++) {
                    File path = paths[i];
                    File logfile = new File(path, value);
                    if (logfile.getParentFile().exists()) {
                        properties.setProperty(key, logfile.getAbsolutePath());
                        found = true;
                    }
                }

                if (!found) {
                    File logfile = new File(paths[0], value);
                    missing.add(logfile);
                }
            }
        }

        if (missing.size() > 0) {
            java.util.logging.Logger logger = getFallabckLogger();

            logger.severe("Logging may not operate as expected.  The directories for the following files do not exist so no file can be created.  See the list below.");
            for (int i = 0; i < missing.size(); i++) {
                File file = missing.get(i);
                logger.severe("[" + i + "] " + file.getAbsolutePath());
            }
        }
    }

    private Logger getFallabckLogger() {
        java.util.logging.Logger logger = Logger.getLogger("OpenEJB.logging");
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);
        return logger;
    }

    private void configureEmbedded() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(EMBEDDED_PROPERTIES_FILE);
        if (resource != null) PropertyConfigurator.configure(resource);
        else System.out.println("FATAL ERROR WHILE CONFIGURING LOGGING!!!. MISSING embedded.logging.properties FILE ");
    }

    private void installLoggingPropertiesFile(File loggingPropertiesFile) throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_LOGGING_PROPERTIES_FILE);
        if (resource == null) {
            System.out.println("FATAL ERROR WHILE CONFIGURING LOGGING!!!. MISSING logging.properties FILE ");
            return;
        }
        InputStream in = resource.openStream();
        in = new BufferedInputStream(in);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte buf[] = new byte[4096];
        for (int count = in.read(buf); count >= 0 ; count = in.read(buf)) {
            bao.write(buf, 0, count);
        }
        byte[] byteArray = bao.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);

        Properties props = new Properties();
        props.load(bis);
        preprocessProperties(props);
        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(loggingPropertiesFile));
        bout.write(byteArray);
        PropertyConfigurator.configure(props);
        try {
            bout.close();
        } catch (IOException e) {

        }
        try {
            in.close();
        } catch (IOException e) {

        }
    }
}
