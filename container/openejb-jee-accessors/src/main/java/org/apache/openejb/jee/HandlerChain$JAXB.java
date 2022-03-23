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
package org.apache.openejb.jee;

import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.Handler$JAXB.readHandler;
import static org.apache.openejb.jee.Handler$JAXB.writeHandler;

@SuppressWarnings({
    "StringEquality"
})
public class HandlerChain$JAXB
    extends JAXBObject<HandlerChain> {


    public HandlerChain$JAXB() {
        super(HandlerChain.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "handler-chainType".intern()), Handler$JAXB.class);
    }

    public static HandlerChain readHandlerChain(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeHandlerChain(final XoXMLStreamWriter writer, final HandlerChain handlerChain, final RuntimeContext context)
        throws Exception {
        _write(writer, handlerChain, context);
    }

    public void write(final XoXMLStreamWriter writer, final HandlerChain handlerChain, final RuntimeContext context)
        throws Exception {
        _write(writer, handlerChain, context);
    }

    public final static HandlerChain _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final HandlerChain handlerChain = new HandlerChain();
        context.beforeUnmarshal(handlerChain, LifecycleCallback.NONE);

        List<String> protocolBindings = null;
        List<Handler> handler = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("handler-chainType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, HandlerChain.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, handlerChain);
                handlerChain.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("service-name-pattern" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceNamePattern
                final String serviceNamePatternRaw = elementReader.getElementAsString();

                final QName serviceNamePattern;
                try {
                    serviceNamePattern = Adapters.handlerChainsStringQNameAdapterAdapter.unmarshal(serviceNamePatternRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, HandlerChainsStringQNameAdapter.class, QName.class, QName.class, e);
                    continue;
                }

                handlerChain.serviceNamePattern = serviceNamePattern;
            } else if (("port-name-pattern" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: portNamePattern
                final String portNamePatternRaw = elementReader.getElementAsString();

                final QName portNamePattern;
                try {
                    portNamePattern = Adapters.handlerChainsStringQNameAdapterAdapter.unmarshal(portNamePatternRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, HandlerChainsStringQNameAdapter.class, QName.class, QName.class, e);
                    continue;
                }

                handlerChain.portNamePattern = portNamePattern;
            } else if (("protocol-bindings" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: protocolBindings
                for (final String protocolBindingsItem : elementReader.getElementAsXmlList()) {

                    final String protocolBindingsItem1;
                    try {
                        protocolBindingsItem1 = Adapters.collapsedStringAdapterAdapter.unmarshal(protocolBindingsItem);
                    } catch (final Exception e) {
                        context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                        continue;
                    }

                    if (protocolBindings == null) {
                        protocolBindings = handlerChain.protocolBindings;
                        if (protocolBindings != null) {
                            protocolBindings.clear();
                        } else {
                            protocolBindings = new ArrayList<String>();
                        }
                    }
                    protocolBindings.add(protocolBindingsItem1);
                }
            } else if (("handler" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: handler
                final Handler handlerItem = readHandler(elementReader, context);
                if (handler == null) {
                    handler = handlerChain.handler;
                    if (handler != null) {
                        handler.clear();
                    } else {
                        handler = new ArrayList<Handler>();
                    }
                }
                handler.add(handlerItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "service-name-pattern"), new QName("http://java.sun.com/xml/ns/javaee", "port-name-pattern"), new QName("http://java.sun.com/xml/ns/javaee", "protocol-bindings"), new QName("http://java.sun.com/xml/ns/javaee", "handler"));
            }
        }
        if (protocolBindings != null) {
            handlerChain.protocolBindings = protocolBindings;
        }
        if (handler != null) {
            handlerChain.handler = handler;
        }

        context.afterUnmarshal(handlerChain, LifecycleCallback.NONE);

        return handlerChain;
    }

    public final HandlerChain read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final HandlerChain handlerChain, RuntimeContext context)
        throws Exception {
        if (handlerChain == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (HandlerChain.class != handlerChain.getClass()) {
            context.unexpectedSubclass(writer, handlerChain, HandlerChain.class);
            return;
        }

        context.beforeMarshal(handlerChain, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = handlerChain.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(handlerChain, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: serviceNamePattern
        final QName serviceNamePatternRaw = handlerChain.serviceNamePattern;
        String serviceNamePattern = null;
        try {
            serviceNamePattern = Adapters.handlerChainsStringQNameAdapterAdapter.marshal(serviceNamePatternRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(handlerChain, "serviceNamePattern", HandlerChainsStringQNameAdapter.class, QName.class, QName.class, e);
        }
        if (serviceNamePattern != null) {
            writer.writeStartElement(prefix, "service-name-pattern", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serviceNamePattern);
            writer.writeEndElement();
        }

        // ELEMENT: portNamePattern
        final QName portNamePatternRaw = handlerChain.portNamePattern;
        String portNamePattern = null;
        try {
            portNamePattern = Adapters.handlerChainsStringQNameAdapterAdapter.marshal(portNamePatternRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(handlerChain, "portNamePattern", HandlerChainsStringQNameAdapter.class, QName.class, QName.class, e);
        }
        if (portNamePattern != null) {
            writer.writeStartElement(prefix, "port-name-pattern", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(portNamePattern);
            writer.writeEndElement();
        }

        // ELEMENT: protocolBindings
        final List<String> protocolBindingsRaw = handlerChain.protocolBindings;
        if (protocolBindingsRaw != null) {
            writer.writeStartElement(prefix, "protocol-bindings", "http://java.sun.com/xml/ns/javaee");
            boolean protocolBindingsFirst = true;
            for (final String protocolBindingsItem : protocolBindingsRaw) {
                String protocolBindings = null;
                try {
                    protocolBindings = Adapters.collapsedStringAdapterAdapter.marshal(protocolBindingsItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(handlerChain, "protocolBindings", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (protocolBindings != null) {
                    if (!protocolBindingsFirst) {
                        writer.writeCharacters(" ");
                    }
                    protocolBindingsFirst = false;
                    writer.writeCharacters(protocolBindings);
                }
            }
            writer.writeEndElement();
        }

        // ELEMENT: handler
        final List<Handler> handler = handlerChain.handler;
        if (handler != null) {
            for (final Handler handlerItem : handler) {
                if (handlerItem != null) {
                    writer.writeStartElement(prefix, "handler", "http://java.sun.com/xml/ns/javaee");
                    writeHandler(writer, handlerItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(handlerChain, "handler");
                }
            }
        }

        context.afterMarshal(handlerChain, LifecycleCallback.NONE);
    }

}
