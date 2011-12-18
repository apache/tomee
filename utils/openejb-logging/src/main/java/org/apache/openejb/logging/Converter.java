/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.apache.openejb.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

import java.util.logging.LogRecord;

public final class Converter {
    private Converter() {
        // no-op
    }

    public static LoggingEvent toLoggingEvent(LogRecord record) {
        return new LoggingEvent(
            record.getSourceClassName(),
            Logger.getLogger(record.getLoggerName()),
            record.getMillis(),
            getPriority(record.getLevel()),
            record.getMessage(),
            record.getThrown());
    }

    public static LogRecord toLogRecord(LoggingEvent event) {
        LogRecord record = new LogRecord(getJULLevel(event.getLevel()), event.getMessage().toString());
        record.setMillis(event.getTimeStamp());
        record.setLoggerName(event.getLoggerName());
        return record;
    }

    private static java.util.logging.Level getJULLevel(Level level) {
        switch (level.toInt()) {
            case Priority.OFF_INT:
                return java.util.logging.Level.OFF;
            case Priority.FATAL_INT:
            case Priority.ERROR_INT:
                return java.util.logging.Level.SEVERE;
            case Priority.WARN_INT:
                return java.util.logging.Level.WARNING;
            case Priority.DEBUG_INT:
                return java.util.logging.Level.FINE;
            case Priority.INFO_INT:
            default:
                return java.util.logging.Level.INFO;
        }
    }

    private static Priority getPriority(java.util.logging.Level level) {
        switch (level.intValue()) {
            case Integer.MAX_VALUE: // OFF
                return Level.OFF;
            case 1000: // SEVERE:
                return Level.ERROR;
            case 900: // WARNING
                return Level.WARN;
            case 700: // CONFIG
                return Level.INFO;
            case 500: // FINE
            case 400: // FINER
            case 300: // FINEST
                return Level.DEBUG;
            case 800: // INFO
            default:
                return Level.INFO;
        }
    }
}
