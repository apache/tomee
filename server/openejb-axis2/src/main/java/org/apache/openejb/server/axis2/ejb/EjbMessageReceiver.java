/**
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

package org.apache.openejb.server.axis2.ejb;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.handler.SoapMessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.xml.ws.Provider;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class EjbMessageReceiver implements MessageReceiver {
    private static final Logger logger = Logger.getInstance(LogCategory.AXIS2, EjbMessageReceiver.class);

    private DeploymentInfo deploymentInfo;
    private Class serviceImplClass;
    private EjbWsContainer container;

    public EjbMessageReceiver(EjbWsContainer container, Class serviceImplClass, DeploymentInfo deploymentInfo) {
        this.container = container;
        this.serviceImplClass = serviceImplClass;
        this.deploymentInfo = deploymentInfo;
    }

    public void receive(org.apache.axis2.context.MessageContext axisMsgCtx) throws AxisFault {
        MessageContext requestMsgCtx = new MessageContext(axisMsgCtx);

        // init some bits
        requestMsgCtx.setOperationName(requestMsgCtx.getAxisMessageContext().getAxisOperation().getName());
        requestMsgCtx.setEndpointDescription(getEndpointDescription(requestMsgCtx));

        Method method = null;
        if (Provider.class.isAssignableFrom(this.serviceImplClass)) {
            method = getProviderMethod();
        } else {
            requestMsgCtx.setOperationDescription(getOperationDescription(requestMsgCtx));
            method = getServiceMethod(requestMsgCtx);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Invoking '" + method.getName() + "' method.");
        }

        EjbInterceptor interceptor = new EjbInterceptor(requestMsgCtx);

        SoapMessageContext jaxwsContext = new SoapMessageContext(requestMsgCtx);
        Object[] arguments = {jaxwsContext, interceptor};

        RpcContainer container = (RpcContainer) this.deploymentInfo.getContainer();

        Class callInterface = this.deploymentInfo.getServiceEndpointInterface();

        method = getMostSpecificMethod(method, callInterface);

        try {
            Object res = container.invoke(this.deploymentInfo.getDeploymentID(), callInterface, method, arguments, null);
            // TODO: update response message with new response value?
        } catch (ApplicationException e) {
            if (e.getCause() instanceof AxisFault) {
                throw (AxisFault) e.getCause();
            } else {
                throw AxisFault.makeFault(e);
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
        if (method != null && targetClass != null) {
            try {
                method = targetClass.getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException ex) {
                // Perhaps the target class doesn't implement this method:
                // that's fine, just use the original method
            }
        }
        return method;
    }

    private Method getServiceMethod(MessageContext mc) {
        OperationDescription opDesc = mc.getOperationDescription();
        if (opDesc == null) {
            throw ExceptionFactory.makeWebServiceException("Operation Description was not set");
        }

        Method returnMethod = opDesc.getMethodFromServiceImpl(this.serviceImplClass);
        if (returnMethod == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("JavaBeanDispatcherErr1"));
        }

        return returnMethod;
    }

    private OperationDescription getOperationDescription(MessageContext mc) {
        EndpointDescription ed = mc.getEndpointDescription();
        EndpointInterfaceDescription eid = ed.getEndpointInterfaceDescription();

        OperationDescription[] ops = eid.getDispatchableOperation(mc.getOperationName());
        if (ops == null || ops.length == 0) {
            throw ExceptionFactory.makeWebServiceException("No operation found.  WSDL Operation name: " + mc.getOperationName());
        }
        if (ops.length > 1) {
            throw ExceptionFactory.makeWebServiceException("More than one operation found. Overloaded WSDL operations are not supported.  WSDL Operation name: " + mc.getOperationName());
        }
        OperationDescription op = ops[0];
        return op;
    }

    private EndpointDescription getEndpointDescription(MessageContext mc) {
        AxisService axisSvc = mc.getAxisMessageContext().getAxisService();

        Parameter param = axisSvc.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER);

        EndpointDescription ed = (EndpointDescription) param.getValue();
        return ed;
    }

    private Method getProviderMethod() {
        try {
            return this.serviceImplClass.getMethod("invoke", getProviderType());
        } catch (NoSuchMethodException e) {
            throw ExceptionFactory.makeWebServiceException("Could not get Provider.invoke() method");
        }
    }

    private Class<?> getProviderType() {
        Class providerType = null;

        Type[] giTypes = this.serviceImplClass.getGenericInterfaces();
        for (Type giType : giTypes) {
            ParameterizedType paramType = null;
            try {
                paramType = (ParameterizedType) giType;
            } catch (ClassCastException e) {
                throw ExceptionFactory.makeWebServiceException("Provider based SEI Class has to implement javax.xml.ws.Provider as javax.xml.ws.Provider<String>, javax.xml.ws.Provider<SOAPMessage>, javax.xml.ws.Provider<Source> or javax.xml.ws.Provider<JAXBContext>");
            }
            Class interfaceName = (Class) paramType.getRawType();

            if (interfaceName == javax.xml.ws.Provider.class) {
                if (paramType.getActualTypeArguments().length > 1) {
                    throw ExceptionFactory.makeWebServiceException("Provider cannot have more than one Generic Types defined as Per JAX-WS Specification");
                }
                providerType = (Class) paramType.getActualTypeArguments()[0];
            }
        }
        return providerType;
    }
}
