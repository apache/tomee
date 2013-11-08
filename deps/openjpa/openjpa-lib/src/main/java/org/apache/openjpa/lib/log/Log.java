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

/**
 * Logging interface that is independent of other logging frameworks.
 *
 * @author Patrick Linskey
 * @author Abe White
 */
public interface Log {

    public static final short TRACE = 1;
    // the old DEBUG constant was 2. Leaving a hole for compatibility.
    public static final short INFO = 3;
    public static final short WARN = 4;
    public static final short ERROR = 5;
    public static final short FATAL = 6;

    /**
     * Returns if the {@link #TRACE} log level is enabled.
     */
    public boolean isTraceEnabled();

    /**
     * Returns if the {@link #INFO} log level is enabled.
     */
    public boolean isInfoEnabled();

    /**
     * Returns if the {@link #WARN} log level is enabled.
     */
    public boolean isWarnEnabled();

    /**
     * Returns if the {@link #ERROR} log level is enabled.
     */
    public boolean isErrorEnabled();

    /**
     * Returns if the {@link #FATAL} log level is enabled.
     */
    public boolean isFatalEnabled();

    /**
     * Write out a log message at the {@link #TRACE}
     * level with the stringification of <code>o</code> as the body
     * of the message.
     */
    public void trace(Object o);

    /**
     * Write out a log message at the {@link #TRACE}
     * level with the stringification of <code>o</code> as the body
     * of the message, also outputting <code>t</code> to the log.
     */
    public void trace(Object o, Throwable t);

    /**
     * Write out a log message at the {@link #INFO}
     * level with the stringification of <code>o</code> as the body
     * of the message, also outputting <code>t</code> to the log.
     */
    public void info(Object o);

    /**
     * Write out a log message at the {@link #INFO}
     * level with the stringification of <code>o</code> as the body
     * of the message, also outputting <code>t</code> to the log.
     */
    public void info(Object o, Throwable t);

    /**
     * Write out a log message at the {@link #WARN}
     * level with the stringification of <code>o</code> as the body
     * of the message, also outputting <code>t</code> to the log.
     */
    public void warn(Object o);

    /**
     * Write out a log message at the {@link #WARN}
     * level with the stringification of <code>o</code> as the body
     * of the message, also outputting <code>t</code> to the log.
     */
    public void warn(Object o, Throwable t);

    /**
     * Write out a log message at the {@link #ERROR}
     * level with the stringification of <code>o</code> as the body
     * of the message, also outputting <code>t</code> to the log.
     */
    public void error(Object o);

    /**
     * Write out a log message at the {@link #ERROR}
     * level with the stringification of <code>o</code> as the body
     * of the message, also outputting <code>t</code> to the log.
     */
    public void error(Object o, Throwable t);

    /**
     * Write out a log message at the {@link #FATAL}
     * level with the stringification of <code>o</code> as the body
     * of the message, also outputting <code>t</code> to the log.
     */
    public void fatal(Object o);

    /**
     * Write out a log message at the {@link #FATAL}
     * level with the stringification of <code>o</code> as the body
     * of the message, also outputting <code>t</code> to the log.
     */
    public void fatal(Object o, Throwable t);
}
