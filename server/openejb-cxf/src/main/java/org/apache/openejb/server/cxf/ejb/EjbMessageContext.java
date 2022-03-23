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
package org.apache.openejb.server.cxf.ejb;

import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;
import jakarta.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.apache.openejb.core.webservices.AddressingSupport;
import org.w3c.dom.Element;

public class EjbMessageContext extends WrappedMessageContext implements AddressingSupport {

    public EjbMessageContext(Message m, Scope defScope) {
        super(m, defScope);
    }

    public EndpointReference getEndpointReference(Element... referenceParameters) {
        org.apache.cxf.message.Message msg = getWrappedMessage();
        Endpoint ep = msg.getExchange().get(Endpoint.class);

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(ep.getEndpointInfo().getAddress());
        builder.serviceName(ep.getService().getName());
        builder.endpointName(ep.getEndpointInfo().getName());

        if (referenceParameters != null) {
            for (Element referenceParameter : referenceParameters) {
                builder.referenceParameter(referenceParameter);
            }
        }

        return builder.build();
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
        if (W3CEndpointReference.class.isAssignableFrom(clazz)) {
            return clazz.cast(getEndpointReference(referenceParameters));
        } else {
            throw new WebServiceException("Endpoint reference type not supported: " + clazz);
        }
    }

}
