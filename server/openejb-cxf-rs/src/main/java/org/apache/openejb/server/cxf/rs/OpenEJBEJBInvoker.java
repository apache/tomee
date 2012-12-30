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
import org.apache.openejb.BeanContext;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.rest.ThreadLocalContextManager;
import org.apache.openejb.util.proxy.BeanContextInvocationHandler;
import org.apache.openejb.util.proxy.ProxyManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Set;

public class OpenEJBEJBInvoker extends JAXRSInvoker {
    @Override
    public Object invoke(final Exchange exchange, final Object request, final Object resourceObject) {

        final Set<Class<?>> types = getContextTypes(resourceObject);

        if (types != null) {
            Contexts.bind(exchange, types);
        } else {
            Contexts.bind(exchange);
        }

        try {
            return super.invoke(exchange, request, resourceObject);
        } finally {
            ThreadLocalContextManager.reset();
        }
    }

    private Set<Class<?>> getContextTypes(Object resourceObject) {
        if (!ProxyManager.isProxyClass(resourceObject.getClass())) return null;
        final InvocationHandler handler = ProxyManager.getInvocationHandler(resourceObject);
        if (!(handler instanceof BeanContextInvocationHandler)) return null;

        final BeanContext beanContext = ((BeanContextInvocationHandler) handler).getBeanContext();
        final ContextReferenceTypes contextReferenceTypes = beanContext.get(ContextReferenceTypes.class);

        if (contextReferenceTypes == null) return null;

        return contextReferenceTypes.get();
    }

    @Override
    protected Object performInvocation(final Exchange exchange, final Object serviceObject,
                                       final Method m, final Object[] paramArray) throws Exception {
        try {
            return m.invoke(serviceObject, insertExchange(m, paramArray, exchange));
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getTargetException();
            // unwrap to get ExceptionMapper working
            if (cause instanceof InvalidateReferenceException) {
                cause = cause.getCause();
                if (cause instanceof RemoteException) {
                    cause = cause.getCause();
                }
            }
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw ite;
        }
    }
}
