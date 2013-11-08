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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.GenericConfigurable;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default {@link LogFactory} implementation. For ease of automatic
 * configuration, this implementation keys on only the last dot-separated
 * token of the log channel name.
 *
 * @author Patrick Linskey
 */
public class LogFactoryImpl 
    implements LogFactory, GenericConfigurable, Configurable {

    private static Localizer _loc = Localizer.forPackage(LogFactoryImpl.class);
    private static Localizer _locEn = Localizer.forPackage(
        LogFactoryImpl.class, Locale.ENGLISH);

    public static final String TRACE_STR = _locEn.get("log-trace").getMessage();
    public static final String INFO_STR = _locEn.get("log-info").getMessage();
    public static final String WARN_STR = _locEn.get("log-warn").getMessage();
    public static final String ERROR_STR = _locEn.get("log-error").getMessage();
    public static final String FATAL_STR = _locEn.get("log-fatal").getMessage();

    public static final String STDOUT = "stdout";
    public static final String STDERR = "stderr";

    private static final String NEWLINE = J2DoPrivHelper.getLineSeparator();

    /**
     * The time at which this factory was initialized.
     */
    protected final long initializationMillis;

    /**
     * The {@link Log}s that this factory manages, keyed by log channel name.
     */
    private Map<String, LogImpl> _logs =
        new ConcurrentHashMap<String, LogImpl>(); 

    /**
     * The default logging level.
     */
    private short _defaultLogLevel = Log.INFO;

    /**
     * Storage for logging level configuration specified at configuration time.
     */
    private Map<String, Short> _configuredLevels =
        new HashMap<String, Short>(); 

    /**
     * The stream to write to. Defaults to System.err.
     */
    private PrintStream _out = System.err;

    /**
     * A token to add to all log messages. If <code>null</code>, the 
     * configuration's id will be used.
     */
    private String _diagContext = null;
    private boolean _diagContextComputed = false;
    
    private Configuration _conf;


    public LogFactoryImpl() {
        initializationMillis = System.currentTimeMillis();
    }

    public Log getLog(String channel) {
        // no locking; ok if same log created multiple times
        LogImpl l = _logs.get(channel);
        if (l == null) {
            l = newLogImpl();
            l.setChannel(channel);  // TODO add to interface?
            Short lvl = _configuredLevels.get(shorten(channel));
            l.setLevel(lvl == null ? _defaultLogLevel : lvl.shortValue());
            _logs.put(channel, l);
        }
        return l;
    }

    /**
     * Create a new log. The log will be cached.
     */
    protected LogImpl newLogImpl() {
        return new LogImpl();
    }

    /**
     * The string name of the default level for unconfigured log channels;
     * used for automatic configuration.
     */
    public void setDefaultLevel(String level) {
        _defaultLogLevel = getLevel(level);
    }

    /**
     * The default level for unconfigured log channels.
     */
    public short getDefaultLevel() {
        return _defaultLogLevel;
    }

    /**
     * The default level for unconfigured log channels.
     */
    public void setDefaultLevel(short level) {
        _defaultLogLevel = level;
    }

    /**
     * A string to prefix all log messages with. Set to
     * <code>null</code> to use the configuration's Id property setting.
     */
    public void setDiagnosticContext(String val) {
        _diagContext = val;
    }

    /**
     * A string to prefix all log messages with. Set to
     * <code>null</code> to use the configuration's Id property setting.
     */
    public String getDiagnosticContext() {
        if (!_diagContextComputed) {
            // this initialization has to happen lazily because there is no
            // guarantee that conf.getId() will be populated by the time that
            // endConfiguration() is called.
            if (_diagContext == null && _conf != null) {
                _diagContext = _conf.getId();
            }
            if ("".equals(_diagContext))
                _diagContext = null;
            _diagContextComputed = true;
        }
        return _diagContext;
    }

    /**
     * The stream to write to. Recognized values are: <code>stdout</code>
     * and <code>stderr</code>. Any other value will be considered a file name.
     */
    public void setFile(String file) {
        if (STDOUT.equals(file))
            _out = System.out;
        else if (STDERR.equals(file))
            _out = System.err;
        else {
            File f = Files.getFile(file, null);
            try {
                _out = new PrintStream((FileOutputStream)
                    AccessController.doPrivileged(
                        J2DoPrivHelper.newFileOutputStreamAction(
                            AccessController.doPrivileged(
                                J2DoPrivHelper.getCanonicalPathAction(f)),
                            true)));
            } catch (PrivilegedActionException pae) {
                throw new IllegalArgumentException(_loc.get("log-bad-file",
                        file) + " " + pae.getException());
            } catch (IOException ioe) {
                throw new IllegalArgumentException(_loc.get("log-bad-file",
                    file) + " " + ioe.toString());
            }
        }
    }

    /**
     * The stream to write to.
     */
    public PrintStream getStream() {
        return _out;
    }

    /**
     * The stream to write to.
     */
    public void setStream(PrintStream stream) {
        if (stream == null)
            throw new NullPointerException("stream == null");
        _out = stream;
    }

    /**
     * Returns a string representation of the specified log level constant.
     */
    public static String getLevelName(short level) {
        switch (level) {
            case Log.TRACE:
                return TRACE_STR;
            case Log.INFO:
                return INFO_STR;
            case Log.WARN:
                return WARN_STR;
            case Log.ERROR:
                return ERROR_STR;
            case Log.FATAL:
                return FATAL_STR;
            default:
                return _locEn.get("log-unknown").getMessage();
        }
    }

    /**
     * Returns a symbolic constant for the specified string level.
     */
    public static short getLevel(String str) {
        str = str.toUpperCase(Locale.ENGLISH).trim();
        short val = TRACE_STR.equals(str) ? Log.TRACE :
            INFO_STR.equals(str) ? Log.INFO :
                WARN_STR.equals(str) ? Log.WARN :
                    ERROR_STR.equals(str) ? Log.ERROR :
                        FATAL_STR.equals(str) ? Log.FATAL : -1;

        if (val == -1)
            throw new IllegalArgumentException
                (_loc.get("log-bad-constant", str).getMessage());

        return val;
    }

    // ---------- Configurable implementation ----------
    
    public void setConfiguration(Configuration conf) {
        _conf = conf;
    }
    
    public void startConfiguration() {
    }

    public void endConfiguration() {
    }

    // ---------- GenericConfigurable implementation ----------

    public void setInto(Options opts) {
        if (!opts.isEmpty()) {
            Map.Entry<Object, Object> e;
            for (Iterator<Map.Entry<Object, Object>> iter =
                opts.entrySet().iterator(); iter.hasNext();) {
                e = iter.next();
                _configuredLevels.put(shorten((String) e.getKey()), Short.valueOf(getLevel((String) e.getValue())));
            }
            opts.clear();
        }
    }

    private static String shorten(String channel) {
        return channel.substring(channel.lastIndexOf('.') + 1);
    }

    /**
     * A simple implementation of the {@link Log} interface. Writes
     * output to stderr.
     */
    public class LogImpl extends AbstractLog {

        private short _level = INFO;
        private String _channel;

        protected boolean isEnabled(short level) {
            return level >= _level;
        }

        protected void log(short level, String message, Throwable t) {
            String msg = formatMessage(level, message, t);
            _out.println(msg);
        }

        /**
         * Convert <code>message</code> into a string ready to be written to
         * the log. 
         *
         * @param t may be null
         */
        protected String formatMessage(short level, String message, Throwable t) {
            // we write to a StringBuilder and then flush it all at
            // once as a single line, since some environments(e.g., JBoss)
            // override the System output stream to flush any calls
            // to write without regard to line breaks, making the
            // output incomprehensible.
            StringBuilder buf = new StringBuilder();

            buf.append(getOffset());
            buf.append("  ");
            if (getDiagnosticContext() != null)
                buf.append(getDiagnosticContext()).append("  ");
            buf.append(getLevelName(level));
            if (level == INFO || level == WARN)
                buf.append(" ");
            buf.append("  [");
            buf.append(Thread.currentThread().getName());
            buf.append("] ");
            buf.append(_channel);
            buf.append(" - ");
            buf.append(message);

            if (t != null) {
                StringWriter swriter = new StringWriter();
                PrintWriter pwriter = new PrintWriter(swriter);
                t.printStackTrace(pwriter);
                pwriter.flush();
                buf.append(swriter.toString());
            }
            return buf.toString();
        }

        private long getOffset() {
            return System.currentTimeMillis() - initializationMillis;
        }

        public void setChannel(String val) {
            _channel = val;
        }

        public String getChannel() {
            return _channel;
        }

        public void setLevel(short val) {
            _level = val;
        }

        public short getLevel() {
            return _level;
        }
    }
}
