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

package org.apache.openejb.slsb;

import java.util.List;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.soap.SOAPFaultException;

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.SimpleInvocationResult;
import org.apache.geronimo.webservices.MessageContextInvocationKey;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.StatelessEjbDeployment;

/**
 * @version $Revision$ $Date$
 */
public class HandlerChainInterceptor implements Interceptor {
    private final Interceptor next;

    public HandlerChainInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        MessageContext messageContext = (MessageContext) invocation.get(MessageContextInvocationKey.INSTANCE);

        if (messageContext == null) {
            return next.invoke(invocation);
        }

        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        EjbDeployment deployment = ejbInvocation.getEjbDeployment();
        if (!(deployment instanceof StatelessEjbDeployment)) {
            throw new IllegalArgumentException("HandlerChainInterceptor can only be used with a StatelessEjbDeploymentContext: " + deployment.getClass().getName());
        }

        List handlerInfos = ((StatelessEjbDeployment) deployment).getHandlerInfos();
        HandlerChain handlerChain = null; //TODO new org.apache.axis.handlers.HandlerChainImpl(handlerInfos);

        InvocationResult invocationResult;
        try {
            try {
                if (handlerChain.handleRequest(messageContext)) {
                    invocationResult = next.invoke(invocation);

                } else {
                    /* The Handler implementation class has the responsibility of setting
                     * the response SOAP message in the handleRequest method and perform
                     * additional processing in the handleResponse method.
                     */
                    invocationResult = new SimpleInvocationResult(true, null);
                }
            } catch (SOAPFaultException e) {
                handlerChain.handleFault(messageContext);
                return new SimpleInvocationResult(false, e);
            }

            handlerChain.handleResponse(messageContext);
        } finally {
            handlerChain.destroy();
        }

        return invocationResult;
    }
}