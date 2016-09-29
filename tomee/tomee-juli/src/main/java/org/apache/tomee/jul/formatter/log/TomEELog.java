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
package org.apache.tomee.jul.formatter.log;

import org.apache.juli.logging.Log;

import java.lang.reflect.Method;

public class TomEELog implements Log {
    private static volatile boolean initialized;
    private static volatile String loggerClazz;
    private static volatile boolean defaultLogger;

    private static synchronized void initialize() {
        if (initialized) {
            return;
        }

        if (!Boolean.getBoolean("tomee.skip-tomcat-log")) {
            final Thread thread = Thread.currentThread();
            try {
                final ClassLoader tccl = thread.getContextClassLoader();
                final Class<?> systemInstance = tccl.loadClass("org.apache.openejb.loader.SystemInstance");
                if (!Boolean.class.cast(systemInstance.getMethod("isInitialized").invoke(null))) { // check if Logger was forced
                    final Class<?> logger = tccl.loadClass("org.apache.openejb.util.Logger");
                    final Method m = logger.getDeclaredMethod("unsafeDelegateClass");
                    loggerClazz = m.invoke(null).getClass().getName();
                }

                if (loggerClazz == null) {
                    final Class<?> logger = tccl.loadClass("org.apache.openejb.util.Logger");
                    final Method m = logger.getDeclaredMethod("delegateClass");
                    loggerClazz = (String) m.invoke(null);
                }

                switch (loggerClazz) {
                    case "org.apache.openejb.util.Log4j2LogStreamFactory":
                    case "org.apache.openejb.util.Log4jLogStreamFactory":
                    case "org.apache.openejb.util.Slf4jLogStreamFactory":
                    case "org.apache.openejb.maven.util.MavenLogStreamFactory":
                        defaultLogger = false;
                        break;
                    default:
                        defaultLogger = true;
                }
                initialized = true;
            } catch (final Throwable th) {
                // no-op
            }
        }
    }

    public static String getLoggerClazz() {
        return loggerClazz;
    }

    private final Log delegate;

    public TomEELog() { // for ServiceLoader
        delegate = null;
    }

    public TomEELog(final String name) {
        initialize();
        this.delegate = defaultLogger ? new JULLogger(name) : ReloadableLog.newLog(name, loggerClazz);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return delegate.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void trace(final Object message) {
        delegate.trace(message);
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        delegate.trace(message, t);
    }

    @Override
    public void debug(final Object message) {
        delegate.debug(message);
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        delegate.debug(message, t);
    }

    @Override
    public void info(final Object message) {
        delegate.info(message);
    }

    @Override
    public void info(final Object message, final Throwable t) {
        delegate.info(message, t);
    }

    @Override
    public void warn(final Object message) {
        delegate.warn(message);
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        delegate.warn(message, t);
    }

    @Override
    public void error(final Object message) {
        delegate.error(message);
    }

    @Override
    public void error(final Object message, final Throwable t) {
        delegate.error(message, t);
    }

    @Override
    public void fatal(final Object message) {
        delegate.fatal(message);
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        delegate.fatal(message, t);
    }
}
