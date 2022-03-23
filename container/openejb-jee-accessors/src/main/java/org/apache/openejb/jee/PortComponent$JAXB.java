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

import static org.apache.openejb.jee.Addressing$JAXB.readAddressing;
import static org.apache.openejb.jee.Addressing$JAXB.writeAddressing;
import static org.apache.openejb.jee.Handler$JAXB.readHandler;
import static org.apache.openejb.jee.Handler$JAXB.writeHandler;
import static org.apache.openejb.jee.HandlerChains$JAXB.readHandlerChains;
import static org.apache.openejb.jee.HandlerChains$JAXB.writeHandlerChains;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.RespectBinding$JAXB.readRespectBinding;
import static org.apache.openejb.jee.RespectBinding$JAXB.writeRespectBinding;
import static org.apache.openejb.jee.ServiceImplBean$JAXB.readServiceImplBean;
import static org.apache.openejb.jee.ServiceImplBean$JAXB.writeServiceImplBean;

@SuppressWarnings({
    "StringEquality"
})
public class PortComponent$JAXB
    extends JAXBObject<PortComponent> {


    public PortComponent$JAXB() {
        super(PortComponent.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "port-componentType".intern()), Icon$JAXB.class, Addressing$JAXB.class, RespectBinding$JAXB.class, ServiceImplBean$JAXB.class, Handler$JAXB.class, HandlerChains$JAXB.class);
    }

    public static PortComponent readPortComponent(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writePortComponent(final XoXMLStreamWriter writer, final PortComponent portComponent, final RuntimeContext context)
        throws Exception {
        _write(writer, portComponent, context);
    }

    public void write(final XoXMLStreamWriter writer, final PortComponent portComponent, final RuntimeContext context)
        throws Exception {
        _write(writer, portComponent, context);
    }

    public final static PortComponent _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final PortComponent portComponent = new PortComponent();
        context.beforeUnmarshal(portComponent, LifecycleCallback.NONE);

        List<Handler> handler = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("port-componentType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, PortComponent.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, portComponent);
                portComponent.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: description
                final String descriptionRaw = elementReader.getElementAsString();

                final String description;
                try {
                    description = Adapters.collapsedStringAdapterAdapter.unmarshal(descriptionRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                portComponent.description = description;
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayName
                final String displayNameRaw = elementReader.getElementAsString();

                final String displayName;
                try {
                    displayName = Adapters.collapsedStringAdapterAdapter.unmarshal(displayNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                portComponent.displayName = displayName;
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                final Icon icon = readIcon(elementReader, context);
                portComponent.icon = icon;
            } else if (("port-component-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: portComponentName
                final String portComponentNameRaw = elementReader.getElementAsString();

                final String portComponentName;
                try {
                    portComponentName = Adapters.collapsedStringAdapterAdapter.unmarshal(portComponentNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                portComponent.portComponentName = portComponentName;
            } else if (("wsdl-service" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlService
                final QName wsdlService = elementReader.getElementAsQName();
                portComponent.wsdlService = wsdlService;
            } else if (("wsdl-port" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlPort
                final QName wsdlPort = elementReader.getElementAsQName();
                portComponent.wsdlPort = wsdlPort;
            } else if (("enable-mtom" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: enableMtom
                final Boolean enableMtom = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                portComponent.enableMtom = enableMtom;
            } else if (("mtom-threshold" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mtomThreshold
                final Integer mtomThreshold = Integer.valueOf(elementReader.getElementAsString());
                portComponent.mtomThreshold = mtomThreshold;
            } else if (("addressing" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: addressing
                final Addressing addressing = readAddressing(elementReader, context);
                portComponent.addressing = addressing;
            } else if (("respect-binding" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: respectBinding
                final RespectBinding respectBinding = readRespectBinding(elementReader, context);
                portComponent.respectBinding = respectBinding;
            } else if (("protocol-binding" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: protocolBinding
                final String protocolBindingRaw = elementReader.getElementAsString();

                final String protocolBinding;
                try {
                    protocolBinding = Adapters.collapsedStringAdapterAdapter.unmarshal(protocolBindingRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                portComponent.protocolBinding = protocolBinding;
            } else if (("service-endpoint-interface" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceEndpointInterface
                final String serviceEndpointInterfaceRaw = elementReader.getElementAsString();

                final String serviceEndpointInterface;
                try {
                    serviceEndpointInterface = Adapters.collapsedStringAdapterAdapter.unmarshal(serviceEndpointInterfaceRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                portComponent.serviceEndpointInterface = serviceEndpointInterface;
            } else if (("service-impl-bean" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceImplBean
                final ServiceImplBean serviceImplBean = readServiceImplBean(elementReader, context);
                portComponent.serviceImplBean = serviceImplBean;
            } else if (("handler" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: handler
                final Handler handlerItem = readHandler(elementReader, context);
                if (handler == null) {
                    handler = portComponent.handler;
                    if (handler != null) {
                        handler.clear();
                    } else {
                        handler = new ArrayList<Handler>();
                    }
                }
                handler.add(handlerItem);
            } else if (("handler-chains" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: handlerChains
                final HandlerChains handlerChains = readHandlerChains(elementReader, context);
                portComponent.handlerChains = handlerChains;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "port-component-name"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-service"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-port"), new QName("http://java.sun.com/xml/ns/javaee", "enable-mtom"), new QName("http://java.sun.com/xml/ns/javaee", "mtom-threshold"), new QName("http://java.sun.com/xml/ns/javaee", "addressing"), new QName("http://java.sun.com/xml/ns/javaee", "respect-binding"), new QName("http://java.sun.com/xml/ns/javaee", "protocol-binding"), new QName("http://java.sun.com/xml/ns/javaee", "service-endpoint-interface"), new QName("http://java.sun.com/xml/ns/javaee", "service-impl-bean"), new QName("http://java.sun.com/xml/ns/javaee", "handler"), new QName("http://java.sun.com/xml/ns/javaee", "handler-chains"));
            }
        }
        if (handler != null) {
            portComponent.handler = handler;
        }

        context.afterUnmarshal(portComponent, LifecycleCallback.NONE);

        return portComponent;
    }

    public final PortComponent read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final PortComponent portComponent, RuntimeContext context)
        throws Exception {
        if (portComponent == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (PortComponent.class != portComponent.getClass()) {
            context.unexpectedSubclass(writer, portComponent, PortComponent.class);
            return;
        }

        context.beforeMarshal(portComponent, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = portComponent.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(portComponent, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: description
        final String descriptionRaw = portComponent.description;
        String description = null;
        try {
            description = Adapters.collapsedStringAdapterAdapter.marshal(descriptionRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(portComponent, "description", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (description != null) {
            writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(description);
            writer.writeEndElement();
        }

        // ELEMENT: displayName
        final String displayNameRaw = portComponent.displayName;
        String displayName = null;
        try {
            displayName = Adapters.collapsedStringAdapterAdapter.marshal(displayNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(portComponent, "displayName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (displayName != null) {
            writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(displayName);
            writer.writeEndElement();
        }

        // ELEMENT: icon
        final Icon icon = portComponent.icon;
        if (icon != null) {
            writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
            writeIcon(writer, icon, context);
            writer.writeEndElement();
        }

        // ELEMENT: portComponentName
        final String portComponentNameRaw = portComponent.portComponentName;
        String portComponentName = null;
        try {
            portComponentName = Adapters.collapsedStringAdapterAdapter.marshal(portComponentNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(portComponent, "portComponentName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (portComponentName != null) {
            writer.writeStartElement(prefix, "port-component-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(portComponentName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(portComponent, "portComponentName");
        }

        // ELEMENT: wsdlService
        final QName wsdlService = portComponent.wsdlService;
        if (wsdlService != null) {
            writer.writeStartElement(prefix, "wsdl-service", "http://java.sun.com/xml/ns/javaee");
            writer.writeQName(wsdlService);
            writer.writeEndElement();
        }

        // ELEMENT: wsdlPort
        final QName wsdlPort = portComponent.wsdlPort;
        if (wsdlPort != null) {
            writer.writeStartElement(prefix, "wsdl-port", "http://java.sun.com/xml/ns/javaee");
            writer.writeQName(wsdlPort);
            writer.writeEndElement();
        }

        // ELEMENT: enableMtom
        final Boolean enableMtom = portComponent.enableMtom;
        if (enableMtom != null) {
            writer.writeStartElement(prefix, "enable-mtom", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(enableMtom));
            writer.writeEndElement();
        }

        // ELEMENT: mtomThreshold
        final Integer mtomThreshold = portComponent.mtomThreshold;
        if (mtomThreshold != null) {
            writer.writeStartElement(prefix, "mtom-threshold", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(mtomThreshold));
            writer.writeEndElement();
        }

        // ELEMENT: addressing
        final Addressing addressing = portComponent.addressing;
        if (addressing != null) {
            writer.writeStartElement(prefix, "addressing", "http://java.sun.com/xml/ns/javaee");
            writeAddressing(writer, addressing, context);
            writer.writeEndElement();
        }

        // ELEMENT: respectBinding
        final RespectBinding respectBinding = portComponent.respectBinding;
        if (respectBinding != null) {
            writer.writeStartElement(prefix, "respect-binding", "http://java.sun.com/xml/ns/javaee");
            writeRespectBinding(writer, respectBinding, context);
            writer.writeEndElement();
        }

        // ELEMENT: protocolBinding
        final String protocolBindingRaw = portComponent.protocolBinding;
        String protocolBinding = null;
        try {
            protocolBinding = Adapters.collapsedStringAdapterAdapter.marshal(protocolBindingRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(portComponent, "protocolBinding", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (protocolBinding != null) {
            writer.writeStartElement(prefix, "protocol-binding", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(protocolBinding);
            writer.writeEndElement();
        }

        // ELEMENT: serviceEndpointInterface
        final String serviceEndpointInterfaceRaw = portComponent.serviceEndpointInterface;
        String serviceEndpointInterface = null;
        try {
            serviceEndpointInterface = Adapters.collapsedStringAdapterAdapter.marshal(serviceEndpointInterfaceRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(portComponent, "serviceEndpointInterface", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (serviceEndpointInterface != null) {
            writer.writeStartElement(prefix, "service-endpoint-interface", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serviceEndpointInterface);
            writer.writeEndElement();
        }

        // ELEMENT: serviceImplBean
        final ServiceImplBean serviceImplBean = portComponent.serviceImplBean;
        if (serviceImplBean != null) {
            writer.writeStartElement(prefix, "service-impl-bean", "http://java.sun.com/xml/ns/javaee");
            writeServiceImplBean(writer, serviceImplBean, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(portComponent, "serviceImplBean");
        }

        // ELEMENT: handler
        final List<Handler> handler = portComponent.handler;
        if (handler != null) {
            for (final Handler handlerItem : handler) {
                writer.writeStartElement(prefix, "handler", "http://java.sun.com/xml/ns/javaee");
                if (handlerItem != null) {
                    writeHandler(writer, handlerItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: handlerChains
        final HandlerChains handlerChains = portComponent.handlerChains;
        if (handlerChains != null) {
            writer.writeStartElement(prefix, "handler-chains", "http://java.sun.com/xml/ns/javaee");
            writeHandlerChains(writer, handlerChains, context);
            writer.writeEndElement();
        }

        context.afterMarshal(portComponent, LifecycleCallback.NONE);
    }

}
