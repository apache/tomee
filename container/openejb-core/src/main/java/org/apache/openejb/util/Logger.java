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

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class Logger {
    private static final String SUFFIX = ".Messages";
    private static final String OPENEJB = "org.apache.tomee";
    private static LogStreamFactory logStreamFactory; // TODO: make it resettable

    // don't return the instance since it needs to stay private but export which one is used to allow integration with other libs (as tomcat ;))
    @SuppressWarnings("UnusedDeclaration")
    public static String delegateClass() {
        if (logStreamFactory == null) {
            throw new IllegalStateException("Call this method after having configured the logger");
        }
        return logStreamFactory.getClass().getName();
    }

    public static LogStreamFactory unsafeDelegateClass() {
        return logStreamFactory;
    }

    public static synchronized void configure() {
        configure(SystemInstance.isInitialized() ? SystemInstance.get().getProperties() : JavaSecurityManagers.getSystemProperties());
    }

    public static synchronized void configure(final Properties config) {
        if (logStreamFactory != null) {
            return;
        }

        //See if user factory has been specified
        final String julFqn = JuliLogStreamFactory.class.getName();
        String factoryName = config.getProperty("openejb.log.factory",
                SystemInstance.isInitialized() ?  SystemInstance.get().getOptions().get("openejb.log.factory", julFqn) : julFqn);

        if ("jul".equalsIgnoreCase(factoryName) || "juli".equalsIgnoreCase(factoryName)) {
            factoryName = JuliLogStreamFactory.class.getName();
        } else if ("slf4j".equalsIgnoreCase(factoryName)) {
            factoryName = Slf4jLogStreamFactory.class.getName();
        } else if ("log4j".equalsIgnoreCase(factoryName)) {

            if (exists("org.apache.log4j.Logger")) {

                // don't use .class to avoid to force loading since log4j is not mandatory
                factoryName = "org.apache.openejb.util.Log4jLogStreamFactory";

            } else {

                System.out.println("Cannot respect 'openejb.log.factory=log4j' setting as Log4j is not in the classpath.");

            }

        } else if ("pax".equalsIgnoreCase(factoryName)) {
            factoryName = "org.apache.openejb.util.PaxLogStreamFactory";
        } else if ("log4j2".equalsIgnoreCase(factoryName)) {
            factoryName = "org.apache.openejb.util.Log4j2LogStreamFactory";
        }

        // we can be called before having SystemInstance so we need this hack to set some specific
        // environment
        // without changing LogStreamFactory contract
        final String[] specialKeys = new String[] { "openejb.jul.forceReload", "openejb.jul.consoleHandlerClazz", "openejb.logger.external" };
        final String[] originals = new String[specialKeys.length];
        for (int i = 0; i < specialKeys.length; i++) {
            originals[i] = JavaSecurityManagers.getSystemProperty(specialKeys[i]);
            final String property = config.getProperty(
                    specialKeys[i],
                    SystemInstance.isInitialized() ? SystemInstance.get().getOptions().get(specialKeys[i], (String) null) : null);
            if (property != null) {
                JavaSecurityManagers.setSystemProperty(specialKeys[i], property);
            }
        }

        try {
            if (factoryName != null) {

                logStreamFactory = createFactory(factoryName);
            }

            if (isLog4jImplied()) {
                logStreamFactory = createFactory("org.apache.openejb.util.Log4jLogStreamFactory");
            }

            //Fall back -> JUL
            if (logStreamFactory == null) {
                logStreamFactory = new JuliLogStreamFactory();
            }

            checkForIgnoredLog4jConfig();
        } finally {
            for (int i = 0; i < specialKeys.length; i++) {
                if (originals[i] == null) {
                    JavaSecurityManagers.removeSystemProperty(specialKeys[i]);
                } else {
                    JavaSecurityManagers.setSystemProperty(specialKeys[i], originals[i]);
                }
            }
        }
    }

    private static void checkForIgnoredLog4jConfig() {
        if (logStreamFactory.getClass().getName().equals("org.apache.openejb.util.Log4jLogStreamFactory")) {
            return;
        }

        try {
            final Properties configFile = log4j(loadLoggingProperties());

            final Properties systemProperties = log4j(SystemInstance.get().getProperties());

            if (configFile.size() == 0 && systemProperties.size() == 0) {
                return;
            }
            if (systemProperties.size() == 1 && "log4j.configurationFile".equals(systemProperties.stringPropertyNames().iterator().next())) {
                // not a logger config but the overall config
                // since log4j2 uses it too we can't pollute logs with warnings there for that only
                return;
            }

            final LogStream stream = logStreamFactory.createLogStream(LogCategory.OPENEJB);

            stream.warn("Log4j not installed. The following properties will be ignored.");

            final String format = "Ignored %s property '%s'";

            for (final Object key : configFile.keySet()) {
                stream.warn(String.format(format, "conf/logging.properties", key));
            }

            for (final Object key : systemProperties.keySet()) {
                stream.warn(String.format(format, "Property overrides", key));
            }
        } catch (final Throwable e) {
            // added strong catch block just in case
            // This check is only a convenience
        }
    }

    private static LogStreamFactory createFactory(final String factoryName) {

        final Class<?> factoryClass = load(factoryName);

        if (factoryClass == null) {
            return null;
        }

        try {
            //Try and use the user specified factory
            return (LogStreamFactory) factoryClass.newInstance();
        } catch (final Throwable e) {
            //Ignore
        }

        return null;
    }

    private static Class<?> load(final String factoryName) {
        try {
            final ClassLoader classLoader = Logger.class.getClassLoader();
            return classLoader.loadClass(factoryName);
        } catch (final Throwable e) {
            //Ignore
        }

        try {
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            return contextClassLoader.loadClass(factoryName);
        } catch (final Throwable e1) {
            //Ignore
        }

        try {
            return Class.forName(factoryName);
        } catch (final Throwable e2) {
            //Ignore
        }

        return null;
    }

    /**
     * Computes the parent of a resource name. E.g. if we pass in a key of
     * a.b.c, it returns the value a.b
     */
    private static final Computable<String, String> heirarchyResolver = new Computable<String, String>() {
        @Override
        public String compute(final String key) throws InterruptedException {
            final int index = key.lastIndexOf('.');
            final String parent = key.substring(0, index);
            if (parent.contains(OPENEJB)) {
                return parent;
            }
            return null;
        }
    };

    /**
     * Simply returns the ResourceBundle for a given baseName
     */
    private static final Computable<String, ResourceBundle> bundleResolver = new Computable<String, ResourceBundle>() {
        @Override
        public ResourceBundle compute(final String baseName) throws InterruptedException {
            try {
                return ResourceBundle.getBundle(baseName + SUFFIX);
            } catch (final MissingResourceException e) {
                return null;
            }
        }
    };

    /**
     * Builds a Logger object and returns it
     */
    private static final Computable<LoggerKey, Logger> loggerResolver = new Computable<LoggerKey, Logger>() {
        @Override
        public Logger compute(final LoggerKey args) throws InterruptedException {
            return new Logger(args.category, logStreamFactory.createLogStream(args.category), args.baseName);
        }
    };

    /**
     * Creates a MessageFormat object for a message and returns it
     */
    private static final Computable<String, MessageFormat> messageFormatResolver = MessageFormat::new;

    /**
     * Cache of parent-child relationships between resource names
     */
    private static final Computable<String, String> heirarchyCache = new Memoizer<>(heirarchyResolver);

    /**
     * Cache of ResourceBundles
     */
    private static final Computable<String, ResourceBundle> bundleCache = new Memoizer<>(bundleResolver);

    /**
     * Cache of Loggers
     */
    private static final Computable<LoggerKey, Logger> loggerCache = new Memoizer<>(loggerResolver);

    /**
     * Cache of MessageFormats
     */
    private static final Computable<String, MessageFormat> messageFormatCache = new Memoizer<>(messageFormatResolver);

    /**
     * Finds a Logger from the cache and returns it. If not found in cache then builds a Logger and returns it.
     *
     * @param category - The category of the logger
     * @param baseName - The baseName for the ResourceBundle
     * @return Logger
     */
    public static Logger getInstance(final LogCategory category, final String baseName) {
        configure();

        try {
            return loggerCache.compute(new LoggerKey(category, baseName));
        } catch (final InterruptedException e) {
            // Don't return null here. Just create a new Logger and set it up.
            // It will not be stored in the cache, but a later lookup for the
            // same Logger would probably end up in the cache
            final LogStream logStream = logStreamFactory.createLogStream(category);
            return new Logger(category, logStream, baseName);
        }
    }

    private final LogCategory category;
    private final LogStream logStream;
    private final String baseName;

    public Logger(final LogCategory category, final LogStream logStream, final String baseName) {
        this.category = category;
        this.baseName = baseName;
        this.logStream = // tomcat is already async so abuse of it
            ("true".equals(SystemInstance.get().getProperty("openejb.log.async", "true")) && JavaSecurityManagers.getSystemProperty("catalina.home") == null) ?
                new LogStreamAsync(logStream) : logStream;
    }

    public static Logger getInstance(final LogCategory category, final Class clazz) {
        return getInstance(category, packageName(clazz));
    }

    private static String packageName(final Class clazz) {
        final String name = clazz.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }

    private static Boolean isLog4j;

    public static boolean isLog4jImplied() {

        if (null == isLog4j) {

            isLog4j = false;

            final List<String> locations = new ArrayList<>();
            {
                final Properties configFile = log4j(loadLoggingProperties());

                final Properties systemProperties = log4j(SystemInstance.get().getProperties());

                if (configFile.size() > 0) {
                    locations.add("conf/logging.properties");
                }
                if (systemProperties.size() > 0) {
                    locations.add("Properties overrides");
                }
            }

            if (locations.size() > 0) {
                if (exists("org.apache.log4j.Logger")) {
                    isLog4j = true;
                }
            }
        }

        return isLog4j;
    }

    private static boolean exists(final String s) {
        return load(s) != null;
    }

    private static Properties log4j(final Properties system) {
        final Properties properties = new Properties();
        for (final Map.Entry<Object, Object> entry : system.entrySet()) {
            final String key = entry.getKey().toString();
            if (key.startsWith("log4j.") && !key.equals("log4j.configuration")) {
                properties.put(key, entry.getValue());
            }
        }
        return properties;
    }

    private static Properties loadLoggingProperties() {
        try {
            final File conf = SystemInstance.get().getConf(null);

            final File file = new File(conf, "logging.properties");

            return IO.readProperties(file);
        } catch (final IOException e) {
            return new Properties();
        }
    }

    public Logger getChildLogger(final String child) {
        return Logger.getInstance(this.category.createChild(child), this.baseName);
    }

    /**
     * Formats a given message
     *
     * @param message String
     * @param args    Object...
     * @return the formatted message
     */
    private String formatMessage(final String message, final Object... args) {
        if (args.length == 0) {
            return message;
        }

        try {
            final MessageFormat mf = messageFormatCache.compute(message);
            return mf.format(args);
        } catch (final Exception e) {
            return "Error in formatting message " + message;
        }

    }

    public boolean isDebugEnabled() {
        return logStream.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return logStream.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return logStream.isFatalEnabled();
    }

    public boolean isInfoEnabled() {
        return logStream.isInfoEnabled();
    }

    public boolean isWarningEnabled() {
        return logStream.isWarnEnabled();
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isLevelEnable(final String level) {
        final String levelLowerCase = level.toLowerCase(Locale.ENGLISH);
        switch (levelLowerCase) {
            case "info":
                return isInfoEnabled();
            case "debug":
                return isDebugEnabled();
            case "warning":
                return isWarningEnabled();
            case "fatal":
                return isFatalEnabled();
            case "error":
                return isErrorEnabled();
        }
        return false;
    }

    public void log(final String level, final String message) {
        final String levelLowerCase = level.toLowerCase(Locale.ENGLISH);
        switch (levelLowerCase) {
            case "info":
                info(message);
                break;
            case "debug":
                debug(message);
                break;
            case "warning":
                warning(message);
                break;
            case "fatal":
                fatal(message);
                break;
            case "error":
                error(message);
                break;
        }
    }

    /**
     * If this level is enabled, then it finds a message for the given key  and logs it
     *
     * @param message - This could be a plain message or a key in Messages.properties
     * @return the formatted i18n message
     */
    public String debug(final String message) {

        if (isDebugEnabled()) {
            final String msg = getMessage(message, baseName);
            logStream.debug(msg);
            return msg;
        }
        return message;
    }

    public String debug(final String message, final Object... args) {

        if (isDebugEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            logStream.debug(msg);
            return msg;
        }
        return message;
    }

    public String debug(final String message, final Throwable t) {

        if (isDebugEnabled()) {
            final String msg = getMessage(message, baseName);
            logStream.debug(msg, t);
            return msg;
        }
        return message;
    }

    public String debug(final String message, final Throwable t, final Object... args) {

        if (isDebugEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            logStream.debug(msg, t);
            return msg;
        }
        return message;
    }

    public String error(final String message) {

        if (isErrorEnabled()) {
            final String msg = getMessage(message, baseName);
            logStream.error(msg);
            return msg;
        }
        return message;
    }

    public String error(final String message, final Object... args) {

        if (isErrorEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            logStream.error(msg);
            return msg;
        }
        return message;
    }

    public String error(final String message, final Throwable t) {

        if (isErrorEnabled()) {
            final String msg = getMessage(message, baseName);
            logStream.error(msg, t);
            return msg;
        }
        return message;
    }

    public String error(final String message, final Throwable t, final Object... args) {

        if (isErrorEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            logStream.error(msg, t);
            return msg;
        }
        return message;
    }

    public String fatal(final String message) {
        if (isFatalEnabled()) {
            final String msg = getMessage(message, baseName);
            logStream.fatal(msg);
            return msg;
        }
        return message;
    }

    public String fatal(final String message, final Object... args) {
        if (isFatalEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            logStream.fatal(msg);
            return msg;
        }
        return message;
    }

    public String fatal(final String message, final Throwable t) {
        if (isFatalEnabled()) {
            final String msg = getMessage(message, baseName);
            logStream.fatal(msg, t);
            return msg;
        }
        return message;
    }

    public String fatal(final String message, final Throwable t, final Object... args) {
        if (isFatalEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            logStream.fatal(msg, t);
            return msg;
        }
        return message;
    }

    public String info(final String message) {
        if (isInfoEnabled()) {
            final String msg = getMessage(message, baseName);
            logStream.info(msg);
            return msg;
        }
        return message;
    }

    public String info(final String message, final Object... args) {
        if (isInfoEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            logStream.info(msg);
            return msg;
        }
        return message;
    }

    public String info(final String message, final Throwable t) {
        if (isInfoEnabled()) {
            final String msg = getMessage(message, baseName);
            logStream.info(msg, t);
            return msg;
        }
        return message;
    }

    public String info(final String message, final Throwable t, final Object... args) {
        if (isInfoEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            logStream.info(msg, t);
            return msg;
        }
        return message;
    }

    public String warning(final String message) {
        if (isWarningEnabled()) {
            final String msg = getMessage(message, baseName);
            logStream.warn(msg);
            return msg;
        }
        return message;
    }

    public String warning(final String message, final Object... args) {
        if (isWarningEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            logStream.warn(msg);
            return msg;
        }
        return message;
    }

    public String warning(final String message, final Throwable t) {
        if (isWarningEnabled()) {
            final String msg = getMessage(message, baseName);
            logStream.warn(msg, t);
            return msg;
        }
        return message;
    }

    public String warning(final String message, final Throwable t, final Object... args) {
        if (isWarningEnabled()) {
            String msg = getMessage(message, baseName);
            msg = formatMessage(msg, args);
            logStream.warn(msg, t);
            return msg;
        }
        return message;
    }

    /**
     * Given a key and a baseName, this method computes a message for a key. if
     * the key is not found in this ResourceBundle for this baseName, then it
     * recursively looks up its parent to find the message for a key. If no
     * message is found for a key, the key is returned as is and is logged by
     * the logger.
     *
     * @param key      String
     * @param baseName String
     * @return String
     */
    private String getMessage(final String key, final String baseName) {
        if (key == null) { // hehe, can happen
            return "null";
        }

        try {

            final ResourceBundle bundle = bundleCache.compute(baseName);
            if (bundle != null) {
                try {
                    return bundle.getString(key);
                } catch (final MissingResourceException e) {
                    final String parentName = heirarchyCache.compute(baseName);
                    if (parentName == null) {
                        return key;
                    } else {
                        return getMessage(key, parentName);
                    }
                }

            } else {
                final String parentName = heirarchyCache.compute(baseName);
                if (parentName == null) {
                    return key;
                } else {
                    return getMessage(key, parentName);
                }

            }
        } catch (final InterruptedException e) {
            // ignore
        }
        return key;
    }

    protected static class LoggerKey implements Serializable {
        protected final LogCategory category;
        protected final String baseName;
        private final int hash;

        protected LoggerKey(final LogCategory category, final String baseName) {
            this.category = category;
            this.baseName = baseName;

            int result = category.hashCode();
            hash = 31 * result + baseName.hashCode();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final LoggerKey loggerKey = LoggerKey.class.cast(o);
            return category.equals(loggerKey.category) && baseName.equals(loggerKey.baseName);

        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
