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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * {@link LogFactory} implementation that delegates to the Log4J framework.
 *
 * @author Patrick Linskey
 */
public class Log4JLogFactory extends LogFactoryAdapter {

    protected Log newLogAdapter(String channel) {
        return new LogAdapter((Logger) LogManager.getLogger(channel));
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
            return _log.isEnabledFor(Level.DEBUG);
        }

        public boolean isInfoEnabled() {
            return _log.isEnabledFor(Level.INFO);
        }

        public boolean isWarnEnabled() {
            return _log.isEnabledFor(Level.WARN);
        }

        public boolean isErrorEnabled() {
            return _log.isEnabledFor(Level.ERROR);
        }

        public boolean isFatalEnabled() {
            return _log.isEnabledFor(Level.FATAL);
        }

        public void trace(Object o) {
            _log.debug(o);
        }

        public void trace(Object o, Throwable t) {
            _log.debug(o, t);
        }

        public void info(Object o) {
            _log.info(o);
        }

        public void info(Object o, Throwable t) {
            _log.info(o, t);
        }

        public void warn(Object o) {
            _log.warn(o);
        }

        public void warn(Object o, Throwable t) {
            _log.warn(o, t);
        }

        public void error(Object o) {
            _log.error(o);
        }

        public void error(Object o, Throwable t) {
            _log.error(o, t);
        }

        public void fatal(Object o) {
            _log.fatal(o);
        }

        public void fatal(Object o, Throwable t) {
            _log.fatal(o, t);
        }
    }
}
