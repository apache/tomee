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
package org.apache.openejb.openjpa;

import org.apache.openejb.util.JuliLogStream;
import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.openjpa.lib.log.Log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JULOpenJPALog  implements Log {
    private final Logger logger;

    public JULOpenJPALog(final String channel) {
        logger = Logger.getLogger(channel);
        if (logger.getHandlers().length == 0) {
            logger.addHandler(new JuliLogStreamFactory.OpenEJBSimpleLayoutHandler());
            logger.setUseParentHandlers(false);
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINEST);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public void trace(Object o) {
        logger.log(record(o, Level.FINEST));
    }

    @Override
    public void trace(Object o, Throwable t) {
        logger.log(record(o, t, Level.FINEST));
    }

    @Override
    public void info(Object o) {
        logger.log(record(o, Level.INFO));
    }

    @Override
    public void info(Object o, Throwable t) {
        logger.log(record(o, t, Level.INFO));
    }

    @Override
    public void warn(Object o) {
        logger.log(record(o, Level.WARNING));
    }

    @Override
    public void warn(Object o, Throwable t) {
        logger.log(record(o, t, Level.WARNING));
    }

    @Override
    public void error(Object o) {
        logger.log(record(o.toString(), Level.SEVERE));
    }

    @Override
    public void error(Object o, Throwable t) {
        logger.log(record(o, t, Level.SEVERE));
    }

    @Override
    public void fatal(Object o) {
        logger.log(record(o, Level.SEVERE));
    }

    @Override
    public void fatal(Object o, Throwable t) {
        logger.log(record(o, t, Level.SEVERE));
    }

    private LogRecord record(final Object o, final Throwable t, final Level level) {
        final LogRecord record = record(o, level);
        record.setThrown(t);
        return record;
    }

    private LogRecord record(final Object o,  final Level level) {
        final LogRecord record = new JuliLogStream.OpenEJBLogRecord(level, o.toString());
        record.setSourceMethodName(logger.getName());
        return record;
    }
}
