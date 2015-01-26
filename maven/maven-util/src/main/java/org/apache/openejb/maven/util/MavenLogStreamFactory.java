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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.maven.util;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.log.logger.AbstractDelegatingLogger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.LogStream;
import org.apache.openejb.util.LogStreamFactory;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.logger.WebBeansLoggerFactory;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MavenLogStreamFactory implements LogStreamFactory {
    private static Log logger;
    static {
        System.setProperty(WebBeansLoggerFacade.OPENWEBBEANS_LOGGING_FACTORY_PROP, OWBMavenLogFactory.class.getName());
        try {
            if (System.getProperty("openjpa.Log") == null) {
                MavenLogStreamFactory.class.getClassLoader().loadClass("org.apache.openjpa.lib.log.LogFactoryAdapter");
                System.setProperty("openjpa.Log", "org.apache.openejb.maven.util.OpenJPALogFactory");
            }
        } catch (final Exception ignored) {
            // no-op: openjpa is not at the classpath
        }

    }

    @Override
    public LogStream createLogStream(final LogCategory logCategory) {
        return new MavenLogStream(logger);
    }

    public static void setLogger(final Log logger) {
        MavenLogStreamFactory.logger = logger;
    }

    public static Log currentLogger() {
        return logger;
    }

    public static class MavenLogger extends AbstractDelegatingLogger {
        public MavenLogger(final String name, final String resourceBundleName) {
            super(name, resourceBundleName);
        }

        @Override
        public Level getLevel() {
            if (logger.isDebugEnabled()) {
                return Level.FINER;
            } else if (logger.isInfoEnabled()) {
                return Level.INFO;
            } else if (logger.isWarnEnabled()) {
                return Level.WARNING;
            } else if (logger.isErrorEnabled()) {
                return Level.SEVERE;
            }
            return Level.OFF;
        }

        @Override
        public boolean isLoggable(final Level level) {
            final int i = level.intValue();
            if (i == Level.OFF.intValue()) {
                return false;
            } else if (i >= Level.SEVERE.intValue()) {
                return logger.isErrorEnabled();
            } else if (i >= Level.WARNING.intValue()) {
                return logger.isWarnEnabled();
            } else if (i >= Level.INFO.intValue()) {
                return logger.isInfoEnabled();
            }
            return logger.isDebugEnabled();
        }

        @Override
        protected void internalLogFormatted(final String msg, final LogRecord record) {
            final Level level = record.getLevel();
            final Throwable t = record.getThrown();
            if (Level.FINE.equals(level) || Level.FINER.equals(level) || Level.CONFIG.equals(level)) {
                if (t == null) {
                    logger.debug(msg);
                } else {
                    logger.debug(msg, t);
                }
            } else if (Level.INFO.equals(level)) {
                if (t == null) {
                    logger.info(msg);
                } else {
                    logger.info(msg, t);
                }
            } else if (Level.WARNING.equals(level)) {
                if (t == null) {
                    logger.warn(msg);
                } else {
                    logger.warn(msg, t);
                }
            } else if (Level.ALL.equals(level) || Level.SEVERE.equals(level)) {
                if (t == null) {
                    logger.error(msg);
                } else {
                    logger.error(msg, t);
                }
            }
        }
    }

    public static class OWBMavenLogFactory implements WebBeansLoggerFactory {
        @Override
        public Logger getLogger(final Class<?> clazz, final Locale desiredLocale) {
            return new MavenLogger(clazz.getName(), "openwebbeans/Messages");
        }

        @Override
        public Logger getLogger(final Class<?> clazz) {
            return new MavenLogger(clazz.getName(), "openwebbeans/Messages");
        }
    }

    private static class MavenLogStream implements LogStream {
        private final Log log;

        public MavenLogStream(final Log logger) {
            log = logger;
        }

        @Override
        public boolean isFatalEnabled() {
            return log.isErrorEnabled();
        }

        @Override
        public void fatal(final String message) {
            log.error(message);
        }

        @Override
        public void fatal(final String message, final Throwable t) {
            log.error(message, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return log.isErrorEnabled();
        }

        @Override
        public void error(final String message) {
            log.error(message);
        }

        @Override
        public void error(final String message, final Throwable t) {
            log.error(message, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return log.isWarnEnabled();
        }

        @Override
        public void warn(final String message) {
            log.warn(message);
        }

        @Override
        public void warn(final String message, final Throwable t) {
            log.warn(message, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return log.isInfoEnabled();
        }

        @Override
        public void info(final String message) {
            log.info(message);
        }

        @Override
        public void info(final String message, final Throwable t) {
            log.info(message, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return log.isDebugEnabled();
        }

        @Override
        public void debug(final String message) {
            log.debug(message);
        }

        @Override
        public void debug(final String message, final Throwable t) {
            log.debug(message, t);
        }
    }
}
