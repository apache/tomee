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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.FileUtils;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xbean.finder.ResourceFinder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Enumeration;

public class Logger {

    protected static final HashMap _loggers = new HashMap();
    protected Category _logger = null;
    public I18N i18n = null;

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

    static public Logger getInstance(String category, String resourceName) {
        HashMap bundles = (HashMap) _loggers.get(category);
        Logger logger = null;

        if (bundles == null) {
            synchronized (Logger.class) {
                bundles = (HashMap) _loggers.get(category);
                if (bundles == null) {
                    bundles = new HashMap();
                    _loggers.put(category, bundles);
                }
            }
        }

        logger = (Logger) bundles.get(resourceName);
        if (logger == null) {
            synchronized (Logger.class) {
                logger = (Logger) bundles.get(resourceName);
                if (logger == null) {
                    logger = new Logger(resourceName);
                    logger._logger = Category.getInstance(category);

                    bundles.put(resourceName, logger);
                }
            }
        }

        return logger;
    }

    protected Logger(String resourceName) {
        i18n = new I18N(resourceName);
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

    public void debug(String message) {
        if (isDebugEnabled()) _logger.debug(message);
    }

    public void debug(String message, Throwable t) {
        if (isDebugEnabled()) _logger.debug(message, t);
    }

    public void error(String message) {
        if (isErrorEnabled()) _logger.error(message);
    }

    public void error(String message, Throwable t) {
        if (isErrorEnabled()) _logger.error(message, t);
    }

    public void fatal(String message) {
        if (isFatalEnabled()) _logger.fatal(message);
    }

    public void fatal(String message, Throwable t) {
        if (isFatalEnabled()) _logger.fatal(message, t);
    }

    public void info(String message) {
        if (isInfoEnabled()) _logger.info(message);
    }

    public void info(String message, Throwable t) {
        if (isInfoEnabled()) _logger.info(message, t);
    }

    public void warning(String message) {
        if (isWarningEnabled()) _logger.warn(message);
    }

    public void warning(String message, Throwable t) {
        if (isWarningEnabled()) _logger.warn(message, t);
    }

    public class I18N {

        protected Messages _messages = null;

        protected I18N(String resourceName) {
            _messages = new Messages(resourceName);
        }

        public void info(String code) {
            if (isInfoEnabled()) _logger.info(_messages.message(code));
        }

        public void info(String code, Throwable t) {
            if (isInfoEnabled()) _logger.info(_messages.message(code), t);
        }

        public void info(String code, Object arg0) {
            if (isInfoEnabled()) {
                Object[] args = {arg0};
                info(code, args);
            }
        }

        public void info(String code, Throwable t, Object arg0) {
            if (isInfoEnabled()) {
                Object[] args = {arg0};
                info(code, t, args);
            }
        }

        public void info(String code, Object arg0, Object arg1) {
            if (isInfoEnabled()) {
                Object[] args = {arg0, arg1};
                info(code, args);
            }
        }

        public void info(String code, Throwable t, Object arg0, Object arg1) {
            if (isInfoEnabled()) {
                Object[] args = {arg0, arg1};
                info(code, t, args);
            }
        }

        public void info(String code, Object arg0, Object arg1, Object arg2) {
            if (isInfoEnabled()) {
                Object[] args = {arg0, arg1, arg2};
                info(code, args);
            }
        }

        public void info(String code, Throwable t, Object arg0, Object arg1, Object arg2) {
            if (isInfoEnabled()) {
                Object[] args = {arg0, arg1, arg2};
                info(code, t, args);
            }
        }

        public void info(String code, Object arg0, Object arg1, Object arg2, Object arg3) {
            if (isInfoEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3};
                info(code, args);
            }
        }

        public void info(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3) {
            if (isInfoEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3};
                info(code, t, args);
            }
        }

        public void info(String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
            if (isInfoEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4};
                info(code, args);
            }
        }

        public void info(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
            if (isInfoEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4};
                info(code, t, args);
            }
        }

        public void info(String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
            if (isInfoEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
                info(code, args);
            }
        }

        public void info(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
            if (isInfoEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
                info(code, t, args);
            }
        }

        public void info(String code, Object[] args) {
            _logger.info(_messages.format(code, args));
        }

        public void info(String code, Throwable t, Object[] args) {
            _logger.info(_messages.format(code, args), t);
        }

        public void warning(String code) {
            if (isWarningEnabled()) _logger.warn(_messages.message(code));
        }

        public void warning(String code, Throwable t) {
            if (isWarningEnabled()) _logger.warn(_messages.message(code), t);
        }

        public void warning(String code, Object arg0) {
            if (isWarningEnabled()) {
                Object[] args = {arg0};
                warning(code, args);
            }
        }

        public void warning(String code, Throwable t, Object arg0) {
            if (isWarningEnabled()) {
                Object[] args = {arg0};
                warning(code, t, args);
            }
        }

        public void warning(String code, Object arg0, Object arg1) {
            if (isWarningEnabled()) {
                Object[] args = {arg0, arg1};
                warning(code, args);
            }
        }

        public void warning(String code, Throwable t, Object arg0, Object arg1) {
            if (isWarningEnabled()) {
                Object[] args = {arg0, arg1};
                warning(code, t, args);
            }
        }

        public void warning(String code, Object arg0, Object arg1, Object arg2) {
            if (isWarningEnabled()) {
                Object[] args = {arg0, arg1, arg2};
                warning(code, args);
            }
        }

        public void warning(String code, Throwable t, Object arg0, Object arg1, Object arg2) {
            if (isWarningEnabled()) {
                Object[] args = {arg0, arg1, arg2};
                warning(code, t, args);
            }
        }

        public void warning(String code, Object arg0, Object arg1, Object arg2, Object arg3) {
            if (isWarningEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3};
                warning(code, args);
            }
        }

        public void warning(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3) {
            if (isWarningEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3};
                warning(code, t, args);
            }
        }

        public void warning(String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
            if (isWarningEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4};
                warning(code, args);
            }
        }

        public void warning(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
            if (isWarningEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4};
                warning(code, t, args);
            }
        }

        public void warning(String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
            if (isWarningEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
                warning(code, args);
            }
        }

        public void warning(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
            if (isWarningEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
                warning(code, t, args);
            }
        }

        public void warning(String code, Object[] args) {
            _logger.warn(_messages.format(code, args));
        }

        public void warning(String code, Throwable t, Object[] args) {
            _logger.warn(_messages.format(code, args), t);
        }

        public void error(String code) {
            if (isErrorEnabled()) _logger.error(_messages.message(code));
        }

        public void error(String code, Throwable t) {
            if (isErrorEnabled()) _logger.error(_messages.message(code), t);
        }

        public void error(String code, Object arg0) {
            if (isErrorEnabled()) {
                Object[] args = {arg0};
                error(code, args);
            }
        }

        public void error(String code, Throwable t, Object arg0) {
            if (isErrorEnabled()) {
                Object[] args = {arg0};
                error(code, t, args);
            }
        }

        public void error(String code, Object arg0, Object arg1) {
            if (isErrorEnabled()) {
                Object[] args = {arg0, arg1};
                error(code, args);
            }
        }

        public void error(String code, Throwable t, Object arg0, Object arg1) {
            if (isErrorEnabled()) {
                Object[] args = {arg0, arg1};
                error(code, t, args);
            }
        }

        public void error(String code, Object arg0, Object arg1, Object arg2) {
            if (isErrorEnabled()) {
                Object[] args = {arg0, arg1, arg2};
                error(code, args);
            }
        }

        public void error(String code, Throwable t, Object arg0, Object arg1, Object arg2) {
            if (isErrorEnabled()) {
                Object[] args = {arg0, arg1, arg2};
                error(code, t, args);
            }
        }

        public void error(String code, Object arg0, Object arg1, Object arg2, Object arg3) {
            if (isErrorEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3};
                error(code, args);
            }
        }

        public void error(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3) {
            if (isErrorEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3};
                error(code, t, args);
            }
        }

        public void error(String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
            if (isErrorEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4};
                error(code, args);
            }
        }

        public void error(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
            if (isErrorEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4};
                error(code, t, args);
            }
        }

        public void error(String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
            if (isErrorEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
                error(code, args);
            }
        }

        public void error(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
            if (isErrorEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
                error(code, t, args);
            }
        }

        public void error(String code, Object[] args) {
            _logger.error(_messages.format(code, args));
        }

        public void error(String code, Throwable t, Object[] args) {
            _logger.error(_messages.format(code, args), t);
        }

        public void fatal(String code) {
            _logger.fatal(_messages.message(code));
        }

        public void fatal(String code, Throwable t) {
            _logger.fatal(_messages.message(code), t);
        }

        public void fatal(String code, Object arg0) {
            Object[] args = {arg0};
            fatal(code, args);
        }

        public void fatal(String code, Throwable t, Object arg0) {
            Object[] args = {arg0};
            fatal(code, t, args);
        }

        public void fatal(String code, Object arg0, Object arg1) {
            Object[] args = {arg0, arg1};
            fatal(code, args);
        }

        public void fatal(String code, Throwable t, Object arg0, Object arg1) {
            Object[] args = {arg0, arg1};
            fatal(code, t, args);
        }

        public void fatal(String code, Object arg0, Object arg1, Object arg2) {
            Object[] args = {arg0, arg1, arg2};
            fatal(code, args);
        }

        public void fatal(String code, Throwable t, Object arg0, Object arg1, Object arg2) {
            Object[] args = {arg0, arg1, arg2};
            fatal(code, t, args);
        }

        public void fatal(String code, Object arg0, Object arg1, Object arg2, Object arg3) {
            Object[] args = {arg0, arg1, arg2, arg3};
            fatal(code, args);
        }

        public void fatal(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3) {
            Object[] args = {arg0, arg1, arg2, arg3};
            fatal(code, t, args);
        }

        public void fatal(String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
            Object[] args = {arg0, arg1, arg2, arg3, arg4};
            fatal(code, args);
        }

        public void fatal(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
            Object[] args = {arg0, arg1, arg2, arg3, arg4};
            fatal(code, t, args);
        }

        public void fatal(String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
            Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
            fatal(code, args);
        }

        public void fatal(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
            Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
            fatal(code, t, args);
        }

        public void fatal(String code, Object[] args) {
            _logger.fatal(_messages.format(code, args));
        }

        public void fatal(String code, Throwable t, Object[] args) {
            _logger.fatal(_messages.format(code, args), t);
        }

        public void debug(String code) {
            if (isDebugEnabled()) _logger.debug(_messages.message(code));
        }

        public void debug(String code, Throwable t) {
            if (isDebugEnabled()) _logger.debug(_messages.message(code), t);
        }

        public void debug(String code, Object arg0) {
            if (isDebugEnabled()) {
                Object[] args = {arg0};
                debug(code, args);
            }
        }

        public void debug(String code, Throwable t, Object arg0) {
            if (isDebugEnabled()) {
                Object[] args = {arg0};
                debug(code, t, args);
            }
        }

        public void debug(String code, Object arg0, Object arg1) {
            if (isDebugEnabled()) {
                Object[] args = {arg0, arg1};
                debug(code, args);
            }
        }

        public void debug(String code, Throwable t, Object arg0, Object arg1) {
            if (isDebugEnabled()) {
                Object[] args = {arg0, arg1};
                debug(code, t, args);
            }
        }

        public void debug(String code, Object arg0, Object arg1, Object arg2) {
            if (isDebugEnabled()) {
                Object[] args = {arg0, arg1, arg2};
                debug(code, args);
            }
        }

        public void debug(String code, Throwable t, Object arg0, Object arg1, Object arg2) {
            if (isDebugEnabled()) {
                Object[] args = {arg0, arg1, arg2};
                debug(code, t, args);
            }
        }

        public void debug(String code, Object arg0, Object arg1, Object arg2, Object arg3) {
            if (isDebugEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3};
                debug(code, args);
            }
        }

        public void debug(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3) {
            if (isDebugEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3};
                debug(code, t, args);
            }
        }

        public void debug(String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
            if (isDebugEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4};
                debug(code, args);
            }
        }

        public void debug(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
            if (isDebugEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4};
                debug(code, t, args);
            }
        }

        public void debug(String code, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
            if (isDebugEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
                debug(code, args);
            }
        }

        public void debug(String code, Throwable t, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
            if (isDebugEnabled()) {
                Object[] args = {arg0, arg1, arg2, arg3, arg4, arg5};
                debug(code, t, args);
            }
        }

        public void debug(String code, Object[] args) {
            _logger.debug(_messages.format(code, args));
        }

        public void debug(String code, Throwable t, Object[] args) {
            _logger.debug(_messages.format(code, args), t);
        }
    }

    static class Log4jConfigUtils {

        Properties props;

        public Log4jConfigUtils(Properties props) {
            this.props = props;
        }

        public void configure() {
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
                            logger.error("Unable to read logging config file " + configFile.getAbsolutePath(), e);
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
                    properties.load(new ByteArrayInputStream(configData.getBytes()));
                } catch (IOException e) {
                    org.apache.log4j.Logger logger = doFallbackConfiguration();
                    logger.error("Unable to read default logging config file.", e);
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
                        logger.warn("Unable write default logging config file to " + configFile.getAbsolutePath(), e);
                    } finally {
                        try {
                            out.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }


            List missing = new ArrayList();

            for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
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
                org.apache.log4j.Logger logger = doFallbackConfiguration();

                logger.warn("Unable use logging config as there are "+missing.size()+" file references containing directories which have not been created.  See the list below.");
                for (int i = 0; i < missing.size(); i++) {
                    File file = (File) missing.get(i);
                    logger.warn("["+i+"] "+file.getAbsolutePath());
                }
            } else {
                PropertyConfigurator.configure(properties);
            }

        }

        private org.apache.log4j.Logger doFallbackConfiguration() {
            set("CastorCMP", Level.ERROR);
            set("org.exolab.castor", Level.ERROR);
            java.util.logging.Logger.getLogger("org.exolab.castor.jdo.engine.DatabaseImpl").setLevel(java.util.logging.Level.SEVERE);
            set("org.exolab.castor.jdo.engine.DatabaseImpl", Level.ERROR);
            //org/exolab/castor.jdo.engine.DatabaseImpl
            set("org.castor", Level.ERROR);
            set("org.apache.openejb", Level.WARN);
            set("Transaction", Level.WARN);
            set("OpenEJB.startup", Level.INFO);
            set("OpenEJB", Level.WARN);
            return org.apache.log4j.Logger.getLogger("OpenEJB");
        }

        private void set(String category, Level level) {
            org.apache.log4j.Logger.getLogger(category).setLevel(level);
            Enumeration allAppenders = org.apache.log4j.Logger.getLogger(category).getAllAppenders();
            while (allAppenders.hasMoreElements()) {
                Object object = allAppenders.nextElement();
                System.out.println(category +" = " + object);
            }
        }


    }
}