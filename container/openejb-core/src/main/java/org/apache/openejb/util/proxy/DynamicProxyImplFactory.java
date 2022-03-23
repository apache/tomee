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

package org.apache.openejb.util.proxy;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.api.Proxy;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.MetaAnnotatedClass;

import javax.naming.NamingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DynamicProxyImplFactory {
    public static boolean isKnownDynamicallyImplemented(final Class<?> clazz) {
        final Annotated<Class<?>> metaClass = new MetaAnnotatedClass(clazz);
        return isKnownDynamicallyImplemented(metaClass, clazz);
    }

    public static boolean isKnownDynamicallyImplemented(final Annotated<?> metaClass, final Class<?> clazz) {
        return clazz.isInterface()
            && (metaClass.getAnnotation(PersistenceContext.class) != null
            || metaClass.getAnnotation(Proxy.class) != null);
    }

    public static Object newProxy(final BeanContext context, final InvocationHandler invocationHandler) {
        if (QueryProxy.class.isInstance(invocationHandler)) {
            EntityManager em = null;
            for (final Injection injection : context.getInjections()) {
                if (QueryProxy.class.equals(injection.getTarget())) {
                    try {
                        em = (EntityManager) context.getJndiEnc().lookup(injection.getJndiName());
                    } catch (final NamingException e) {
                        throw new OpenEJBRuntimeException("a dynamic bean should reference at least one correct PersistenceContext", e);
                    }
                }
            }
            if (em == null) {
                throw new OpenEJBRuntimeException("can't find the entity manager to use for the dynamic bean " + context.getEjbName());
            }
            QueryProxy.class.cast(invocationHandler).setEntityManager(em);
        }

        return newProxy(context.getBeanClass(), invocationHandler);
    }

    public static Object newProxy(final Class<?> type, final InvocationHandler invocationHandler) {
        try {
            return ProxyManager.newProxyInstance(type, new Handler(invocationHandler));
        } catch (final IllegalAccessException e) {
            throw new OpenEJBRuntimeException("illegal access", e);
        }
    }

    public static Object realHandler(final Object proxy) {
        if (proxy != null && java.lang.reflect.Proxy.isProxyClass(proxy.getClass())) {
            final Object handler = java.lang.reflect.Proxy.getInvocationHandler(proxy);
            if (handler instanceof Handler) {
                return ((Handler) handler).realHandler();
            }
        }
        return null;
    }

    private static final class Handler implements InvocationHandler {
        private final InvocationHandler handler;

        private Handler(final InvocationHandler handler) {
            this.handler = handler;
        }

        public InvocationHandler realHandler() {
            return handler;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return handler.invoke(proxy, method, args);
        }
    }
}
