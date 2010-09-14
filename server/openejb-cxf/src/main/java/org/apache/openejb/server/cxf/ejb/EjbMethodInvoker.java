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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.interceptor.InvocationContext;
import javax.xml.ws.WebFault;
import javax.xml.ws.handler.MessageContext.Scope;

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

public class EjbMethodInvoker extends AbstractJAXWSMethodInvoker {

    private static final Logger log = Logger.getInstance(LogCategory.CXF, EjbMethodInvoker.class);

    private static final String HANDLER_PROPERTIES = "HandlerProperties";

    private BeanContext beanContext;
    private Bus bus;

    public EjbMethodInvoker(Bus bus, BeanContext beanContext) {
        super((Factory) null);
        this.bus = bus;
        this.beanContext = beanContext;
    }

    public Object getServiceObject(Exchange context) {
        return null;
    }

    public void releaseServiceObject(Exchange ex, Object obj) {
        // do nothing
    }

    protected Object invoke(Exchange exchange, Object serviceObject, Method m,
            List<Object> params) {
        Object result = null;

        InvocationContext invContext = exchange.get(InvocationContext.class);
        if (invContext == null) {
            log.debug("PreEJBInvoke");
            result = preEjbInvoke(exchange, serviceObject, m, params);
        } else {
            log.debug("EJBInvoke"); // calls performInvocation()
            result = super.invoke(exchange, serviceObject, m, params);
        }

        return result;
    }

    protected Object performInvocation(Exchange exchange, Object serviceObject,
            Method m, Object[] paramArray) throws Exception {
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

    private Object preEjbInvoke(Exchange exchange, Object serviceObject,
            Method method, List<Object> params) {

        EjbMessageContext ctx = new EjbMessageContext(exchange.getInMessage(),
                Scope.APPLICATION);
        WebServiceContextImpl.setMessageContext(ctx);

        Map<String, Object> handlerProperties = removeHandlerProperties(ctx);
        exchange.put(HANDLER_PROPERTIES, handlerProperties);

        try {
            EjbInterceptor interceptor = new EjbInterceptor(params, method,
                    this.bus, exchange);
            Object[] arguments = { ctx, interceptor };

            RpcContainer container = (RpcContainer) this.beanContext
                    .getContainer();

            Class callInterface = this.beanContext
                    .getServiceEndpointInterface();
            method = getMostSpecificMethod(method, callInterface);
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
                if (t.getClass().isAssignableFrom(RuntimeException.class)
                        && t.getClass().isAnnotationPresent(
                                javax.ejb.ApplicationException.class)) {
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

    public Object directEjbInvoke(Exchange exchange, Method m,
            List<Object> params) throws Exception {
        Object[] paramArray;
        if (params != null) {
            paramArray = params.toArray();
        } else {
            paramArray = new Object[] {};
        }
        return performInvocation(exchange, null, m, paramArray);
    }
}