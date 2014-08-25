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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// some helper reused accross several modules
public final class Proxys {
    public static <T> T threadLocalProxy(final Class<T> type, final ThreadLocal<? extends T> threadLocal) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { type, Serializable.class }, new ThreadLocalHandler<>(threadLocal));
    }

    public static HttpSession threadLocalRequestSessionProxy(final ThreadLocal<? extends HttpServletRequest> threadLocal) {
        return (HttpSession) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { HttpSession.class, Serializable.class }, new ThreadLocalSessionFromRequestHandler(threadLocal));
    }

    public static <T> T handlerProxy(final Class<T> type, final InvocationHandler raw) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { type, Serializable.class }, new EnsureExceptionIsUnwrapped(raw));
    }

    private Proxys() {
        // no-op
    }

    private static final class ThreadLocalSessionFromRequestHandler implements InvocationHandler {
        private final ThreadLocal<? extends HttpServletRequest> holder;

        public ThreadLocalSessionFromRequestHandler(final ThreadLocal<? extends HttpServletRequest> threadLocal) {
            holder = threadLocal;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                return method.invoke(holder.get().getSession(), args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }

    private static final class ThreadLocalHandler<T> implements InvocationHandler {
        private final ThreadLocal<T> holder;

        public ThreadLocalHandler(final ThreadLocal<T> threadLocal) {
            holder = threadLocal;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                return method.invoke(holder.get(), args);
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
