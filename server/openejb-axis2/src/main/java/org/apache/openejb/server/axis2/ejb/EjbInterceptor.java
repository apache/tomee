/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.openejb.server.axis2.ejb;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004_Constants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2006Constants;

import javax.interceptor.AroundInvoke;
import javax.xml.ws.Binding;

public class EjbInterceptor {
    private MessageContext requestMsgCtx;

    public EjbInterceptor(MessageContext requestCtx) {
        this.requestMsgCtx = requestCtx;
    }

    @AroundInvoke
    public Object intercept(javax.interceptor.InvocationContext invContext) throws Exception {
        AxisOperation operation = this.requestMsgCtx.getAxisMessageContext().getAxisOperation();
        String mep = operation.getMessageExchangePattern();

        EjbEndpointController controller = new EjbEndpointController(invContext);

        Binding binding = (Binding) this.requestMsgCtx.getAxisMessageContext().getProperty(JAXWSMessageReceiver.PARAM_BINDING);
        InvocationContext ic = InvocationContextFactory.createInvocationContext(binding);
        ic.setRequestMessageContext(this.requestMsgCtx);

        controller.invoke(ic);

        MessageContext responseMsgCtx = ic.getResponseMessageContext();

        //If there is a fault it could be Robust In-Only
        if (!isMepInOnly(mep) || hasFault(responseMsgCtx)) {
            // If this is a two-way exchange, there should already be a
            // JAX-WS MessageContext for the response.  We need to pull 
            // the Message data out of there and set it on the Axis2 
            // MessageContext.
            org.apache.axis2.context.MessageContext axisResponseMsgCtx = responseMsgCtx.getAxisMessageContext();

            MessageUtils.putMessageOnMessageContext(responseMsgCtx.getMessage(), axisResponseMsgCtx);

            OperationContext opCtx = axisResponseMsgCtx.getOperationContext();
            opCtx.addMessageContext(axisResponseMsgCtx);

            // If this is a fault message, we want to throw it as an
            // exception so that the transport can do the appropriate things
            if (responseMsgCtx.getMessage().isFault()) {
                throw new AxisFault("An error was detected during JAXWS processing", axisResponseMsgCtx);
            } else {
                //Create the AxisEngine for the reponse and send it.
                // todo this is wacky
                new AxisEngine(axisResponseMsgCtx.getConfigurationContext());
                AxisEngine.send(axisResponseMsgCtx);
            }
        }

        // TODO: convert response into object?
        return null;
    }

    private boolean hasFault(MessageContext responseMsgCtx) {
        return responseMsgCtx != null && responseMsgCtx.getMessage() != null && responseMsgCtx.getMessage().isFault();
    }

    private boolean isMepInOnly(String mep) {
        boolean inOnly = mep.equals(WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY) || 
                mep.equals(WSDL20_2004_Constants.MEP_URI_IN_ONLY) ||
                mep.equals(WSDL2Constants.MEP_URI_IN_ONLY) ||
                mep.equals(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY) ||
                mep.equals(WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY) ||
                mep.equals(WSDL20_2006Constants.MEP_URI_IN_ONLY);
        return inOnly;
    }

}
