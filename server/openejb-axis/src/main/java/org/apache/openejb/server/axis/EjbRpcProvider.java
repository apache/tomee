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
package org.apache.openejb.server.axis;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Handler;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.RPCParam;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPFault;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.utils.JavaUtils;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.server.ServerRuntimeException;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPMessage;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class EjbRpcProvider extends RPCProvider {

    private final BeanContext ejbDeployment;
    private final List<HandlerInfo> handlerInfos;

    public EjbRpcProvider(final BeanContext ejbDeployment) {
        this.ejbDeployment = ejbDeployment;
        this.handlerInfos = new ArrayList<HandlerInfo>();
    }

    public EjbRpcProvider(final BeanContext ejbDeployment, final List<HandlerInfo> handlerInfos) {
        this.ejbDeployment = ejbDeployment;
        this.handlerInfos = handlerInfos;
    }

    public void processMessage(final MessageContext msgContext, final SOAPEnvelope reqEnv, final SOAPEnvelope resEnv, final Object obj) throws Exception {

        final RPCElement body = getBody(reqEnv, msgContext);
        final OperationDesc operation = getOperationDesc(msgContext, body);

        final AxisRpcInterceptor interceptor = new AxisRpcInterceptor(operation, msgContext);
        final SOAPMessage message = msgContext.getMessage();

        try {
            message.getSOAPPart().getEnvelope();
            msgContext.setProperty(org.apache.axis.SOAPPart.ALLOW_FORM_OPTIMIZATION, Boolean.FALSE);

            final RpcContainer container = (RpcContainer) ejbDeployment.getContainer();

            final Object[] arguments = {msgContext, interceptor};

            final Class callInterface = ejbDeployment.getServiceEndpointInterface();
            final Object result = container.invoke(ejbDeployment.getDeploymentID(), InterfaceType.SERVICE_ENDPOINT, callInterface, operation.getMethod(), arguments, null);

            interceptor.createResult(result);
        } catch (final InvalidateReferenceException e) {
            interceptor.createExceptionResult(e.getCause());
        } catch (final ApplicationException e) {
            interceptor.createExceptionResult(e.getCause());
        } catch (final Throwable throwable) {
            throw new AxisFault("Web Service EJB Invocation failed: method " + operation.getMethod(), throwable);
        }
    }

    public Object getServiceObject(final MessageContext msgContext, final Handler service, final String clsName, final IntHolder scopeHolder) throws Exception {
        return ejbDeployment;
    }

    /**
     * This class is intentionally not static  or top level class
     * as it leverages logic in RPCProvider
     *
     * @see org.apache.axis.providers.java.RPCProvider
     */
    public class AxisRpcInterceptor {

        private OperationDesc operation;
        private MessageContext messageContext;

        public AxisRpcInterceptor(final OperationDesc operation, final MessageContext msgContext) throws Exception {
            this.messageContext = msgContext;
            this.operation = operation;
        }

        @AroundInvoke
        public Object intercept(final InvocationContext context) throws Exception {
            final HandlerChain handlerChain = new HandlerChainImpl(handlerInfos);
            try {
                Object invocationResult = null;

                try {
                    if (handlerChain.handleRequest(messageContext)) {
                        // update arguments as handlers could change the soap msg
                        context.setParameters(getArguments());

                        invocationResult = context.proceed();

                        // update the soap msg so that handlers see invocation result
                        if (!handlerChain.isEmpty()) {
                            createResult(invocationResult);
                        }

                    } else {
                        /* The Handler implementation class has the responsibility of setting
                         * the response SOAP message in the handleRequest method and perform
                         * additional processing in the handleResponse method.
                         */
                        invocationResult = null;
                    }
                } catch (final SOAPFaultException e) {
                    handlerChain.handleFault(messageContext);
                    throw e;
                }

                handlerChain.handleResponse(messageContext);

                if (!handlerChain.isEmpty()) {
                    /*
                     * Deserialize the result value from soap msg as handers could have
                     * changed it.
                     */
                    try {
                        invocationResult = demarshallResult();
                    } catch (final Exception e) {
                        // if this fails, return invocationResult from above
                    }
                }

                return invocationResult;
            } finally {
                handlerChain.destroy();
            }
        }

        public Object[] getArguments() {
            try {
                return demarshallArguments();
            } catch (final Exception e) {
                throw (IllegalStateException) new IllegalStateException("Cannot demarshal the soap parts into arguments").initCause(e);
            }
        }

        private Object[] demarshallArguments() throws Exception {
            final SOAPMessage message = messageContext.getMessage();
            messageContext.setProperty(org.apache.axis.SOAPPart.ALLOW_FORM_OPTIMIZATION, Boolean.TRUE);
            if (message != null) {
                message.saveChanges();
            }

            try {
                final Message reqMsg = messageContext.getRequestMessage();
                final SOAPEnvelope requestEnvelope = reqMsg.getSOAPEnvelope();
                final RPCElement body = getBody(requestEnvelope, messageContext);
                body.setNeedDeser(true);
                Vector args = null;
                try {
                    args = body.getParams();
                } catch (final SAXException e) {
                    if (e.getException() != null) {
                        throw e.getException();
                    }
                    throw e;
                }

                final Object[] argValues = new Object[operation.getNumParams()];

                for (int i = 0; i < args.size(); i++) {
                    final RPCParam rpcParam = (RPCParam) args.get(i);
                    Object value = rpcParam.getObjectValue();

                    final ParameterDesc paramDesc = rpcParam.getParamDesc();

                    if (paramDesc != null && paramDesc.getJavaType() != null) {
                        value = JavaUtils.convert(value, paramDesc.getJavaType());
                        rpcParam.setObjectValue(value);
                    }
                    final int order = (paramDesc == null || paramDesc.getOrder() == -1) ? i : paramDesc.getOrder();
                    argValues[order] = value;
                }
                return argValues;
            } finally {
                messageContext.setProperty(org.apache.axis.SOAPPart.ALLOW_FORM_OPTIMIZATION, Boolean.FALSE);
            }
        }

        private Object demarshallResult() throws Exception {
            final Message resMsg = messageContext.getResponseMessage();

            /*
             * This is not the most efficient way to deserialize the result
             * but could not find better or more reliable way to do this.
             */
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            resMsg.writeTo(out);
            final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            final DeserializationContext dser =
                new DeserializationContext(new InputSource(in), resMsg.getMessageContext(), null);
            dser.parse();
            final SOAPEnvelope responseEnvelope = dser.getEnvelope();

            final SOAPBodyElement bodyEl = responseEnvelope.getFirstBody();
            if (bodyEl == null) {
                return null;
            }

            final QName returnType = operation.getReturnType();
            if (XMLType.AXIS_VOID.equals(returnType)) {
                return null;
            }

            Object result = null;

            if (bodyEl instanceof RPCElement) {
                final RPCElement body = (RPCElement) bodyEl;
                body.setNeedDeser(true);
                Vector args = null;
                try {
                    args = body.getParams();
                } catch (final SAXException e) {
                    if (e.getException() != null) {
                        throw e.getException();
                    }
                    throw e;
                }

                final QName returnParamQName = operation.getReturnQName();
                if (args != null && args.size() > 0) {

                    if (returnParamQName == null) {
                        final RPCParam param = (RPCParam) args.get(0);
                        result = param.getObjectValue();
                    } else {
                        for (int i = 0; i < args.size(); i++) {
                            final RPCParam param = (RPCParam) args.get(i);
                            if (returnParamQName.equals(param.getQName())) {
                                result = param.getObjectValue();
                                break;
                            }
                        }
                    }

                }
            } else {
                try {
                    result = bodyEl.getValueAsType(returnType);
                } catch (final Exception e) {
                    result = bodyEl;
                }
            }

            if (operation.getReturnClass() != null) {
                result = JavaUtils.convert(result, operation.getReturnClass());
            }

            return result;
        }

        public void createResult(final Object object) {
            messageContext.setPastPivot(true);
            try {
                final Message requestMessage = messageContext.getRequestMessage();
                final SOAPEnvelope requestEnvelope = requestMessage.getSOAPEnvelope();
                final RPCElement requestBody = getBody(requestEnvelope, messageContext);

                final Message responseMessage = messageContext.getResponseMessage();
                final SOAPEnvelope responseEnvelope = responseMessage.getSOAPEnvelope();
                final ServiceDesc serviceDescription = messageContext.getService().getServiceDescription();
                final RPCElement responseBody = createResponseBody(requestBody, messageContext, operation, serviceDescription, object, responseEnvelope, getInOutParams());

                responseEnvelope.removeBody();
                responseEnvelope.addBodyElement(responseBody);
            } catch (final Exception e) {
                throw new ServerRuntimeException("Failed while creating response message body", e);
            }
        }

        public void createExceptionResult(final Throwable exception) {
            messageContext.setPastPivot(true);

            AxisFault axisFault = null;
            if (exception instanceof Exception) {
                axisFault = AxisFault.makeFault((Exception) exception);
                axisFault.setFaultCodeAsString(Constants.FAULT_SERVER_GENERAL);
            } else {
                axisFault = new AxisFault("Server", "Server Error", null, null);
            }

            final SOAPFault fault = new SOAPFault(axisFault);
            final SOAPEnvelope envelope = new SOAPEnvelope();
            envelope.addBodyElement(fault);
            final Message message = new Message(envelope);
            message.setMessageType(Message.RESPONSE);
            messageContext.setResponseMessage(message);
        }

        public ArrayList getInOutParams() {
            return new ArrayList(); //TODO collect out an inout params in demarshalArguments
        }
    }

}
