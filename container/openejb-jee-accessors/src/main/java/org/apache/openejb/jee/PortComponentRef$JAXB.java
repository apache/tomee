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

import static org.apache.openejb.jee.Addressing$JAXB.readAddressing;
import static org.apache.openejb.jee.Addressing$JAXB.writeAddressing;
import static org.apache.openejb.jee.RespectBinding$JAXB.readRespectBinding;
import static org.apache.openejb.jee.RespectBinding$JAXB.writeRespectBinding;

@SuppressWarnings({
    "StringEquality"
})
public class PortComponentRef$JAXB
    extends JAXBObject<PortComponentRef> {


    public PortComponentRef$JAXB() {
        super(PortComponentRef.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "port-component-refType".intern()), Addressing$JAXB.class, RespectBinding$JAXB.class);
    }

    public static PortComponentRef readPortComponentRef(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writePortComponentRef(final XoXMLStreamWriter writer, final PortComponentRef portComponentRef, final RuntimeContext context)
        throws Exception {
        _write(writer, portComponentRef, context);
    }

    public void write(final XoXMLStreamWriter writer, final PortComponentRef portComponentRef, final RuntimeContext context)
        throws Exception {
        _write(writer, portComponentRef, context);
    }

    public final static PortComponentRef _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final PortComponentRef portComponentRef = new PortComponentRef();
        context.beforeUnmarshal(portComponentRef, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("port-component-refType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, PortComponentRef.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, portComponentRef);
                portComponentRef.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("service-endpoint-interface" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceEndpointInterface
                final String serviceEndpointInterfaceRaw = elementReader.getElementAsString();

                final String serviceEndpointInterface;
                try {
                    serviceEndpointInterface = Adapters.collapsedStringAdapterAdapter.unmarshal(serviceEndpointInterfaceRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                portComponentRef.serviceEndpointInterface = serviceEndpointInterface;
            } else if (("enable-mtom" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: enableMtom
                final Boolean enableMtom = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                portComponentRef.enableMtom = enableMtom;
            } else if (("mtom-threshold" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mtomThreshold
                final Integer mtomThreshold = Integer.valueOf(elementReader.getElementAsString());
                portComponentRef.mtomThreshold = mtomThreshold;
            } else if (("addressing" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: addressing
                final Addressing addressing = readAddressing(elementReader, context);
                portComponentRef.addressing = addressing;
            } else if (("respect-binding" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: respectBinding
                final RespectBinding respectBinding = readRespectBinding(elementReader, context);
                portComponentRef.respectBinding = respectBinding;
            } else if (("port-component-link" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: portComponentLink
                final String portComponentLinkRaw = elementReader.getElementAsString();

                final String portComponentLink;
                try {
                    portComponentLink = Adapters.collapsedStringAdapterAdapter.unmarshal(portComponentLinkRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                portComponentRef.portComponentLink = portComponentLink;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "service-endpoint-interface"), new QName("http://java.sun.com/xml/ns/javaee", "enable-mtom"), new QName("http://java.sun.com/xml/ns/javaee", "mtom-threshold"), new QName("http://java.sun.com/xml/ns/javaee", "addressing"), new QName("http://java.sun.com/xml/ns/javaee", "respect-binding"), new QName("http://java.sun.com/xml/ns/javaee", "port-component-link"));
            }
        }

        context.afterUnmarshal(portComponentRef, LifecycleCallback.NONE);

        return portComponentRef;
    }

    public final PortComponentRef read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final PortComponentRef portComponentRef, RuntimeContext context)
        throws Exception {
        if (portComponentRef == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (PortComponentRef.class != portComponentRef.getClass()) {
            context.unexpectedSubclass(writer, portComponentRef, PortComponentRef.class);
            return;
        }

        context.beforeMarshal(portComponentRef, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = portComponentRef.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(portComponentRef, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: serviceEndpointInterface
        final String serviceEndpointInterfaceRaw = portComponentRef.serviceEndpointInterface;
        String serviceEndpointInterface = null;
        try {
            serviceEndpointInterface = Adapters.collapsedStringAdapterAdapter.marshal(serviceEndpointInterfaceRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(portComponentRef, "serviceEndpointInterface", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (serviceEndpointInterface != null) {
            writer.writeStartElement(prefix, "service-endpoint-interface", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serviceEndpointInterface);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(portComponentRef, "serviceEndpointInterface");
        }

        // ELEMENT: enableMtom
        final Boolean enableMtom = portComponentRef.enableMtom;
        if (enableMtom != null) {
            writer.writeStartElement(prefix, "enable-mtom", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(enableMtom));
            writer.writeEndElement();
        }

        // ELEMENT: mtomThreshold
        final Integer mtomThreshold = portComponentRef.mtomThreshold;
        if (mtomThreshold != null) {
            writer.writeStartElement(prefix, "mtom-threshold", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(mtomThreshold));
            writer.writeEndElement();
        }

        // ELEMENT: addressing
        final Addressing addressing = portComponentRef.addressing;
        if (addressing != null) {
            writer.writeStartElement(prefix, "addressing", "http://java.sun.com/xml/ns/javaee");
            writeAddressing(writer, addressing, context);
            writer.writeEndElement();
        }

        // ELEMENT: respectBinding
        final RespectBinding respectBinding = portComponentRef.respectBinding;
        if (respectBinding != null) {
            writer.writeStartElement(prefix, "respect-binding", "http://java.sun.com/xml/ns/javaee");
            writeRespectBinding(writer, respectBinding, context);
            writer.writeEndElement();
        }

        // ELEMENT: portComponentLink
        final String portComponentLinkRaw = portComponentRef.portComponentLink;
        String portComponentLink = null;
        try {
            portComponentLink = Adapters.collapsedStringAdapterAdapter.marshal(portComponentLinkRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(portComponentRef, "portComponentLink", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (portComponentLink != null) {
            writer.writeStartElement(prefix, "port-component-link", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(portComponentLink);
            writer.writeEndElement();
        }

        context.afterMarshal(portComponentRef, LifecycleCallback.NONE);
    }

}
