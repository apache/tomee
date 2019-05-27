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

package org.apache.openejb.log.logger;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * java.util.logging.Logger implementation delegating to another framework.
 * All methods can be used except:
 * setLevel
 * addHandler / getHandlers
 * setParent / getParent
 * setUseParentHandlers / getUseParentHandlers
 *
 * @author gnodet
 */
public abstract class AbstractDelegatingLogger extends Logger {

    protected AbstractDelegatingLogger(final String name, final String resourceBundleName) {
        super(name, resourceBundleName);
    }

    public void log(final LogRecord record) {
        if (isLoggable(record.getLevel())) {
            doLog(record);
        }
    }

    public void log(final Level level, final String msg) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            doLog(lr);
        }
    }

    public void log(final Level level, final String msg, final Object param1) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            final Object[] params = {param1};
            lr.setParameters(params);
            doLog(lr);
        }
    }

    public void log(final Level level, final String msg, final Object[] params) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setParameters(params);
            doLog(lr);
        }
    }

    public void log(final Level level, final String msg, final Throwable thrown) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setThrown(thrown);
            doLog(lr);
        }
    }

    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            doLog(lr);
        }
    }

    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Object param1) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            final Object[] params = {param1};
            lr.setParameters(params);
            doLog(lr);
        }
    }

    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Object[] params) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setParameters(params);
            doLog(lr);
        }
    }

    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Throwable thrown) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setThrown(thrown);
            doLog(lr);
        }
    }

    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            doLog(lr, bundleName);
        }
    }

    public void logrb(final Level level, final String sourceClass, final String sourceMethod,
                      final String bundleName, final String msg, final Object param1) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            final Object[] params = {param1};
            lr.setParameters(params);
            doLog(lr, bundleName);
        }
    }

    public void logrb(final Level level, final String sourceClass, final String sourceMethod,
                      final String bundleName, final String msg, final Object[] params) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setParameters(params);
            doLog(lr, bundleName);
        }
    }

    public void logrb(final Level level, final String sourceClass, final String sourceMethod,
                      final String bundleName, final String msg, final Throwable thrown) {
        if (isLoggable(level)) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setThrown(thrown);
            doLog(lr, bundleName);
        }
    }

    public void entering(final String sourceClass, final String sourceMethod) {
        if (isLoggable(Level.FINER)) {
            logp(Level.FINER, sourceClass, sourceMethod, "ENTRY");
        }
    }

    public void entering(final String sourceClass, final String sourceMethod, final Object param1) {
        if (isLoggable(Level.FINER)) {
            final Object[] params = {param1};
            logp(Level.FINER, sourceClass, sourceMethod, "ENTRY {0}", params);
        }
    }

    public void entering(final String sourceClass, final String sourceMethod, final Object[] params) {
        if (isLoggable(Level.FINER)) {
            final String msg = "ENTRY";
            if (params == null) {
                logp(Level.FINER, sourceClass, sourceMethod, msg);
                return;
            }
            final StringBuilder builder = new StringBuilder(msg);
            for (int i = 0; i < params.length; i++) {
                builder.append(" {");
                builder.append(Integer.toString(i));
                builder.append("}");
            }
            logp(Level.FINER, sourceClass, sourceMethod, builder.toString(), params);
        }
    }

    public void exiting(final String sourceClass, final String sourceMethod) {
        if (isLoggable(Level.FINER)) {
            logp(Level.FINER, sourceClass, sourceMethod, "RETURN");
        }
    }

    public void exiting(final String sourceClass, final String sourceMethod, final Object result) {
        if (isLoggable(Level.FINER)) {
            final Object[] params = {result};
            logp(Level.FINER, sourceClass, sourceMethod, "RETURN {0}", params);
        }
    }

    public void throwing(final String sourceClass, final String sourceMethod, final Throwable thrown) {
        if (isLoggable(Level.FINER)) {
            final LogRecord lr = new LogRecord(Level.FINER, "THROW");
            lr.setSourceClassName(sourceClass);
            lr.setSourceMethodName(sourceMethod);
            lr.setThrown(thrown);
            doLog(lr);
        }
    }

    public void severe(final String msg) {
        if (isLoggable(Level.SEVERE)) {
            final LogRecord lr = new LogRecord(Level.SEVERE, msg);
            doLog(lr);
        }
    }

    public void warning(final String msg) {
        if (isLoggable(Level.WARNING)) {
            final LogRecord lr = new LogRecord(Level.WARNING, msg);
            doLog(lr);
        }
    }

    public void info(final String msg) {
        if (isLoggable(Level.INFO)) {
            final LogRecord lr = new LogRecord(Level.INFO, msg);
            doLog(lr);
        }
    }

    public void config(final String msg) {
        if (isLoggable(Level.CONFIG)) {
            final LogRecord lr = new LogRecord(Level.CONFIG, msg);
            doLog(lr);
        }
    }

    public void fine(final String msg) {
        if (isLoggable(Level.FINE)) {
            final LogRecord lr = new LogRecord(Level.FINE, msg);
            doLog(lr);
        }
    }

    public void finer(final String msg) {
        if (isLoggable(Level.FINER)) {
            final LogRecord lr = new LogRecord(Level.FINER, msg);
            doLog(lr);
        }
    }

    public void finest(final String msg) {
        if (isLoggable(Level.FINEST)) {
            final LogRecord lr = new LogRecord(Level.FINEST, msg);
            doLog(lr);
        }
    }

    public void setLevel(final Level newLevel) throws SecurityException {
        throw new UnsupportedOperationException();
    }

    public abstract Level getLevel();

    public boolean isLoggable(final Level level) {
        final Level l = getLevel();
        return level.intValue() >= l.intValue() && l != Level.OFF;
    }

    protected boolean supportsHandlers() {
        return false;
    }

    public synchronized void addHandler(final Handler handler) throws SecurityException {
        if (supportsHandlers()) {
            super.addHandler(handler);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public synchronized void removeHandler(final Handler handler) throws SecurityException {
        if (supportsHandlers()) {
            super.removeHandler(handler);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public synchronized Handler[] getHandlers() {
        if (supportsHandlers()) {
            return super.getHandlers();
        }
        throw new UnsupportedOperationException();
    }

    public synchronized void setUseParentHandlers(final boolean useParentHandlers) {
        if (supportsHandlers()) {
            super.setUseParentHandlers(useParentHandlers);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public synchronized boolean getUseParentHandlers() {
        if (supportsHandlers()) {
            return super.getUseParentHandlers();
        }
        throw new UnsupportedOperationException();
    }

    public Logger getParent() {
        return null;
    }

    public void setParent(final Logger parent) {
        throw new UnsupportedOperationException();
    }

    protected void doLog(final LogRecord lr) {
        lr.setLoggerName(getName());
        final String rbname = getResourceBundleName();
        if (rbname != null) {
            lr.setResourceBundleName(rbname);
            lr.setResourceBundle(getResourceBundle());
        }
        internalLog(lr);
    }

    protected void doLog(final LogRecord lr, final String rbname) {
        lr.setLoggerName(getName());
        if (rbname != null) {
            lr.setResourceBundleName(rbname);
            lr.setResourceBundle(loadResourceBundle(rbname));
        }
        internalLog(lr);
    }

    protected void internalLog(final LogRecord record) {
        final Filter filter = getFilter();
        if (filter != null && !filter.isLoggable(record)) {
            return;
        }
        final String msg = formatMessage(record);
        internalLogFormatted(msg, record);
    }

    protected abstract void internalLogFormatted(String msg, LogRecord record);

    protected String formatMessage(final LogRecord record) {
        String format = record.getMessage();
        final ResourceBundle catalog = record.getResourceBundle();
        if (catalog != null) {
            try {
                format = catalog.getString(record.getMessage());
            } catch (final MissingResourceException ex) {
                format = record.getMessage();
            }
        }
        try {
            final Object[] parameters = record.getParameters();
            if (parameters == null || parameters.length == 0) {
                return format;
            }
            if (format.contains("{0") || format.contains("{1")
                || format.contains("{2") || format.contains("{3")) {
                return MessageFormat.format(format, parameters);
            }
            return format;
        } catch (final Exception ex) {
            return format;
        }
    }

    /**
     * Load the specified resource bundle
     *
     * @param resourceBundleName the name of the resource bundle to load, cannot be null
     * @return the loaded resource bundle.
     * @throws MissingResourceException If the specified resource bundle can not be loaded.
     */
    static ResourceBundle loadResourceBundle(final String resourceBundleName) {
        // try context class loader to load the resource
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (null != cl) {
            try {
                return ResourceBundle.getBundle(resourceBundleName, Locale.getDefault(), cl);
            } catch (final MissingResourceException e) {
                // Failed to load using context classloader, ignore
            }
        }
        // try system class loader to load the resource
        cl = ClassLoader.getSystemClassLoader();
        if (null != cl) {
            try {
                return ResourceBundle.getBundle(resourceBundleName, Locale.getDefault(), cl);
            } catch (final MissingResourceException e) {
                // Failed to load using system classloader, ignore
            }
        }
        return null;
    }

}
