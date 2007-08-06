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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.finder.ResourceFinder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class Logger {

    protected org.apache.log4j.Logger _logger = null;

    private String baseName;

    private static final String SUFFIX = ".Messages";

    private static final String OPENEJB = "org.apache.openejb";
    /**
     * Computes the parent of a resource name. E.g. if we pass in a key of
     * a.b.c, it returns the value a.b
     */
    private static final Computable<String, String> heirarchyResolver = new Computable<String, String>() {
        public String compute(String key) throws InterruptedException {
            int index = key.lastIndexOf(".");
            String parent = key.substring(0, index);
            if (parent.contains(OPENEJB))
                return parent;
            return null;
        }
    };
    /**
     * Simply returns the ResourceBundle for a given baseName
     */
    private static final Computable<String, ResourceBundle> bundleResolver = new Computable<String, ResourceBundle>() {
        public ResourceBundle compute(String baseName)
                throws InterruptedException {
            try {
                return ResourceBundle.getBundle(baseName + SUFFIX);
            } catch (MissingResourceException e) {
                return null;
            }
        }
    };
    /**
     * Builds a Logger object and returns it
     */
    private static final Computable<String[], Logger> loggerResolver = new Computable<String[], Logger>() {
        public Logger compute(String[] args) throws InterruptedException {

            Logger logger = new Logger();
            logger._logger = org.apache.log4j.Logger.getLogger(args[0]);
            logger.baseName = args[1];
            return logger;

        }
    };
    /**
     * Creates a MessageFormat object for a message and returns it
     */
    private static final Computable<String, MessageFormat> messageFormatResolver = new Computable<String, MessageFormat>() {
        public MessageFormat compute(String message)
                throws InterruptedException {

            return new MessageFormat(message);

        }
    };
    /**
     * Cache of parent-child relationships between resource names
     */
    private static final Computable<String, String> heirarchyCache = new Memoizer<String, String>(
            heirarchyResolver);
    /**
     * Cache of ResourceBundles
     */
    private static final Computable<String, ResourceBundle> bundleCache = new Memoizer<String, ResourceBundle>(
            bundleResolver);
    /**
     * Cache of Loggers
     */
    private static final Computable<String[], Logger> loggerCache = new Memoizer<String[], Logger>(
            loggerResolver);
    /**
     * Cache of MessageFormats
     */
    private static final Computable<String, MessageFormat> messageFormatCache = new Memoizer<String, MessageFormat>(
            messageFormatResolver);

    /**
     * Given a key and a baseName, this method computes a message for a key. if
     * the key is not found in this ResourceBundle for this baseName, then it
     * recursively looks up its parent to find the message for a key. If no
     * message is found for a key, the key is returned as is and is logged by
     * the logger.
     */
    private String getMessage(String key, String baseName) {
        try {

            ResourceBundle bundle = bundleCache.compute(baseName);
            if (bundle != null) {
                String message = null;
                try {
                    message = bundle.getString(key);
                    return message;
                } catch (MissingResourceException e) {
                    String parentName = heirarchyCache.compute(baseName);
                    if (parentName == null)
                        return key;
                    else
                        return getMessage(key, parentName);
                }

            } else {
                String parentName = heirarchyCache.compute(baseName);
                if (parentName == null)
                    return key;
                else
                    return getMessage(key, parentName);

            }
        } catch (InterruptedException e) {
            // ignore
        }
        return key;
    }

    /**
     * @deprecated Use {@link #init()} instead
     */
    public static void initialize(Properties props) {
        Log4jConfigUtils log4j = new Logger.Log4jConfigUtils(props);

        log4j.configure();
    }

    /**
     * Initialise using {@link SystemInstance} as the source of properties
     */
    public static void init() {
        initialize(SystemInstance.get().getProperties());
    }

    /**
     * Finds a Logger from the cache and returns it. If not found in cache then builds a Logger and returns it.
     *
     * @param name     - The name of the logger
     * @param baseName - The baseName for the ResourceBundle
     * @return Logger
     */
    public static Logger getInstance(String name, String baseName) {
        try {
            Logger logger = loggerCache
                    .compute(new String[]{name, baseName});
            return logger;
        } catch (InterruptedException e) {
            /*
                * Don't return null here. Just create a new Logger and set it up.
                * It will not be stored in the cache, but a later lookup for the
                * same Logger would probably end up in the cache
                */
            Logger logger = new Logger();
            logger._logger = org.apache.log4j.Logger.getLogger(name);
            logger.baseName = baseName;
            return logger;
        }
    }

    public static Logger getInstance(String name, Class clazz) {
        return getInstance(name, packageName(clazz));
    }

    private static String packageName(Class clazz) {
        String name = clazz.getName();
        return name.substring(0, name.lastIndexOf("."));
    }

    /**
     * Formats a given message
     *
     * @param message
     * @param args
     * @return the formatted message
     */
    private String formatMessage(String message, Object... args) {
        try {
            MessageFormat mf = messageFormatCache.compute(message);
            String msg = mf.format(args);
            return msg;
        } catch (InterruptedException e) {
            return "Error in formatting message " + message;
        }

    }

    private Logger() {
    }

    public boolean isDebugEnabled() {
        return _logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return _logger.isEnabledFor(Level.ERROR);
    }

    public boolean isFatalEnabled() {
        return _logger.isEnabledFor(Level.FATAL);
    }

    public boolean isInfoEnabled() {
        return _logger.isInfoEnabled();
    }

    public boolean isWarningEnabled() {
        return _logger.isEnabledFor(Level.WARN);
    }

    /**
     * If this level is enabled, then it finds a message for the given key  and logs it
     *
     * @param message - This could be a plain message or a key in Messages.properties
     * @return the formatted i18n message
     */
    public String debug(String message) {

        if (isDebugEnabled()) {
            String msg = getMessage(message, baseName);
            _logger.debug(msg);
            return msg;
        }
        return message;
    }

    public String debug(String message, Object... args) {

        if (isDebugEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            _logger.debug(msg);
            return msg;
        }
        return message;
    }

    public String debug(String message, Throwable t) {

        if (isDebugEnabled()) {
            String msg = getMessage(message, baseName);
            _logger.debug(msg, t);
            return msg;
        }
        return message;
    }

    public String debug(String message, Throwable t, Object... args) {

        if (isDebugEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            _logger.debug(msg, t);
            return msg;
        }
        return message;
    }

    public String error(String message) {

        if (isErrorEnabled()) {
            String msg = getMessage(message, baseName);
            _logger.error(msg);
            return msg;
        }
        return message;
    }

    public String error(String message, Object... args) {

        if (isErrorEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            _logger.error(msg);
            return msg;
        }
        return message;
    }

    public String error(String message, Throwable t) {

        if (isErrorEnabled()) {
            String msg = getMessage(message, baseName);
            _logger.error(msg, t);
            return msg;
        }
        return message;
    }

    public String error(String message, Throwable t, Object... args) {

        if (isErrorEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            _logger.error(msg, t);
            return msg;
        }
        return message;
    }

    public String fatal(String message) {
        if (isFatalEnabled()) {
            String msg = getMessage(message, baseName);
            _logger.fatal(msg);
            return msg;
        }
        return message;
    }

    public String fatal(String message, Object... args) {
        if (isFatalEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            _logger.fatal(msg);
            return msg;
        }
        return message;
    }

    public String fatal(String message, Throwable t) {
        if (isFatalEnabled()) {
            String msg = getMessage(message, baseName);
            _logger.fatal(msg, t);
            return msg;
        }
        return message;
    }

    public String fatal(String message, Throwable t, Object... args) {
        if (isFatalEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            _logger.fatal(msg, t);
            return msg;
        }
        return message;
    }

    public String info(String message) {
        if (isInfoEnabled()) {
            String msg = getMessage(message, baseName);
            _logger.info(msg);
            return msg;
        }
        return message;
    }

    public String info(String message, Object... args) {
        if (isInfoEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            _logger.info(msg);
            return msg;
        }
        return message;
    }

    public String info(String message, Throwable t) {
        if (isInfoEnabled()) {
            String msg = getMessage(message, baseName);
            _logger.info(msg, t);
            return msg;
        }
        return message;
    }

    public String info(String message, Throwable t, Object... args) {
        if (isInfoEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            _logger.info(msg, t);
            return msg;
        }
        return message;
    }

    public String warning(String message) {
        if (isWarningEnabled()) {
            String msg = getMessage(message, baseName);
            _logger.warn(msg);
            return msg;
        }
        return message;
    }

    public String warning(String message, Object... args) {
        if (isWarningEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            _logger.warn(msg);
            return msg;
        }
        return message;
    }

    public String warning(String message, Throwable t) {
        if (isWarningEnabled()) {
            String msg = getMessage(message, baseName);
            _logger.warn(msg, t);
            return msg;
        }
        return message;
    }

    public String warning(String message, Throwable t, Object... args) {
        if (isWarningEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            _logger.warn(msg, t);
            return msg;
        }
        return message;
    }

    static class Log4jConfigUtils {

        Properties props;

        public Log4jConfigUtils(Properties props) {
            this.props = props;
        }

        public void configure() {
            // make openjpa use log4j
            System.setProperty("openjpa.Log", "log4j");

            Properties properties = null;

            String config = props.getProperty("log4j.configuration");
            String[] search = {config, "logging.properties", "logging.conf"};

            FileUtils base = SystemInstance.get().getBase();
            File confDir = new File(base.getDirectory(), "conf");
            File baseDir = base.getDirectory();
            File userDir = new File("foo").getParentFile();

            File[] paths = {confDir, baseDir, userDir};

            for (int i = 0; i < search.length && properties == null; i++) {
                String fileName = search[i];
                if (fileName == null) {
                    continue;
                }

                for (int j = 0; j < paths.length; j++) {
                    File path = paths[j];

                    File configFile = new File(path, fileName);

                    if (configFile.exists()) {

                        InputStream in = null;
                        try {
                            in = new FileInputStream(configFile);
                            in = new BufferedInputStream(in);
                            properties = new Properties();
                            properties.load(in);
                        } catch (IOException e) {
                            org.apache.log4j.Logger logger = doFallbackConfiguration();
                            logger.error("Unable to read logging config file "
                                    + configFile.getAbsolutePath(), e);
                        } finally {
                            try {
                                in.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }

            if (properties == null) {
                String configData = null;
                try {
                    ResourceFinder finder = new ResourceFinder("");
                    configData = finder.findString("default.logging.conf");
                    properties = new Properties();
                    properties.load(new ByteArrayInputStream(configData
                            .getBytes()));
                } catch (IOException e) {
                    org.apache.log4j.Logger logger = doFallbackConfiguration();
                    logger.error("Unable to read default logging config file.",
                            e);
                    return;
                }

                if (confDir.exists()) {
                    OutputStream out = null;
                    File configFile = new File(confDir, "logging.properties");
                    try {
                        out = new FileOutputStream(configFile);
                        out.write(configData.getBytes());
                    } catch (IOException e) {
                        org.apache.log4j.Logger logger = doFallbackConfiguration();
                        logger.warn(
                                "Unable write default logging config file to "
                                        + configFile.getAbsolutePath(), e);
                    } finally {
                        try {
                            out.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }

            List missing = new ArrayList();

            for (Iterator iterator = properties.entrySet().iterator(); iterator
                    .hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                if (key.endsWith(".File")) {

                    boolean found = false;
                    for (int i = 0; i < paths.length && !found; i++) {
                        File path = paths[i];
                        File logfile = new File(path, value);
                        if (logfile.getParentFile().exists()) {
                            properties.setProperty(key, logfile
                                    .getAbsolutePath());
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
                org.apache.log4j.Logger logger = doFallbackConfiguration();

                logger
                        .warn("Unable use logging config as there are "
                                + missing.size()
                                + " file references containing directories which have not been created.  See the list below.");
                for (int i = 0; i < missing.size(); i++) {
                    File file = (File) missing.get(i);
                    logger.warn("[" + i + "] " + file.getAbsolutePath());
                }
            } else {
                PropertyConfigurator.configure(properties);
            }

        }

        private org.apache.log4j.Logger doFallbackConfiguration() {
            set("org.apache.activemq", Level.INFO);
            set("openjpa", Level.WARN);
            set("Transaction", Level.WARN);
            set("OpenEJB.startup", Level.INFO);
            set("OpenEJB.startup.config", Level.WARN);
            set("OpenEJB", Level.WARN);

            org.apache.log4j.Logger logger = org.apache.log4j.Logger
                    .getLogger("OpenEJB");

            SimpleLayout simpleLayout = new SimpleLayout();
            ConsoleAppender newAppender = new ConsoleAppender(simpleLayout);
            logger.addAppender(newAppender);
            return logger;

        }

        private void set(String category, Level level) {
            org.apache.log4j.Logger.getLogger(category).setLevel(level);
            // Enumeration allAppenders =
            // org.apache.log4j.Logger.getLogger(category).getAllAppenders();
            // while (allAppenders.hasMoreElements()) {
            // Object object = allAppenders.nextElement();
            // System.out.println(category +" = " + object);
            // }
		}

	}
}
