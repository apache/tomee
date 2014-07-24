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
package org.apache.openejb.server.axis;

import org.apache.openejb.server.ServerRuntimeException;

import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of HandlerChain
 */
public class HandlerChainImpl extends ArrayList implements javax.xml.rpc.handler.HandlerChain {
    private String[] roles;
    private LinkedList<Handler> invokedHandlers = new LinkedList<Handler>();

    public HandlerChainImpl(final List<HandlerInfo> handlerInfos) {
        this(handlerInfos, null);
    }

    @SuppressWarnings({"unchecked"})
    public HandlerChainImpl(final List<HandlerInfo> handlerInfos, final String[] roles) {
        this.roles = roles;
        for (int i = 0; i < handlerInfos.size(); i++) {
            final HandlerInfo handlerInfo = handlerInfos.get(i);
            try {
                final Handler handler = (Handler) handlerInfo.getHandlerClass().newInstance();
                handler.init(handlerInfo);
                add(handler);
            } catch (final Exception e) {
                throw new JAXRPCException("Unable to initialize handler class: " + handlerInfo.getHandlerClass().getName(), e);
            }
        }
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(final String[] roles) {
        if (roles == null) {
            this.roles = new String[0];
        } else {
            this.roles = roles;
        }
    }

    public void init(final Map map) {
    }

    public void destroy() {
        for (final Iterator iterator = invokedHandlers.iterator(); iterator.hasNext(); ) {
            final Handler handler = (Handler) iterator.next();
            handler.destroy();
        }
        invokedHandlers.clear();
        clear();
    }

    public boolean handleRequest(final MessageContext context) {
        final MessageSnapshot snapshot = new MessageSnapshot(context);
        try {
            for (int i = 0; i < size(); i++) {
                final Handler currentHandler = (Handler) get(i);
                invokedHandlers.addFirst(currentHandler);
                try {
                    if (!currentHandler.handleRequest(context)) {
                        return false;
                    }
                } catch (final SOAPFaultException e) {
                    throw e;
                }
            }
        } finally {
            saveChanges(context);
        }

        if (!snapshot.equals(context)) {
            throw new IllegalStateException("The soap message operation or arguments were illegally modified by the HandlerChain");
        }
        return true;
    }

    public boolean handleResponse(final MessageContext context) {
        final MessageSnapshot snapshot = new MessageSnapshot(context);
        try {
            for (final Iterator iterator = invokedHandlers.iterator(); iterator.hasNext(); ) {
                final Handler handler = (Handler) iterator.next();
                if (!handler.handleResponse(context)) {
                    return false;
                }
            }
        } finally {
            saveChanges(context);
        }
        if (!snapshot.equals(context)) {
            throw new IllegalStateException("The soap message operation or arguments were illegally modified by the HandlerChain");
        }
        return true;
    }

    public boolean handleFault(final MessageContext context) {
        final MessageSnapshot snapshot = new MessageSnapshot(context);
        try {
            for (final Iterator iterator = invokedHandlers.iterator(); iterator.hasNext(); ) {
                final Handler handler = (Handler) iterator.next();
                if (!handler.handleFault(context)) {
                    return false;
                }
            }
        } finally {
            saveChanges(context);
        }
        if (!snapshot.equals(context)) {
            throw new IllegalStateException("The soap message operation or arguments were illegally modified by the HandlerChain");
        }
        return true;
    }

    private void saveChanges(final MessageContext context) {
        try {
            final SOAPMessage message = ((SOAPMessageContext) context).getMessage();
            if (message != null) {
                message.saveChanges();
            }
        } catch (final SOAPException e) {
            throw new ServerRuntimeException("Unable to save changes to SOAPMessage : " + e.toString());
        }
    }


    /**
     * Handlers cannot:
     * <p/>
     * - re-target a request to a different component.
     * - change the operation
     * - change the message part types
     * - change the number of message parts.
     */
    static class MessageSnapshot {
        private final String operationName;
        private final List<String> parameterNames;

        public MessageSnapshot(final MessageContext soapMessage) {
            final SOAPMessage message = ((SOAPMessageContext) soapMessage).getMessage();
            if (message == null || message.getSOAPPart() == null) {
                operationName = null;
                parameterNames = null;
            } else {
                final SOAPBody body = getBody(message);

                final SOAPElement operation = ((SOAPElement) body.getChildElements().next());
                this.operationName = operation.getElementName().toString();

                this.parameterNames = new ArrayList<String>();
                for (final Iterator i = operation.getChildElements(); i.hasNext(); ) {
                    final SOAPElement parameter = (SOAPElement) i.next();
                    final String element = parameter.getElementName().toString();
                    parameterNames.add(element);
                }
            }
        }

        private SOAPBody getBody(final SOAPMessage message) {
            try {
                return message.getSOAPPart().getEnvelope().getBody();
            } catch (final SOAPException e) {
                throw new ServerRuntimeException(e);
            }
        }

        public boolean equals(final Object obj) {
            return (obj instanceof SOAPMessageContext) && equals((SOAPMessageContext) obj);
        }

        private boolean equals(final SOAPMessageContext soapMessage) {
            final SOAPMessage message = soapMessage.getMessage();

            if (operationName == null) {
                return message == null || message.getSOAPPart() == null;
            }

            final SOAPBody body = getBody(message);

            // Handlers can't change the operation
            final SOAPElement operation = ((SOAPElement) body.getChildElements().next());
            if (!this.operationName.equals(operation.getElementName().toString())) {
                return false;
            }

            final Iterator parameters = operation.getChildElements();
            for (final Iterator i = parameterNames.iterator(); i.hasNext(); ) {
                // Handlers can't remove parameters
                if (!parameters.hasNext()) {
                    return false;
                }

                final String original = (String) i.next();
                final SOAPElement parameter = (SOAPElement) parameters.next();
                // Handlers can't change parameter types
                if (parameter == null || !original.equals(parameter.getElementName().toString())) {
                    return false;
                }
            }

            // Handlers can't add parameters
            return !parameters.hasNext();
        }
    }
}
