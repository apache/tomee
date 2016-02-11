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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Log4jLogStream implements LogStream {
    protected Logger logger;

    public Log4jLogStream(final LogCategory logCategory) {
        logger = Logger.getLogger(logCategory.getName());
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isEnabledFor(Level.FATAL);
    }

    @Override
    public void fatal(final String message) {
        logger.fatal(message);
    }

    @Override
    public void fatal(final String message, final Throwable t) {
        logger.fatal(message, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    @Override
    public void error(final String message) {
        logger.error(message);
    }

    @Override
    public void error(final String message, final Throwable t) {
        logger.error(message, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

    @Override
    public void warn(final String message) {
        logger.warn(message);
    }

    @Override
    public void warn(final String message, final Throwable t) {
        logger.warn(message, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(final String message) {
        logger.info(message);
    }

    @Override
    public void info(final String message, final Throwable t) {
        logger.info(message, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(final String message) {
        logger.debug(message);
    }

    @Override
    public void debug(final String message, final Throwable t) {
        logger.debug(message, t);
    }
}
