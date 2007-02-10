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
import org.apache.axis.Handler;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.RPCParam;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPFault;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.utils.JavaUtils;
import org.apache.openejb.ApplicationException;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.RpcContainer;
import org.xml.sax.SAXException;

import javax.xml.rpc.holders.IntHolder;
import javax.xml.soap.SOAPMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class EjbContainerProvider extends RPCProvider {

    private final DeploymentInfo ejbDeployment;

    public EjbContainerProvider(DeploymentInfo ejbDeployment) {
        this.ejbDeployment = ejbDeployment;
    }

    public void processMessage(MessageContext msgContext, SOAPEnvelope reqEnv, SOAPEnvelope resEnv, Object obj) throws Exception {

        RPCElement body = getBody(reqEnv, msgContext);
        OperationDesc operation = getOperationDesc(msgContext, body);

        AxisRpcInvocation invocation = new AxisRpcInvocation(operation, msgContext);
        SOAPMessage message = msgContext.getMessage();

        try {
            message.getSOAPPart().getEnvelope();
            msgContext.setProperty(org.apache.axis.SOAPPart.ALLOW_FORM_OPTIMIZATION, Boolean.FALSE);

            RpcContainer container = (RpcContainer) ejbDeployment.getContainer();
            Object result = container.invoke(ejbDeployment.getDeploymentID(), null, invocation.getArguments(), null, null);
            invocation.createResult(result);
        } catch (InvalidateReferenceException e) {
            invocation.createExceptionResult(e.getCause());
        } catch (ApplicationException e) {
            invocation.createExceptionResult(e.getCause());
        } catch (Throwable throwable) {
            throw new AxisFault("Web Service EJB Invocation failed: method " + operation.getMethod(), throwable);
        }
    }

    public Object getServiceObject(MessageContext msgContext, Handler service, String clsName, IntHolder scopeHolder) throws Exception {
        return ejbDeployment;
    }

    /**
     * This class is intentionally not static  or top level class
     * as it leverages logic in RPCProvider
     *
     * @see org.apache.axis.providers.java.RPCProvider
     */
    private class AxisRpcInvocation {

        private Map attributes = new HashMap();
        private OperationDesc operation;
        private MessageContext messageContext;

        public AxisRpcInvocation(OperationDesc operation, MessageContext msgContext) throws Exception {
            this.messageContext = msgContext;
            this.operation = operation;
        }

        public Object[] getArguments() {
            try {
                return demarshallArguments();
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Cannot demarshal the soap parts into arguments").initCause(e);
            }
        }

        private Object[] demarshallArguments() throws Exception {
            SOAPMessage message = messageContext.getMessage();
            messageContext.setProperty(org.apache.axis.SOAPPart.ALLOW_FORM_OPTIMIZATION, Boolean.TRUE);
            if (message != null) {
                message.saveChanges();
            }
            try {
                Message reqMsg = messageContext.getRequestMessage();
                SOAPEnvelope requestEnvelope = reqMsg.getSOAPEnvelope();
                RPCElement body = getBody(requestEnvelope, messageContext);
                body.setNeedDeser(true);
                Vector args = null;
                try {
                    args = body.getParams();
                } catch (SAXException e) {
                    if (e.getException() != null) {
                        throw e.getException();
                    }
                    throw e;
                }

                Object[] argValues = new Object[operation.getNumParams()];

                for (int i = 0; i < args.size(); i++) {
                    RPCParam rpcParam = (RPCParam) args.get(i);
                    Object value = rpcParam.getObjectValue();

                    ParameterDesc paramDesc = rpcParam.getParamDesc();

                    if (paramDesc != null && paramDesc.getJavaType() != null) {
                        value = JavaUtils.convert(value, paramDesc.getJavaType());
                        rpcParam.setObjectValue(value);
                    }
                    int order = (paramDesc == null || paramDesc.getOrder() == -1) ? i : paramDesc.getOrder();
                    argValues[order] = value;
                }
                return argValues;
            } finally {
                messageContext.setProperty(org.apache.axis.SOAPPart.ALLOW_FORM_OPTIMIZATION, Boolean.FALSE);
            }
        }

        public Object getId() {
            return null;
        }

        public void createResult(Object object) {
            messageContext.setPastPivot(true);
            try {
                Message requestMessage = messageContext.getRequestMessage();
                SOAPEnvelope requestEnvelope = requestMessage.getSOAPEnvelope();
                RPCElement requestBody = getBody(requestEnvelope, messageContext);

                Message responseMessage = messageContext.getResponseMessage();
                SOAPEnvelope responseEnvelope = responseMessage.getSOAPEnvelope();
                ServiceDesc serviceDescription = messageContext.getService().getServiceDescription();
                RPCElement responseBody = createResponseBody(requestBody, messageContext, operation, serviceDescription, object, responseEnvelope, getInOutParams());

                responseEnvelope.addBodyElement(responseBody);
            } catch (Exception e) {
                throw new RuntimeException("Failed while creating response message body", e);
            }
        }

        public void createExceptionResult(Throwable exception) {
            messageContext.setPastPivot(true);

            SOAPFault fault = new SOAPFault(new AxisFault("Server", "Server Error", null, null));
            SOAPEnvelope envelope = new SOAPEnvelope();
            envelope.addBodyElement(fault);
            Message message = new Message(envelope);
            message.setMessageType(Message.RESPONSE);
            messageContext.setResponseMessage(message);
        }

        public ArrayList getInOutParams() {
            return new ArrayList(); //TODO collect out an inout params in demarshalArguments
        }
    }
}
