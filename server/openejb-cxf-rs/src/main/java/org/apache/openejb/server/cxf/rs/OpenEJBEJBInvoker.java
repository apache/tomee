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

import java.rmi.RemoteException;
import javax.ejb.EJBLocalHome;
import javax.xml.ws.WebFault;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxrs.JAXRSInvoker;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.FaultMode;
import org.apache.cxf.message.MessageContentsList;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.managed.ManagedContainer;
import org.apache.openejb.rest.ThreadLocalContextManager;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class OpenEJBEJBInvoker extends JAXRSInvoker {
    private BeanContext context;

    public OpenEJBEJBInvoker(BeanContext beanContext) {
        context = beanContext;
    }

    @Override public Object invoke(Exchange exchange, Object request, Object resourceObject) {
        final OperationResourceInfo ori = exchange.get(OperationResourceInfo.class);
        final ClassResourceInfo cri = ori.getClassResourceInfo();
        final Method method = cri.getMethodDispatcher().getMethod(ori);
        final RpcContainer container = RpcContainer.class.cast(context.getContainer());

        Object[] parameters;
        if (request instanceof List) {
            List<Object> params = CastUtils.cast((List<?>) request);
            parameters = params.toArray(new Object[params.size()]);
        } else if (request != null) {
            List<Object> params = new MessageContentsList(request);
            parameters = params.toArray(new Object[params.size()]);
        } else {
            parameters = new Object[0];
        }

        // injecting context parameters
        super.insertExchange(method, parameters, exchange);

        // binding context fields
        for (Field field : cri.getContextFields()) {
            Class<?> type = field.getType();
            if (Request.class.equals(type)) {
                Request binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, Request.class);
                ThreadLocalContextManager.REQUEST.set(binding);
            } else if (UriInfo.class.equals(type)) {
                UriInfo binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, UriInfo.class);
                ThreadLocalContextManager.URI_INFO.set(binding);
            } else if (HttpHeaders.class.equals(type)) {
                HttpHeaders binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, HttpHeaders.class);
                ThreadLocalContextManager.HTTP_HEADERS.set(binding);
            } else if (SecurityContext.class.equals(type)) {
                SecurityContext binding = JAXRSUtils.createContextValue(exchange.getInMessage(), null, SecurityContext.class);
                ThreadLocalContextManager.SECURITY_CONTEXT.set(binding);
            } else if (ContextResolver.class.equals(type)) {
                ContextResolver<?> binding = JAXRSUtils.createContextValue(exchange.getInMessage(), type, ContextResolver.class);
                ThreadLocalContextManager.CONTEXT_RESOLVER.set(binding);
            }
        }

        // invoking the EJB
        final boolean  createAndDestroy = container instanceof ManagedContainer;
        Object primKey = null;
        try {
            if (createAndDestroy) {
                Method create = null;
                try {
                    create = BeanContext.BusinessLocalBeanHome.class.getMethod("create");
                } catch (NoSuchMethodException e) {
                    // shouldn't occur
                }

                primKey = ((ProxyInfo) container.invoke(context.getDeploymentID(),
                        InterfaceType.BUSINESS_LOCALBEAN_HOME,
                        create.getDeclaringClass(), create, null, null)).getPrimaryKey();
            }

            Object result = container.invoke(context.getDeploymentID(),
                context.getInterfaceType(method.getDeclaringClass()),
                method.getDeclaringClass(), method, parameters, primKey);

            if (createAndDestroy && !context.getRemoveMethods().isEmpty()) {
                // should we cache such information?
                final Method remove = context.getRemoveMethods().iterator().next();
                container.invoke(context.getDeploymentID(),
                        context.getInterfaceType(remove.getDeclaringClass()),
                        remove.getDeclaringClass(), remove, null, primKey);
            }

            return new MessageContentsList(result);
        } catch (OpenEJBException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RemoteException && cause.getCause() != null) {
                cause = cause.getCause();
            }

            final Response excResponse = JAXRSUtils.convertFaultToResponse(cause, exchange.getInMessage());
            return new MessageContentsList(excResponse);
        } finally {
            ThreadLocalContextManager.reset();
        }
    }

    @Override public Object getServiceObject(Exchange exchange) {
        return null;
    }
}
