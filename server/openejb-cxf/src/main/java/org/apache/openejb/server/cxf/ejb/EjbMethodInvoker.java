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
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.support.ContextPropertiesMapping;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.FaultMode;
import org.apache.cxf.service.invoker.AbstractInvoker;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.interceptor.InvocationContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EjbMethodInvoker extends AbstractInvoker {
    private static final Logger log = Logger.getInstance(LogCategory.CXF, EjbMethodInvoker.class);

    private DeploymentInfo deploymentInfo;
    private Bus bus;

    public EjbMethodInvoker(Bus bus, DeploymentInfo deploymentInfo) {
        this.bus = bus;
        this.deploymentInfo = deploymentInfo;
    }

    public Object getServiceObject(Exchange context) {
        return null;
    }

    protected Object invoke(Exchange exchange, Object serviceObject, Method m, List<Object> params) {
        Object result = null;

        InvocationContext invContext = exchange.get(InvocationContext.class);
        if (invContext == null) {
            log.debug("PreEJBInvoke");
            result = preEjbInvoke(exchange, m, params);
        } else {
            log.debug("EJBInvoke");
            result = ejbInvoke(exchange, m, params);
        }

        return result;
    }

    private Object preEjbInvoke(Exchange exchange, Method method, List<Object> params) {

        MessageContext ctx = ContextPropertiesMapping.createWebServiceContext(exchange);
        WebServiceContextImpl.setMessageContext(ctx);

        try {
            EjbInterceptor interceptor = new EjbInterceptor(params, method, this.bus, exchange);
            Object[] arguments = {ctx, interceptor};

            RpcContainer container = (RpcContainer) this.deploymentInfo.getContainer();

            Class callInterface = this.deploymentInfo.getServiceEndpointInterface();
            method = getMostSpecificMethod(method, callInterface);
            Object res = container.invoke(this.deploymentInfo.getDeploymentID(), callInterface, method, arguments, null);

            if (exchange.isOneWay()) {
                return null;
            }

            List<Object> retList = new ArrayList<Object>(1);
            if (!method.getReturnType().getName().equals("void")) {
                retList.add(res);
            }

            return retList;
        } catch (Exception e) {
            exchange.getInMessage().put(FaultMode.class, FaultMode.UNCHECKED_APPLICATION_FAULT);
            throw new Fault(e);
        } finally {
            WebServiceContextImpl.clear();
        }
    }

    private Object ejbInvoke(Exchange exchange, Method m, List<Object> params) {
        try {
            Object res = directEjbInvoke(exchange, m, params);

            if (exchange.isOneWay()) {
                return null;
            }

            List<Object> retList = new ArrayList<Object>(1);
            if (!m.getReturnType().getName().equals("void")) {
                retList.add(res);
            }

            return retList;
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t == null) {
                t = e;
            }
            exchange.getInMessage().put(FaultMode.class, FaultMode.CHECKED_APPLICATION_FAULT);
            throw new Fault(t);
        } catch (Exception e) {
            exchange.getInMessage().put(FaultMode.class, FaultMode.UNCHECKED_APPLICATION_FAULT);
            throw new Fault(e);
        }
    }

    public Object directEjbInvoke(Exchange exchange, Method m, List<Object> params) throws Exception {
        InvocationContext invContext = exchange.get(InvocationContext.class);
        Object[] paramArray;
        if (params != null) {
            paramArray = params.toArray();
        } else {
            paramArray = new Object[]{};
        }

        invContext.setParameters(paramArray);
        Object res = invContext.proceed();

        ContextPropertiesMapping.updateWebServiceContext(exchange, (MessageContext) invContext.getContextData());

        return res;
    }
}
