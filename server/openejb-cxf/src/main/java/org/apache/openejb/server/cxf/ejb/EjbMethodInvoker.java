/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.cxf.ejb;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.AbstractJAXWSMethodInvoker;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.FaultMode;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.service.invoker.Factory;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.interceptor.InvocationContext;
import jakarta.xml.ws.WebFault;
import jakarta.xml.ws.handler.MessageContext.Scope;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EjbMethodInvoker extends AbstractJAXWSMethodInvoker {

    private static final Logger log = Logger.getInstance(LogCategory.CXF, EjbMethodInvoker.class);

    private static final String HANDLER_PROPERTIES = "HandlerProperties";
    private final Object instance;

    private final BeanContext beanContext;
    private final Bus bus;

    public EjbMethodInvoker(final Bus bus, final BeanContext beanContext) {
        super(null);
        this.bus = bus;
        this.beanContext = beanContext;

        Object inst;
        try {
            inst = beanContext.getBeanClass().newInstance();
        } catch (final Exception e) {
            inst = null;
        }
        this.instance = inst;
    }

    @Override
    public Object getServiceObject(final Exchange context) {
        return instance; // just to not get a NPE
    }

    @Override
    public void releaseServiceObject(final Exchange ex, final Object obj) {
        // do nothing
    }

    @Override
    protected Object invoke(final Exchange exchange, final Object serviceObject,
                            final Method m, final List<Object> params) {
        final InvocationContext invContext = exchange.get(InvocationContext.class);
        if (invContext == null) {
            return preEjbInvoke(exchange, m, params);
        }
        return super.invoke(exchange, serviceObject, m, params);
    }

    @Override
    protected Object performInvocation(final Exchange exchange, final Object serviceObject,
                                       final Method m, final Object[] paramArray) throws Exception {
        InvocationContext invContext = exchange.get(InvocationContext.class);
        invContext.setParameters(paramArray);
        Object res = invContext.proceed();

        EjbMessageContext ctx = (EjbMessageContext) invContext.getContextData();

        Map<String, Object> handlerProperties = (Map<String, Object>) exchange
            .get(HANDLER_PROPERTIES);
        addHandlerProperties(ctx, handlerProperties);

        updateWebServiceContext(exchange, ctx);

        return res;
    }

    private Object preEjbInvoke(Exchange exchange, Method method, List<Object> params) {

        EjbMessageContext ctx = new EjbMessageContext(exchange.getInMessage(),
            Scope.APPLICATION);
        WebServiceContextImpl.setMessageContext(ctx);

        Map<String, Object> handlerProperties = removeHandlerProperties(ctx);
        exchange.put(HANDLER_PROPERTIES, handlerProperties);

        try {
            EjbInterceptor interceptor = new EjbInterceptor(params, method,
                this.bus, exchange);
            Object[] arguments = {ctx, interceptor};

            RpcContainer container = (RpcContainer) this.beanContext
                .getContainer();

            Class callInterface = this.beanContext
                .getServiceEndpointInterface();
            method = getMostSpecificMethod(beanContext, method, callInterface);
            Object res = container.invoke(
                this.beanContext.getDeploymentID(),
                InterfaceType.SERVICE_ENDPOINT, callInterface, method,
                arguments, null);

            if (exchange.isOneWay()) {
                return null;
            }

            return new MessageContentsList(res);
        } catch (ApplicationException e) {
            // when no handler is defined, EjbInterceptor will directly delegate
            // to #directEjbInvoke. So if an application exception is thrown by
            // the end user, when must consider the ApplicationException as a
            // web fault if it contains the @WebFault exception
            Throwable t = e.getCause();
            if (t != null) {
                if (RuntimeException.class.isAssignableFrom(t.getClass())
                    && t.getClass().isAnnotationPresent(
                    jakarta.ejb.ApplicationException.class)) {
                    // it's not a checked exception so it can not be a WebFault
                    throw (RuntimeException) t;

                } else if (!t.getClass().isAnnotationPresent(WebFault.class)) {
                    // not a web fault even if it's an EJB ApplicationException
                    exchange.getInMessage().put(FaultMode.class,
                        FaultMode.UNCHECKED_APPLICATION_FAULT);
                    throw createFault(t, method, params, false);
                }

            } else { // may not occurs ...
                t = e;
            }
            // TODO may be we can change to FaultMode.CHECKED_APPLICATION_FAULT
            exchange.getInMessage().put(FaultMode.class,
                FaultMode.UNCHECKED_APPLICATION_FAULT);
            throw createFault(t, method, params, false);
        } catch (Exception e) {
            exchange.getInMessage().put(FaultMode.class,
                FaultMode.UNCHECKED_APPLICATION_FAULT);
            throw createFault(e, method, params, false);
        } finally {
            WebServiceContextImpl.clear();
        }
    }

    // seems the cxf impl is slow so caching it in BeanContext
    private Method getMostSpecificMethod(final BeanContext beanContext, final Method method, final Class callInterface) {
        MostSpecificMethodCache cache = beanContext.get(MostSpecificMethodCache.class);

        if (cache == null) {
            synchronized (beanContext) { // no need to use a lock IMO here
                cache = beanContext.get(MostSpecificMethodCache.class);
                if (cache == null) {
                    cache = new MostSpecificMethodCache();
                    beanContext.set(MostSpecificMethodCache.class, cache);
                }
            }
        }

        final MostSpecificMethodKey key = new MostSpecificMethodKey(callInterface, method);

        Method m = cache.methods.get(key);
        if (m == null) { // no need of more synchro since Method will be resolved to the same instance
            m = getMostSpecificMethod(method, callInterface);
            cache.methods.putIfAbsent(key, m);
        }

        return m;
    }

    public Object directEjbInvoke(Exchange exchange, Method m,
                                  List<Object> params) throws Exception {
        Object[] paramArray;
        if (params != null) {
            paramArray = params.toArray();
        } else {
            paramArray = new Object[]{};
        }
        return performInvocation(exchange, null, m, paramArray);
    }

    public static class MostSpecificMethodCache { // just a wrapper to put in BeanContext without conflict
        public final ConcurrentMap<MostSpecificMethodKey, Method> methods = new ConcurrentHashMap<MostSpecificMethodKey, Method>();
    }

    public static class MostSpecificMethodKey {
        public final Class<?> ejbInterface;
        public final Method method;
        private int hashCode;

        public MostSpecificMethodKey(final Class<?> ejbInterface, final Method method) {
            this.ejbInterface = ejbInterface;
            this.method = method;

            // this class exists for map usage so simply precalculate hashcode
            hashCode = ejbInterface != null ? ejbInterface.hashCode() : 0;
            hashCode = 31 * hashCode + (method != null ? method.hashCode() : 0);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final MostSpecificMethodKey that = (MostSpecificMethodKey) o;

            if (!(ejbInterface != null ? !ejbInterface.equals(that.ejbInterface) : that.ejbInterface != null)) {
                if (!(method != null ? !method.equals(that.method) : that.method != null)) {
                    return true;
                }
            }
            return false;

        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}