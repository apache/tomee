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

package org.apache.openejb.log.commonslogging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.openejb.util.reflection.Reflections;

import java.io.Serializable;

// totally done by reflection to avoid conflict between Log from container and app classloaders
public class Log4J4AppOpenEJB4ContainerLog implements Log, Serializable {
    private static final Class<?>[] NO_PARAM = new Class<?>[0];
    private static final Class<?>[] OBJECT_PARAM = new Class<?>[]{Object.class};
    private static final Class<?>[] OBJECT_THROWABLE_PARAM = new Class<?>[]{Object.class, Throwable.class};
    private static final Object[] NO_ARGS = new Object[0];

    private Object delegate;

    public Log4J4AppOpenEJB4ContainerLog(final String category) {
        if (URLClassLoaderFirst.shouldSkip(category)) {
            delegate = new OpenEJBCommonsLog(category);
        } else {
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try { // need to be done with TCCL
                delegate = contextClassLoader
                    .loadClass("org.apache.commons.logging.impl.Log4JLogger")
                    .getConstructor(String.class).newInstance(category);
            } catch (final Exception ex) {
                delegate = new Jdk14Logger(category);
            }
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return (Boolean) Reflections.invokeByReflection(delegate, "isDebugEnabled", NO_PARAM, NO_ARGS);
    }

    @Override
    public boolean isErrorEnabled() {
        return (Boolean) Reflections.invokeByReflection(delegate, "isErrorEnabled", NO_PARAM, NO_ARGS);
    }

    @Override
    public boolean isFatalEnabled() {
        return (Boolean) Reflections.invokeByReflection(delegate, "isFatalEnabled", NO_PARAM, NO_ARGS);
    }

    @Override
    public boolean isInfoEnabled() {
        return (Boolean) Reflections.invokeByReflection(delegate, "isInfoEnabled", NO_PARAM, NO_ARGS);
    }

    @Override
    public boolean isTraceEnabled() {
        return (Boolean) Reflections.invokeByReflection(delegate, "isTraceEnabled", NO_PARAM, NO_ARGS);
    }

    @Override
    public boolean isWarnEnabled() {
        return (Boolean) Reflections.invokeByReflection(delegate, "isWarnEnabled", NO_PARAM, NO_ARGS);
    }

    @Override
    public void trace(final Object message) {
        Reflections.invokeByReflection(delegate, "trace", OBJECT_PARAM, new Object[]{message});
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        Reflections.invokeByReflection(delegate, "trace", OBJECT_THROWABLE_PARAM, new Object[]{message, t});
    }

    @Override
    public void debug(final Object message) {
        Reflections.invokeByReflection(delegate, "debug", OBJECT_PARAM, new Object[]{message});
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        Reflections.invokeByReflection(delegate, "debug", OBJECT_THROWABLE_PARAM, new Object[]{message, t});
    }

    @Override
    public void info(final Object message) {
        Reflections.invokeByReflection(delegate, "info", OBJECT_PARAM, new Object[]{message});
    }

    @Override
    public void info(final Object message, final Throwable t) {
        Reflections.invokeByReflection(delegate, "info", OBJECT_THROWABLE_PARAM, new Object[]{message, t});
    }

    @Override
    public void warn(final Object message) {
        Reflections.invokeByReflection(delegate, "warn", OBJECT_PARAM, new Object[]{message});
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        Reflections.invokeByReflection(delegate, "warn", OBJECT_THROWABLE_PARAM, new Object[]{message, t});
    }

    @Override
    public void error(final Object message) {
        Reflections.invokeByReflection(delegate, "error", OBJECT_PARAM, new Object[]{message});
    }

    @Override
    public void error(final Object message, final Throwable t) {
        Reflections.invokeByReflection(delegate, "error", OBJECT_THROWABLE_PARAM, new Object[]{message, t});
    }

    @Override
    public void fatal(final Object message) {
        Reflections.invokeByReflection(delegate, "fatal", OBJECT_PARAM, new Object[]{message});
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        Reflections.invokeByReflection(delegate, "fatal", OBJECT_THROWABLE_PARAM, new Object[]{message, t});
    }
}
