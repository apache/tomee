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

package org.apache.openejb.core.singleton;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.webservices.AddressingSupport;
import org.w3c.dom.Element;

import jakarta.ejb.SessionContext;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import java.security.Principal;

public class EjbWsContext implements WebServiceContext {
    private final SessionContext context;

    public EjbWsContext(final SessionContext context) {
        this.context = context;
    }

    public MessageContext getMessageContext() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final MessageContext messageContext = threadContext.get(MessageContext.class);
        if (messageContext == null) {
            throw new IllegalStateException("Only calls on the service-endpoint have a MessageContext.");
        }
        return messageContext;
    }

    public Principal getUserPrincipal() {
        return this.context.getCallerPrincipal();
    }

    public boolean isUserInRole(final String roleName) {
        return this.context.isCallerInRole(roleName);
    }

    private AddressingSupport getAddressingSupport() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final AddressingSupport wsaSupport = threadContext.get(AddressingSupport.class);
        if (wsaSupport == null) {
            throw new IllegalStateException("Only calls on the service-endpoint can get the EndpointReference.");
        }
        return wsaSupport;
    }

    public EndpointReference getEndpointReference(final Element... referenceParameters) {
        return getAddressingSupport().getEndpointReference(referenceParameters);
    }

    public <T extends EndpointReference> T getEndpointReference(final Class<T> clazz, final Element... referenceParameters) {
        return getAddressingSupport().getEndpointReference(clazz, referenceParameters);
    }
}
