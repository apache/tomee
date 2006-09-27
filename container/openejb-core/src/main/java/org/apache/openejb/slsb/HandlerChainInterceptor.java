/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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