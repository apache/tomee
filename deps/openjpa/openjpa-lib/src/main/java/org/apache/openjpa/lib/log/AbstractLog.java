/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A simple implementation of the {@link Log} interface. Writes
 * output to stdout.
 *
 * @author Patrick Linskey
 */
public abstract class AbstractLog implements Log {

    /**
     * Check to see if the specified logging level is enabled.
     */
    protected abstract boolean isEnabled(short level);

    /**
     * Send the specified log message to the handler.
     *
     * @param level the level of the log message
     * @param message the message to send
     * @param t the Throwable, or null if this is not an error
     */
    protected abstract void log(short level, String message, Throwable t);

    public boolean isTraceEnabled() {
        return isEnabled(TRACE);
    }

    public boolean isInfoEnabled() {
        return isEnabled(INFO);
    }

    public boolean isWarnEnabled() {
        return isEnabled(WARN);
    }

    public boolean isErrorEnabled() {
        return isEnabled(ERROR);
    }

    public boolean isFatalEnabled() {
        return isEnabled(FATAL);
    }

    public void trace(Object message) {
        trace(message, throwableParam(message, null));
    }

    public void trace(Object message, Throwable t) {
        if (isTraceEnabled())
            log(TRACE, toString(message), throwableParam(message, t));
    }

    public void info(Object message) {
        info(message, throwableParam(message, null));
    }

    public void info(Object message, Throwable t) {
        if (isInfoEnabled())
            log(INFO, toString(message), throwableParam(message, t));
    }

    public void warn(Object message) {
        warn(message, throwableParam(message, null));
    }

    public void warn(Object message, Throwable t) {
        if (isWarnEnabled())
            log(WARN, toString(message), throwableParam(message, t));
    }

    public void error(Object message) {
        error(message, throwableParam(message, null));
    }

    public void error(Object message, Throwable t) {
        if (isErrorEnabled())
            log(ERROR, toString(message), throwableParam(message, t));
    }

    public void fatal(Object message) {
        fatal(message, throwableParam(message, null));
    }

    public void fatal(Object message, Throwable t) {
        if (isFatalEnabled())
            log(FATAL, toString(message), throwableParam(message, t));
    }

    /**
     * Utility method to obtain a stack trace as a String.
     */
    protected static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

    /**
     * If either given instance is a throwable, return it.
     */
    private static Throwable throwableParam(Object message, Throwable t) {
        if (t != null)
            return t;
        if (message instanceof Throwable)
            return (Throwable) message;

        return null;
    }

    /**
     * Efficiently turn the given object into a string.
     */
    private static String toString(Object o) {
        return (o == null) ? "null" : o.toString();
    }
}
