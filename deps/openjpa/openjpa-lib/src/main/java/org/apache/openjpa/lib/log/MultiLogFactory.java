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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A LogFactory implementation to pass events through multiple
 * LogFactory implementations(such as log4j and LogPanelFactory).
 *
 * @author Steve Kim
 */
public class MultiLogFactory implements LogFactory {

    private List<LogFactory> _delegates;

    /**
     * Create an instance with the given delegates.
     */
    public MultiLogFactory(LogFactory d1, LogFactory d2) {
        this(new LogFactory[]{ d1, d2 });
    }

    /**
     * Create an instance with the given delegates.
     */
    public MultiLogFactory(LogFactory d1, LogFactory d2, LogFactory d3) {
        this(new LogFactory[]{ d1, d2, d3 });
    }

    /**
     * Create an instance with the given delegates.
     */
    public MultiLogFactory(LogFactory[] delegates) {
        _delegates =
            new CopyOnWriteArrayList<LogFactory>(Arrays.asList(delegates));
    }

    public void addLogFactory(LogFactory factory) {
        _delegates.add(factory);
    }

    public void removeLogFactory(LogFactory factory) {
        _delegates.remove(factory);
    }

    /**
     * Returns the delegates that this MultiLogFactory delegates messages to.
     */
    public LogFactory[] getDelegates() {
        return (LogFactory[]) _delegates.toArray(new LogFactory[_delegates.size()]);
    }

    /**
     * Returns a Log impl that combines all logs.
     */
    public synchronized Log getLog(String channel) {
        List<Log> logs = new ArrayList<Log>(_delegates.size());
        for(LogFactory f : _delegates) { 
            if (f != null) {
                Log l = f.getLog(channel);
                if (l != null)
                    logs.add(l);
            }
        }
        return new MultiLog(logs.toArray(new Log[logs.size()]));
    }

    /**
     * Combinatory Log impl.
     */
    private static class MultiLog implements Log {

        private Log[] _logs;

        public MultiLog(Log[] logs) {
            _logs = logs;
        }

        /**
         * Return the logs that this log delegates to.
         */
        public Log[] getDelegates() {
            return _logs;
        }

        public void trace(Object msg) {
            for (int i = 0; i < _logs.length; i++)
                _logs[i].trace(msg);
        }

        public void trace(Object msg, Throwable t) {
            for (int i = 0; i < _logs.length; i++)
                _logs[i].trace(msg, t);
        }

        public void info(Object msg) {
            for (int i = 0; i < _logs.length; i++)
                _logs[i].info(msg);
        }

        public void info(Object msg, Throwable t) {
            for (int i = 0; i < _logs.length; i++)
                _logs[i].info(msg, t);
        }

        public void warn(Object msg) {
            for (int i = 0; i < _logs.length; i++)
                _logs[i].warn(msg);
        }

        public void warn(Object msg, Throwable t) {
            for (int i = 0; i < _logs.length; i++)
                _logs[i].warn(msg, t);
        }

        public void error(Object msg) {
            for (int i = 0; i < _logs.length; i++)
                _logs[i].error(msg);
        }

        public void error(Object msg, Throwable t) {
            for (int i = 0; i < _logs.length; i++)
                _logs[i].error(msg, t);
        }

        public void fatal(Object msg) {
            for (int i = 0; i < _logs.length; i++)
                _logs[i].fatal(msg);
        }

        public void fatal(Object msg, Throwable t) {
            for (int i = 0; i < _logs.length; i++)
                _logs[i].fatal(msg, t);
        }

        public boolean isTraceEnabled() {
            for (int i = 0; i < _logs.length; i++)
                if (_logs[i].isTraceEnabled())
                    return true;
            return false;
        }

        public boolean isInfoEnabled() {
            for (int i = 0; i < _logs.length; i++)
                if (_logs[i].isInfoEnabled())
                    return true;
            return false;
        }

        public boolean isWarnEnabled() {
            for (int i = 0; i < _logs.length; i++)
                if (_logs[i].isWarnEnabled())
                    return true;
            return false;
        }

        public boolean isErrorEnabled() {
            for (int i = 0; i < _logs.length; i++)
                if (_logs[i].isErrorEnabled())
                    return true;
            return false;
        }

        public boolean isFatalEnabled() {
            for (int i = 0; i < _logs.length; i++)
                if (_logs[i].isFatalEnabled())
                    return true;
            return false;
        }
    }
}
