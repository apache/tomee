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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.stateless;

import java.security.Principal;

import javax.ejb.SessionContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.handler.MessageContext;

import org.apache.openejb.core.ThreadContext;
import org.w3c.dom.Element;

public class EjbWsContext implements WebServiceContext {
    private SessionContext context;
    
    public EjbWsContext(SessionContext context) {
        this.context = context;
    }
    
    public MessageContext getMessageContext() {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        MessageContext messageContext = threadContext.get(MessageContext.class);
        if (messageContext == null) {
            throw new IllegalStateException("Only calls on the service-endpoint have a MessageContext.");
        }
        return messageContext;
    }

    public Principal getUserPrincipal() {
        return this.context.getCallerPrincipal();
    }

    public boolean isUserInRole(String roleName) {
        return this.context.isCallerInRole(roleName);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
        throw new UnsupportedOperationException("JaxWS 2.1 APIs are not supported");
    }
}
