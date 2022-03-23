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

import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.PortComponent$JAXB.readPortComponent;
import static org.apache.openejb.jee.PortComponent$JAXB.writePortComponent;

@SuppressWarnings({
    "StringEquality"
})
public class WebserviceDescription$JAXB
    extends JAXBObject<WebserviceDescription> {


    public WebserviceDescription$JAXB() {
        super(WebserviceDescription.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "webservice-descriptionType".intern()), Icon$JAXB.class, PortComponent$JAXB.class);
    }

    public static WebserviceDescription readWebserviceDescription(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeWebserviceDescription(final XoXMLStreamWriter writer, final WebserviceDescription webserviceDescription, final RuntimeContext context)
        throws Exception {
        _write(writer, webserviceDescription, context);
    }

    public void write(final XoXMLStreamWriter writer, final WebserviceDescription webserviceDescription, final RuntimeContext context)
        throws Exception {
        _write(writer, webserviceDescription, context);
    }

    public final static WebserviceDescription _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final WebserviceDescription webserviceDescription = new WebserviceDescription();
        context.beforeUnmarshal(webserviceDescription, LifecycleCallback.NONE);

        KeyedCollection<String, PortComponent> portComponent = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("webservice-descriptionType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, WebserviceDescription.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, webserviceDescription);
                webserviceDescription.id = id;
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

                webserviceDescription.description = description;
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

                webserviceDescription.displayName = displayName;
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                final Icon icon = readIcon(elementReader, context);
                webserviceDescription.icon = icon;
            } else if (("webservice-description-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: webserviceDescriptionName
                final String webserviceDescriptionNameRaw = elementReader.getElementAsString();

                final String webserviceDescriptionName;
                try {
                    webserviceDescriptionName = Adapters.collapsedStringAdapterAdapter.unmarshal(webserviceDescriptionNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webserviceDescription.webserviceDescriptionName = webserviceDescriptionName;
            } else if (("wsdl-file" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlFile
                final String wsdlFileRaw = elementReader.getElementAsString();

                final String wsdlFile;
                try {
                    wsdlFile = Adapters.collapsedStringAdapterAdapter.unmarshal(wsdlFileRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webserviceDescription.wsdlFile = wsdlFile;
            } else if (("jaxrpc-mapping-file" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: jaxrpcMappingFile
                final String jaxrpcMappingFileRaw = elementReader.getElementAsString();

                final String jaxrpcMappingFile;
                try {
                    jaxrpcMappingFile = Adapters.collapsedStringAdapterAdapter.unmarshal(jaxrpcMappingFileRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webserviceDescription.jaxrpcMappingFile = jaxrpcMappingFile;
            } else if (("port-component" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: portComponent
                final PortComponent portComponentItem = readPortComponent(elementReader, context);
                if (portComponent == null) {
                    portComponent = webserviceDescription.portComponent;
                    if (portComponent != null) {
                        portComponent.clear();
                    } else {
                        portComponent = new KeyedCollection<String, PortComponent>();
                    }
                }
                portComponent.add(portComponentItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "webservice-description-name"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-file"), new QName("http://java.sun.com/xml/ns/javaee", "jaxrpc-mapping-file"), new QName("http://java.sun.com/xml/ns/javaee", "port-component"));
            }
        }
        if (portComponent != null) {
            webserviceDescription.portComponent = portComponent;
        }

        context.afterUnmarshal(webserviceDescription, LifecycleCallback.NONE);

        return webserviceDescription;
    }

    public final WebserviceDescription read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final WebserviceDescription webserviceDescription, RuntimeContext context)
        throws Exception {
        if (webserviceDescription == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (WebserviceDescription.class != webserviceDescription.getClass()) {
            context.unexpectedSubclass(writer, webserviceDescription, WebserviceDescription.class);
            return;
        }

        context.beforeMarshal(webserviceDescription, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = webserviceDescription.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(webserviceDescription, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: description
        final String descriptionRaw = webserviceDescription.description;
        String description = null;
        try {
            description = Adapters.collapsedStringAdapterAdapter.marshal(descriptionRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(webserviceDescription, "description", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (description != null) {
            writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(description);
            writer.writeEndElement();
        }

        // ELEMENT: displayName
        final String displayNameRaw = webserviceDescription.displayName;
        String displayName = null;
        try {
            displayName = Adapters.collapsedStringAdapterAdapter.marshal(displayNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(webserviceDescription, "displayName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (displayName != null) {
            writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(displayName);
            writer.writeEndElement();
        }

        // ELEMENT: icon
        final Icon icon = webserviceDescription.icon;
        if (icon != null) {
            writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
            writeIcon(writer, icon, context);
            writer.writeEndElement();
        }

        // ELEMENT: webserviceDescriptionName
        final String webserviceDescriptionNameRaw = webserviceDescription.webserviceDescriptionName;
        String webserviceDescriptionName = null;
        try {
            webserviceDescriptionName = Adapters.collapsedStringAdapterAdapter.marshal(webserviceDescriptionNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(webserviceDescription, "webserviceDescriptionName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (webserviceDescriptionName != null) {
            writer.writeStartElement(prefix, "webservice-description-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(webserviceDescriptionName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(webserviceDescription, "webserviceDescriptionName");
        }

        // ELEMENT: wsdlFile
        final String wsdlFileRaw = webserviceDescription.wsdlFile;
        String wsdlFile = null;
        try {
            wsdlFile = Adapters.collapsedStringAdapterAdapter.marshal(wsdlFileRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(webserviceDescription, "wsdlFile", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (wsdlFile != null) {
            writer.writeStartElement(prefix, "wsdl-file", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(wsdlFile);
            writer.writeEndElement();
        }

        // ELEMENT: jaxrpcMappingFile
        final String jaxrpcMappingFileRaw = webserviceDescription.jaxrpcMappingFile;
        String jaxrpcMappingFile = null;
        try {
            jaxrpcMappingFile = Adapters.collapsedStringAdapterAdapter.marshal(jaxrpcMappingFileRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(webserviceDescription, "jaxrpcMappingFile", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (jaxrpcMappingFile != null) {
            writer.writeStartElement(prefix, "jaxrpc-mapping-file", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(jaxrpcMappingFile);
            writer.writeEndElement();
        }

        // ELEMENT: portComponent
        final KeyedCollection<String, PortComponent> portComponent = webserviceDescription.portComponent;
        if (portComponent != null) {
            for (final PortComponent portComponentItem : portComponent) {
                if (portComponentItem != null) {
                    writer.writeStartElement(prefix, "port-component", "http://java.sun.com/xml/ns/javaee");
                    writePortComponent(writer, portComponentItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webserviceDescription, "portComponent");
                }
            }
        }

        context.afterMarshal(webserviceDescription, LifecycleCallback.NONE);
    }

}
