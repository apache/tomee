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
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.JAXRSInvoker;
import org.apache.cxf.message.Exchange;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.monitoring.StatsInterceptor;
import org.apache.openejb.util.proxy.BeanContextInvocationHandler;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.proxy.ProxyManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import jakarta.ejb.EJBException;

public class OpenEJBEJBInvoker extends JAXRSInvoker {
    private final Map<Class<?>, Collection<Class<?>>> contextTypes = new HashMap<Class<?>, Collection<Class<?>>>();

    public OpenEJBEJBInvoker(final Collection<BeanContext> restEjbs) {
        for (final BeanContext context : restEjbs) {
            final Collection<Class<?>> classes = new HashSet<>();
            Contexts.findContextFields(context.getBeanClass(), classes);
            for (final Collection<InterceptorData> list :
                Arrays.asList(
                    context.getInterceptorData(),
                    context.getInstanceScopedInterceptors(),
                    context.getCallbackInterceptors())) {
                for (final InterceptorData id : list) {
                    final Class<?> interceptorClass = id.getInterceptorClass();
                    if (!StatsInterceptor.class.equals(interceptorClass)) {
                        Contexts.findContextFields(interceptorClass, classes);
                    }
                }
            }
            contextTypes.put(context.getBeanClass(), classes);
        }
    }

    @Override
    public Object invoke(final Exchange exchange, final Object request, final Object resourceObject) {
        Contexts.bind(exchange, getContextTypes(resourceObject));
        return super.invoke(exchange, request, resourceObject);
    }

    private Collection<Class<?>> getContextTypes(final Object resourceObject) {
        if (!ProxyManager.isProxyClass(resourceObject.getClass())
            && !LocalBeanProxyFactory.isProxy(resourceObject.getClass())) {
            return Collections.emptySet();
        }

        final InvocationHandler handler = ProxyManager.getInvocationHandler(resourceObject);
        if (!(handler instanceof BeanContextInvocationHandler)) {
            return Collections.emptySet();
        }

        final BeanContext beanContext = ((BeanContextInvocationHandler) handler).getBeanContext();

        if (beanContext == null) {
            return Collections.emptySet();
        }
        return contextTypes.get(beanContext.getBeanClass());
    }

    @Override
    protected Object performInvocation(final Exchange exchange, final Object serviceObject,
                                       final Method m, final Object[] paramArray) throws Exception {
        try {
            final Object[] args = super.insertExchange(m, paramArray, exchange);
            return m.invoke(serviceObject, args);
        } catch (final InvocationTargetException ite) {
            Throwable cause = ite.getTargetException();
            // unwrap to get ExceptionMapper working
            if (cause instanceof InvalidateReferenceException) {
                cause = cause.getCause();
                if (cause instanceof RemoteException) {
                    cause = cause.getCause();
                }
            }

            if (EJBException.class.isInstance(cause)) {
                cause = EJBException.class.cast(cause).getCause();
            }

            if (ApplicationException.class.isInstance(cause) && Exception.class.isInstance(cause.getCause())) {
                throw Exception.class.cast(ApplicationException.class.cast(cause).getCause());
            }

            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw ite;
        }
    }
}
