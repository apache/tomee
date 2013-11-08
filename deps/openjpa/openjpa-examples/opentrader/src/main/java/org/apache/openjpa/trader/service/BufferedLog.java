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
package org.apache.openjpa.trader.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactory;
import org.apache.openjpa.trader.domain.LogStatement;

/**
 * Specialized log to consolidate multiple logs into a single one.
 * Selects only query related messages.
 * <br>
 * Designed to capture multiple logs used by different slices. It would have 
 * been more useful to capture the slice thread that executed the query, but
 * that is not possible in all cases as the log statement is emitted from the
 * kernel's main thread.
 *      
 * @author Pinaki Poddar
 *
 */
public class BufferedLog implements LogFactory, Configurable  {
    private int _history    = 100;
    private String _diagCtx;
    private Configuration _conf;
    static String[] SQL_MARKERS  = {"INSERT INTO", "SELECT", "UPDATE", "DELETE"};
    static String[] JPQL_MARKERS = {"Executing query: ["};
    static List<String> CHANNELS = Arrays.asList(OpenJPAConfiguration.LOG_QUERY, JDBCConfiguration.LOG_SQL);
    
    private static LinkedList<LogStatement> _messageModel;
    static {
        _messageModel = new LinkedList<LogStatement>();
    }
    
    public void setConfiguration(Configuration conf) {
        _conf = conf;
    }

    public void endConfiguration() {
    }

    public void startConfiguration() {
    }

    
    public BufferedLog() {
        super();
    }
    
    public void setDiagnosticContext(String ctx) {
        System.err.println(ctx);
        _diagCtx = ctx;
    }
    
    
    public Log getLog(String channel) {
        return new ChannelLog(channel);
    }

    public void setHistory(int i) {
        _history = Math.max(i, 1);
    }
    
    public int getHistory() {
        return _history;
    }
    
    String getContext() {
        if (_diagCtx != null)
            return _diagCtx;
        if (_conf == null || isEmpty(_conf.getId()))
            return "";
        else {
            _diagCtx = _conf.getId();
            return _conf.getId();
        }
    }
    
    void addStatement(LogStatement stmt) {
        _messageModel.addLast(stmt);
        if (_messageModel.size() > _history) {
            _messageModel.removeFirst();
        }
    }
    
    boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }
    
    public List<LogStatement> get() {
        List<LogStatement> result = new ArrayList<LogStatement>(_messageModel);
        _messageModel.clear();
        return result;
    }

    
    public class ChannelLog implements Log {
        final String _channel;
        final String _thread;
        public ChannelLog(String channel) {
            _channel = channel;
            _thread = Thread.currentThread().getName();
        }
        
        public void error(Object o) {
            createLogStatement("ERROR", o, null);
        }

        public void error(Object o, Throwable t) {
            createLogStatement("ERROR", o, t);
        }

        
        public void fatal(Object o) {
            createLogStatement("FATAL", o, null);
        }

        
        public void fatal(Object o, Throwable t) {
            createLogStatement("FATAL", o, t);
        }

        
        public void info(Object o) {
            createLogStatement("INFO", o, null);
        }

        
        public void info(Object o, Throwable t) {
            createLogStatement("INFO", o, t);
        }

        
        public boolean isErrorEnabled() {
            return true;
        }
        
        public boolean isFatalEnabled() {
            return true;
        }
        
        public boolean isInfoEnabled() {
            return CHANNELS.contains(_channel);
        }
        
        public boolean isTraceEnabled() {
            return CHANNELS.contains(_channel);
        }
        
        public boolean isWarnEnabled() {
            return true;
        }

        
        public void trace(Object o) {
            createLogStatement("TRACE", o, null);
        }
        
        public void trace(Object o, Throwable t) {
            createLogStatement("TRACE", o, t);
        }
        
        public void warn(Object o) {
            createLogStatement("WARN", o, null);
        }
        
        public void warn(Object o, Throwable t) {
            createLogStatement("WARN", o, t);
        }
        
        protected void createLogStatement(String level, Object message, Throwable t) {
            String msg = message == null ? null : message.toString();
            msg = extractQuery(msg);
            if (msg == null) {
                return;
            }
            addStatement(new LogStatement(level, getContext(), 
                    _thread, _channel, msg));
            if (t != null) {
                StringWriter buffer = new StringWriter();
                t.printStackTrace(new PrintWriter(buffer));
                addStatement(new LogStatement(
                        level, getContext(), 
                        Thread.currentThread().getName(), _channel, 
                        buffer.toString()));
            }
        }
        
        public String extractQuery(String msg) {
            if (msg == null)
                return null;
            if ("openjpa.jdbc.SQL".equals(_channel)) {
                return getQuery(msg, SQL_MARKERS, true);
            } else if ("openjpa.Query".equals(_channel)) {
                return getQuery(msg, JPQL_MARKERS, false);
            } 
            return null;
        }
        
        private String getQuery(String message, String[] markers, boolean sql) {
            int k = -1;
            for (int i = 0; i < markers.length; i++) {
                k = message.indexOf(markers[i]);
                if (k != -1) {
                    int m = sql ? 0 : markers[i].length();
                    return message.substring(k+m).trim();
                }
            }
            return null;
        }
 
    }


}
