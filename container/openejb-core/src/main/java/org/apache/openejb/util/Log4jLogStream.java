/**
 *
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
package org.apache.openejb.util;

import org.apache.log4j.Level;

public class Log4jLogStream implements LogStream {
    protected org.apache.log4j.Logger logger;

    public Log4jLogStream(LogCategory logCategory) {
        logger = org.apache.log4j.Logger.getLogger(logCategory.getName());
    }

    public boolean isFatalEnabled() {
        return logger.isEnabledFor(Level.FATAL);
    }

    public void fatal(String message) {
        logger.fatal(message);
    }

    public void fatal(String message, Throwable t) {
        logger.fatal(message, t);
    }

    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Throwable t) {
        logger.error(message, t);
    }

    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void warn(String message, Throwable t) {
        logger.warn(message, t);
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void info(String message) {
        logger.info(message);
    }

    public void info(String message, Throwable t) {
        logger.info(message, t);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void debug(String message, Throwable t) {
        logger.debug(message, t);
    }
}
