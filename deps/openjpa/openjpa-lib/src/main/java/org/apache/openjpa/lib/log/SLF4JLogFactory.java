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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * {@link LogFactory} implementation that delegates to the SLF4J framework.
 *
 */
public class SLF4JLogFactory extends LogFactoryAdapter {

    protected Log newLogAdapter(String channel) {
        return new LogAdapter((Logger) LoggerFactory.getLogger(channel));
    }

    /**
     * Adapts a Log4J logger to the {@link org.apache.openjpa.lib.log.Log}
     * interface.
     */
    public static class LogAdapter implements Log {

        private Logger _log;

        private LogAdapter(Logger wrapee) {
            _log = wrapee;
        }

        public Logger getDelegate() {
            return _log;
        }

        public boolean isTraceEnabled() {
            return _log.isTraceEnabled();
        }

        // added for SLF4J - not in Log4JLogFactory
        public boolean isDebugEnabled() {
            return _log.isDebugEnabled();
        }

        public boolean isInfoEnabled() {
            return _log.isInfoEnabled();
        }

        public boolean isWarnEnabled() {
            return _log.isWarnEnabled();
        }

        public boolean isErrorEnabled() {
            return _log.isErrorEnabled();
        }

        public boolean isFatalEnabled() {
            // SLF4J has no FATAL level, so map to ERROR like log4j-over-slf4j
            return _log.isErrorEnabled();
        }

        public void trace(Object o) {
            _log.trace(objectToString(o));
        }

        public void trace(Object o, Throwable t) {
            _log.trace(objectToString(o), t);
        }

        // added for SLF4J - not in Log4JLogFactory
        public void debug(Object o) {
            _log.debug(objectToString(o));
        }

        // added for SLF4J - not in Log4JLogFactory
        public void debug(Object o, Throwable t) {
            _log.debug(objectToString(o), t);
        }

        public void info(Object o) {
            _log.info(objectToString(o));
        }

        public void info(Object o, Throwable t) {
            _log.info(objectToString(o), t);
        }

        public void warn(Object o) {
            _log.warn(objectToString(o));
        }

        public void warn(Object o, Throwable t) {
            _log.warn(objectToString(o), t);
        }

        public void error(Object o) {
            _log.error(objectToString(o));
        }

        public void error(Object o, Throwable t) {
            _log.error(objectToString(o), t);
        }

        public void fatal(Object o) {
            // SLF4J has no FATAL level, so map to ERROR like log4j-over-slf4j
            _log.error(objectToString(o));
        }

        public void fatal(Object o, Throwable t) {
            // SLF4J has no FATAL level, so map to ERROR like log4j-over-slf4j
            _log.error(objectToString(o), t);
        }

        private String objectToString(Object o) {
            if (o == null)
                return (String) o;
            else
                return o.toString();
        }
    }
}
