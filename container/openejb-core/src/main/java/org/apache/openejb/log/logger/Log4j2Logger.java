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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openejb.util.reflection.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Log4j2Logger extends AbstractDelegatingLogger {
    private static final Map<Level, org.apache.logging.log4j.Level> TO_LOG4J = new HashMap<>();

    private final Logger log;

    static {
        //older versions of log4j don't have TRACE, use debug
        org.apache.logging.log4j.Level t = org.apache.logging.log4j.Level.DEBUG;

        TO_LOG4J.put(Level.ALL, org.apache.logging.log4j.Level.ALL);
        TO_LOG4J.put(Level.SEVERE, org.apache.logging.log4j.Level.ERROR);
        TO_LOG4J.put(Level.WARNING, org.apache.logging.log4j.Level.WARN);
        TO_LOG4J.put(Level.INFO, org.apache.logging.log4j.Level.INFO);
        TO_LOG4J.put(Level.CONFIG, org.apache.logging.log4j.Level.DEBUG);
        TO_LOG4J.put(Level.FINE, org.apache.logging.log4j.Level.DEBUG);
        TO_LOG4J.put(Level.FINER, org.apache.logging.log4j.Level.TRACE);
        TO_LOG4J.put(Level.FINEST, org.apache.logging.log4j.Level.TRACE);
        TO_LOG4J.put(Level.OFF, org.apache.logging.log4j.Level.OFF);
    }

    public Log4j2Logger(final String name, final String resourceBundleName) {
        super(name, resourceBundleName);
        log = LogManager.getLogger(name);
    }

    @Override
    public void setLevel(final Level newLevel) throws SecurityException {
        try {
            Reflections.invokeByReflection(
                    log, "setLevel",
                    new Class<?>[]{org.apache.logging.log4j.Level.class},
                    new Object[]{TO_LOG4J.get(newLevel)});
        } catch (final Throwable ignore) {
            // no-op
        }
    }

    public Level getLevel() {
        final org.apache.logging.log4j.Level l = log.getLevel();
        if (l != null) {
            return fromL4J(l);
        }
        return null;
    }

    protected void internalLogFormatted(final String msg, final LogRecord record) {
        log.log(TO_LOG4J.get(record.getLevel()), msg, record.getThrown());
    }


    private Level fromL4J(final org.apache.logging.log4j.Level l) {
        Level l2 = null;
        switch (l.getStandardLevel()) {
            case ALL:
                l2 = Level.ALL;
                break;
            case FATAL:
                l2 = Level.SEVERE;
                break;
            case ERROR:
                l2 = Level.SEVERE;
                break;
            case WARN:
                l2 = Level.WARNING;
                break;
            case INFO:
                l2 = Level.INFO;
                break;
            case DEBUG:
                l2 = Level.FINE;
                break;
            case OFF:
                l2 = Level.OFF;
                break;
            case TRACE:
                l2 = Level.FINEST;
                break;
            default:
                l2 = Level.FINE;
        }
        return l2;
    }
}
