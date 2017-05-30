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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// don't do the same "error" (historical actually) that in log4j one putting too much logic here
public class Log4j2LogStreamFactory implements LogStreamFactory {
    public Log4j2LogStreamFactory() {
        // WARN: don't set all subproject loggers to log4j since API is not the same
        // and it needs log4j1 fallback module to "work" (some parts are broken like config)
        JavaSecurityManagers.setSystemProperty("openwebbeans.logging.factory", "org.apache.openejb.cdi.logging.Log4j2LoggerFactory");
    }

    @Override
    public LogStream createLogStream(final LogCategory logCategory) {
        return new Log4j2Stream(logCategory.getName());
    }

    private class Log4j2Stream implements LogStream {
        private final Logger delegate;

        public Log4j2Stream(final String name) {
            this.delegate = LogManager.getLogger(name);
        }

        public boolean isFatalEnabled() {
            return delegate.isFatalEnabled();
        }

        public void fatal(final String message) {
            delegate.fatal(message);
        }

        public void fatal(final String message, final Throwable t) {
            delegate.fatal(message, t);
        }

        public boolean isErrorEnabled() {
            return delegate.isErrorEnabled();
        }

        public void error(final String message) {
            delegate.error(message);
        }

        public void error(final String message, final Throwable t) {
            delegate.error(message, t);
        }

        public boolean isWarnEnabled() {
            return delegate.isWarnEnabled();
        }

        public void warn(final String message) {
            delegate.warn(message);
        }

        public void warn(final String message, final Throwable t) {
            delegate.warn(message, t);
        }

        public boolean isInfoEnabled() {
            return delegate.isInfoEnabled();
        }

        public void info(final String message) {
            delegate.info(message);
        }

        public void info(final String message, final Throwable t) {
            delegate.info(message, t);
        }

        public boolean isDebugEnabled() {
            return delegate.isDebugEnabled();
        }

        public void debug(final String message) {
            delegate.debug(message);
        }

        public void debug(final String message, final Throwable t) {
            delegate.debug(message, t);
        }
    }
}
