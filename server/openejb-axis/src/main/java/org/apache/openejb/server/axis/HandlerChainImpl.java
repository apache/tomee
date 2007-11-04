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

    public HandlerChainImpl(List<HandlerInfo> handlerInfos) {
        this(handlerInfos, null);
    }

    @SuppressWarnings({"unchecked"})
    public HandlerChainImpl(List<HandlerInfo> handlerInfos, String[] roles) {
        this.roles = roles;
        for (int i = 0; i < handlerInfos.size(); i++) {
            HandlerInfo handlerInfo = handlerInfos.get(i);
            try {
                Handler handler = (Handler) handlerInfo.getHandlerClass().newInstance();
                handler.init(handlerInfo);
                add(handler);
            } catch (Exception e) {
                throw new JAXRPCException("Unable to initialize handler class: " + handlerInfo.getHandlerClass().getName(), e);
            }
        }
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        if (roles == null) {
            this.roles = new String[0];
        } else {
            this.roles = roles;
        }
    }

    public void init(Map map) {
    }

    public void destroy() {
        for (Iterator iterator = invokedHandlers.iterator(); iterator.hasNext();) {
            Handler handler = (Handler) iterator.next();
            handler.destroy();
        }
        invokedHandlers.clear();
        clear();
    }

    public boolean handleRequest(MessageContext context) {
        MessageSnapshot snapshot = new MessageSnapshot(context);
        try {
            for (int i = 0; i < size(); i++) {
                Handler currentHandler = (Handler) get(i);
                invokedHandlers.addFirst(currentHandler);
                try {
                    if (!currentHandler.handleRequest(context)) {
                        return false;
                    }
                } catch (SOAPFaultException e) {
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

    public boolean handleResponse(MessageContext context) {
        MessageSnapshot snapshot = new MessageSnapshot(context);
        try {
            for (Iterator iterator = invokedHandlers.iterator(); iterator.hasNext();) {
                Handler handler = (Handler) iterator.next();
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

    public boolean handleFault(MessageContext context) {
        MessageSnapshot snapshot = new MessageSnapshot(context);
        try {
            for (Iterator iterator = invokedHandlers.iterator(); iterator.hasNext();) {
                Handler handler = (Handler) iterator.next();
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

    private void saveChanges(MessageContext context) {
        try {
            SOAPMessage message = ((SOAPMessageContext) context).getMessage();
            if (message != null) {
                message.saveChanges();
            }
        } catch (SOAPException e) {
            throw new RuntimeException("Unable to save changes to SOAPMessage : " + e.toString());
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

        public MessageSnapshot(MessageContext soapMessage) {
            SOAPMessage message = ((SOAPMessageContext) soapMessage).getMessage();
            if (message == null || message.getSOAPPart() == null) {
                operationName = null;
                parameterNames = null;
            } else {
                SOAPBody body = getBody(message);

                SOAPElement operation = ((SOAPElement) body.getChildElements().next());
                this.operationName = operation.getElementName().toString();

                this.parameterNames = new ArrayList<String>();
                for (Iterator i = operation.getChildElements(); i.hasNext();) {
                    SOAPElement parameter = (SOAPElement) i.next();
                    String element = parameter.getElementName().toString();
                    parameterNames.add(element);
                }
            }
        }

        private SOAPBody getBody(SOAPMessage message) {
            try {
                return message.getSOAPPart().getEnvelope().getBody();
            } catch (SOAPException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean equals(Object obj) {
            return (obj instanceof SOAPMessageContext) && equals((SOAPMessageContext) obj);
        }

        private boolean equals(SOAPMessageContext soapMessage) {
            SOAPMessage message = soapMessage.getMessage();

            if (operationName == null) {
                return message == null || message.getSOAPPart() == null;
            }

            SOAPBody body = getBody(message);

            // Handlers can't change the operation
            SOAPElement operation = ((SOAPElement) body.getChildElements().next());
            if (!this.operationName.equals(operation.getElementName().toString())) {
                return false;
            }

            Iterator parameters = operation.getChildElements();
            for (Iterator i = parameterNames.iterator(); i.hasNext();) {
                // Handlers can't remove parameters
                if (!parameters.hasNext()) {
                    return false;
                }

                String original = (String) i.next();
                SOAPElement parameter = (SOAPElement) parameters.next();
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
