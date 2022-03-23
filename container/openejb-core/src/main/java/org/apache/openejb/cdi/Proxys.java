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
package org.apache.openejb.cdi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.asList;

// some helper reused accross several modules
public final class Proxys {
    public static <T> T threadLocalProxy(final Class<T> type, final ThreadLocal<? extends T> threadLocal, final T defaultValue) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { type, Serializable.class }, new ThreadLocalHandler<>(threadLocal, defaultValue));
    }

    public static HttpSession threadLocalRequestSessionProxy(final ThreadLocal<? extends HttpServletRequest> threadLocal, final HttpSession defaultValue) {
        return (HttpSession) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { HttpSession.class, Serializable.class }, new ThreadLocalSessionFromRequestHandler(threadLocal, defaultValue));
    }

    public static <T> T handlerProxy(final InvocationHandler raw, final Class<T> main, final Class<?>... type) {
        final Collection<Class<?>> types = new ArrayList<>(type.length + 2);
        types.add(main);
        types.addAll(asList(type));
        types.add(Serializable.class);
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                types.toArray(new Class<?>[types.size()]), new EnsureExceptionIsUnwrapped(raw));
    }

    private Proxys() {
        // no-op
    }

    private static final class ThreadLocalSessionFromRequestHandler implements InvocationHandler {
        private final ThreadLocal<? extends HttpServletRequest> holder;
        private final HttpSession defaultValue;

        public ThreadLocalSessionFromRequestHandler(final ThreadLocal<? extends HttpServletRequest> threadLocal, final HttpSession defaultValue) {
            this.holder = threadLocal;
            this.defaultValue = defaultValue;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                final HttpServletRequest request = holder.get();
                if (request == null) {
                    return method.invoke(defaultValue, args);
                }
                return method.invoke(request.getSession(), args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }

    private static final class ThreadLocalHandler<T> implements InvocationHandler {
        private final ThreadLocal<? extends T> holder;
        private final T defaultValue;

        public ThreadLocalHandler(final ThreadLocal<? extends T> threadLocal, final T defaultValue) {
            this.holder = threadLocal;
            this.defaultValue = defaultValue;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                T obj = holder.get();
                if (obj == null) {
                    obj = defaultValue;
                }
                return method.invoke(obj, args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }

    private static final class EnsureExceptionIsUnwrapped implements InvocationHandler {
        private final InvocationHandler delegate;

        public EnsureExceptionIsUnwrapped(final InvocationHandler raw) {
            this.delegate = raw;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                return delegate.invoke(proxy, method, args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }
}
