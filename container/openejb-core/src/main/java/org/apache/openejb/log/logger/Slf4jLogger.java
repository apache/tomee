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

package org.apache.openejb.log.logger;

import org.slf4j.spi.LocationAwareLogger;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * <p>
 * java.util.logging.Logger implementation delegating to SLF4J.
 * </p>
 * <p>
 * Methods {@link java.util.logging.Logger#doSetParent(Logger)}, {@link java.util.logging.Logger#getParent()},
 * {@link java.util.logging.Logger#setUseParentHandlers(boolean)} and
 * {@link java.util.logging.Logger#getUseParentHandlers()} are not overrriden.
 * </p>
 * <p>
 * Level mapping inspired by org.slf4j.bridge.SLF4JBridgeHandler:
 * </p>
 * <p/>
 * <pre>
 * FINEST  -&gt; TRACE
 * FINER   -&gt; DEBUG
 * FINE    -&gt; DEBUG
 * CONFIG  -&gt; DEBUG
 * INFO    -&gt; INFO
 * WARN ING -&gt; WARN
 * SEVER   -&gt; ERROR
 * </pre>
 */
public class Slf4jLogger extends AbstractDelegatingLogger {

    private static final String FQCN = AbstractDelegatingLogger.class.getName();

    private final org.slf4j.Logger logger;
    private LocationAwareLogger locationAwareLogger;


    public Slf4jLogger(final String name, final String resourceBundleName) {
        super(name, resourceBundleName);
        logger = org.slf4j.LoggerFactory.getLogger(name);
        if (logger instanceof LocationAwareLogger) {
            locationAwareLogger = (LocationAwareLogger) logger;
        }
    }

    @Override
    protected boolean supportsHandlers() {
        return true;
    }

    @Override
    public Level getLevel() {
        final Level level;
        // Verify from the wider (trace) to the narrower (error)
        if (logger.isTraceEnabled()) {
            level = Level.FINEST;
        } else if (logger.isDebugEnabled()) {
            // map to the lowest between FINER, FINE and CONFIG
            level = Level.FINER;
        } else if (logger.isInfoEnabled()) {
            level = Level.INFO;
        } else if (logger.isWarnEnabled()) {
            level = Level.WARNING;
        } else if (logger.isErrorEnabled()) {
            level = Level.SEVERE;
        } else {
            level = Level.OFF;
        }
        return level;
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
        } else if (i >= Level.FINER.intValue()) {
            return logger.isDebugEnabled();
        }
        return logger.isTraceEnabled();
    }


    @Override
    protected void internalLogFormatted(final String msg, final LogRecord record) {

        final Level level = record.getLevel();
        final Throwable t = record.getThrown();

        final Handler[] targets = getHandlers();
        if (targets != null) {
            for (final Handler h : targets) {
                h.publish(record);
            }
        }
        if (!getUseParentHandlers()) {
            return;
        }

        /*
        * As we can not use a "switch ... case" block but only a "if ... else if ..." block, the order of the
        * comparisons is important. We first try log level FINE then INFO, WARN, FINER, etc
        */
        if (Level.FINE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, t);
            }
        } else if (Level.INFO.equals(level)) {
            if (locationAwareLogger == null) {
                logger.info(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.INFO_INT, msg, null, t);
            }
        } else if (Level.WARNING.equals(level)) {
            if (locationAwareLogger == null) {
                logger.warn(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, t);
            }
        } else if (Level.FINER.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, t);
            }
        } else if (Level.FINEST.equals(level)) {
            if (locationAwareLogger == null) {
                logger.trace(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, null, t);
            }
        } else if (Level.ALL.equals(level)) {
            // should never occur, all is used to configure java.util.logging
            // but not accessible by the API Logger.xxx() API
            if (locationAwareLogger == null) {
                logger.error(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, t);
            }
        } else if (Level.SEVERE.equals(level)) {
            if (locationAwareLogger == null) {
                logger.error(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, t);
            }
        } else if (Level.CONFIG.equals(level)) {
            if (locationAwareLogger == null) {
                logger.debug(msg, t);
            } else {
                locationAwareLogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, t);
            }
        }
        // don't log if Level.OFF
    }
}
